package org.devocative.demeter.web;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.https.RequireHttps;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.demeter.entity.DPageInfo;
import org.devocative.demeter.entity.DPageInstance;
import org.devocative.demeter.iservice.IDPageInstanceService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.UserVO;
import org.devocative.demeter.web.dpage.LoginDPage;
import org.devocative.demeter.web.panel.EditProfilePanel;
import org.devocative.wickomp.WDefaults;
import org.devocative.wickomp.WebUtil;
import org.devocative.wickomp.async.AsyncBehavior;
import org.devocative.wickomp.html.menu.OMenuItem;
import org.devocative.wickomp.html.menu.WMenuBar;
import org.devocative.wickomp.html.window.WModalWindow;
import org.devocative.wickomp.wrcs.FontAwesomeBehavior;
import org.devocative.wickomp.wrcs.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequireHttps
public class Index extends WebPage {
	private static final long serialVersionUID = -4461033595410274295L;

	private static final Logger logger = LoggerFactory.getLogger(Index.class);

	private static final HeaderItem INDEX_CSS = CssHeaderItem.forReference(new CssResourceReference(Index.class, "wrcs/index.css"));
	private static final HeaderItem INDEX_JS = JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(Index.class, "wrcs/index.js"));

	// ------------------------------

	@Inject
	private IDPageInstanceService pageInstanceService;

	@Inject
	private ISecurityService securityService;

	// ------------------------------

	private WModalWindow window;
	private UserVO currentUser;
	private WebMarkupContainer signIn, signOut, editProfile;
	private List<OMenuItem> oMenuItems = new ArrayList<>();
	private AbstractDefaultAjaxBehavior ajaxBehavior;

	// ------------------------------

	public Index(PageParameters pageParameters) {
		TransparentWebMarkupContainer html = new TransparentWebMarkupContainer("html");
		html.add(new AttributeModifier("dir", DemeterWebSession.get().getLayoutDirection().toString()));
		html.add(new AttributeModifier("class", String.format("dmt-%s", DemeterWebSession.get().getLayoutDirection().toString())));
		add(html);

		window = new WModalWindow("window");
		html.add(window);


		DPageInstance pageInstance;
		IModel<String> headerTitle = new Model<>("");

		// URI: [/<CONTEXT>]/<INNER CONTEXT>
		// PARAMS: /<MODULE>/<D PAGE>[/REF ID PARAM]
		// INDEX   0         1         2

		Component content = authenticateByHTTP();
		if (content == null) {
			if (pageParameters.getIndexedCount() > 0) {
				StringBuilder uriBuilder = new StringBuilder();
				for (int i = 0; i < pageParameters.getIndexedCount() && i < 2; i++) {
					uriBuilder.append("/").append(pageParameters.get(i));
				}
				String refIdParam = pageParameters.getIndexedCount() >= 2 ? pageParameters.get(2).toString() : null;
				pageInstance = pageInstanceService.getPageInstanceByURI(uriBuilder.toString(), refIdParam);
				if (pageInstance != null) {
					headerTitle = getDPageTitle(pageInstance);

					List<String> params = new ArrayList<>();
					if (pageInstance.getRefId() != null) {
						params.add(pageInstance.getRefId());
						for (int i = 3; i < pageParameters.getIndexedCount(); i++) {
							params.add(pageParameters.get(i).toString());
						}
					} else {
						for (int i = 2; i < pageParameters.getIndexedCount(); i++) {
							params.add(pageParameters.get(i).toString());
						}
					}

					content = createDPageFromType(pageInstance.getUri(), pageInstance.getPageInfo(), params);
				} else {
					content = new Label("content", new ResourceModel("err.dmt.UnknownDPage"));
				}
			} else {
				content = new Label("content", "");
				content.setVisible(false);
			}
		}

		html.add(new Label("headerTitle", headerTitle));
		html.add(content);

		boolean printable = !pageParameters.get(DemeterWebParam.PRINTABLE).isNull();

		final String appName = ConfigUtil.getString(DemeterConfigKey.WebAppName);

		WebMarkupContainer header = new WebMarkupContainer("header");
		header.add(new WMenuBar("menu", oMenuItems));
		header.add(new Label("appName", appName != null ? new Model<>(appName) : new ResourceModel("label.APP")));
		header.setVisible(!printable);
		html.add(header);

		if (printable) {
			html.add(new AttributeAppender("class", " dmt-no-header"));
		} else {
			html.add(new AttributeAppender("class", " dmt-has-header"));
		}

		// ---------------------- User Menu: FullName, Sign In & Out

		WebMarkupContainer userMenu = new WebMarkupContainer("userMenu");
		userMenu.setVisible(ConfigUtil.getBoolean(DemeterConfigKey.EnabledSecurity));
		header.add(userMenu);

		userMenu.add(new Label("userInfo", new PropertyModel<>(this, "currentUser.fullName")));

		editProfile = new AjaxLink("editProfile") {
			private static final long serialVersionUID = 1230977798401836525L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				window.setContent(new EditProfilePanel(window.getContentId()));
				window.show(target);
			}
		};
		userMenu.add(editProfile);

		signOut = new Link("signOut") {
			private static final long serialVersionUID = -8414668952922460126L;

			@Override
			public void onClick() {
				DemeterWebSession.get().invalidate();
				securityService.signOut();
				setResponsePage(Index.class);
			}
		};
		signIn = new ExternalLink("signIn", UrlUtil.createUri(LoginDPage.class, true));

		userMenu.add(signIn);
		userMenu.add(signOut);

		add(new FontAwesomeBehavior());

		if (currentUser.getSessionTimeout() > 0) {
			add(ajaxBehavior = new AbstractDefaultAjaxBehavior() {
				private static final long serialVersionUID = -7894142833970170434L;

				@Override
				protected void respond(AjaxRequestTarget target) {
					logger.info("User reconnect: {}", currentUser.getUsername());
				}
			});
		}

		add(new AsyncBehavior());
	}

	// ------------------------------

	@Override
	public void renderHead(IHeaderResponse response) {
		Resource.addJQueryReference(response);
		response.render(INDEX_CSS);

		int sessionTimeoutInSec = currentUser.getSessionTimeout() * 60;
		String ajaxUrl = sessionTimeoutInSec > 0 ? ajaxBehavior.getCallbackUrl().toString() : "";

		response.render(JavaScriptHeaderItem.forScript(
			String.format("var sessionTO=%d;var ajaxUrl='%s';", sessionTimeoutInSec, ajaxUrl),
			"initJSVariables"));

		if (ConfigUtil.getBoolean(DemeterConfigKey.PingWebSocketEnabled)) {
			int webSocketPingIntervalInSec = ConfigUtil.getInteger(DemeterConfigKey.PingWebSocketPeriod);
			response.render(JavaScriptHeaderItem.forScript(String.format("var WebSocketPingInterval=%d;", webSocketPingIntervalInSec), "WebSocketPingInterval"));
		}

		response.render(INDEX_JS);

		// TODO theme-based CSS loading based on user profile
		String ctx = getRequest().getContextPath();
		for (String css : DemeterWebApplication.get().getModulesRelatedCSS()) {
			response.render(CssHeaderItem.forUrl(ctx + css));
		}
	}

	// ------------------------------

	@Override
	protected void onBeforeRender() {
		currentUser = securityService.getCurrentUser();

		createDefaultMenus();

		if (currentUser.isAuthenticated()) {
			signIn.setVisible(false);
			signOut.setVisible(true);
			editProfile.setVisible(true);
		} else {
			signIn.setVisible(true);
			signOut.setVisible(false);
			editProfile.setVisible(false);
		}

		super.onBeforeRender();
	}

	// ------------------------------

	private Component authenticateByHTTP() {
		Component result = null;
		try {
			//TODO authenticate by Basic & Digest based on configuration!
			securityService.authenticateByUrlParams(WebUtil.toMap(true, true));
		} catch (Exception e) {
			logger.error("Index.authenticateByHTTP()", e);
			result = new Label("content", WDefaults.getExceptionToMessageHandler().handleMessage(this, e));
		}

		currentUser = securityService.getCurrentUser();

		if (currentUser.isPageEmpty()) {
			UserVO.PageVO pageVO = pageInstanceService.getAccessiblePages(currentUser.getRoles());
			currentUser.setPageVO(pageVO);
			if (logger.isDebugEnabled()) {
				logger.debug("User=[{}] {}", currentUser.getUsername(), pageVO);
			}
		}

		return result;
	}

	private Component createDPageFromType(String uri, DPageInfo pageInfo, List<String> params) {
		Component result;
		try {
			Class<? extends DPage> dPageClass = findDPageClass(pageInfo);
			if (DPage.class.isAssignableFrom(dPageClass)) {
				if (currentUser.hasPermission(uri)) {
					Constructor<?> constructor = dPageClass.getDeclaredConstructor(String.class, List.class);
					result = (DPage) constructor.newInstance("content", params);
				} else if (currentUser.isAuthenticated()) {
					result = new Label("content", new ResourceModel("err.dmt.AccessDenied"));
				} else {
					DemeterWebSession.get()
						.setOriginalDPage(dPageClass)
						.setOriginalParams(params)
						.setQueryParameters(getRequest().getQueryParameters());
					UrlUtil.redirectTo(LoginDPage.class);
					result = new WebComponent("content");
				}
			} else {
				logger.error("The class is not DPage: {}", dPageClass.getName());
				result = new Label("content", new ResourceModel("err.dmt.DPageNotFound"));
			}
		} catch (ClassNotFoundException e) {
			logger.error("DPage class not found", e);
			result = new Label("content", new ResourceModel("err.dmt.DPageNotFound"));
		} catch (InvocationTargetException e) {
			logger.error("DPage call problem", e.getTargetException());
			result = new Label("content", WDefaults.getExceptionToMessageHandler().handleMessage(this, e.getTargetException()));
		} catch (Exception e) {
			logger.error("DPage instantiation problem", e);
			result = new Label("content", new ResourceModel("err.dmt.DPageInstantiation"));
		}

		return result;
	}

	private Class<? extends DPage> findDPageClass(DPageInfo pageInfo) throws ClassNotFoundException {
		if (pageInfo.getTypeAlt() != null) {
			return (Class<? extends DPage>) Class.forName(pageInfo.getTypeAlt());
		}

		return (Class<? extends DPage>) Class.forName(pageInfo.getType());
	}

	private void createDefaultMenus() {
		oMenuItems.clear();
		oMenuItems.add(new OMenuItem(UrlUtil.createUri("", true), new ResourceModel("label.home")));

		// TODO replace DPageInstance with a VO
		Map<String, Set<DPageInstance>> defaultPages = currentUser.getMainMenuEntries();
		if (defaultPages != null) {
			for (Map.Entry<String, Set<DPageInstance>> entry : defaultPages.entrySet()) {
				OMenuItem moduleEntry = new OMenuItem(new Model<>(entry.getKey()));
				List<OMenuItem> subMenus = new ArrayList<>();
				for (DPageInstance pageInstance : entry.getValue()) {
					subMenus.add(new OMenuItem(UrlUtil.createUri(pageInstance, true), getDPageTitle(pageInstance), pageInstance.getIcon()));
				}
				moduleEntry.setSubMenus(subMenus);
				oMenuItems.add(moduleEntry);
			}
		}
	}

	private IModel<String> getDPageTitle(DPageInstance dPageInstance) {
		IModel<String> result;
		String title = dPageInstance.getTitle();
		if (title != null && title.startsWith(IDPageInstanceService.D_PAGE_RESOURCE_KEY_PREFIX)) {
			title = title.substring(IDPageInstanceService.D_PAGE_RESOURCE_KEY_PREFIX.length());
			result = new ResourceModel(title, String.format("[%s]", title));
		} else {
			result = new Model<>(title);
		}
		return result;
	}
}

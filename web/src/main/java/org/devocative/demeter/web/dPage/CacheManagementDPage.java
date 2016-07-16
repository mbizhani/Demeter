package org.devocative.demeter.web.dPage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.devocative.demeter.iservice.ICacheService;
import org.devocative.demeter.iservice.ISecurityService;
import org.devocative.demeter.vo.CacheInfoVO;
import org.devocative.demeter.web.DPage;
import org.devocative.wickomp.wrcs.FontAwesomeBehavior;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class CacheManagementDPage extends DPage {
	private List<CacheInfoVO> list = new ArrayList<>();

	@Inject
	private ICacheService cacheService;

	@Inject
	private ISecurityService securityService;

	public CacheManagementDPage(String id, List<String> params) {
		super(id, params);

		add(new ListView<CacheInfoVO>("rows", list) {
			@Override
			protected void populateItem(ListItem<CacheInfoVO> item) {
				final CacheInfoVO vo = item.getModelObject();

				item.add(new Label("rowNum", item.getIndex() + 1));
				item.add(new Label("id", vo.getId()));
				item.add(new Label("capacity", vo.getCapacity()));
				item.add(new Label("size", vo.getSize()));
				item.add(new Label("missHitCount", vo.getMissHitCount()));
				item.add(new Link("clear") {
					@Override
					public void onClick() {
						cacheService.clear(vo.getId());
					}
				}.add(new AttributeModifier("onclick", "return confirm('Are you sure?');")));
			}
		}.setVisible(securityService.getCurrentUser().getUsername().equals("root")));

		add(new FontAwesomeBehavior());
	}

	@Override
	protected void onBeforeRender() {
		list.clear();
		list.addAll(cacheService.list());

		super.onBeforeRender();
	}
}

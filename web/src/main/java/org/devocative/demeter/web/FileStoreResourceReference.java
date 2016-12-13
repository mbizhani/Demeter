package org.devocative.demeter.web;

import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.FileStore;
import org.devocative.demeter.iservice.IFileStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.OutputStream;

public class FileStoreResourceReference extends ResourceReference {
	private static final long serialVersionUID = -6897167971731607084L;

	private static final Logger logger = LoggerFactory.getLogger(FileStoreResourceReference.class);

	private IFileStoreService fileStoreService;

	public FileStoreResourceReference(ApplicationContext applicationContext) {
		super("FileStoreResourceReference");

		fileStoreService = applicationContext.getBean(IFileStoreService.class);
	}

	@Override
	public IResource getResource() {

		return new AbstractResource() {
			private static final long serialVersionUID = -1222302913955939110L;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				final String fileid = attributes.getParameters().get("fileid").toString();

				final FileStore fileStore = fileStoreService.loadByFileId(fileid);

				ResourceResponse resourceResponse = new ResourceResponse();

				// TODO: check authorization, e.g. currentUser=fileStore.creatorUser

				if (fileStore != null) {
					logger.info("Download file: fileid={} filename={}", fileid, fileStore.getName());

					resourceResponse.setContentDisposition(ContentDisposition.ATTACHMENT);
					resourceResponse.setFileName(fileStore.getName());
					resourceResponse.setContentType(fileStore.getMimeType().getType());
					resourceResponse.disableCaching();

					resourceResponse.setWriteCallback(new WriteCallback() {
						@Override
						public void writeData(Attributes attributes) throws IOException {
							OutputStream out = attributes.getResponse().getOutputStream();
							fileStoreService.writeFile(fileStore, out);
						}
					});
				} else {
					resourceResponse.setContentDisposition(ContentDisposition.INLINE);
					resourceResponse.setContentType(EMimeType.TEXT.getType());
					resourceResponse.setWriteCallback(new WriteCallback() {
						@Override
						public void writeData(Attributes attributes) throws IOException {
							OutputStream out = attributes.getResponse().getOutputStream();
							out.write(String.format("File not found: %s", fileid).getBytes());
						}
					});
				}
				return resourceResponse;
			}
		};
	}
}

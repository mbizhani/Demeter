package org.devocative.demeter.web;

import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.time.Duration;
import org.devocative.demeter.entity.EFileStatus;
import org.devocative.demeter.entity.EMimeType;
import org.devocative.demeter.entity.FileStore;
import org.devocative.demeter.iservice.IFileStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class FileStoreResourceReference extends ResourceReference {
	private static final long serialVersionUID = -6897167971731607084L;

	private static final Logger logger = LoggerFactory.getLogger(FileStoreResourceReference.class);

	private final IFileStoreService fileStoreService;

	FileStoreResourceReference(IFileStoreService fileStoreService) {
		super("FileStoreResourceReference");

		this.fileStoreService = fileStoreService;
	}

	@Override
	public IResource getResource() {

		return new AbstractResource() {
			private static final long serialVersionUID = -1222302913955939110L;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				final String fileId = attributes.getParameters().get("fileId").toString();
				final FileStore fileStore = fileStoreService.loadByFileId(fileId);

				ResourceResponse resourceResponse = new ResourceResponse();

				if (fileStore != null) {
					logger.info("Download file: fileId={} filename={}", fileId, fileStore.getName());

					if (fileStore.getStatus() == EFileStatus.VALID) {
						if (fileStore.getMimeType().isInline()) {
							resourceResponse.setContentDisposition(ContentDisposition.INLINE);
						} else {
							resourceResponse.setContentDisposition(ContentDisposition.ATTACHMENT);
						}
						resourceResponse.setFileName(fileStore.getName());
						resourceResponse.setContentType(fileStore.getMimeType().getType());
						resourceResponse.disableCaching();

						resourceResponse.setWriteCallback(new WriteCallback() {
							@Override
							public void writeData(Attributes attributes) {
								OutputStream out = attributes.getResponse().getOutputStream();
								fileStoreService.writeFile(fileStore, out);
							}
						});
					} else {
						writeError(resourceResponse, "Invalid FileStore State: " + fileStore.getStatus());
					}
				} else {
					writeError(resourceResponse, "FileStore Not Found: " + fileId);
				}
				return resourceResponse;
			}

			private void writeError(ResourceResponse resourceResponse, String msg) {
				resourceResponse.setContentDisposition(ContentDisposition.INLINE);
				resourceResponse.setContentType(EMimeType.HTML.getType());
				resourceResponse.setCacheDuration(Duration.NONE);
				resourceResponse.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(Attributes attributes) throws IOException {
						String finalMsg = String.format(
							"<html><body><script>window.close();alert('%s');</script></body></html>",
							msg);
						OutputStream out = attributes.getResponse().getOutputStream();
						out.write(finalMsg.getBytes());
					}
				});
			}
		};
	}
}

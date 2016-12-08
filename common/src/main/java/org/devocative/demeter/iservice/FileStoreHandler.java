package org.devocative.demeter.iservice;

import org.devocative.demeter.entity.FileStore;

import java.io.IOException;
import java.io.OutputStream;

public class FileStoreHandler extends OutputStream {
	private transient IFileStoreService fileInfoService;
	private transient OutputStream mainStream;
	private transient FileStore fileStore;

	// ------------------------------

	public FileStoreHandler(IFileStoreService fileInfoService, OutputStream mainStream, FileStore fileStore) {
		this.fileInfoService = fileInfoService;
		this.mainStream = mainStream;
		this.fileStore = fileStore;
	}

	// ------------------------------

	@Override
	public void write(int b) throws IOException {
		mainStream.write(b);
	}

	@Override
	public void flush() throws IOException {
		mainStream.flush();
	}

	@Override
	public void close() throws IOException {
		mainStream.close();
		fileInfoService.saveOrUpdate(fileStore);
	}

	// ---------------

	public FileStore getFileStore() {
		return fileStore;
	}
}

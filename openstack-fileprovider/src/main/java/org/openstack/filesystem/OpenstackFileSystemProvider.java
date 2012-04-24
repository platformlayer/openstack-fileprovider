package org.openstack.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttributeView;
import java.util.Map;

import org.openstack.filesystem.common.FileSystemProviderBase;

public class OpenstackFileSystemProvider extends FileSystemProviderBase<OpenstackPath> {
	public static final String SCHEME = "openstack";
	public static final String PREFIX = SCHEME + "://";

	// private final Map<String, OpenstackFileSystem> filesystems = new HashMap<>();

	public OpenstackFileSystemProvider() {
		super(OpenstackPath.class);
	}

	@Override
	public String getScheme() {
		return SCHEME;
	}

	@Override
	public OpenstackFileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		// For security, we don't allow caching of filesystems
		OpenstackFileSystem fs = new OpenstackFileSystem(this, uri, env);
		return fs;

		// synchronized (filesystems) {
		// String key = uri.getHost();
		// if (filesystems.containsKey(key))
		// throw new FileSystemAlreadyExistsException();
		//
		// OpenstackFileSystem fs = new OpenstackFileSystem(this, uri, env);
		//
		// filesystems.put(key, fs);
		// return fs;
		// }
	}

	@Override
	public OpenstackFileSystem getFileSystem(URI uri) {
		throw new UnsupportedOperationException();

		// synchronized (filesystems) {
		// String key = uri.getHost();
		// OpenstackFileSystem fs = filesystems.get(key);
		// if (fs == null)
		// throw new FileSystemNotFoundException();
		// return fs;
		// }
	}

	@Override
	public Path getPath(URI uri) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isHidden(Path path) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "OpenstackFileSystemProvider";
	}

}

package org.openstack.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

import org.openstack.filesystem.common.PathBase;

public class OpenstackPath extends PathBase {
	private final OpenstackFileSystem fs;
	private final String path;

	public OpenstackPath(OpenstackFileSystem fs, String path) {
		super();
		this.fs = fs;
		this.path = path;
	}

	@Override
	public OpenstackFileSystem getFileSystem() {
		return fs;
	}

	@Override
	public boolean isAbsolute() {
		return path.charAt(0) == '/';
	}

	@Override
	public Path getRoot() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path getFileName() {
		if (path == null)
			return null;

		int lastSlash = path.lastIndexOf('/');
		if (lastSlash == -1) {
			return this;
		}

		String fileName = path.substring(lastSlash + 1);
		return new OpenstackPath(fs, fileName);
	}

	@Override
	public Path getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNameCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path getName(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean startsWith(Path other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean startsWith(String other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean endsWith(Path other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean endsWith(String other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path normalize() {
		throw new UnsupportedOperationException();
	}

	private OpenstackPath checkPath(Path path) {
		if (path == null)
			throw new NullPointerException();
		if (!(path instanceof OpenstackPath))
			throw new ProviderMismatchException();
		return (OpenstackPath) path;
	}

	@Override
	public Path resolve(Path other) {
		final OpenstackPath o = checkPath(other);
		if (o.isAbsolute())
			return o;

		String resolved = joinPaths(this.path, o.path);
		return new OpenstackPath(fs, resolved);
	}

	@Override
	public Path resolveSibling(Path other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path resolveSibling(String other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path relativize(Path obj) {
		OpenstackPath other = checkPath(obj);
		if (other.equals(this))
			return new OpenstackPath(fs, "");

		if (this.isAbsolute() != other.isAbsolute())
			throw new IllegalArgumentException("Cannot relativize paths when one is absolute and one is relative");

		if (!this.fs.equals(other.fs)) {
			throw new IllegalArgumentException("Cannot relativize paths from different filesystems");
		}

		String thisPath = this.path;
		String otherPath = other.path;

		if (thisPath.equals(otherPath))
			return new OpenstackPath(fs, "");

		if (!thisPath.endsWith("/"))
			thisPath += "/";

		if (!otherPath.startsWith(thisPath)) {
			throw new UnsupportedOperationException("Non-derived paths not supported for relativizing");
		}

		String relative = otherPath.substring(thisPath.length());
		if (relative.charAt(0) == '/') { // Should be impossible
			throw new IllegalStateException();
		}

		return new OpenstackPath(fs, relative);
	}

	@Override
	public URI toUri() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path toAbsolutePath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public File toFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Path> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(Path other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		if (path.startsWith(OpenstackFileSystemProvider.PREFIX)) {
			return OpenstackFileSystemProvider.PREFIX + path;
		}
		return path;
	}

	public String getPath() {
		return path;
	}

	public String getContainerName() {
		if (!isAbsolute())
			throw new IllegalArgumentException();
		if (path.charAt(0) != '/')
			throw new IllegalStateException();
		int slash = path.indexOf('/', 1);
		if (slash == -1) {
			if (path.length() == 1)
				return null;
			else
				return path.substring(1);
		}
		return path.substring(1, slash);
	}

	public String getObjectPath() {
		if (!isAbsolute())
			throw new IllegalArgumentException();
		if (path.charAt(0) != '/')
			throw new IllegalStateException();
		int slash = path.indexOf('/', 1);
		if (slash == -1) {
			return null;
		}
		String s = path.substring(slash + 1);
		if (s.length() == 0)
			return null;
		return s;
	}

}

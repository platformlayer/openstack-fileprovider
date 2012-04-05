package org.openstack.filesystem.common;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

public abstract class FileSystemProviderBase<T extends PathBase> extends FileSystemProvider {
	private final Class<T> pathClass;

	protected FileSystemProviderBase(Class<T> pathClass) {
		this.pathClass = pathClass;
	}

	protected T checkPath(Path path) {
		if (path == null)
			throw new NullPointerException();
		if (!pathClass.isAssignableFrom(path.getClass()))
			throw new ProviderMismatchException();
		return (T) path;
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		T typedPath = checkPath(path);

		FileSystemBase<T> fs = (FileSystemBase<T>) typedPath.getFileSystem();
		return fs.newByteChannel(typedPath, options, attrs);
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		T typedPath = checkPath(dir);

		FileSystemBase<T> fs = (FileSystemBase<T>) typedPath.getFileSystem();
		fs.createDirectory(typedPath, attrs);
	}

	@Override
	public void delete(Path path) throws IOException {
		T typedPath = checkPath(path);

		FileSystemBase<T> fs = (FileSystemBase<T>) typedPath.getFileSystem();
		fs.delete(typedPath);
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		T typedPath = checkPath(path);

		FileSystemBase<T> fs = (FileSystemBase<T>) typedPath.getFileSystem();
		fs.checkAccess(typedPath, modes);
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		T typedPath = checkPath(path);

		FileSystemBase<T> fs = (FileSystemBase<T>) typedPath.getFileSystem();
		return fs.readAttributes(typedPath, type, options);
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		T typedPath = checkPath(dir);

		FileSystemBase<T> fs = (FileSystemBase<T>) typedPath.getFileSystem();
		return fs.newDirectoryStream(typedPath, filter);
	}

}

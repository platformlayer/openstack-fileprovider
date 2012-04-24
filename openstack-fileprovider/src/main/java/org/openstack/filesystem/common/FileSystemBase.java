package org.openstack.filesystem.common;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

public abstract class FileSystemBase<T extends PathBase> extends FileSystem {
	public abstract SeekableByteChannel newByteChannel(T path, Set<? extends OpenOption> options,
			FileAttribute<?>... attrs) throws IOException;

	public abstract void createDirectory(T path, FileAttribute<?>[] attrs) throws IOException;

	public abstract void delete(T path) throws IOException;

	public abstract void checkAccess(T path, AccessMode... modes) throws IOException;

	public abstract <A extends BasicFileAttributes> A readAttributes(T path, Class<A> type, LinkOption... options)
			throws IOException;

	public abstract DirectoryStream<Path> newDirectoryStream(T dir, Filter<? super Path> filter) throws IOException;
}

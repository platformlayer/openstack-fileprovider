package java.nio.file;

import java.io.IOException;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

public abstract class FileSystem {

	public abstract Path getPath(String first, String... more);

	public abstract void close() throws IOException;

	public abstract boolean isOpen();

	public abstract boolean isReadOnly();

	public abstract String getSeparator();

	public abstract Iterable<Path> getRootDirectories();

	public abstract Iterable<FileStore> getFileStores();

	public abstract Set<String> supportedFileAttributeViews();

	public abstract PathMatcher getPathMatcher(String syntaxAndPattern);

	public abstract UserPrincipalLookupService getUserPrincipalLookupService();

	public abstract WatchService newWatchService() throws IOException;

	public abstract FileSystemProvider provider();

}

package org.openstack.filesystem;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.openstack.client.OpenstackCredentials;
import org.openstack.client.OpenstackNotFoundException;
import org.openstack.client.OpenstackProperties;
import org.openstack.client.common.OpenstackSession;
import org.openstack.client.storage.ObjectResource;
import org.openstack.client.storage.ObjectsResource;
import org.openstack.client.storage.OpenstackStorageClient;
import org.openstack.filesystem.common.FileSystemBase;
import org.openstack.model.storage.Container;
import org.openstack.model.storage.ObjectProperties;
import org.openstack.model.storage.StorageObject;

public class OpenstackFileSystem extends FileSystemBase<OpenstackPath> {
	public static final String DEFAULT_MIME_TYPE_DIRECTORY = "application/x-directory";

	private final OpenstackFileSystemProvider provider;
	private final URI uri;
	private final Map<String, ?> env;

	final OpenstackSession session;

	public OpenstackFileSystem(OpenstackFileSystemProvider openstackFilesystemProvider, URI uri, Map<String, ?> env) {
		this.provider = openstackFilesystemProvider;
		this.uri = uri;
		this.env = env;

		this.session = connect();
	}

	private OpenstackSession connect() {
		OpenstackSession session = OpenstackSession.create();

		String debugString = getOptionalProperty("openstack.debug", "false");
		boolean debug = Boolean.parseBoolean(debugString);
		if (debug) {
			session = session.with(OpenstackSession.Feature.VERBOSE);
		}

		session.authenticate(getOpenstackCredentials());
		return session;
	}

	private OpenstackStorageClient getStorageClient() {
		return session.getStorageClient();
	}

	private String getOptionalProperty(String key, String defaultValue) {
		String value = (String) env.get(key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	private String getRequiredProperty(String key) {
		String value = getOptionalProperty(key, null);
		if (value == null) {
			throw new IllegalArgumentException("Property is required: " + key);
		}
		return value;
	}

	@Override
	public OpenstackFileSystemProvider provider() {
		return provider;
	}

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOpen() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getSeparator() {
		return "/";
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		ArrayList<Path> pathArr = new ArrayList<Path>();
		OpenstackStorageClient storageClient = getStorageClient();
		for (Container container : storageClient.root().containers().list()) {
			pathArr.add(new OpenstackPath(this, "/" + container.getName()));
		}
		return pathArr;
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path getPath(String first, String... more) {
		String path;

		if (more == null || more.length == 0) {
			path = first;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(first);
			for (String moreItem : more) {
				sb.append('/');
				sb.append(moreItem);
			}
			path = sb.toString();
		}

		return new OpenstackPath(this, path);
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		throw new UnsupportedOperationException();
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchService newWatchService() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void createDirectory(OpenstackPath path, FileAttribute<?>[] attrs) throws IOException {
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException("Expected absolute path");
		}

		String pathString = path.getPath();
		if (!pathString.startsWith("/")) {
			throw new IllegalArgumentException();
		}
		pathString = pathString.substring(1);

		int slash = pathString.indexOf('/');
		String containerName;
		String objectPath;

		if (slash == -1) {
			containerName = pathString;
			objectPath = null;

			if (containerName.isEmpty()) {
				containerName = null;
			}
		} else {
			containerName = pathString.substring(0, slash);
			objectPath = pathString.substring(slash + 1);
			if (objectPath.isEmpty()) {
				objectPath = null;
			}
		}

		if (containerName == null) {
			throw new IllegalStateException();
		}

		if (objectPath == null) {
			// Create a bucket
			getStorageClient().root().containers().create(containerName);
		} else {
			// Create a dummy directory file
			if (!objectPath.endsWith("/")) {
				objectPath += "/";
			}

			ObjectProperties properties = new ObjectProperties();
			properties.setName(objectPath);
			properties.setContentType(DEFAULT_MIME_TYPE_DIRECTORY);

			ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
			getStorageClient().root().containers().id(containerName).objects().putObject(bais, 0, properties);
		}
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(OpenstackPath path, Filter<? super Path> filter) {
		OpenstackStorageClient storageClient = getStorageClient();

		String containerName = path.getContainerName();
		String objectNamePrefix = path.getObjectPath();
		if (objectNamePrefix == null) {
			objectNamePrefix = "";
		} else if (!objectNamePrefix.endsWith("/")) {
			objectNamePrefix += "/";
		}
		Iterable<StorageObject> listObjects = storageClient.listObjects(containerName, objectNamePrefix, "/");

		return new OpenstackDirectoryStream(path, objectNamePrefix, listObjects, filter);
	}

	@Override
	public SeekableByteChannel newByteChannel(OpenstackPath path, Set<? extends OpenOption> options,
			FileAttribute<?>... attrs) throws IOException {
		OpenstackStorageClient storageClient = getStorageClient();

		String containerName = path.getContainerName();
		String objectPath = path.getObjectPath();

		// checkOptions(options);
		if (options.contains(StandardOpenOption.WRITE) || options.contains(StandardOpenOption.APPEND)) {
			ObjectProperties properties = new ObjectProperties();
			properties.setName(objectPath);
			ObjectsResource objectResource = storageClient.root().containers().id(containerName).objects();

			OpenstackWriteByteChannel channel = new OpenstackWriteByteChannel(objectResource, properties);
			return channel;
		} else {
			ObjectResource objectResource = storageClient.root().containers().id(containerName).objects()
					.id(objectPath);
			InputStream is = objectResource.openStream();

			ObjectProperties metadata = objectResource.metadata();

			long size = -1;
			if (metadata.getContentLength() != null) {
				size = metadata.getContentLength();
			}

			return SeekableByteChannels.wrap(SeekableByteChannels.asByteChannel(Channels.newChannel(is)), size);
		}
	}

	@Override
	public void delete(OpenstackPath path) throws IOException {
		String containerName = path.getContainerName();
		String objectPath = path.getObjectPath();

		OpenstackStorageClient storageClient = getStorageClient();

		if (objectPath == null) {
			// Delete bucket
			storageClient.root().containers().id(containerName).delete();
		} else {
			// Delete file
			storageClient.root().containers().id(containerName).objects().id(objectPath).delete();
		}
	}

	@Override
	public void checkAccess(OpenstackPath path, AccessMode... modes) throws IOException {
		boolean checkRead = false;
		boolean checkWrite = false;
		boolean checkExecute = false;

		boolean checkExists = false;
		if (modes.length == 0) {
			checkExists = true;
		} else {
			for (AccessMode mode : modes) {
				switch (mode) {
				case READ:
					checkRead = true;
					break;
				case WRITE:
					checkWrite = true;
					break;
				case EXECUTE:
					checkExecute = true;
					break;
				default:
					throw new UnsupportedOperationException();
				}
			}
		}

		if (checkExecute) {
			throw new AccessDeniedException("No execute permission");
		}

		String containerName = path.getContainerName();
		String objectPath = path.getObjectPath();

		OpenstackStorageClient storageClient = getStorageClient();

		if (objectPath == null) {
			throw new UnsupportedOperationException();
		} else {
			try {
				ObjectProperties properties = storageClient.root().containers().id(containerName).objects()
						.id(objectPath).metadata();
			} catch (OpenstackNotFoundException e) {
				throw new FileNotFoundException();
			}

		}
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(OpenstackPath path, Class<A> type, LinkOption... options)
			throws IOException {
		if (type == BasicFileAttributes.class || type == HashAttributes.class) {
			String containerName = path.getContainerName();
			String objectPath = path.getObjectPath();

			OpenstackStorageClient storageClient = getStorageClient();

			if (objectPath == null) {
				throw new UnsupportedOperationException();
			} else {
				ObjectProperties properties = storageClient.root().containers().id(containerName).objects()
						.id(objectPath).metadata();
				OpenstackFileAttributes attributes = new OpenstackFileAttributes(properties);
				return (A) attributes;
			}

		}
		return null;
	}

	public OpenstackCredentials getOpenstackCredentials() {
		String authUrl = getRequiredProperty(OpenstackProperties.AUTH_URL);
		String username = getRequiredProperty(OpenstackProperties.AUTH_USER);
		String password = getRequiredProperty(OpenstackProperties.AUTH_SECRET);
		String tenant = getOptionalProperty(OpenstackProperties.AUTH_TENANT, null);

		OpenstackCredentials credentials = new OpenstackCredentials(authUrl, username, password, tenant);
		return credentials;
	}
}

package org.openstack.filesystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.openstack.utils.Io;
import org.openstack.utils.PropertyUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class OpenstackFilesystemTest {
	@Test
	public void testCreateAndDeleteFile() throws Exception {
		// Path dir = Paths.get("openstack:///");

		URI uri = URI.create("openstack:///");
		Map<String, String> env = new HashMap<String, String>();

		String configPath = System.getProperties().getProperty("openstack.config", "~/.credentials/openstack");
		Properties properties = PropertyUtils.loadProperties(Io.resolve(configPath));
		PropertyUtils.copyToMap(properties, env);
		env.put("openstack.debug", "true");

		FileSystem fs = FileSystems.newFileSystem(uri, env);

		String containerPrefix = "unittest-";

		Path rootDir = null;
		for (Path root : fs.getRootDirectories()) {
			if (root.getFileName().toString().startsWith(containerPrefix)) {
				rootDir = root;
			}
		}

		if (rootDir == null) {
			rootDir = fs.getPath("/" + containerPrefix + System.currentTimeMillis());

			Files.createDirectory(rootDir);
		}

		String dirName = UUID.randomUUID().toString();

		Path dir = rootDir.resolve(dirName);
		Files.createDirectory(dir);

		List<Path> files = listChildren(dir);
		Assert.assertEquals(files.size(), 0);

		String name = UUID.randomUUID().toString();
		Path destFile = dir.resolve(name);

		Random random = new Random();
		byte[] randomData = new byte[random.nextInt(100000)];
		random.nextBytes(randomData);

		InputStream is = new ByteArrayInputStream(randomData);
		try {
			Files.copy(is, destFile);
		} finally {
			Io.safeClose(is);
		}

		files = listChildren(dir);
		Assert.assertEquals(files.size(), 1);
		Assert.assertEquals(files.get(0).getFileName().toString(), name);

		Path downloadPath = dir.resolve(name);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Files.copy(downloadPath, baos);

		Assert.assertEquals(baos.toByteArray(), randomData);

		Files.delete(downloadPath);

		boolean exists = Files.exists(downloadPath);
		Assert.assertFalse(exists);

		recursiveDelete(rootDir);
	}

	private void recursiveDelete(Path dir) throws IOException, InterruptedException {
		List<Path> paths = listChildren(dir);
		for (Path child : paths) {
			if (Files.isDirectory(child)) {
				recursiveDelete(child);
			} else {
				Files.delete(child);
			}
		}
		Files.delete(dir);
	}

	private List<Path> listChildren(Path path) throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(path);
		try {
			List<Path> paths = Lists.newArrayList();
			for (Path entry : stream) {
				paths.add(entry);
			}
			return paths;
		} finally {
			Io.safeClose(stream);
		}
	}
}

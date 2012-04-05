package org.openstack.filesystem;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

import org.openstack.model.storage.StorageObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class OpenstackDirectoryStream implements DirectoryStream<Path> {

	private final Iterable<StorageObject> listObjects;
	private final OpenstackPath basePath;
	private final java.nio.file.DirectoryStream.Filter<? super Path> filter;
	private final String baseName;

	public OpenstackDirectoryStream(OpenstackPath basePath, String baseName, Iterable<StorageObject> listObjects,
			Filter<? super Path> filter) {
		this.basePath = basePath;
		this.baseName = baseName;
		this.listObjects = listObjects;
		this.filter = filter;
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public Iterator<Path> iterator() {
		return Iterators.filter(Iterators.transform(listObjects.iterator(), new Function<StorageObject, Path>() {

			@Override
			public Path apply(StorageObject input) {
				String name = input.getName();
				if (!name.startsWith(baseName))
					throw new IllegalStateException();
				String suffix = name.substring(baseName.length());
				if (suffix.length() == 0) {
					return null;
				}
				return basePath.resolve(suffix);
			}

		}), new Predicate<Path>() {
			@Override
			public boolean apply(Path input) {
				if (input == null)
					return false;
				try {
					return filter.accept(input);
				} catch (IOException e) {
					throw new RuntimeException("Error applying filter", e);
				}
			}
		});

	}
}

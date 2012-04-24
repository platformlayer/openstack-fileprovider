package org.openstack.filesystem.common;

import java.nio.file.Path;

public abstract class PathBase implements Path {
	@Override
	public final Path resolve(String other) {
		return resolve(getFileSystem().getPath(other));
	}

	protected String joinPaths(String base, String extension) {
		if (base.endsWith("/")) {
			return base + extension;
		} else {
			return base + "/" + extension;
		}
	}

}

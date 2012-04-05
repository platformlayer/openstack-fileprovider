package org.openstack.filesystem;

import java.nio.file.attribute.BasicFileAttributes;

public interface HashAttributes extends BasicFileAttributes {

	public enum Algorithm {
		MD5;

		public boolean is(String algorithm) {
			return this.toString().equalsIgnoreCase(algorithm);
		}
	}

	public byte[] getHash(String algorithm);
}

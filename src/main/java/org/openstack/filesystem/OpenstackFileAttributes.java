package org.openstack.filesystem;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import org.openstack.model.storage.ObjectProperties;
import org.openstack.utils.Hex;

import com.google.common.base.Objects;

public class OpenstackFileAttributes implements BasicFileAttributes, HashAttributes {

	private final ObjectProperties properties;

	public OpenstackFileAttributes(ObjectProperties properties) {
		this.properties = properties;
	}

	@Override
	public FileTime lastModifiedTime() {
		return asFileTime(properties.getLastModifiedDate());
	}

	private FileTime asFileTime(Date d) {
		if (d == null) {
			return null;
		}
		return FileTime.fromMillis(d.getTime());
	}

	@Override
	public FileTime lastAccessTime() {
		return null;
	}

	@Override
	public FileTime creationTime() {
		return null;
	}

	@Override
	public boolean isRegularFile() {
		return !isDirectory();
	}

	@Override
	public boolean isDirectory() {
		String contentType = properties.getContentType();
		return Objects.equal(OpenstackFileSystem.DEFAULT_MIME_TYPE_DIRECTORY, contentType);
	}

	@Override
	public boolean isSymbolicLink() {
		return false;
	}

	@Override
	public boolean isOther() {
		return false;
	}

	@Override
	public long size() {
		return properties.getContentLength();
	}

	@Override
	public Object fileKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getHash(String algorithm) {
		if (HashAttributes.Algorithm.MD5.is(algorithm)) {
			String etag = properties.getETag();
			return Hex.fromHex(etag);
		}

		return null;
	}

}

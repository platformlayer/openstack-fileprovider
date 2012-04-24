package org.openstack.filesystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.openstack.client.storage.ObjectsResource;
import org.openstack.model.storage.ObjectProperties;

public class OpenstackWriteByteChannel implements SeekableByteChannel {

	boolean open = true;
	long position = 0;

	final ByteArrayOutputStream os;
	final ObjectProperties properties;
	final ObjectsResource objectResource;
	final WritableByteChannel out;

	public OpenstackWriteByteChannel(ObjectsResource objectResource, ObjectProperties properties) {
		this.objectResource = objectResource;
		this.properties = properties;

		// TODO: We need to replace this with something that doesn't require memory!!
		this.os = new ByteArrayOutputStream();
		this.out = Channels.newChannel(os);
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void close() throws IOException {
		open = false;

		byte[] data = os.toByteArray();
		long objectStreamLength = data.length;
		ByteArrayInputStream objectStream = new ByteArrayInputStream(data);
		objectResource.putObject(objectStream, objectStreamLength, properties);
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		int n = out.write(src);
		position += n;
		return n;
	}

	@Override
	public long position() throws IOException {
		return position;
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long size() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		throw new UnsupportedOperationException();
	}

}

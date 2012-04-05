package org.openstack.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

public class SeekableByteChannels {

	public static SeekableByteChannel wrap(final ByteChannel channel, final long size) {
		return new SeekableByteChannel() {
			long position;

			@Override
			public boolean isOpen() {
				return channel.isOpen();
			}

			@Override
			public void close() throws IOException {
				channel.close();
			}

			@Override
			public int read(ByteBuffer dst) throws IOException {
				int n = channel.read(dst);
				position += n;
				return n;
			}

			@Override
			public int write(ByteBuffer src) throws IOException {
				int n = channel.write(src);
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
				if (size < 0) {
					throw new UnsupportedOperationException();
				}
				return size;
			}

			@Override
			public SeekableByteChannel truncate(long size) throws IOException {
				throw new UnsupportedOperationException();
			}

		};
	}

	public static ByteChannel asByteChannel(final ReadableByteChannel channel) {
		return new ByteChannel() {
			@Override
			public int write(ByteBuffer src) throws IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isOpen() {
				return channel.isOpen();
			}

			@Override
			public void close() throws IOException {
				channel.close();
			}

			@Override
			public int read(ByteBuffer dst) throws IOException {
				return channel.read(dst);
			}
		};
	}

}

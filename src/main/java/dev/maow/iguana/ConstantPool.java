package dev.maow.iguana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class ConstantPool implements Iterable<Constant<?>>, Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConstantPool.class);

	private final DataInputStream stream;
	private final int length;
	private final List<Constant<?>> entries;

	private boolean closed;

	public ConstantPool(DataInputStream stream) throws IOException {
		this.stream = stream;
		this.length = stream.readUnsignedShort();
		this.entries = new ArrayList<>(this.length);
	}

	public ConstantPool(InputStream stream) throws IOException {
		this(new DataInputStream(stream));
	}

	public <T> Constant<T> get(int idx, ConstantType<T> type) throws IOException {
		final var constant = getUntyped(idx);
		return type.cast(constant);
	}

	@SafeVarargs
	public final <T> Constant<T> get(int idx, ConstantType<T>... types) throws IOException {
		final var constant = getUntyped(idx);
		return ConstantType.cast(constant, types);
	}

	public Constant<?> getUntyped(int idx) throws IOException {
		checkClosed();
		while (idx >= this.entries.size()) {
			nextEntry();
		}
		return this.entries.get(idx);
	}

	private void nextEntry() throws IOException {
		final var id = this.stream.readByte();
		final var type = ConstantType.fromId(id)
			.orElseThrow(() -> new IllegalStateException("unknown constant id: " + id));
		final var constant = type.parse(new ConstantParsingContext(this, this.stream));
		this.entries.add(constant);
	}

	public int length() {
		return this.length;
	}

	public void visit(ConstantVisitor visitor) {
		this.forEach(constant -> constant.visit(visitor));
	}

	@Override
	public void close() throws IOException {
		checkClosed();
		this.stream.close();
		this.closed = true;
	}

	private void checkClosed() {
		if (this.closed) {
			throw new IllegalStateException("closed");
		}
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public Iterator<Constant<?>> iterator() {
		return new Iterator<>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return length >= idx;
			}

			@Override
			public Constant<?> next() {
				try {
					return getUntyped(idx++);
				} catch (IOException e) {
					LOGGER.error("could not get constant at index {}", idx, e);
					return null;
				}
			}
		};
	}
}

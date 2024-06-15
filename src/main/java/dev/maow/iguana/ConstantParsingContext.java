package dev.maow.iguana;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public record ConstantParsingContext(ConstantPool pool, DataInputStream stream) {
	public DataInputStream slice(int length) throws IOException {
		final var buffer = new byte[length];
		this.stream.readFully(buffer);
		return new DataInputStream(new ByteArrayInputStream(buffer));
	}

	public ConstantParsingContext sliced(int length) throws IOException {
		return new ConstantParsingContext(pool, slice(length));
	}
}
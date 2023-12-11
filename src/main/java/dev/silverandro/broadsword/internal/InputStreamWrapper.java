package dev.silverandro.broadsword.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class InputStreamWrapper {
    private final ByteBuffer converter = ByteBuffer.allocate(8);
    private final byte[] backing = converter.array();
    private final InputStream stream;

    public InputStreamWrapper(InputStream inputStream) {
        this.stream = inputStream;
    }

    public short getShort() throws IOException {
        stream.read(backing, 0, 2);
        return converter.getShort(0);
    }

    public long getLong() throws IOException {
        stream.read(backing, 0, 8);
        return converter.getLong(0);
    }

    public byte get() throws IOException {
        return (byte) stream.read();
    }

    public int getInt() throws IOException {
        stream.read(backing, 0, 4);
        return converter.getInt(0);
    }

    public void skip(int i) throws IOException {
        stream.skipNBytes(i);
    }
}

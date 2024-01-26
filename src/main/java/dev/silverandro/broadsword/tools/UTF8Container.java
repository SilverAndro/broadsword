/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.tools;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A representation of a UTF-8 sequence of bytes. This class is used throughout broadsword to both make explicit the
 * encoding of this data and to avoid array copies in several locations. The data stored in this class is mutable and
 * must be explicitly copied when required.
 */
public final class UTF8Container {
    private final byte[] data;
    private int hash;
    private boolean hashIsZero;

    private static final byte[] JAVA = new byte[] {'j', 'a', 'v', 'a'};

    public UTF8Container(byte[] data) {
        this.data = data;
    }
    public UTF8Container(String value) { this(value.getBytes(StandardCharsets.UTF_8)); }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UTF8Container that = (UTF8Container) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && !hashIsZero) {
            h = Arrays.hashCode(data);
            if (h == 0) {
                hashIsZero = true;
            } else {
                hash = h;
            }
        }
        return h;
    }

    public boolean startsWith(char c) {
        return data[0] == c;
    }

    boolean startsWithJava() {
        return Arrays.mismatch(data, JAVA) < 0;
    }
}

package com.echo.common.util;

import java.util.Arrays;

/**
 * @author: li-yuanwen
 */
public class ByteArrayWrapper {

    private final byte[] array;
    private final int hashCode;

    public ByteArrayWrapper(byte[] array) {
        this.array = array;
        this.hashCode = Arrays.hashCode(array);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ByteArrayWrapper) {
            return Arrays.equals(array, ((ByteArrayWrapper) obj).array);
        }

        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    /**
     * Returns the array.
     *
     * @return Returns the array
     */
    public byte[] getArray() {
        return array;
    }

}

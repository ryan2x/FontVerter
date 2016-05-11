package org.fontverter.io;

import java.io.*;
import java.nio.charset.Charset;

public class ByteDataInputStream extends DataInputStream {
    private final SeekableByteArrayInputStream byteInput;
    Charset encoding = ByteDataOutputStream.OPEN_TYPE_CHARSET;

    public ByteDataInputStream(byte[] data) {
        super(new SeekableByteArrayInputStream(data));
        byteInput = (SeekableByteArrayInputStream) in;
    }

    public long readUnsignedInt() throws IOException
    {
        long byte1 = read();
        long byte2 = read();
        long byte3 = read();
        long byte4 = read();
        if (byte4 < 0)
            throw new EOFException();
        return (byte1 << 24) + (byte2 << 16) + (byte3 << 8) + (byte4 << 0);
    }

    public String readString(int length) throws IOException {
        byte[] bytes = readBytes(length);

        return new String(bytes, encoding);
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];
        for(int i = 0; i<bytes.length; i++)
            bytes[i] = (byte) in.read();
        return bytes;
    }

    public void seek(int offset) {
        byteInput.seek(offset);
    }

    // converted from pseduo C like reader code from woff spec
    public int readUIntBase128() throws IOException {
        int accum = 0;
        for (int i = 0; i < 5; i++) {
            int data_byte = readByte();

            if (i == 0 && data_byte == 0x80)
                throw new IOException("No leading 0's");


            if ((accum & 0xFE000000) > 0)
                throw new IOException("UIntBase128 read error If any of top 7 bits are set then << 7 would overflow");

            accum = (accum << 7) | (data_byte & 0x7F);

            // Spin until most significant bit of data byte is false
            if ((data_byte & 0x80) == 0) {
                return accum;
            }
        }

        throw new IOException("UIntBase128 sequence exceeds 5 bytes");
    }

    protected static class SeekableByteArrayInputStream extends ByteArrayInputStream {
        public SeekableByteArrayInputStream(byte[] buf) {
            super(buf);
        }

        public void seek(int n) {
            pos = n;
        }
    }
}

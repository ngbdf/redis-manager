package com.newegg.ec.redis.client.entity;

import io.lettuce.core.codec.RedisCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StringByteRedisCodec implements RedisCodec<String, byte[]> {
	private static final byte[] EMPTY = new byte[0];
	
	@Override
	public String decodeKey(ByteBuffer bytes) {
		 return Unpooled.wrappedBuffer(bytes).toString(UTF_8);
	}

	@Override
	public ByteBuffer encodeKey(String key) {
		return encodeAndAllocateBuffer(key);
	}

	@Override
	public ByteBuffer encodeValue(byte[] value) {
	    if (value == null) {
            return ByteBuffer.wrap(EMPTY);
        }

        return ByteBuffer.wrap(value);
	}

	@Override
	public byte[] decodeValue(ByteBuffer bytes) {
		return getBytes(bytes);
	}
	
	private static byte[] getBytes(ByteBuffer buffer) {
        int remaining = buffer.remaining();
        if (remaining == 0) {
            return EMPTY;
        }
        byte[] b = new byte[remaining];
        buffer.get(b);
        return b;
	}
	
	 private ByteBuffer encodeAndAllocateBuffer(String key) {
	        if (key == null) {
	            return ByteBuffer.wrap(EMPTY);
	        }

	        CharsetEncoder encoder = CharsetUtil.encoder(UTF_8);
	        ByteBuffer buffer = ByteBuffer.allocate((int) (encoder.maxBytesPerChar() * key.length()));

	        ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer);
	        byteBuf.clear();
	        ByteBufUtil.writeUtf8(byteBuf, key);
	        buffer.limit(byteBuf.writerIndex());

	        return buffer;
	}
}

package network.rs485.logisticspipes.util;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestUtil {

	public static byte[] getBytesFromInteger(int i) {
		ByteBuf b = buffer(Integer.BYTES);
		byte[] data = new byte[Integer.BYTES];

		b.writeInt(i);
		b.readBytes(data);

		b.release();
		return data;
	}

	@org.junit.jupiter.api.Test
	public void testGetBytesFromInteger() throws Exception {
		assertArrayEquals(new byte[] { 0, 0, 0, 0 }, getBytesFromInteger(0));
		assertArrayEquals(new byte[] { 1, 2, 3, 4 }, getBytesFromInteger(16909060));
		assertArrayEquals(new byte[] { -128, 0, 0, 0 }, getBytesFromInteger(Integer.MIN_VALUE));
		assertArrayEquals(new byte[] { 127, -1, -1, -1 }, getBytesFromInteger(Integer.MAX_VALUE));
		assertArrayEquals(new byte[] { -1, -1, -1, -1 }, getBytesFromInteger(-1));
	}
}

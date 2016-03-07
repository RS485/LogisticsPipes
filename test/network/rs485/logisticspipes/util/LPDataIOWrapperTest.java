package network.rs485.logisticspipes.util;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LPDataIOWrapperTest {

	@Test
	public void testProvideByteData() throws Exception {
		int result = 16909060;

		LPDataIOWrapper.provideData(TestUtil.getBytesFromInteger(result), dataInput -> {
			assertEquals(result, dataInput.readInt());
		});
	}

	@Test
	public void testProvideByteBufData() throws Exception {
		int result = 1234;

		ByteBuf dataBuffer = wrappedBuffer(TestUtil.getBytesFromInteger(result));

		LPDataIOWrapper.provideData(dataBuffer, dataInput -> {
			assertEquals(result, dataInput.readInt());
		});

		dataBuffer.release();
	}

	@Test
	public void testWriteData() throws Exception {
		ByteBuf dataBuffer = buffer(Integer.BYTES);

		LPDataIOWrapper.writeData(dataBuffer, dataOutput -> dataOutput.writeInt(5));
		assertEquals(5, dataBuffer.readInt());

		dataBuffer.release();
	}

	@Test
	public void testCollectData() throws Exception {
		byte[] arr = LPDataIOWrapper.collectData(dataOutput -> dataOutput.writeInt(7890));

		assertArrayEquals(TestUtil.getBytesFromInteger(7890), arr);
	}

	@Test
	public void testWriteLengthAndBytes() throws Exception {
		ByteBuf dataBuffer = buffer(Integer.BYTES * 2);
		byte[] arr = TestUtil.getBytesFromInteger(-1);

		LPDataIOWrapper.writeData(dataBuffer, dataOutput -> dataOutput.writeLengthAndBytes(arr));

		assertEquals(4, dataBuffer.readInt());
		assertEquals(-1, dataBuffer.readInt());

		dataBuffer.release();
	}

	@Test
	public void testReadLengthAndBytes() throws Exception {
		ByteBuf dataBuffer = buffer(Integer.BYTES * 2);

		dataBuffer.writeInt(4);
		dataBuffer.writeInt(-1);

		LPDataIOWrapper.provideData(dataBuffer, dataInput -> {
			byte[] bytes = dataInput.readLengthAndBytes();
			assertArrayEquals(TestUtil.getBytesFromInteger(-1), bytes);
		});

		dataBuffer.release();
	}

	@Test
	public void testWriteByte() throws Exception {

	}

	@Test
	public void testWriteByte1() throws Exception {

	}

	@Test
	public void testWriteShort() throws Exception {

	}

	@Test
	public void testWriteShort1() throws Exception {

	}

	@Test
	public void testWriteInt() throws Exception {

	}

	@Test
	public void testWriteLong() throws Exception {

	}

	@Test
	public void testWriteFloat() throws Exception {

	}

	@Test
	public void testWriteDouble() throws Exception {

	}

	@Test
	public void testWriteBoolean() throws Exception {

	}

	@Test
	public void testWriteUTF() throws Exception {

	}

	@Test
	public void testWriteByteArray() throws Exception {

	}

	@Test
	public void testWriteForgeDirection() throws Exception {

	}

	@Test
	public void testWriteLPPosition() throws Exception {

	}

	@Test
	public void testWriteBitSet() throws Exception {

	}

	@Test
	public void testWriteBooleanArray() throws Exception {

	}

	@Test
	public void testWriteIntArray() throws Exception {

	}

	@Test
	public void testWriteByteBuf() throws Exception {

	}

	@Test
	public void testWriteLongArray() throws Exception {

	}

	@Test
	public void testReadByte() throws Exception {

	}

	@Test
	public void testReadShort() throws Exception {

	}

	@Test
	public void testReadInt() throws Exception {

	}

	@Test
	public void testReadLong() throws Exception {

	}

	@Test
	public void testReadFloat() throws Exception {

	}

	@Test
	public void testReadDouble() throws Exception {

	}

	@Test
	public void testReadBoolean() throws Exception {

	}

	@Test
	public void testReadUTF() throws Exception {

	}

	@Test
	public void testReadForgeDirection() throws Exception {

	}

	@Test
	public void testReadLPPosition() throws Exception {

	}

	@Test
	public void testReadBitSet() throws Exception {

	}

	@Test
	public void testReadBooleanArray() throws Exception {

	}

	@Test
	public void testReadIntArray() throws Exception {

	}

	@Test
	public void testReadByteBuf() throws Exception {

	}

	@Test
	public void testReadLongArray() throws Exception {

	}
}

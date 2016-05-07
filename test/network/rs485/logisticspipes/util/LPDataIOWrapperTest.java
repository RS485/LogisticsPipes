package network.rs485.logisticspipes.util;

import java.util.BitSet;

import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import static io.netty.buffer.Unpooled.buffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import network.rs485.logisticspipes.world.DoubleCoordinates;

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
		byte value = 0x6f;
		ByteBuf testBuffer = buffer(Byte.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeByte(value));

		ByteBuf compareBuffer = buffer(Byte.BYTES);
		compareBuffer.writeByte(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	public void testWriteByteInt() throws Exception {
		int byteValue = 0x6f;
		ByteBuf testBuffer = buffer(Byte.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeByte(byteValue));

		ByteBuf compareBuffer = buffer(Byte.BYTES);
		compareBuffer.writeByte(byteValue);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	public void testWriteShort() throws Exception {
		short value = 0x6f0f;
		ByteBuf testBuffer = buffer(Short.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeShort(value));

		ByteBuf compareBuffer = buffer(Short.BYTES);
		compareBuffer.writeShort(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	public void testWriteShortInt() throws Exception {
		int shortValue = 0x6f0f;
		ByteBuf testBuffer = buffer(Short.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeShort(shortValue));

		ByteBuf compareBuffer = buffer(Short.BYTES);
		compareBuffer.writeShort(shortValue);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	public void testWriteInt() throws Exception {
		int value = 0x6f0f9f3f;
		ByteBuf testBuffer = buffer(Integer.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeInt(value));

		ByteBuf compareBuffer = buffer(Integer.BYTES);
		compareBuffer.writeInt(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	public void testWriteLong() throws Exception {
		long value = 0x6f0f9f3f6f0f9f3fL;
		ByteBuf testBuffer = buffer(Long.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeLong(value));

		ByteBuf compareBuffer = buffer(Long.BYTES);
		compareBuffer.writeLong(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	public void testWriteFloat() throws Exception {
		float value = 0.123456F;
		ByteBuf testBuffer = buffer(Float.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeFloat(value));

		ByteBuf compareBuffer = buffer(Float.BYTES);
		compareBuffer.writeFloat(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	public void testWriteDouble() throws Exception {
		double value = 0.1234567890123456;
		ByteBuf testBuffer = buffer(Double.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeDouble(value));

		ByteBuf compareBuffer = buffer(Double.BYTES);
		compareBuffer.writeDouble(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void testWriteBoolean() throws Exception {
		boolean value = true;
		ByteBuf testBuffer = buffer(1);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeBoolean(value));

		ByteBuf compareBuffer = buffer(1);
		compareBuffer.writeBoolean(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));
	}

	@Test
	public void testWriteUTF() throws Exception {
		String value = "◘ËTest♀StringßüöäÜÖÄ";

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeUTF(value));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(value, input.readUTF());
		});
	}

	@Test
	public void testWriteByteArray() throws Exception {
		byte[] arr = TestUtil.getBytesFromInteger(741893247);
		ByteBuf testBuffer = buffer(Integer.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeByteArray(arr));

		LPDataIOWrapper.provideData(testBuffer, input -> {
			for (byte b : arr) {
				assertEquals(b, input.readByte());
			}
		});
	}

	@Test
	public void testWriteForgeDirection() throws Exception {
		ForgeDirection value = ForgeDirection.UP;
		ByteBuf testBuffer = buffer(Long.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeForgeDirection(value));

		LPDataIOWrapper.provideData(testBuffer, input -> {
			assertEquals(value, input.readForgeDirection());
		});
	}

	@Test
	public void testWriteLPPosition() throws Exception {
		DoubleCoordinates value = new DoubleCoordinates(1.0, 2.0, 3.0);

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeLPPosition(value));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(value, input.readLPPosition());
		});
	}

	@Test
	public void testWriteBitSet() throws Exception {
		BitSet value = new BitSet(9);
		value.set(3, true);
		value.set(4, true);
		value.set(9, true);

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeBitSet(value));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(value, input.readBitSet());
		});
	}

	@Test
	public void testWriteBooleanArray() throws Exception {
		boolean[] arr = new boolean[] {true, false, true, true};

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeBooleanArray(arr));

		LPDataIOWrapper.provideData(data, input -> {
			assertArrayEquals(arr, input.readBooleanArray());
		});
	}

	@Test
	public void testWriteIntArray() throws Exception {
		int[] arr = new int[] {12, 13, 13513, Integer.MAX_VALUE, Integer.MIN_VALUE};

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeIntArray(arr));

		LPDataIOWrapper.provideData(data, input -> {
			assertArrayEquals(arr, input.readIntArray());
		});
	}

	@Test
	public void testWriteByteBuf() throws Exception {
		byte[] arr = TestUtil.getBytesFromInteger(741893247);
		ByteBuf testBuffer = buffer(Integer.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeByteArray(arr));

		// buffer in byte array
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeByteBuf(testBuffer));

		LPDataIOWrapper.provideData(data, input -> {
			LPDataIOWrapper.provideData(input.readByteBuf(), bufferInput -> {
				for (byte b : arr) {
					assertEquals(b, bufferInput.readByte());
				}
			});
		});
	}

	@Test
	public void testWriteLongArray() throws Exception {
		long[] arr = new long[] {12L, 13L, 1351312398172398L, Long.MAX_VALUE, Long.MIN_VALUE};

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeLongArray(arr));

		LPDataIOWrapper.provideData(data, input -> {
			assertArrayEquals(arr, input.readLongArray());
		});
	}

	@Test
	public void testReadShort() throws Exception {
		short value = 12;

		ByteBuf dataBuffer = buffer(Short.BYTES);
		dataBuffer.writeShort(value);

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertEquals(value, input.readShort());
		});
	}

	@Test
	public void testReadLong() throws Exception {
		long value = 1092347801374L;

		ByteBuf dataBuffer = buffer(Long.BYTES);
		dataBuffer.writeLong(value);

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertEquals(value, input.readLong());
		});
	}

	@Test
	public void testReadFloat() throws Exception {
		float value = 0.123456F;

		ByteBuf dataBuffer = buffer(Float.BYTES);
		dataBuffer.writeFloat(value);

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertEquals(value, input.readFloat(), 0.000001F);
		});
	}

	@Test
	public void testReadDouble() throws Exception {
		double value = 0.1234567890123456F;

		ByteBuf dataBuffer = buffer(Double.BYTES);
		dataBuffer.writeDouble(value);

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertEquals(value, input.readDouble(), 0.000000000000001);
		});
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void testReadBoolean() throws Exception {
		boolean value = true;

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeBoolean(value));

		LPDataIOWrapper.provideData(data, input -> {
			assertTrue(value == input.readBoolean());
		});
	}
}

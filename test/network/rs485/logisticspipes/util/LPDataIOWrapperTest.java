package network.rs485.logisticspipes.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import static io.netty.buffer.Unpooled.buffer;
import static io.netty.buffer.Unpooled.directBuffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LPDataIOWrapperTest {

	private static final String BUFFER_EMPTY_MSG = "Buffer must be empty";

	@Test
	public void testDirectBuffer() throws IOException {
		ByteBuf directBuf = directBuffer();

		LPDataIOWrapper.writeData(directBuf, output -> {
			output.writeInt(12);
			output.writeByte(13);
		});

		LPDataIOWrapper.provideData(directBuf, input -> {
			assertEquals(12, input.readInt());
			assertEquals(13, input.readByte());
		});

		assertEquals(BUFFER_EMPTY_MSG, 0, directBuf.readableBytes());
	}

	@Test
	public void testProvideByteData() throws Exception {
		int result = 16909060;

		LPDataIOWrapper.provideData(TestUtil.getBytesFromInteger(result), input -> {
			assertEquals(result, input.readInt());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testProvideByteBufData() throws Exception {
		int result = 1234;

		ByteBuf dataBuffer = wrappedBuffer(TestUtil.getBytesFromInteger(result));

		LPDataIOWrapper.provideData(dataBuffer, dataInput -> {
			assertEquals(result, dataInput.readInt());
		});

		assertEquals(BUFFER_EMPTY_MSG, 0, dataBuffer.readableBytes());

		dataBuffer.release();
	}

	@Test
	public void testWriteData() throws Exception {
		ByteBuf dataBuffer = buffer(Integer.BYTES);

		LPDataIOWrapper.writeData(dataBuffer, dataOutput -> dataOutput.writeInt(5));
		assertEquals(5, dataBuffer.readInt());

		assertEquals(BUFFER_EMPTY_MSG, 0, dataBuffer.readableBytes());

		dataBuffer.release();
	}

	@Test
	public void testCollectData() throws Exception {
		byte[] arr = LPDataIOWrapper.collectData(dataOutput -> dataOutput.writeInt(7890));

		assertArrayEquals(TestUtil.getBytesFromInteger(7890), arr);
	}

	@Test
	public void testWriteByteArray() throws Exception {
		ByteBuf dataBuffer = buffer(Integer.BYTES * 2);
		byte[] arr = TestUtil.getBytesFromInteger(-1);

		LPDataIOWrapper.writeData(dataBuffer, dataOutput -> dataOutput.writeByteArray(arr));

		assertEquals(4, dataBuffer.readInt());
		assertEquals(-1, dataBuffer.readInt());

		assertEquals(BUFFER_EMPTY_MSG, 0, dataBuffer.readableBytes());

		dataBuffer.release();
	}

	@Test
	public void testNullByteArray() throws IOException {
		ByteBuf dataBuffer = Unpooled.buffer();

		LPDataIOWrapper.writeData(dataBuffer, output -> output.writeByteArray(null));

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertNull(input.readByteArray());
		});

		assertEquals(BUFFER_EMPTY_MSG, 0, dataBuffer.readableBytes());

		dataBuffer.release();
	}

	@Test
	public void testReadByteArray() throws Exception {
		ByteBuf dataBuffer = buffer(Integer.BYTES * 2);

		dataBuffer.writeInt(4);
		dataBuffer.writeInt(-1);

		LPDataIOWrapper.provideData(dataBuffer, dataInput -> {
			byte[] bytes = dataInput.readByteArray();
			assertArrayEquals(TestUtil.getBytesFromInteger(-1), bytes);
		});

		assertEquals(BUFFER_EMPTY_MSG, 0, dataBuffer.readableBytes());

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

		testBuffer.release();
		compareBuffer.release();
	}

	@Test
	public void testWriteByteInt() throws Exception {
		int byteValue = 0x6f;
		ByteBuf testBuffer = buffer(Byte.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeByte(byteValue));

		ByteBuf compareBuffer = buffer(Byte.BYTES);
		compareBuffer.writeByte(byteValue);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));

		testBuffer.release();
		compareBuffer.release();
	}

	@Test
	public void testWriteShort() throws Exception {
		short value = 0x6f0f;
		ByteBuf testBuffer = buffer(Short.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeShort(value));

		ByteBuf compareBuffer = buffer(Short.BYTES);
		compareBuffer.writeShort(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));

		testBuffer.release();
		compareBuffer.release();
	}

	@Test
	public void testWriteShortInt() throws Exception {
		int shortValue = 0x6f0f;
		ByteBuf testBuffer = buffer(Short.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeShort(shortValue));

		ByteBuf compareBuffer = buffer(Short.BYTES);
		compareBuffer.writeShort(shortValue);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));

		testBuffer.release();
		compareBuffer.release();
	}

	@Test
	public void testWriteInt() throws Exception {
		int value = 0x6f0f9f3f;
		ByteBuf testBuffer = buffer(Integer.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeInt(value));

		ByteBuf compareBuffer = buffer(Integer.BYTES);
		compareBuffer.writeInt(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));

		testBuffer.release();
		compareBuffer.release();
	}

	@Test
	public void testWriteLong() throws Exception {
		long value = 0x6f0f9f3f6f0f9f3fL;
		ByteBuf testBuffer = buffer(Long.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeLong(value));

		ByteBuf compareBuffer = buffer(Long.BYTES);
		compareBuffer.writeLong(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));

		testBuffer.release();
		compareBuffer.release();
	}

	@Test
	public void testWriteFloat() throws Exception {
		float value = 0.123456F;
		ByteBuf testBuffer = buffer(Float.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeFloat(value));

		ByteBuf compareBuffer = buffer(Float.BYTES);
		compareBuffer.writeFloat(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));

		testBuffer.release();
		compareBuffer.release();
	}

	@Test
	public void testWriteDouble() throws Exception {
		double value = 0.1234567890123456;
		ByteBuf testBuffer = buffer(Double.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeDouble(value));

		ByteBuf compareBuffer = buffer(Double.BYTES);
		compareBuffer.writeDouble(value);

		assertTrue(ByteBufUtil.equals(testBuffer, compareBuffer));

		testBuffer.release();
		compareBuffer.release();
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

		testBuffer.release();
		compareBuffer.release();
	}

	@Test
	public void testUTF() throws Exception {
		String value = "◘ËTest♀StringßüöäÜÖÄ";

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeUTF(value));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(value, input.readUTF());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNullUTF() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeUTF(null));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readUTF());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testForgeDirection() throws Exception {
		ForgeDirection value = ForgeDirection.UP;
		ByteBuf testBuffer = buffer(Long.BYTES);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeForgeDirection(value));

		LPDataIOWrapper.provideData(testBuffer, input -> {
			assertEquals(value, input.readForgeDirection());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});

		testBuffer.release();
	}

	@Test
	public void testNullForgeDirection() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeForgeDirection(null));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readForgeDirection());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testLPPosition() throws Exception {
		DoubleCoordinates value = new DoubleCoordinates(1.0, 2.0, 3.0);

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeLPPosition(value));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(value, input.readLPPosition());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testBitSet() throws Exception {
		BitSet value = new BitSet(9);
		value.set(3, true);
		value.set(4, true);
		value.set(9, true);

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeBitSet(value));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(value, input.readBitSet());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test(expected = NullPointerException.class)
	public void testNullBitSet() throws Exception {
		LPDataIOWrapper.collectData(output -> output.writeBitSet(null));
	}

	@Test
	public void testBooleanArray() throws Exception {
		boolean[] arr = new boolean[] { true, false, true, true };

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeBooleanArray(arr));

		LPDataIOWrapper.provideData(data, input -> {
			assertArrayEquals(arr, input.readBooleanArray());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testEmptyBooleanArray() throws Exception {
		boolean[] arr = new boolean[0];

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeBooleanArray(arr));

		LPDataIOWrapper.provideData(data, input -> {
			assertArrayEquals(arr, input.readBooleanArray());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNullBooleanArray() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeBooleanArray(null));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readBooleanArray());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test(expected = NullPointerException.class)
	public void testInvalidBooleanArray() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> {
			output.writeInt(12);
			output.writeByteArray(null);
		});

		LPDataIOWrapper.provideData(data, LPDataInput::readBooleanArray);
	}

	@Test
	public void testIntArray() throws Exception {
		int[] arr = new int[] { 12, 13, 13513, Integer.MAX_VALUE, Integer.MIN_VALUE };

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeIntArray(arr));

		LPDataIOWrapper.provideData(data, input -> {
			assertArrayEquals(arr, input.readIntArray());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNullIntArray() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeIntArray(null));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readIntArray());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testByteBuf() throws Exception {
		byte[] arr = TestUtil.getBytesFromInteger(741893247);
		ByteBuf testBuffer = buffer(arr.length);

		LPDataIOWrapper.writeData(testBuffer, output -> output.writeBytes(arr));

		// buffer in byte array
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeByteBuf(testBuffer));

		LPDataIOWrapper.provideData(data, input -> {
			LPDataIOWrapper.provideData(input.readByteBuf(), bufferInput -> {
				assertArrayEquals(arr, bufferInput.readBytes(arr.length));

				assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) bufferInput).localBuffer.readableBytes());
			});

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test(expected = NullPointerException.class)
	public void testNullByteBuf() throws Exception {
		LPDataIOWrapper.collectData(output -> output.writeByteBuf(null));
	}

	@Test(expected = NullPointerException.class)
	public void testInvalidByteBuf() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeByteArray(null));

		LPDataIOWrapper.provideData(data, LPDataInput::readByteBuf);
	}

	@Test
	public void testLongArray() throws Exception {
		long[] arr = new long[] { 12L, 13L, 1351312398172398L, Long.MAX_VALUE, Long.MIN_VALUE };

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeLongArray(arr));

		LPDataIOWrapper.provideData(data, input -> {
			assertArrayEquals(arr, input.readLongArray());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNullLongArray() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeLongArray(null));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readLongArray());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testReadShort() throws Exception {
		short value = 12;

		ByteBuf dataBuffer = buffer(Short.BYTES);
		dataBuffer.writeShort(value);

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertEquals(value, input.readShort());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testReadLong() throws Exception {
		long value = 1092347801374L;

		ByteBuf dataBuffer = buffer(Long.BYTES);
		dataBuffer.writeLong(value);

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertEquals(value, input.readLong());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testReadFloat() throws Exception {
		float value = 0.123456F;

		ByteBuf dataBuffer = buffer(Float.BYTES);
		dataBuffer.writeFloat(value);

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertEquals(value, input.readFloat(), 0.000001F);

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testReadDouble() throws Exception {
		double value = 0.1234567890123456F;

		ByteBuf dataBuffer = buffer(Double.BYTES);
		dataBuffer.writeDouble(value);

		LPDataIOWrapper.provideData(dataBuffer, input -> {
			assertEquals(value, input.readDouble(), 0.000000000000001);

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	public void testBoolean() throws Exception {
		boolean value = true;

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeBoolean(value));

		LPDataIOWrapper.provideData(data, input -> {
			assertTrue(value == input.readBoolean());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNBTTagCompound() throws Exception {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("bool", true);
		tag.setByte("byte", (byte) 127);
		tag.setByteArray("byteArray", new byte[] { -1, 127, 0, 12 });
		tag.setDouble("double", 0.12D);
		tag.setFloat("float", 0.13F);
		tag.setIntArray("intArray", new int[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE, -1 });
		tag.setInteger("int", 12);
		tag.setLong("long", -1);
		tag.setShort("short", (short) 15);
		tag.setString("string", "text");
		tag.setTag("tag", new NBTTagCompound());

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeNBTTagCompound(tag));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(tag, input.readNBTTagCompound());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNullNBTTagCompound() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeNBTTagCompound(null));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readNBTTagCompound());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	// ItemStack cannot be unit tested

	@Test
	public void testNullItemStack() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeItemStack(null));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readItemStack());

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testArrayList() throws Exception {
		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add("drölf");
		arrayList.add("text");

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeCollection(arrayList, LPDataOutput::writeUTF));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(arrayList, input.readArrayList(LPDataInput::readUTF));

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNullArrayList() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeCollection(null, LPDataOutput::writeUTF));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readArrayList(LPDataInput::readUTF));

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testLinkedList() throws Exception {
		LinkedList<String> linkedList = new LinkedList<>();
		linkedList.add("drölf");
		linkedList.add("text");

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeCollection(linkedList, LPDataOutput::writeUTF));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(linkedList, input.readLinkedList(LPDataInput::readUTF));

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNullLinkedList() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeCollection(null, LPDataOutput::writeUTF));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readLinkedList(LPDataInput::readUTF));

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testSet() throws Exception {
		HashSet<String> set = new HashSet<>();
		set.add("drölf");
		set.add("text");

		byte[] data = LPDataIOWrapper.collectData(output -> output.writeCollection(set, LPDataOutput::writeUTF));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(set, input.readSet(LPDataInput::readUTF));

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}

	@Test
	public void testNullSet() throws Exception {
		byte[] data = LPDataIOWrapper.collectData(output -> output.writeCollection(null, LPDataOutput::writeUTF));

		LPDataIOWrapper.provideData(data, input -> {
			assertEquals(null, input.readSet(LPDataInput::readUTF));

			assertEquals(BUFFER_EMPTY_MSG, 0, ((LPDataIOWrapper) input).localBuffer.readableBytes());
		});
	}
}

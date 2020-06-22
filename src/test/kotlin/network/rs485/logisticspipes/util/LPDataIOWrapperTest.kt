/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.util

import io.netty.buffer.Unpooled
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import network.rs485.logisticspipes.util.TestUtil.Companion.getBytesFromInteger
import network.rs485.util.use
import java.util.*
import kotlin.test.*

private const val BUFFER_EMPTY_MSG = "Buffer must be empty"

class LPDataIOWrapperTest {
    @Test
    fun testDirectBuffer() {
        Unpooled.directBuffer().use { directBuf ->
            LPDataIOWrapper.writeData(directBuf) { output: LPDataOutput ->
                output.writeInt(12)
                output.writeByte(13)
            }
            LPDataIOWrapper.provideData(directBuf) { input: LPDataInput ->
                assertEquals(12, input.readInt())
                assertEquals(13, input.readByte())
            }
            assertEquals(0, directBuf.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testProvideByteData() {
        val result = 16909060
        LPDataIOWrapper.provideData(getBytesFromInteger(result)) { input: LPDataInput ->
            assertEquals(result, input.readInt())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testProvideByteBufData() {
        val result = 1234
        Unpooled.wrappedBuffer(getBytesFromInteger(result)).use { dataBuffer ->
            LPDataIOWrapper.provideData(dataBuffer) { dataInput: LPDataInput -> assertEquals(result, dataInput.readInt()) }
            assertEquals(0, dataBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testWriteData() {
        val dataBuffer = Unpooled.buffer(Integer.BYTES)
        LPDataIOWrapper.writeData(dataBuffer) { dataOutput: LPDataOutput -> dataOutput.writeInt(5) }
        assertEquals(5, dataBuffer.readInt())
        assertEquals(0, dataBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        dataBuffer.release()
    }

    @Test
    fun testCollectData() {
        val arr = LPDataIOWrapper.collectData { dataOutput: LPDataOutput -> dataOutput.writeInt(7890) }
        assertTrue(getBytesFromInteger(7890).contentEquals(arr))
    }

    @Test
    fun testWriteByteArray() {
        val dataBuffer = Unpooled.buffer(Integer.BYTES * 2)
        val arr = getBytesFromInteger(-1)
        LPDataIOWrapper.writeData(dataBuffer) { dataOutput: LPDataOutput -> dataOutput.writeByteArray(arr) }
        assertEquals(4, dataBuffer.readInt())
        assertEquals(-1, dataBuffer.readInt())
        assertEquals(0, dataBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        dataBuffer.release()
    }

    @Test
    fun testNullByteArray() {
        val dataBuffer = Unpooled.buffer()
        LPDataIOWrapper.writeData(dataBuffer) { output: LPDataOutput -> output.writeByteArray(null) }
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput -> assertNull(input.readByteArray()) }
        assertEquals(0, dataBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        dataBuffer.release()
    }

    @Test
    fun testReadByteArray() {
        val dataBuffer = Unpooled.buffer(Integer.BYTES * 2)
        dataBuffer.writeInt(4)
        dataBuffer.writeInt(-1)
        LPDataIOWrapper.provideData(dataBuffer) { dataInput: LPDataInput ->
            val bytes = dataInput.readByteArray()!!
            assertTrue(getBytesFromInteger(-1).contentEquals(bytes))
        }
        assertEquals(0, dataBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        dataBuffer.release()
    }

    @Test
    fun testWriteByte() {
        val value: Byte = 0x6f
        val testBuffer = Unpooled.buffer(java.lang.Byte.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeByte(value) }
        val compareBuffer = Unpooled.buffer(java.lang.Byte.BYTES)
        compareBuffer.writeByte(value.toInt())
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testWriteByteInt() {
        val byteValue = 0x6f
        val testBuffer = Unpooled.buffer(java.lang.Byte.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeByte(byteValue) }
        val compareBuffer = Unpooled.buffer(java.lang.Byte.BYTES)
        compareBuffer.writeByte(byteValue)
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testWriteShort() {
        val value: Short = 0x6f0f
        val testBuffer = Unpooled.buffer(java.lang.Short.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeShort(value) }
        val compareBuffer = Unpooled.buffer(java.lang.Short.BYTES)
        compareBuffer.writeShort(value.toInt())
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testWriteShortInt() {
        val shortValue = 0x6f0f
        val testBuffer = Unpooled.buffer(java.lang.Short.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeShort(shortValue) }
        val compareBuffer = Unpooled.buffer(java.lang.Short.BYTES)
        compareBuffer.writeShort(shortValue)
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testWriteInt() {
        val value = 0x6f0f9f3f
        val testBuffer = Unpooled.buffer(Integer.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeInt(value) }
        val compareBuffer = Unpooled.buffer(Integer.BYTES)
        compareBuffer.writeInt(value)
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testWriteLong() {
        val value = 0x6f0f9f3f6f0f9f3fL
        val testBuffer = Unpooled.buffer(java.lang.Long.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeLong(value) }
        val compareBuffer = Unpooled.buffer(java.lang.Long.BYTES)
        compareBuffer.writeLong(value)
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testWriteFloat() {
        val value = 0.123456f
        val testBuffer = Unpooled.buffer(java.lang.Float.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeFloat(value) }
        val compareBuffer = Unpooled.buffer(java.lang.Float.BYTES)
        compareBuffer.writeFloat(value)
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testWriteDouble() {
        val value = 0.1234567890123456
        val testBuffer = Unpooled.buffer(java.lang.Double.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeDouble(value) }
        val compareBuffer = Unpooled.buffer(java.lang.Double.BYTES)
        compareBuffer.writeDouble(value)
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testWriteBoolean() {
        val value = true
        val testBuffer = Unpooled.buffer(1)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeBoolean(value) }
        val compareBuffer = Unpooled.buffer(1)
        compareBuffer.writeBoolean(value)
        assertEquals(testBuffer, compareBuffer)
        testBuffer.release()
        compareBuffer.release()
    }

    @Test
    fun testUTF() {
        val value = "◘ËTest♀StringßüöäÜÖÄ"
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTF(value) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(value, input.readUTF())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullUTF() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTF(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readUTF())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testForgeDirection() {
        val value = EnumFacing.UP
        val testBuffer = Unpooled.buffer(java.lang.Long.BYTES)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeFacing(value) }
        LPDataIOWrapper.provideData(testBuffer) { input: LPDataInput ->
            assertEquals(value, input.readFacing())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
        testBuffer.release()
    }

    @Test
    fun testNullForgeDirection() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeFacing(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readFacing())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testBitSet() {
        val value = BitSet(9)
        value[3] = true
        value[4] = true
        value[9] = true
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBitSet(value) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(value, input.readBitSet())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullBitSet() {
        assertFailsWith<NullPointerException> {
            LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBitSet(null) }
        }
    }

    @Test
    fun testBooleanArray() {
        val arr = booleanArrayOf(true, false, true, true)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBooleanArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertTrue(arr.contentEquals(input.readBooleanArray()!!))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testEmptyBooleanArray() {
        val arr = BooleanArray(0)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBooleanArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertTrue(arr.contentEquals(input.readBooleanArray()!!))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullBooleanArray() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBooleanArray(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readBooleanArray())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testInvalidBooleanArray() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput ->
            output.writeInt(12)
            output.writeByteArray(null)
        }
        assertFailsWith<NullPointerException> {
            LPDataIOWrapper.provideData(data) { obj: LPDataInput -> obj.readBooleanArray() }
        }
    }

    @Test
    fun testIntArray() {
        val arr = intArrayOf(12, 13, 13513, Int.MAX_VALUE, Int.MIN_VALUE)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeIntArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertTrue(arr.contentEquals(input.readIntArray()!!))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullIntArray() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeIntArray(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readIntArray())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testByteBuf() {
        val arr = getBytesFromInteger(741893247)
        val testBuffer = Unpooled.buffer(arr.size)
        LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeBytes(arr) }

        // buffer in byte array
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeByteBuf(testBuffer) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            LPDataIOWrapper.provideData(input.readByteBuf()) { bufferInput: LPDataInput ->
                assertTrue(arr.contentEquals(bufferInput.readBytes(arr.size)))
                assertEquals(0, (bufferInput as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
            }
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullByteBuf() {
        assertFailsWith<NullPointerException> {
            LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeByteBuf(null) }
        }
    }

    @Test
    fun testInvalidByteBuf() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeByteArray(null) }
        assertFailsWith<NullPointerException> {
            LPDataIOWrapper.provideData(data) { obj: LPDataInput -> obj.readByteBuf() }
        }
    }

    @Test
    fun testLongArray() {
        val arr = longArrayOf(12L, 13L, 1351312398172398L, Long.MAX_VALUE, Long.MIN_VALUE)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeLongArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertTrue(arr.contentEquals(input.readLongArray()!!))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullLongArray() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeLongArray(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readLongArray())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testReadShort() {
        val value: Short = 12
        val dataBuffer = Unpooled.buffer(java.lang.Short.BYTES)
        dataBuffer.writeShort(value.toInt())
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
            assertEquals(value, input.readShort())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testReadLong() {
        val value = 1092347801374L
        val dataBuffer = Unpooled.buffer(java.lang.Long.BYTES)
        dataBuffer.writeLong(value)
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
            assertEquals(value, input.readLong())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testReadFloat() {
        val value = 0.123456f
        val dataBuffer = Unpooled.buffer(java.lang.Float.BYTES)
        dataBuffer.writeFloat(value)
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
            // compares floating point numbers correctly in Kotlin
            assertEquals(value, input.readFloat())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testReadDouble() {
        val value = 0.1234567890123456
        val dataBuffer = Unpooled.buffer(java.lang.Double.BYTES)
        dataBuffer.writeDouble(value)
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
            // compares floating point numbers correctly in Kotlin
            assertEquals(value, input.readDouble())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testBoolean() {
        val value = true
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBoolean(value) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(value, input.readBoolean())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNBTTagCompound() {
        val tag = NBTTagCompound()
        tag.setBoolean("bool", true)
        tag.setByte("byte", 127.toByte())
        tag.setByteArray("byteArray", byteArrayOf(-1, 127, 0, 12))
        tag.setDouble("double", 0.12)
        tag.setFloat("float", 0.13f)
        tag.setIntArray("intArray", intArrayOf(Int.MIN_VALUE, 0, Int.MAX_VALUE, -1))
        tag.setInteger("int", 12)
        tag.setLong("long", -1)
        tag.setShort("short", 15.toShort())
        tag.setString("string", "text")
        tag.setTag("tag", NBTTagCompound())
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeNBTTagCompound(tag) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(tag, input.readNBTTagCompound())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullNBTTagCompound() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeNBTTagCompound(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readNBTTagCompound())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testEmptyItemStack() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeItemStack(ItemStack.EMPTY) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(input.readItemStack(), ItemStack.EMPTY)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testArrayList() {
        val arrayList = ArrayList<String>()
        arrayList.add("drölf")
        arrayList.add("text")
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection(arrayList) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(arrayList, input.readArrayList<String> { obj: LPDataInput -> obj.readUTF() })
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullArrayList() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection<String>(null) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readArrayList { obj: LPDataInput -> obj.readUTF() })
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testLinkedList() {
        val linkedList = LinkedList<String>()
        linkedList.add("drölf")
        linkedList.add("text")
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection(linkedList) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(linkedList, input.readLinkedList<String> { obj: LPDataInput -> obj.readUTF() })
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullLinkedList() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection<String>(null) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readLinkedList { obj: LPDataInput -> obj.readUTF() })
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testSet() {
        val set = HashSet<String>()
        set.add("drölf")
        set.add("text")
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection(set) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(set, input.readSet { obj: LPDataInput -> obj.readUTF() })
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullSet() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection<String>(null) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readSet { obj: LPDataInput -> obj.readUTF() })
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNonNullList() {
        val nonNullList = NonNullList.withSize(3, "")
        nonNullList[0] = "drölf"
        nonNullList[1] = "text"
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection(nonNullList) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertEquals(nonNullList, input.readNonNullList({ obj: LPDataInput -> obj.readUTF() }, ""))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullNonNullList() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection<String>(null) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readNonNullList({ obj: LPDataInput -> obj.readUTF() }, ""))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testUTFArray() {
        val array = arrayOf("◘ËTest♀StringßüöäÜÖÄ", "◘ËTest♀TESTpartßüöäÜÖÄ")
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTFArray(array) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertTrue(array.contentEquals(input.readUTFArray()!!))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testNullUTFArray() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTFArray(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertNull(input.readUTFArray())
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun testUTFArrayNull() {
        val array = arrayOf("◘ËTest♀StringßüöäÜÖÄ", null)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTFArray(array) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            assertTrue(array.contentEquals(input.readUTFArray()!!))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }
}

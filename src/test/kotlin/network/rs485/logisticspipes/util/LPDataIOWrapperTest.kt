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
    fun `test reading and writing to direct buffer`() {
        Unpooled.directBuffer().use { directBuf ->
            LPDataIOWrapper.writeData(directBuf) { output: LPDataOutput ->
                output.writeInt(12)
                output.writeByte(13)
            }
            LPDataIOWrapper.provideData(directBuf) { input: LPDataInput ->
                val firstReadVal = input.readInt()
                val secondReadVal = input.readByte()

                assertEquals(12, firstReadVal)
                assertEquals(13, secondReadVal)
            }
            val bytesLeft = directBuf.readableBytes()
            assertEquals(0, bytesLeft, BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test provideData on byte array`() {
        val result = 16909060
        LPDataIOWrapper.provideData(getBytesFromInteger(result)) { input: LPDataInput ->
            val actual = input.readInt()

            assertEquals(result, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test provideData with a ByteBuf`() {
        val result = 1234
        Unpooled.wrappedBuffer(getBytesFromInteger(result)).use { dataBuffer ->
            LPDataIOWrapper.provideData(dataBuffer) { dataInput: LPDataInput -> assertEquals(result, dataInput.readInt()) }
            val actual = dataBuffer.readableBytes()

            assertEquals(0, actual, BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeData on ByteBuf`() {
        Unpooled.buffer(Integer.BYTES).use { dataBuffer ->
            LPDataIOWrapper.writeData(dataBuffer) { dataOutput: LPDataOutput -> dataOutput.writeInt(5) }
            val actual = dataBuffer.readInt()

            assertEquals(5, actual)
            assertEquals(0, dataBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test collectData with an integer`() {
        val value = 7890
        val expected = getBytesFromInteger(value)
        val arr = LPDataIOWrapper.collectData { dataOutput: LPDataOutput -> dataOutput.writeInt(value) }

        assertTrue(expected.contentEquals(arr))
    }

    @Test
    fun `test writeByteArray to a ByteBuf`() {
        Unpooled.buffer(Int.SIZE_BYTES * 2).use { dataBuffer ->
            val arr = getBytesFromInteger(-1)
            LPDataIOWrapper.writeData(dataBuffer) { dataOutput: LPDataOutput -> dataOutput.writeByteArray(arr) }
            val firstReadVal = dataBuffer.readInt()
            val secondReadVal = dataBuffer.readInt()

            assertEquals(4, firstReadVal)
            assertEquals(-1, secondReadVal)

            val bytesLeft = dataBuffer.readableBytes()
            assertEquals(0, bytesLeft, BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeByteArray and readByteArray with null`() {
        Unpooled.buffer().use { dataBuffer ->
            LPDataIOWrapper.writeData(dataBuffer) { output: LPDataOutput -> output.writeByteArray(null) }
            LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
                val actual = input.readByteArray()

                assertNull(actual)
            }
            val bytesLeft = dataBuffer.readableBytes()
            assertEquals(0, bytesLeft, BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test readByteArray with four bytes`() {
        Unpooled.buffer(Int.SIZE_BYTES * 2).use { dataBuffer ->
            dataBuffer.writeInt(4)
            dataBuffer.writeInt(-1)
            LPDataIOWrapper.provideData(dataBuffer) { dataInput: LPDataInput ->
                val expected = getBytesFromInteger(-1)
                val actual = dataInput.readByteArray()!!

                assertTrue(expected.contentEquals(actual))
            }
            val bytesLeft = dataBuffer.readableBytes()
            assertEquals(0, bytesLeft, BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeByte with a single byte`() {
        val value: Byte = 0x6f
        Unpooled.buffer(Byte.SIZE_BYTES).use { expected ->
            expected.writeByte(value.toInt())
            Unpooled.buffer(Byte.SIZE_BYTES).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeByte(value) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeByte with a byte converted to int`() {
        val byteValue = 0x6f
        Unpooled.buffer(Byte.SIZE_BYTES).use { expected ->
            expected.writeByte(byteValue)
            Unpooled.buffer(Byte.SIZE_BYTES).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeByte(byteValue) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeShort with a single short`() {
        val value: Short = 0x6f0f
        Unpooled.buffer(java.lang.Short.BYTES).use { expected ->
            expected.writeShort(value.toInt())
            Unpooled.buffer(java.lang.Short.BYTES).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeShort(value) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeShort with a short converted to int`() {
        val shortValue = 0x6f0f
        Unpooled.buffer(java.lang.Short.BYTES).use { expected ->
            expected.writeShort(shortValue)
            Unpooled.buffer(java.lang.Short.BYTES).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeShort(shortValue) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeInt with a single int`() {
        val value = 0x6f0f9f3f
        Unpooled.buffer(Int.SIZE_BYTES).use { expected ->
            expected.writeInt(value)
            Unpooled.buffer(Int.SIZE_BYTES).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeInt(value) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeLong with a single long`() {
        val value = 0x6f0f9f3f6f0f9f3fL
        Unpooled.buffer(Long.SIZE_BYTES).use { expected ->
            expected.writeLong(value)
            Unpooled.buffer(Long.SIZE_BYTES).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeLong(value) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeFloat with a single float`() {
        val value = 0.123456f
        Unpooled.buffer(java.lang.Float.BYTES).use { expected ->
            expected.writeFloat(value)
            Unpooled.buffer(java.lang.Float.BYTES).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeFloat(value) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeDouble with a single double value`() {
        val value = 0.1234567890123456
        Unpooled.buffer(java.lang.Double.BYTES).use { expected ->
            expected.writeDouble(value)
            Unpooled.buffer(java.lang.Double.BYTES).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeDouble(value) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeBoolean with a single boolean`() {
        val value = true
        Unpooled.buffer(1).use { expected ->
            expected.writeBoolean(value)
            Unpooled.buffer(1).use { actual ->
                LPDataIOWrapper.writeData(actual) { output: LPDataOutput -> output.writeBoolean(value) }

                assertEquals(expected, actual)
            }
        }
    }

    @Test
    fun `test writeUTF and readUTF with a unicode string`() {
        val value = "◘ËTest♀StringßüöäÜÖÄ"
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTF(value) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readUTF()

            assertEquals(value, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeUTF and readUTF with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTF(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readUTF()

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeFacing and readFacing for a single EnumFacing value`() {
        val value = EnumFacing.UP
        Unpooled.buffer(Long.SIZE_BYTES).use { testBuffer ->
            LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeFacing(value) }
            LPDataIOWrapper.provideData(testBuffer) { input: LPDataInput ->
                val actual = input.readFacing()

                assertEquals(value, actual)
                assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
            }
        }
    }

    @Test
    fun `test writeFacing and readFacing with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeFacing(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readFacing()

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeBitSet and readBitSet`() {
        val value = BitSet(9)
        value[3] = true
        value[4] = true
        value[9] = true
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBitSet(value) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readBitSet()

            assertEquals(value, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeBooleanArray and readBooleanArray`() {
        val arr = booleanArrayOf(true, false, true, true)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBooleanArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readBooleanArray()!!

            assertTrue(arr.contentEquals(actual))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeBooleanArray and readBooleanArray with empty array`() {
        val arr = BooleanArray(0)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBooleanArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readBooleanArray()!!

            assertTrue(arr.contentEquals(actual))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeBooleanArray and readBooleanArray with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBooleanArray(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readBooleanArray()

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test readBooleanArray with invalid values`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput ->
            output.writeInt(12)
            output.writeByteArray(null)
        }

        assertFailsWith<NullPointerException> {
            LPDataIOWrapper.provideData(data) { obj: LPDataInput -> obj.readBooleanArray() }
        }
    }

    @Test
    fun `test writeIntArray and readIntArray`() {
        val arr = intArrayOf(12, 13, 13513, Int.MAX_VALUE, Int.MIN_VALUE)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeIntArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readIntArray()!!

            assertTrue(arr.contentEquals(actual))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeIntArray and readIntArray with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeIntArray(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readIntArray()

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writing and reading bytes via ByteBuf`() {
        val arr = getBytesFromInteger(741893247)
        Unpooled.buffer(arr.size).use { testBuffer ->
            LPDataIOWrapper.writeData(testBuffer) { output: LPDataOutput -> output.writeBytes(arr) }

            // buffer in byte array
            val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeByteBuf(testBuffer) }
            LPDataIOWrapper.provideData(data) { input: LPDataInput ->
                LPDataIOWrapper.provideData(input.readByteBuf()) { bufferInput: LPDataInput ->
                    val actual = bufferInput.readBytes(arr.size)

                    assertTrue(arr.contentEquals(actual))
                    assertEquals(0, (bufferInput as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
                }
                assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
            }
        }
    }

    @Test
    fun `test writeByteBuf with null`() {
        assertFailsWith<NullPointerException> {
            LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeByteBuf(null) }
        }
    }

    @Test
    fun `test readByteBuf with invalid values`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeByteArray(null) }
        assertFailsWith<NullPointerException> {
            LPDataIOWrapper.provideData(data) { obj: LPDataInput -> obj.readByteBuf() }
        }
    }

    @Test
    fun `test writeLongArray and readLongArray`() {
        val arr = longArrayOf(12L, 13L, 1351312398172398L, Long.MAX_VALUE, Long.MIN_VALUE)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeLongArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readLongArray()!!

            assertTrue(arr.contentEquals(actual))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeLongArray and readLongArray with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeLongArray(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readLongArray()

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test readShort with regular value`() {
        val value: Short = 12
        val dataBuffer = Unpooled.buffer(java.lang.Short.BYTES)
        dataBuffer.writeShort(value.toInt())
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
            val actual = input.readShort()

            assertEquals(value, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test readLong with regular value`() {
        val value = 1092347801374L
        val dataBuffer = Unpooled.buffer(Long.SIZE_BYTES)
        dataBuffer.writeLong(value)
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
            val actual = input.readLong()

            assertEquals(value, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test readFloat with regular value`() {
        val value = 0.123456f
        val dataBuffer = Unpooled.buffer(java.lang.Float.BYTES)
        dataBuffer.writeFloat(value)
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
            val actual = input.readFloat()

            // compares floating point numbers correctly in Kotlin
            assertEquals(value, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test readDouble with regular value`() {
        val value = 0.1234567890123456
        val dataBuffer = Unpooled.buffer(java.lang.Double.BYTES)
        dataBuffer.writeDouble(value)
        LPDataIOWrapper.provideData(dataBuffer) { input: LPDataInput ->
            val actual = input.readDouble()

            // compares floating point numbers correctly in Kotlin
            assertEquals(value, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test readBoolean with regular value`() {
        val value = true
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeBoolean(value) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readBoolean()

            assertEquals(value, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeNBTTagCompound and readNBTTagCompound with plenty information`() {
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
            val actual = input.readNBTTagCompound()

            assertEquals(tag, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeNBTTagCompound and readNBTTagCompound with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeNBTTagCompound(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readNBTTagCompound()

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test readItemStack with EMPTY value`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeItemStack(ItemStack.EMPTY) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readItemStack()

            assertEquals(ItemStack.EMPTY, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeCollection and readArrayList with UTF strings`() {
        val arr = arrayListOf("drölf", "text")
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection(arr) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readArrayList<String> { obj: LPDataInput -> obj.readUTF() }

            assertEquals(arr, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeCollection and readArrayList with null UTF string`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection<String>(null) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readArrayList { obj: LPDataInput -> obj.readUTF() }

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeCollection and readLinkedList with UTF strings`() {
        val linkedList = LinkedList<String>()
        linkedList.add("drölf")
        linkedList.add("text")
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection(linkedList) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readLinkedList<String> { obj: LPDataInput -> obj.readUTF() }

            assertEquals(linkedList, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeCollection and readLinkedList with UTF null value`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection<String>(null) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readLinkedList { obj: LPDataInput -> obj.readUTF() }

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeCollection and readSet with UTF strings`() {
        val expected = setOf("drölf", "text")
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection(expected) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readSet { obj: LPDataInput -> obj.readUTF() }

            assertEquals(expected, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeCollection and readSet with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput ->
            output.writeCollection<String>(null) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) }
        }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readSet { obj: LPDataInput -> obj.readUTF() }

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeCollection and readNonNullList with UTF strings`() {
        val expected = NonNullList.withSize(3, "")
        expected[0] = "drölf"
        expected[1] = "text"
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection(expected) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readNonNullList({ obj: LPDataInput -> obj.readUTF() }, "")

            assertEquals(expected, actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeCollection and readNonNullList with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeCollection<String>(null) { obj: LPDataOutput, s: String? -> obj.writeUTF(s) } }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readNonNullList({ obj: LPDataInput -> obj.readUTF() }, "")

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeUTFArray and readUTFArray`() {
        val arr = arrayOf("◘ËTest♀StringßüöäÜÖÄ", "◘ËTest♀TESTpartßüöäÜÖÄ")
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTFArray(arr) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readUTFArray()!!

            assertTrue(arr.contentEquals(actual))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeUTFArray and readUTFArray with null`() {
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTFArray(null) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readUTFArray()

            assertNull(actual)
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }

    @Test
    fun `test writeUTFArray and readUTFArray with null value`() {
        val array = arrayOf("◘ËTest♀StringßüöäÜÖÄ", null)
        val data = LPDataIOWrapper.collectData { output: LPDataOutput -> output.writeUTFArray(array) }
        LPDataIOWrapper.provideData(data) { input: LPDataInput ->
            val actual = input.readUTFArray()!!

            assertTrue(array.contentEquals(actual))
            assertEquals(0, (input as LPDataIOWrapper).localBuffer.readableBytes(), BUFFER_EMPTY_MSG)
        }
    }
}

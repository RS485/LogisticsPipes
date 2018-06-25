/*
 * Copyright (c) 2016  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2016  RS485
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

package network.rs485.logisticspipes.util;

import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import io.netty.buffer.ByteBuf;

import logisticspipes.network.IWriteListObject;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public interface LPDataOutput {

	/**
	 * @see java.io.DataOutput#writeByte(int)
	 */
	void writeByte(int b);

	void writeByte(byte b);

	/**
	 * @see java.io.DataOutput#writeShort(int)
	 */
	void writeShort(int s);

	void writeShort(short s);

	/**
	 * @see java.io.DataOutput#writeInt(int)
	 */
	void writeInt(int i);

	/**
	 * @see java.io.DataOutput#writeLong(long)
	 */
	void writeLong(long l);

	/**
	 * @see java.io.DataOutput#writeFloat(float)
	 */
	void writeFloat(float f);

	/**
	 * @see java.io.DataOutput#writeDouble(double)
	 */
	void writeDouble(double d);

	/**
	 * @see java.io.DataOutput#writeBoolean(boolean)
	 */
	void writeBoolean(boolean b);

	/**
	 * Uses UTF-8 and not UTF-16.
	 *
	 * @see java.io.DataOutput#writeUTF(String)
	 */
	void writeUTF(String s);

	void writeByteArray(byte[] arr);

	void writeByteBuf(ByteBuf buffer);

	void writeIntArray(int[] arr);

	void writeLongArray(long[] arr);

	void writeBooleanArray(boolean[] arr);

	void writeFacing(EnumFacing direction);

	void writeResourceLocation(ResourceLocation resource);

	<T extends Enum<T>> void writeEnumSet(EnumSet<T> types, Class<T> clazz);

	void writeBitSet(BitSet bits);

	void writeNBTTagCompound(NBTTagCompound tag);

	void writeItemStack(ItemStack itemstack);

	void writeItemIdentifier(ItemIdentifier item);

	void writeItemIdentifierStack(ItemIdentifierStack stack);

	<T> void writeCollection(Collection<T> collection, IWriteListObject<T> handler);

	default <T extends LPFinalSerializable> void writeCollection(Collection<T> collection) {
		writeCollection(collection, LPDataOutput::writeSerializable);
	}

	<T extends Enum<T>> void writeEnum(T obj);

	void writeBytes(byte[] arr);

	default void writeSerializable(LPFinalSerializable finalSerializable) {
		finalSerializable.write(this);
	}


	interface LPDataOutputConsumer {

		void accept(LPDataOutput dataOutput);
	}
}

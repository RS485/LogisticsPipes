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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import io.netty.buffer.ByteBuf;

import logisticspipes.network.IReadListObject;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.PlayerIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public interface LPDataInput {

	@Nullable
	byte[] readByteArray();

	/**
	 * @see java.io.DataInput#readByte()
	 */
	byte readByte();

	/**
	 * @see java.io.DataInput#readShort()
	 */
	short readShort();

	/**
	 * @see java.io.DataInput#readInt()
	 */
	int readInt();

	/**
	 * @see java.io.DataInput#readLong()
	 */
	long readLong();

	/**
	 * @see java.io.DataInput#readFloat()
	 */
	float readFloat();

	/**
	 * @see java.io.DataInput#readDouble()
	 */
	double readDouble();

	/**
	 * @see java.io.DataInput#readBoolean()
	 */
	boolean readBoolean();

	/**
	 * @see java.io.DataInput#readUTF()
	 */
	@Nullable
	String readUTF();

	@Nullable
	EnumFacing readFacing();

	@Nullable
	ResourceLocation readResourceLocation();

	@Nonnull
	<T extends Enum<T>> EnumSet<T> readEnumSet(Class<T> clazz);

	@Nonnull
	BitSet readBitSet();

	@Nullable
	NBTTagCompound readNBTTagCompound();

	@Nullable
	boolean[] readBooleanArray();

	@Nullable
	String[] readUTFArray();

	@Nullable
	int[] readIntArray();

	@Nonnull
	byte[] readBytes(int length);

	@Nullable
	ItemIdentifier readItemIdentifier();

	@Nullable
	ItemIdentifierStack readItemIdentifierStack();

	@Nonnull
	ItemStack readItemStack();

	@Nullable
	<T> ArrayList<T> readArrayList(IReadListObject<T> reader);

	@Nullable
	<T> LinkedList<T> readLinkedList(IReadListObject<T> reader);

	@Nullable
	<T> Set<T> readSet(IReadListObject<T> handler);

	@Nullable
	<T> NonNullList<T> readNonNullList(IReadListObject<T> reader, @Nonnull T fillItem);

	@Nullable
	<T extends Enum<T>> T readEnum(Class<T> clazz);

	@Nonnull
	ByteBuf readByteBuf();

	@Nullable
	long[] readLongArray();

	@Nonnull
	ChannelInformation readChannelInformation();

	@Nullable
	UUID readUUID();

	@Nonnull
	PlayerIdentifier readPlayerIdentifier();

	default void readSerializable(LPSerializable serializable) {
		serializable.read(this);
	}

	interface LPDataInputConsumer {

		void accept(LPDataInput dataInput);
	}
}

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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

import logisticspipes.network.IReadListObject;
import logisticspipes.request.resources.IResource;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public interface LPDataInput {

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
	String readUTF();

	ForgeDirection readForgeDirection();

	ExitRoute readExitRoute(World world);

	/**
	 * @return ServerRouter or ClientRouter depending where we are
	 */
	IRouter readIRouter(World world);

	<T extends Enum<T>> EnumSet<T> readEnumSet(Class<T> clazz);

	BitSet readBitSet();

	NBTTagCompound readNBTTagCompound();

	boolean[] readBooleanArray();

	int[] readIntArray();

	byte[] readBytes(int length);

	ItemIdentifier readItemIdentifier();

	ItemIdentifierStack readItemIdentifierStack();

	ItemStack readItemStack();

	<T> ArrayList<T> readArrayList(IReadListObject<T> reader);

	<T> LinkedList<T> readLinkedList(IReadListObject<T> reader);

	<T> Set<T> readSet(IReadListObject<T> handler);

	IOrderInfoProvider readOrderInfo();

	<T extends Enum<T>> T readEnum(Class<T> clazz);

	LinkedLogisticsOrderList readLinkedLogisticsOrderList();

	ByteBuf readByteBuf();

	long[] readLongArray();

	IResource readResource();

	//LPSerializable readSerializable(Class<? extends LPSerializable> serializableClass);

	default void readSerializable(LPSerializable serializable) {
		serializable.read(this);
	}

	interface LPDataInputConsumer {

		void accept(LPDataInput dataInput);
	}
}

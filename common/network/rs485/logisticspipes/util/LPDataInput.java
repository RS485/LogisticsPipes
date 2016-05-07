/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the MIT license:
 *
 * Copyright (c) 2015  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular MIT license in your project, replace this copyright notice (this line and any lines below and NOT the copyright line above) with the lines from the original MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this file and associated documentation files (the "Source Code"), to deal in the Source Code without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Source Code, and to permit persons to whom the Source Code is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Source Code, which also can be distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package network.rs485.logisticspipes.util;

import java.io.IOException;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface LPDataInput {

	byte[] readByteArray() throws IOException;

	/**
	 * @see java.io.DataInput#readByte()
	 */
	byte readByte() throws IOException;

	/**
	 * @see java.io.DataInput#readShort()
	 */
	short readShort() throws IOException;

	/**
	 * @see java.io.DataInput#readInt()
	 */
	int readInt() throws IOException;

	/**
	 * @see java.io.DataInput#readLong()
	 */
	long readLong() throws IOException;

	/**
	 * @see java.io.DataInput#readFloat()
	 */
	float readFloat() throws IOException;

	/**
	 * @see java.io.DataInput#readDouble()
	 */
	double readDouble() throws IOException;

	/**
	 * @see java.io.DataInput#readBoolean()
	 */
	boolean readBoolean() throws IOException;

	/**
	 * @see java.io.DataInput#readUTF()
	 */
	String readUTF() throws IOException;

	ForgeDirection readForgeDirection() throws IOException;

	ExitRoute readExitRoute(World world) throws IOException;

	/**
	 * @return ServerRouter or ClientRouter depending where we are
	 */
	IRouter readIRouter(World world) throws IOException;

	DoubleCoordinates readLPPosition() throws IOException;

	<T extends Enum<T>> EnumSet<T> readEnumSet(Class<T> clazz) throws IOException;

	BitSet readBitSet() throws IOException;

	NBTTagCompound readNBTTagCompound() throws IOException;

	boolean[] readBooleanArray() throws IOException;

	int[] readIntArray() throws IOException;

	byte[] readBytes(int length) throws IOException;

	ItemIdentifier readItemIdentifier() throws IOException;

	ItemIdentifierStack readItemIdentifierStack() throws IOException;

	<T> List<T> readList(IReadListObject<T> handler) throws IOException;

	<T> Set<T> readSet(IReadListObject<T> handler) throws IOException;

	IOrderInfoProvider readOrderInfo() throws IOException;

	<T extends Enum<T>> T readEnum(Class<T> clazz) throws IOException;

	LinkedLogisticsOrderList readLinkedLogisticsOrderList() throws IOException;

	ByteBuf readByteBuf() throws IOException;

	long[] readLongArray() throws IOException;

	IResource readResource() throws IOException;

	interface LPDataInputConsumer {

		void accept(LPDataInput dataInput) throws IOException;
	}
}

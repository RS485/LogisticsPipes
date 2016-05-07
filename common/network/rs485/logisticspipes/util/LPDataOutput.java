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
import java.util.Collection;
import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

import logisticspipes.network.IWriteListObject;
import logisticspipes.request.resources.IResource;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface LPDataOutput {

	/**
	 * @see java.io.DataOutput#writeByte(int)
	 */
	void writeByte(int b) throws IOException;

	void writeByte(byte b) throws IOException;

	/**
	 * @see java.io.DataOutput#writeShort(int)
	 */
	void writeShort(int s) throws IOException;

	void writeShort(short s) throws IOException;

	/**
	 * @see java.io.DataOutput#writeInt(int)
	 */
	void writeInt(int i) throws IOException;

	/**
	 * @see java.io.DataOutput#writeLong(long)
	 */
	void writeLong(long l) throws IOException;

	/**
	 * @see java.io.DataOutput#writeFloat(float)
	 */
	void writeFloat(float f) throws IOException;

	/**
	 * @see java.io.DataOutput#writeDouble(double)
	 */
	void writeDouble(double d) throws IOException;

	/**
	 * @see java.io.DataOutput#writeBoolean(boolean)
	 */
	void writeBoolean(boolean b) throws IOException;

	/**
	 * Uses UTF-8 and not UTF-16.
	 *
	 * @see java.io.DataOutput#writeUTF(String)
	 */
	void writeUTF(String s) throws IOException;

	void writeByteArray(byte[] arr) throws IOException;

	void writeByteBuf(ByteBuf buffer) throws IOException;

	void writeIntArray(int[] arr) throws IOException;

	void writeLongArray(long[] arr) throws IOException;

	void writeBooleanArray(boolean[] arr) throws IOException;

	void writeForgeDirection(ForgeDirection direction) throws IOException;

	void writeExitRoute(ExitRoute route) throws IOException;

	void writeIRouter(IRouter router) throws IOException;

	void writeLPPosition(DoubleCoordinates pos) throws IOException;

	<T extends Enum<T>> void writeEnumSet(EnumSet<T> types, Class<T> clazz) throws IOException;

	void writeBitSet(BitSet bits) throws IOException;

	void writeNBTTagCompound(NBTTagCompound tag) throws IOException;

	void writeItemIdentifier(ItemIdentifier item) throws IOException;

	void writeItemIdentifierStack(ItemIdentifierStack stack) throws IOException;

	<T> void writeCollection(Collection<T> collection, IWriteListObject<T> handler) throws IOException;

	void writeOrderInfo(IOrderInfoProvider order) throws IOException;

	<T extends Enum<T>> void writeEnum(T obj) throws IOException;

	void writeLinkedLogisticsOrderList(LinkedLogisticsOrderList orderList) throws IOException;

	void writeResource(IResource res) throws IOException;

	void writeBytes(byte[] arr) throws IOException;

	interface LPDataOutputConsumer {

		void accept(LPDataOutput dataOutput) throws IOException;
	}
}

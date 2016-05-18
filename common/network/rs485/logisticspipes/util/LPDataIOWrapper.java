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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import static io.netty.buffer.Unpooled.buffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;

import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ResourceNetwork;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.order.ClientSideOrderInfo;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public final class LPDataIOWrapper implements LPDataInput, LPDataOutput {

	private static final Charset UTF_8 = Charset.forName("utf-8");
	private static final HashMap<Long, LPDataIOWrapper> BUFFER_WRAPPER_MAP = new HashMap<>();
	ByteBuf localBuffer;
	private int reference;

	private LPDataIOWrapper(ByteBuf buffer) {
		localBuffer = buffer;
	}

	private static LPDataIOWrapper getInstance(ByteBuf buffer) {
		if (buffer.hasMemoryAddress()) {
			LPDataIOWrapper instance = BUFFER_WRAPPER_MAP.get(buffer.memoryAddress());
			if (instance == null) {
				instance = new LPDataIOWrapper(buffer);
				BUFFER_WRAPPER_MAP.put(buffer.memoryAddress(), instance);
			}
			++instance.reference;
			return instance;
		} else {
			return new LPDataIOWrapper(buffer);
		}
	}

	private void unsetBuffer() {
		if (localBuffer.hasMemoryAddress()) {
			if (--reference < 1) {
				BUFFER_WRAPPER_MAP.remove(localBuffer.memoryAddress());
			}
		}
		localBuffer = null;
	}

	public static void provideData(byte[] data, LPDataInputConsumer dataInputConsumer) throws IOException {
		ByteBuf dataBuffer = wrappedBuffer(data);
		LPDataIOWrapper lpData = getInstance(dataBuffer);

		dataInputConsumer.accept(lpData);

		lpData.unsetBuffer();
		dataBuffer.release();
	}

	public static byte[] collectData(LPDataOutputConsumer dataOutputConsumer) throws IOException {
		ByteBuf dataBuffer = buffer();
		LPDataIOWrapper lpData = getInstance(dataBuffer);

		dataOutputConsumer.accept(lpData);

		lpData.unsetBuffer();

		byte[] data = new byte[dataBuffer.readableBytes()];
		dataBuffer.getBytes(0, data);

		dataBuffer.release();
		return data;
	}

	public static void provideData(ByteBuf dataBuffer, LPDataInputConsumer dataInputConsumer) throws IOException {
		LPDataIOWrapper lpData = getInstance(dataBuffer);

		dataInputConsumer.accept(lpData);

		lpData.unsetBuffer();
	}

	public static void writeData(ByteBuf dataBuffer, LPDataOutputConsumer dataOutputConsumer) throws IOException {
		LPDataIOWrapper lpData = getInstance(dataBuffer);

		dataOutputConsumer.accept(lpData);

		lpData.unsetBuffer();
	}

	@Override
	public void writeByteArray(byte[] arr) throws IOException {
		if (arr == null) {
			writeInt(-1);
		} else {
			writeInt(arr.length);
			writeBytes(arr);
		}
	}

	@Override
	public byte[] readByteArray() throws IOException {
		final int length = readInt();
		if (length == -1) {
			return null;
		}

		return readBytes(length);
	}

	@Override
	public void writeByte(int b) {
		localBuffer.writeByte(b);
	}

	@Override
	public void writeByte(byte b) {
		localBuffer.writeByte(b);
	}

	@Override
	public void writeShort(int s) {
		localBuffer.writeShort(s);
	}

	@Override
	public void writeShort(short b) {
		localBuffer.writeShort(b);
	}

	@Override
	public void writeInt(int i) {
		localBuffer.writeInt(i);
	}

	@Override
	public void writeLong(long l) {
		localBuffer.writeLong(l);
	}

	@Override
	public void writeFloat(float f) {
		localBuffer.writeFloat(f);
	}

	@Override
	public void writeDouble(double d) {
		localBuffer.writeDouble(d);
	}

	@Override
	public void writeBoolean(boolean b) {
		localBuffer.writeBoolean(b);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		if (s == null) {
			writeInt(-1);
		} else {
			writeByteArray(s.getBytes(UTF_8));
		}
	}

	@Override
	public void writeForgeDirection(ForgeDirection direction) {
		if (direction == null) {
			writeByte(Byte.MIN_VALUE);
		} else {
			writeByte(direction.ordinal());
		}
	}

	@Override
	public void writeExitRoute(ExitRoute route) throws IOException {
		writeIRouter(route.destination);
		writeIRouter(route.root);
		writeForgeDirection(route.exitOrientation);
		writeForgeDirection(route.insertOrientation);
		writeEnumSet(route.connectionDetails, PipeRoutingConnectionType.class);
		writeDouble(route.distanceToDestination);
		writeDouble(route.destinationDistanceToRoot);
		writeInt(route.blockDistance);
		writeCollection(route.filters, (data, filter) -> data.writeLPPosition(filter.getLPPosition()));
		writeUTF(route.toString());
		writeBoolean(route.debug.isNewlyAddedCanidate);
		writeBoolean(route.debug.isTraced);
		writeInt(route.debug.index);
	}

	@Override
	public void writeIRouter(IRouter router) {
		if (router == null) {
			writeBoolean(false);
		} else {
			writeBoolean(true);
			writeLPPosition(router.getLPPosition());
		}
	}

	@Override
	public void writeLPPosition(DoubleCoordinates pos) {
		writeDouble(pos.getXCoord());
		writeDouble(pos.getYCoord());
		writeDouble(pos.getZCoord());
	}

	@Override
	public <T extends Enum<T>> void writeEnumSet(EnumSet<T> types, Class<T> clazz) throws IOException {
		T[] parts = clazz.getEnumConstants();
		final int length = parts.length / 8 + (parts.length % 8 == 0 ? 0 : 1);
		byte[] set = new byte[length];

		for (T part : parts) {
			if (types.contains(part)) {
				byte i = (byte) (1 << (part.ordinal() % 8));
				set[part.ordinal() / 8] |= i;
			}
		}
		writeByteArray(set);
	}

	@Override
	public void writeBitSet(BitSet bits) throws IOException {
		if (bits == null) {
			throw new NullPointerException("BitSet may not be null");
		}
		writeByteArray(bits.toByteArray());
	}

	@Override
	public void writeNBTTagCompound(NBTTagCompound tag) throws IOException {
		if (tag == null) {
			writeByte(0);
		} else {
			writeByte(1);
			CompressedStreamTools.write(tag, new ByteBufOutputStream(localBuffer));
		}
	}

	@Override
	public void writeBooleanArray(boolean[] arr) throws IOException {
		if (arr == null) {
			writeInt(-1);
		} else if (arr.length == 0) {
			writeInt(0);
			writeByteArray(null);
		} else {
			BitSet bits = new BitSet(arr.length);
			for (int i = 0; i < arr.length; i++) {
				bits.set(i, arr[i]);
			}
			writeInt(arr.length);
			writeByteArray(bits.toByteArray());
		}
	}

	@Override
	public void writeIntArray(int[] arr) {
		if (arr == null) {
			writeInt(-1);
		} else {
			writeInt(arr.length);
			for (int i : arr) {
				writeInt(i);
			}
		}
	}

	@Override
	public void writeItemStack(ItemStack itemstack) throws IOException {
		if (itemstack == null) {
			writeInt(0);
		} else {
			writeInt(Item.getIdFromItem(itemstack.getItem()));
			writeInt(itemstack.stackSize);
			writeInt(itemstack.getItemDamage());
			writeNBTTagCompound(itemstack.getTagCompound());
		}
	}

	@Override
	public void writeItemIdentifier(ItemIdentifier item) throws IOException {
		if (item == null) {
			writeInt(0);
		} else {
			writeInt(Item.getIdFromItem(item.item));
			writeInt(item.itemDamage);
			writeNBTTagCompound(item.tag);
		}
	}

	@Override
	public void writeItemIdentifierStack(ItemIdentifierStack stack) throws IOException {
		if (stack == null) {
			writeInt(-1);
		} else {
			writeInt(stack.getStackSize());
			writeItemIdentifier(stack.getItem());
		}
	}

	@Override
	public <T> void writeCollection(Collection<T> collection, IWriteListObject<T> handler) throws IOException {
		if (collection == null) {
			writeInt(-1);
		} else {
			writeInt(collection.size());
			for (T obj : collection) {
				handler.writeObject(this, obj);
			}
		}
	}

	@Override
	public void writeOrderInfo(IOrderInfoProvider order) throws IOException {
		writeItemIdentifierStack(order.getAsDisplayItem());
		writeInt(order.getRouterId());
		writeBoolean(order.isFinished());
		writeBoolean(order.isInProgress());
		writeEnum(order.getType());
		writeCollection(order.getProgresses(), LPDataOutput::writeFloat);
		writeByte(order.getMachineProgress());
		writeLPPosition(order.getTargetPosition());
		writeItemIdentifier(order.getTargetType());
	}

	@Override
	public <T extends Enum<T>> void writeEnum(T obj) {
		writeInt(obj.ordinal());
	}

	@Override
	public void writeLinkedLogisticsOrderList(LinkedLogisticsOrderList orderList) throws IOException {
		writeCollection(orderList, LPDataOutput::writeOrderInfo);
		writeCollection(orderList.getSubOrders(), LPDataOutput::writeLinkedLogisticsOrderList);
	}

	@Override
	public void writeByteBuf(ByteBuf otherBuffer) {
		if (otherBuffer == null) {
			throw new NullPointerException("Other buffer may not be null");
		}

		writeInt(otherBuffer.readableBytes());
		localBuffer.writeBytes(otherBuffer, otherBuffer.readableBytes());
	}

	@Override
	public void writeLongArray(long[] arr) {
		if (arr == null) {
			writeInt(-1);
		} else {
			writeInt(arr.length);
			for (long l : arr) {
				writeLong(l);
			}
		}
	}

	@Override
	public void writeResource(IResource res) throws IOException {
		ResourceNetwork.writeResource(this, res);
	}

	@Override
	public void writeBytes(byte[] arr) throws IOException {
		localBuffer.writeBytes(arr);
	}

	@Override
	public byte readByte() {
		return localBuffer.readByte();
	}

	@Override
	public short readShort() {
		return localBuffer.readShort();
	}

	@Override
	public int readInt() {
		return localBuffer.readInt();
	}

	@Override
	public long readLong() {
		return localBuffer.readLong();
	}

	@Override
	public float readFloat() {
		return localBuffer.readFloat();
	}

	@Override
	public double readDouble() {
		return localBuffer.readDouble();
	}

	@Override
	public boolean readBoolean() {
		return localBuffer.readBoolean();
	}

	@Override
	public String readUTF() throws IOException {
		byte[] arr = readByteArray();
		if (arr == null) {
			return null;
		} else {
			return new String(arr, UTF_8);
		}
	}

	@Override
	public ForgeDirection readForgeDirection() {
		byte b = localBuffer.readByte();

		if (b == Byte.MIN_VALUE) {
			return null;
		}
		return ForgeDirection.values()[b];
	}

	@Override
	public ExitRoute readExitRoute(World world) throws IOException {
		IRouter destination = readIRouter(world);
		IRouter root = readIRouter(world);
		ForgeDirection exitOri = readForgeDirection();
		ForgeDirection insertOri = readForgeDirection();
		EnumSet<PipeRoutingConnectionType> connectionDetails = readEnumSet(PipeRoutingConnectionType.class);
		double distanceToDestination = localBuffer.readDouble();
		double destinationDistanceToRoot = localBuffer.readDouble();
		int blockDistance = localBuffer.readInt();
		List<DoubleCoordinates> positions = readArrayList(LPDataInput::readLPPosition);
		ExitRoute e = new ExitRoute(root, destination, exitOri, insertOri, destinationDistanceToRoot, connectionDetails, blockDistance);
		e.distanceToDestination = distanceToDestination;
		e.debug.filterPosition = positions;
		e.debug.toStringNetwork = readUTF();
		e.debug.isNewlyAddedCanidate = localBuffer.readBoolean();
		e.debug.isTraced = localBuffer.readBoolean();
		e.debug.index = localBuffer.readInt();
		return e;
	}

	@Override
	public IRouter readIRouter(World world) {
		if (localBuffer.readBoolean()) {
			DoubleCoordinates pos = readLPPosition();
			TileEntity tile = pos.getTileEntity(world);
			if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof CoreRoutedPipe) {
				return ((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).getRouter();
			}
		}
		return null;
	}

	@Override
	public DoubleCoordinates readLPPosition() {
		return new DoubleCoordinates(localBuffer.readDouble(), localBuffer.readDouble(), localBuffer.readDouble());
	}

	@Override
	public <T extends Enum<T>> EnumSet<T> readEnumSet(Class<T> clazz) throws IOException {
		EnumSet<T> types = EnumSet.noneOf(clazz);
		byte[] arr = readByteArray();
		if (arr != null) {
			T[] parts = clazz.getEnumConstants();
			for (T part : parts) {
				if ((arr[part.ordinal() / 8] & (1 << (part.ordinal() % 8))) != 0) {
					types.add(part);
				}
			}
		}
		return types;
	}

	@Override
	public BitSet readBitSet() throws IOException {
		byte[] arr = readByteArray();
		if (arr == null) {
			return new BitSet();
		} else {
			return BitSet.valueOf(arr);
		}
	}

	@Override
	public NBTTagCompound readNBTTagCompound() throws IOException {
		boolean isEmpty = (readByte() == 0);
		if (isEmpty) {
			return null;
		}

		return CompressedStreamTools.func_152456_a(new ByteBufInputStream(localBuffer), NBTSizeTracker.field_152451_a);
	}

	@Override
	public boolean[] readBooleanArray() throws IOException {
		final int bitCount = localBuffer.readInt();
		if (bitCount == -1) {
			return null;
		}

		byte[] data = readByteArray();
		if (bitCount == 0) {
			return new boolean[0];
		} else if (data == null) {
			throw new NullPointerException("Boolean's byte array is null");
		}

		BitSet bits = BitSet.valueOf(data);

		final boolean[] arr = new boolean[bitCount];
		IntStream.range(0, bitCount).forEach(i -> arr[i] = bits.get(i));
		return arr;
	}

	@Override
	public int[] readIntArray() {
		final int length = localBuffer.readInt();
		if (length == -1) {
			return null;
		}

		final int[] arr = new int[length];
		IntStream.range(0, length).forEach(i -> arr[i] = localBuffer.readInt());
		return arr;
	}

	@Override
	public byte[] readBytes(int length) throws IOException {
		byte[] arr = new byte[length];
		localBuffer.readBytes(arr, 0, length);
		return arr;
	}

	@Override
	public ItemStack readItemStack() throws IOException {
		final int itemId = readInt();
		if (itemId == 0) {
			return null;
		}

		int stackSize = readInt();
		int damage = readInt();
		ItemStack stack = new ItemStack(Item.getItemById(itemId), stackSize, damage);
		stack.setTagCompound(readNBTTagCompound());
		return stack;
	}

	@Override
	public ItemIdentifier readItemIdentifier() throws IOException {
		final int itemId = readInt();
		if (itemId == 0) {
			return null;
		}

		int damage = readInt();
		NBTTagCompound tag = readNBTTagCompound();
		return ItemIdentifier.get(Item.getItemById(itemId), damage, tag);
	}

	@Override
	public ItemIdentifierStack readItemIdentifierStack() throws IOException {
		int stacksize = readInt();
		if (stacksize == -1) {
			return null;
		}

		ItemIdentifier item = readItemIdentifier();
		return new ItemIdentifierStack(item, stacksize);
	}

	@Override
	public <T> ArrayList<T> readArrayList(IReadListObject<T> reader) throws IOException {
		int size = readInt();
		if (size == -1) {
			return null;
		}

		ArrayList<T> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(reader.readObject(this));
		}
		return list;
	}

	@Override
	public <T> LinkedList<T> readLinkedList(IReadListObject<T> reader) throws IOException {
		int size = readInt();
		if (size == -1) {
			return null;
		}

		LinkedList<T> list = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			list.add(reader.readObject(this));
		}
		return list;
	}

	@Override
	public <T> Set<T> readSet(IReadListObject<T> handler) throws IOException {
		int size = readInt();
		if (size == -1) {
			return null;
		}

		Set<T> set = new HashSet<>(size);
		for (int i = 0; i < size; i++) {
			set.add(handler.readObject(this));
		}
		return set;
	}

	@Override
	public IOrderInfoProvider readOrderInfo() throws IOException {
		ItemIdentifierStack stack = readItemIdentifierStack();
		int routerId = localBuffer.readInt();
		boolean isFinished = localBuffer.readBoolean();
		boolean inProgress = localBuffer.readBoolean();
		IOrderInfoProvider.ResourceType type = readEnum(IOrderInfoProvider.ResourceType.class);
		List<Float> list = readArrayList(LPDataInput::readFloat);
		byte machineProgress = localBuffer.readByte();
		DoubleCoordinates pos = readLPPosition();
		ItemIdentifier ident = readItemIdentifier();
		return new ClientSideOrderInfo(stack, isFinished, type, inProgress, routerId, list, machineProgress, pos, ident);
	}

	@Override
	public <T extends Enum<T>> T readEnum(Class<T> clazz) {
		return clazz.getEnumConstants()[localBuffer.readInt()];
	}

	@Override
	public LinkedLogisticsOrderList readLinkedLogisticsOrderList() throws IOException {
		LinkedLogisticsOrderList list = new LinkedLogisticsOrderList();

		List<IOrderInfoProvider> orderInfoProviders = readArrayList(LPDataInput::readOrderInfo);
		if (orderInfoProviders == null) {
			throw new IOException("Expected order info provider list");
		}
		list.addAll(orderInfoProviders);

		List<LinkedLogisticsOrderList> orderLists = readArrayList(LPDataInput::readLinkedLogisticsOrderList);
		if (orderLists == null) {
			throw new IOException("Expected logistics order list");
		}
		list.getSubOrders().addAll(orderLists);

		return list;
	}

	@Override
	public ByteBuf readByteBuf() throws IOException {
		byte[] arr = readByteArray();
		if (arr == null) {
			throw new NullPointerException("Buffer may not be null, but read null");
		} else {
			return wrappedBuffer(arr);
		}
	}

	@Override
	public long[] readLongArray() {
		final int length = localBuffer.readInt();
		if (length == -1) {
			return null;
		}

		final long[] arr = new long[length];
		IntStream.range(0, length).forEach(i -> arr[i] = localBuffer.readLong());
		return arr;
	}

	@Override
	public IResource readResource() throws IOException {
		return ResourceNetwork.readResource(this);
	}
}

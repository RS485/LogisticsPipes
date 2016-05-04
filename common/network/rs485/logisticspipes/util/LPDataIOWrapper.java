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
import java.util.List;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;
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

	public static final Charset UTF_8 = Charset.forName("utf-8");
	private static final HashMap<Long, LPDataIOWrapper> bufferWrapperMap = new HashMap<>();
	private ByteBuf localBuffer;
	private int reference;

	private LPDataIOWrapper(ByteBuf buffer) {
		localBuffer = buffer;
	}

	private static LPDataIOWrapper getInstance(ByteBuf buffer) {
		if (buffer.hasMemoryAddress()) {
			LPDataIOWrapper instance = bufferWrapperMap.get(buffer.memoryAddress());
			if (instance == null) {
				instance = new LPDataIOWrapper(buffer);
				bufferWrapperMap.put(buffer.memoryAddress(), instance);
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
				bufferWrapperMap.remove(localBuffer.memoryAddress());
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
	public void writeLengthAndBytes(byte[] arr) {
		if (arr == null) {
			localBuffer.writeInt(-1);
		} else {
			localBuffer.writeInt(arr.length);
			localBuffer.writeBytes(arr);
		}
	}

	@Override
	public byte[] readLengthAndBytes() {
		final int length = localBuffer.readInt();
		if (length < 0) {
			return null;
		}
		byte[] arr = new byte[length];
		if (!localBuffer.isReadable(length)) {
			System.err.println("Trying to read " + length + " bytes");
			throw new IndexOutOfBoundsException(length + " > " + localBuffer.readableBytes());
		} else {
			localBuffer.readBytes(arr, 0, length);
		}
		return arr;
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
	public void writeUTF(String s) {
		if (s == null) {
			localBuffer.writeInt(-1);
		} else {
			this.writeLengthAndBytes(s.getBytes(UTF_8));
		}
	}

	@Override
	public void writeByteArray(byte[] data) {
		if (data == null) {
			throw new NullPointerException("Byte array may not be null");
		}
		localBuffer.writeBytes(data);
	}

	@Override
	public void writeForgeDirection(ForgeDirection direction) {
		if (direction == null) {
			localBuffer.writeByte(Byte.MIN_VALUE);
		} else {
			localBuffer.writeByte(direction.ordinal());
		}
	}

	@Override
	public void writeExitRoute(ExitRoute route) throws IOException {
		this.writeIRouter(route.destination);
		this.writeIRouter(route.root);
		this.writeForgeDirection(route.exitOrientation);
		this.writeForgeDirection(route.insertOrientation);
		this.writeEnumSet(route.connectionDetails, PipeRoutingConnectionType.class);
		localBuffer.writeDouble(route.distanceToDestination);
		localBuffer.writeDouble(route.destinationDistanceToRoot);
		localBuffer.writeInt(route.blockDistance);
		this.writeCollection(route.filters, (data, filter) -> data.writeLPPosition(filter.getLPPosition()));
		this.writeUTF(route.toString());
		localBuffer.writeBoolean(route.debug.isNewlyAddedCanidate);
		localBuffer.writeBoolean(route.debug.isTraced);
		localBuffer.writeInt(route.debug.index);
	}

	@Override
	public void writeIRouter(IRouter router) {
		if (router == null) {
			localBuffer.writeBoolean(false);
		} else {
			localBuffer.writeBoolean(true);
			this.writeLPPosition(router.getLPPosition());
		}
	}

	@Override
	public void writeLPPosition(DoubleCoordinates pos) {
		localBuffer.writeDouble(pos.getXCoord());
		localBuffer.writeDouble(pos.getYCoord());
		localBuffer.writeDouble(pos.getZCoord());
	}

	@Override
	public <T extends Enum<T>> void writeEnumSet(EnumSet<T> types, Class<T> clazz) {
		T[] parts = clazz.getEnumConstants();
		final int length = parts.length / 8 + (parts.length % 8 == 0 ? 0 : 1);
		byte[] set = new byte[length];

		for (T part : parts) {
			if (types.contains(part)) {
				byte i = (byte) (1 << (part.ordinal() % 8));
				set[part.ordinal() / 8] |= i;
			}
		}
		this.writeLengthAndBytes(set);
	}

	@Override
	public void writeBitSet(BitSet bits) {
		if (bits == null) {
			throw new NullPointerException("BitSet may not be null");
		}
		this.writeLengthAndBytes(bits.toByteArray());
	}

	@Override
	public void writeNBTTagCompound(NBTTagCompound tag) throws IOException {
		if (tag == null) {
			localBuffer.writeInt(-1);
		} else {
			byte[] bytes = CompressedStreamTools.compress(tag);
			this.writeLengthAndBytes(bytes);
		}
	}

	@Override
	public void writeBooleanArray(boolean[] arr) {
		if (arr == null) {
			localBuffer.writeInt(-1);
		} else {
			BitSet bits = new BitSet(arr.length);
			for (int i = 0; i < arr.length; i++) {
				bits.set(i, arr[i]);
			}
			localBuffer.writeInt(arr.length);
			this.writeByteArray(bits.toByteArray());
		}
	}

	@Override
	public void writeIntArray(int[] arr) {
		if (arr == null) {
			localBuffer.writeInt(-1);
		} else {
			localBuffer.writeInt(arr.length);
			for (int i : arr) {
				localBuffer.writeInt(i);
			}
		}
	}

	@Override
	public void writeItemIdentifier(ItemIdentifier item) throws IOException {
		if (item == null) {
			localBuffer.writeBoolean(false);
		} else {
			localBuffer.writeBoolean(true);
			localBuffer.writeInt(Item.getIdFromItem(item.item));
			localBuffer.writeInt(item.itemDamage);
			this.writeNBTTagCompound(item.tag);
		}
	}

	@Override
	public void writeItemIdentifierStack(ItemIdentifierStack stack) throws IOException {
		this.writeItemIdentifier(stack.getItem());
		localBuffer.writeInt(stack.getStackSize());
	}

	@Override
	public <T> void writeCollection(Collection<T> collection, IWriteListObject<T> handler) throws IOException {
		if (collection == null) {
			localBuffer.writeInt(-1);
		} else {
			localBuffer.writeInt(collection.size());
			for (T obj : collection) {
				handler.writeObject(this, obj);
			}
		}
	}

	@Override
	public void writeOrderInfo(IOrderInfoProvider order) throws IOException {
		this.writeItemIdentifierStack(order.getAsDisplayItem());
		localBuffer.writeInt(order.getRouterId());
		localBuffer.writeBoolean(order.isFinished());
		localBuffer.writeBoolean(order.isInProgress());
		this.writeEnum(order.getType());
		this.writeCollection(order.getProgresses(), LPDataOutput::writeFloat);
		localBuffer.writeByte(order.getMachineProgress());
		this.writeLPPosition(order.getTargetPosition());
		this.writeItemIdentifier(order.getTargetType());
	}

	@Override
	public <T extends Enum<T>> void writeEnum(T obj) {
		localBuffer.writeInt(obj.ordinal());
	}

	@Override
	public void writeLinkedLogisticsOrderList(LinkedLogisticsOrderList orderList) throws IOException {
		this.writeCollection(orderList, LPDataOutput::writeOrderInfo);
		this.writeCollection(orderList.getSubOrders(), LPDataOutput::writeLinkedLogisticsOrderList);
	}

	@Override
	public void writeByteBuf(ByteBuf otherBuffer) {
		final ByteBuf copy = otherBuffer.copy();
		copy.readerIndex(0);
		localBuffer.writeInt(copy.readableBytes());
		localBuffer.writeBytes(copy);
	}

	@Override
	public void writeLongArray(long[] arr) {
		localBuffer.writeInt(arr.length);
		for (long l : arr) {
			localBuffer.writeLong(l);
		}
	}

	@Override
	public void writeResource(IResource res) throws IOException {
		ResourceNetwork.writeResource(this, res);
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
	public String readUTF() {
		byte[] arr = this.readLengthAndBytes();
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
		IRouter destination = this.readIRouter(world);
		IRouter root = this.readIRouter(world);
		ForgeDirection exitOri = this.readForgeDirection();
		ForgeDirection insertOri = this.readForgeDirection();
		EnumSet<PipeRoutingConnectionType> connectionDetails = this.readEnumSet(PipeRoutingConnectionType.class);
		double distanceToDestination = localBuffer.readDouble();
		double destinationDistanceToRoot = localBuffer.readDouble();
		int blockDistance = localBuffer.readInt();
		List<DoubleCoordinates> positions = this.readList(LPDataInput::readLPPosition);
		ExitRoute e = new ExitRoute(root, destination, exitOri, insertOri, destinationDistanceToRoot, connectionDetails, blockDistance);
		e.distanceToDestination = distanceToDestination;
		e.debug.filterPosition = positions;
		e.debug.toStringNetwork = this.readUTF();
		e.debug.isNewlyAddedCanidate = localBuffer.readBoolean();
		e.debug.isTraced = localBuffer.readBoolean();
		e.debug.index = localBuffer.readInt();
		return e;
	}

	@Override
	public IRouter readIRouter(World world) {
		if (localBuffer.readBoolean()) {
			DoubleCoordinates pos = this.readLPPosition();
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
	public <T extends Enum<T>> EnumSet<T> readEnumSet(Class<T> clazz) {
		EnumSet<T> types = EnumSet.noneOf(clazz);
		byte[] arr = this.readLengthAndBytes();
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
	public BitSet readBitSet() {
		byte[] arr = this.readLengthAndBytes();
		if (arr == null) {
			return new BitSet();
		} else {
			return BitSet.valueOf(arr);
		}
	}

	@Override
	public NBTTagCompound readNBTTagCompound() throws IOException {
		byte[] arr = this.readLengthAndBytes();
		if (arr == null) {
			return null;
		}
		return CompressedStreamTools.func_152457_a(arr, new NBTSizeTracker(Long.MAX_VALUE));
	}

	@Override
	public boolean[] readBooleanArray() {
		final int bitCount = localBuffer.readInt();
		if (bitCount < 0) {
			return null;
		}

		boolean[] arr = new boolean[bitCount];
		final int byteCount = (bitCount / 8) + (bitCount % 8 == 0 ? 0 : 1);
		byte[] data = new byte[byteCount];

		if (!localBuffer.isReadable(byteCount)) {
			System.err.println("Trying to read " + byteCount + " bytes (" + bitCount + " bits)");
			throw new IndexOutOfBoundsException(byteCount + " > " + localBuffer.readableBytes());
		} else {
			localBuffer.readBytes(data, 0, byteCount);
		}
		BitSet bits = BitSet.valueOf(data);
		for (int i = 0; i < arr.length; i++) {
			arr[i] = bits.get(i);
		}
		return arr;
	}

	@Override
	public int[] readIntArray() {
		final int length = localBuffer.readInt();
		if (length < 0) {
			return null;
		} else {
			int[] arr = new int[length];
			for (int i = 0; i < length; i++) {
				arr[i] = localBuffer.readInt();
			}
			return arr;
		}
	}

	@Override
	public ItemIdentifier readItemIdentifier() throws IOException {
		if (localBuffer.readBoolean()) {
			int itemId = localBuffer.readInt();
			int damage = localBuffer.readInt();
			NBTTagCompound tag = readNBTTagCompound();
			return ItemIdentifier.get(Item.getItemById(itemId), damage, tag);
		}
		return null;
	}

	@Override
	public ItemIdentifierStack readItemIdentifierStack() throws IOException {
		ItemIdentifier item = this.readItemIdentifier();
		return new ItemIdentifierStack(item, localBuffer.readInt());
	}

	@Override
	public <T> List<T> readList(IReadListObject<T> handler) throws IOException {
		int length = localBuffer.readInt();
		if (length < 0) {
			return null;
		} else {
			List<T> list = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				list.add(handler.readObject(this));
			}
			return list;
		}
	}

	@Override
	public <T> Set<T> readSet(IReadListObject<T> handler) throws IOException {
		int length = localBuffer.readInt();
		if (length < 0) {
			return null;
		} else {
			Set<T> set = new HashSet<>(length);
			for (int i = 0; i < length; i++) {
				set.add(handler.readObject(this));
			}
			return set;
		}
	}

	@Override
	public IOrderInfoProvider readOrderInfo() throws IOException {
		ItemIdentifierStack stack = this.readItemIdentifierStack();
		int routerId = localBuffer.readInt();
		boolean isFinished = localBuffer.readBoolean();
		boolean inProgress = localBuffer.readBoolean();
		IOrderInfoProvider.ResourceType type = this.readEnum(IOrderInfoProvider.ResourceType.class);
		List<Float> list = this.readList(LPDataInput::readFloat);
		byte machineProgress = localBuffer.readByte();
		DoubleCoordinates pos = this.readLPPosition();
		ItemIdentifier ident = this.readItemIdentifier();
		return new ClientSideOrderInfo(stack, isFinished, type, inProgress, routerId, list, machineProgress, pos, ident);
	}

	@Override
	public <T extends Enum<T>> T readEnum(Class<T> clazz) {
		return clazz.getEnumConstants()[localBuffer.readInt()];
	}

	@Override
	public LinkedLogisticsOrderList readLinkedLogisticsOrderList() throws IOException {
		LinkedLogisticsOrderList list = new LinkedLogisticsOrderList();

		List<IOrderInfoProvider> orderInfoProviders = this.readList(LPDataInput::readOrderInfo);
		if (orderInfoProviders == null) {
			throw new IOException("Expected order info provider list");
		}
		list.addAll(orderInfoProviders);

		List<LinkedLogisticsOrderList> orderLists = this.readList(LPDataInput::readLinkedLogisticsOrderList);
		if (orderLists == null) {
			throw new IOException("Expected logistics order list");
		}
		list.getSubOrders().addAll(orderLists);

		return list;
	}

	@Override
	public ByteBuf readByteBuf() {
		byte[] arr = this.readLengthAndBytes();
		if (arr == null) {
			return buffer();
		} else {
			return wrappedBuffer(arr);
		}
	}

	@Override
	public long[] readLongArray() {
		final int length = localBuffer.readInt();
		long[] arr = new long[length];
		for (int i = 0; i < length; i++) {
			arr[i] = localBuffer.readLong();
		}
		return arr;
	}

	@Override
	public IResource readResource() throws IOException {
		return ResourceNetwork.readResource(this);
	}
}

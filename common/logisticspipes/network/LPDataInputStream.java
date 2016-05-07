package logisticspipes.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
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
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ResourceNetwork;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.order.ClientSideOrderInfo;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LPDataInputStream extends DataInputStream implements LPDataInput {

	public LPDataInputStream(byte[] inputBytes) throws IOException {
		super(new ByteArrayInputStream(inputBytes));
	}

	public LPDataInputStream(ByteBuf inputBytes) throws IOException {
		super(new ByteBufInputStream(inputBytes));
	}

	@Override
	public byte[] readByteArray() throws IOException {
		int length = readInt();
		return readBytes(length);
	}

	public ForgeDirection readForgeDirection() throws IOException {
		int dir = readByte();
		if (dir == 10) {
			return null;
		}
		return ForgeDirection.values()[dir];
	}

	public ExitRoute readExitRoute(World world) throws IOException {
		IRouter destination = readIRouter(world);
		IRouter root = readIRouter(world);
		ForgeDirection exitOri = readForgeDirection();
		ForgeDirection insertOri = readForgeDirection();
		EnumSet<PipeRoutingConnectionType> connectionDetails = this.readEnumSet(PipeRoutingConnectionType.class);
		double distanceToDestination = readDouble();
		double destinationDistanceToRoot = readDouble();
		int blockDistance = readInt();
		List<DoubleCoordinates> positions = this.readList(LPDataInput::readLPPosition);
		ExitRoute e = new ExitRoute(root, destination, exitOri, insertOri, destinationDistanceToRoot, connectionDetails, blockDistance);
		e.distanceToDestination = distanceToDestination;
		e.debug.filterPosition = positions;
		e.debug.toStringNetwork = this.readUTF();
		e.debug.isNewlyAddedCanidate = readBoolean();
		e.debug.isTraced = readBoolean();
		e.debug.index = readInt();
		return e;
	}

	/**
	 * @return ServerRouter or ClientRouter depending where we are
	 * @throws IOException
	 */
	public IRouter readIRouter(World world) throws IOException {
		if (readByte() == 0) {
			return null;
		} else {
			DoubleCoordinates pos = readLPPosition();
			TileEntity tile = pos.getTileEntity(world);
			if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof CoreRoutedPipe) {
				return ((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).getRouter();
			}
			return null;
		}
	}

	public DoubleCoordinates readLPPosition() throws IOException {
		return new DoubleCoordinates(readDouble(), readDouble(), readDouble());
	}

	public <T extends Enum<T>> EnumSet<T> readEnumSet(Class<T> clazz) throws IOException {
		EnumSet<T> types = EnumSet.noneOf(clazz);
		T[] parts = clazz.getEnumConstants();
		int length;
		length = readByte();
		byte[] set = readBytes(length);
		for (T part : parts) {
			if ((set[part.ordinal() / 8] & (1 << (part.ordinal() % 8))) != 0) {
				types.add(part);
			}
		}
		return types;
	}

	public byte[] readBytes(int count) throws IOException {
		byte[] bytes = new byte[count];
		int read = in.read(bytes);
		assert read == count;
		return bytes;
	}

	public BitSet readBitSet() throws IOException {
		byte size = readByte();
		byte[] bytes = readBytes(size);
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	public NBTTagCompound readNBTTagCompound() throws IOException {
		short legth = readShort();
		if (legth < 0) {
			return null;
		} else {
			byte[] array = new byte[legth];
			this.readFully(array);
			return CompressedStreamTools.func_152457_a(array, new NBTSizeTracker(Long.MAX_VALUE));
		}

	}

	public boolean[] readBooleanArray() throws IOException {
		boolean[] array = new boolean[0];
		array = new boolean[readInt()];
		BitSet set = readBitSet();
		for (int i = 0; i < array.length; i++) {
			array[i] = set.get(i);
		}
		return array;
	}

	@Override
	public int[] readIntArray() {
		return new int[0];
	}

	public int[] readIntegerArray() throws IOException {
		int[] array = new int[readInt()];
		for (int i = 0; i < array.length; i++) {
			array[i] = readInt();
		}
		return array;
	}

	public ItemIdentifier readItemIdentifier() throws IOException {
		if (!readBoolean()) {
			return null;
		}
		int itemID = readInt();
		int damage = readInt();
		NBTTagCompound tag = readNBTTagCompound();
		return ItemIdentifier.get(Item.getItemById(itemID), damage, tag);
	}

	public ItemIdentifierStack readItemIdentifierStack() throws IOException {
		ItemIdentifier item = readItemIdentifier();
		int stacksize = readInt();
		return new ItemIdentifierStack(item, stacksize);
	}

	public <T> List<T> readList(IReadListObject<T> handler) throws IOException {
		int size = readInt();
		List<T> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(handler.readObject(this));
		}
		return list;
	}

	public <T> Set<T> readSet(IReadListObject<T> handler) throws IOException {
		int size = readInt();
		Set<T> set = new HashSet<>(size);
		for (int i = 0; i < size; i++) {
			set.add(handler.readObject(this));
		}
		return set;
	}

	public IOrderInfoProvider readOrderInfo() throws IOException {
		ItemIdentifierStack stack = readItemIdentifierStack();
		int routerId = readInt();
		boolean isFinished = readBoolean();
		boolean inProgress = readBoolean();
		ResourceType type = this.readEnum(ResourceType.class);
		List<Float> list = this.readList(LPDataInput::readFloat);
		byte machineProgress = readByte();
		DoubleCoordinates pos = readLPPosition();
		ItemIdentifier ident = readItemIdentifier();
		return new ClientSideOrderInfo(stack, isFinished, type, inProgress, routerId, list, machineProgress, pos, ident);
	}

	public <T extends Enum<T>> T readEnum(Class<T> clazz) throws IOException {
		return clazz.getEnumConstants()[readInt()];
	}

	public LinkedLogisticsOrderList readLinkedLogisticsOrderList() throws IOException {
		LinkedLogisticsOrderList list = new LinkedLogisticsOrderList();
		list.addAll(this.readList(LPDataInput::readOrderInfo));
		list.getSubOrders().addAll(this.readList(LPDataInput::readLinkedLogisticsOrderList));
		return list;
	}

	public ByteBuf readByteBuf() throws IOException {
		byte[] bytes = readByteArray();
		return Unpooled.copiedBuffer(bytes);
	}

	public long[] readLongArray() throws IOException {
		long[] array = new long[readInt()];
		for (int i = 0; i < array.length; i++) {
			array[i] = readLong();
		}
		return array;
	}

	@Override
	public IResource readResource() throws IOException {
		return ResourceNetwork.readResource(this);
	}

}

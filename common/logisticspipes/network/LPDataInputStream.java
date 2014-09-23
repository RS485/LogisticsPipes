package logisticspipes.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.order.ClientSideOrderInfo;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.IOrderInfoProvider.RequestType;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LPDataInputStream extends DataInputStream {

	public LPDataInputStream(byte[] inputBytes) throws IOException {
		super(new ByteArrayInputStream(inputBytes));
	}

	public LPDataInputStream(ByteBuf inputBytes) throws IOException {
		super(new ByteBufInputStream(inputBytes));
	}
	
	public ForgeDirection readForgeDirection() throws IOException {
		return ForgeDirection.values()[in.read()];
	}
	
	public ExitRoute readExitRoute(World world) throws IOException {
		IRouter destination = this.readIRouter(world);
		IRouter root = this.readIRouter(world);
		ForgeDirection exitOri = this.readForgeDirection();
		ForgeDirection insertOri = this.readForgeDirection();
		EnumSet<PipeRoutingConnectionType> connectionDetails = this.readEnumSet(PipeRoutingConnectionType.class);
		int distanceToDestination = this.readInt();
		int destinationDistanceToRoot = this.readInt();
		int blockDistance = this.readInt();
		List<LPPosition> positions = this.readList(new IReadListObject<LPPosition>() {
			@Override
			public LPPosition readObject(LPDataInputStream data) throws IOException {
				return data.readLPPosition();
			}
		});
		ExitRoute e = new ExitRoute(root, destination, exitOri, insertOri, destinationDistanceToRoot, connectionDetails, blockDistance);
		e.distanceToDestination = distanceToDestination;
		e.debug.filterPosition = positions;
		e.debug.toStringNetwork = this.readUTF();
		e.debug.isNewlyAddedCanidate = this.readBoolean();
		e.debug.isTraced = this.readBoolean();
		e.debug.index = this.readInt();
		return e;
	}

	/**
	 * @return ServerRouter or ClientRouter depending where we are
	 * @throws IOException
	 */
	public IRouter readIRouter(World world) throws IOException {
		if(in.read() == 0) {
			return null;
		} else {
			LPPosition pos = this.readLPPosition();
			TileEntity tile = pos.getTileEntity(world);
			if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).pipe instanceof CoreRoutedPipe) {
				return ((CoreRoutedPipe)((LogisticsTileGenericPipe)tile).pipe).getRouter();
			}
			return null;
		}
	}

	public LPPosition readLPPosition() throws IOException {
		return new LPPosition(this.readDouble(), this.readDouble(), this.readDouble());
	}
	
	public <T extends Enum<T>> EnumSet<T> readEnumSet(Class<T> clazz) throws IOException {
		EnumSet<T> types = EnumSet.noneOf(clazz);
		T[] parts = clazz.getEnumConstants();
		int length = in.read();
		byte[] set = new byte[length];
		in.read(set);
		for(T part: parts) {
			if((set[part.ordinal() / 8] & (1 << (part.ordinal() % 8))) != 0) {
				types.add(part);
			}
		}
		return types;
	}
	
	public BitSet readBitSet() throws IOException {
		byte size = this.readByte();
		byte[] bytes = new byte[size];
		this.read(bytes);
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}
	
	public NBTTagCompound readNBTTagCompound() throws IOException {
		short legth = this.readShort();
		if(legth < 0) {
			return null;
		} else {
			byte[] array = new byte[legth];
			this.readFully(array);
			return CompressedStreamTools.func_152457_a(array, new NBTSizeTracker(Long.MAX_VALUE));
		}
		
	}
	
	public boolean[] readBooleanArray() throws IOException {
		boolean[] array = new boolean[this.readByte()];
		BitSet set = this.readBitSet();
		for(int i=0;i<array.length;i++) {
			array[i] = set.get(i);
		}
		return array;
	}
	
	public int[] readIntegerArray() throws IOException {
		int[] array = new int[this.readByte()];
		for(int i=0;i<array.length;i++) {
			array[i] = this.readInt();
		}
		return array;
	}
	
	public ItemIdentifier readItemIdentifier() throws IOException {
		if(!this.readBoolean()) return null;
		int itemID = this.readInt();
		int damage = this.readInt();
		NBTTagCompound tag = this.readNBTTagCompound();
		return ItemIdentifier.get(Item.getItemById(itemID), damage, tag);
	}
	
	public ItemIdentifierStack readItemIdentifierStack() throws IOException {
		ItemIdentifier item = this.readItemIdentifier();
		int stacksize = this.readInt();
		return new ItemIdentifierStack(item, stacksize);
	}
	
	public <T> List<T> readList(IReadListObject<T> handler) throws IOException {
		int size = this.readInt();
		List<T> list = new ArrayList<T>(size);
		for(int i=0;i<size;i++) {
			list.add(handler.readObject(this));
		}
		return list;
	}

	public IOrderInfoProvider readOrder() throws IOException {
		ItemIdentifierStack stack = this.readItemIdentifierStack();
		int routerId = this.readInt();
		boolean isFinished = this.readBoolean();
		boolean inProgress = this.readBoolean();
		RequestType type = this.readEnum(RequestType.class);
		List<Float> list = this.readList(new IReadListObject<Float>() {
			@Override
			public Float readObject(LPDataInputStream data) throws IOException {
				return data.readFloat();
			}});
		byte machineProgress = this.readByte();
		return new ClientSideOrderInfo(stack, isFinished, type, inProgress, routerId, list, machineProgress);
	}
	
	public <T extends Enum<T>> T readEnum(Class<T> clazz) throws IOException {
		return clazz.getEnumConstants()[this.readInt()];
	}

	public LinkedLogisticsOrderList readLinkedLogisticsOrderList() throws IOException {
		LinkedLogisticsOrderList list = new LinkedLogisticsOrderList();
		list.addAll(this.readList(new IReadListObject<IOrderInfoProvider>() {
			@Override
			public IOrderInfoProvider readObject(LPDataInputStream data) throws IOException {
				return data.readOrder();
			}}));
		list.getSubOrders().addAll(this.readList(new IReadListObject<LinkedLogisticsOrderList>() {
			@Override
			public LinkedLogisticsOrderList readObject(LPDataInputStream data) throws IOException {
				return data.readLinkedLogisticsOrderList();
			}}));
		return list;
	}

	public byte[] readByteArray() throws IOException {
		byte[] array = new byte[this.readInt()];
		for(int i=0;i<array.length;i++) {
			array[i] = this.readByte();
		}
		return array;
	}
}

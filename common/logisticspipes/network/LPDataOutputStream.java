package logisticspipes.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ResourceNetwork;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LPDataOutputStream extends DataOutputStream implements LPDataOutput {

	private final ByteArrayOutputStream byteStream;

	public LPDataOutputStream() {
		super(new ByteArrayOutputStream());
		byteStream = (ByteArrayOutputStream) out;
	}

	public LPDataOutputStream(ByteBuf outBytes) throws IOException {
		super(new ByteBufOutputStream(outBytes));
		byteStream = null;
	}

	public void writeForgeDirection(ForgeDirection dir) throws IOException {
		if (dir == null) {
			writeByte(10);
		} else {
			writeByte(dir.ordinal());
		}
	}

	public void writeExitRoute(ExitRoute route) throws IOException {
		writeIRouter(route.destination);
		writeIRouter(route.root);
		writeForgeDirection(route.exitOrientation);
		writeForgeDirection(route.insertOrientation);
		this.writeEnumSet(route.connectionDetails, PipeRoutingConnectionType.class);
		writeDouble(route.distanceToDestination);
		writeDouble(route.destinationDistanceToRoot);
		writeInt(route.blockDistance);
		this.writeList(route.filters, (data, filter) -> data.writeLPPosition(filter.getLPPosition()));
		writeUTF(route.toString());
		writeBoolean(route.debug.isNewlyAddedCanidate);
		writeBoolean(route.debug.isTraced);
		writeInt(route.debug.index);
	}

	public void writeIRouter(IRouter router) throws IOException {
		if (router == null) {
			writeByte(0);
		} else {
			writeByte(1);
			writeLPPosition(router.getLPPosition());
		}
	}

	public void writeLPPosition(DoubleCoordinates pos) throws IOException {
		writeDouble(pos.getXCoord());
		writeDouble(pos.getYCoord());
		writeDouble(pos.getZCoord());
	}

	public <T extends Enum<T>> void writeEnumSet(EnumSet<T> types, Class<T> clazz) throws IOException {
		T[] parts = clazz.getEnumConstants();
		byte[] set = new byte[parts.length / 8 + (parts.length % 8 == 0 ? 0 : 1)];
		writeByte(set.length);
		for (T part : parts) {
			if (types.contains(part)) {
				byte i = (byte) (1 << (part.ordinal() % 8));
				set[part.ordinal() / 8] |= i;
			}
		}
		writeBytes(set);
	}

	public void writeBytes(byte[] set) throws IOException {
		out.write(set);
	}

	public void writeBitSet(BitSet bits) throws IOException {
		byte[] bytes = new byte[(bits.length() + 7) / 8];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
			}
		}
		writeByte(bytes.length);
		writeBytes(bytes);
	}

	public void writeNBTTagCompound(NBTTagCompound tag) throws IOException {
		if (tag == null) {
			writeShort(-1);
		} else {
			byte[] var3;
			var3 = CompressedStreamTools.compress(tag);
			writeShort((short) var3.length);
			writeBytes(var3);
		}
	}

	public void writeBooleanArray(boolean[] array) throws IOException {
		writeInt(array.length);
		BitSet set = new BitSet();
		for (int i = 0; i < array.length; i++) {
			set.set(i, array[i]);
		}
		writeBitSet(set);
	}

	public byte[] toByteArray() {
		return byteStream.toByteArray();
	}

	public void writeItemIdentifier(ItemIdentifier item) throws IOException {
		if (item == null) {
			writeBoolean(false);
			return;
		}
		writeBoolean(true);
		writeInt(Item.getIdFromItem(item.item));
		writeInt(item.itemDamage);
		writeNBTTagCompound(item.tag);
	}

	public void writeItemIdentifierStack(ItemIdentifierStack stack) throws IOException {
		writeItemIdentifier(stack.getItem());
		writeInt(stack.getStackSize());
	}

	public <T> void writeList(List<T> list, IWriteListObject<T> handler) throws IOException {
		writeInt(list.size());
		for (T aList : list) {
			handler.writeObject(this, aList);
		}
	}

	public <T> void writeSet(Set<T> list, IWriteListObject<T> handler) throws IOException {
		writeInt(list.size());
		Object[] array = list.toArray();
		for (int i = 0; i < list.size(); i++) {
			handler.writeObject(this, (T) array[i]);
		}
	}

	public <T> void writeCollection(Collection<T> collection, IWriteListObject<T> handler) throws IOException {
		this.writeList(new ArrayList<>(collection), handler);
	}

	public void writeOrderInfo(IOrderInfoProvider order) throws IOException {
		writeItemIdentifierStack(order.getAsDisplayItem());
		writeInt(order.getRouterId());
		writeBoolean(order.isFinished());
		writeBoolean(order.isInProgress());
		this.writeEnum(order.getType());
		this.writeList(order.getProgresses(), LPDataOutput::writeFloat);
		writeByte(order.getMachineProgress());
		writeLPPosition(order.getTargetPosition());
		writeItemIdentifier(order.getTargetType());
	}

	public <T extends Enum<T>> void writeEnum(T object) throws IOException {
		writeInt(object.ordinal());
	}

	public void writeLinkedLogisticsOrderList(LinkedLogisticsOrderList orders) throws IOException {
		this.writeList(orders, LPDataOutput::writeOrderInfo);
		this.writeList(orders.getSubOrders(), LPDataOutput::writeLinkedLogisticsOrderList);
	}

	@Override
	public void writeResource(IResource res) throws IOException {
		ResourceNetwork.writeResource(this, res);
	}

	@Override
	public void writeByte(byte b) throws IOException {
		super.writeByte(b);
	}

	@Override
	public void writeShort(short s) throws IOException {
		super.writeShort(s);
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

	public void writeByteBuf(ByteBuf buf) throws IOException {
		buf = buf.copy();
		buf.setIndex(0, 0);
		byte[] data;
		int length = buf.readableBytes();

		if (buf.hasArray()) {
			data = buf.array();
		} else {
			data = new byte[length];
			buf.getBytes(buf.readerIndex(), data);
		}
		writeByteArray(data);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void writeIntArray(int[] arr) throws IOException {
		if (arr == null) {
			writeInt(-1);
		} else {
			writeInt(arr.length);
			for (int i : arr) {
				writeInt(i);
			}
		}
	}

	public void writeLongArray(long[] array) throws IOException {
		writeInt(array.length);
		for (long element : array) {
			writeLong(element);
		}
	}
}

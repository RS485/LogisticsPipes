package logisticspipes.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;

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

	@Override
	public void writeForgeDirection(ForgeDirection dir) throws IOException {
		if (dir == null) {
			writeByte(10);
		} else {
			writeByte(dir.ordinal());
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
		writeCollection(route.filters, (output, filter) -> output.writeLPPosition(filter.getLPPosition()));
		writeUTF(route.toString());
		writeBoolean(route.debug.isNewlyAddedCanidate);
		writeBoolean(route.debug.isTraced);
		writeInt(route.debug.index);
	}

	@Override
	public void writeIRouter(IRouter router) throws IOException {
		if (router == null) {
			writeByte(0);
		} else {
			writeByte(1);
			writeLPPosition(router.getLPPosition());
		}
	}

	@Override
	public void writeLPPosition(DoubleCoordinates pos) throws IOException {
		writeDouble(pos.getXCoord());
		writeDouble(pos.getYCoord());
		writeDouble(pos.getZCoord());
	}

	@Override
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

	@Override
	public void writeBytes(byte[] set) throws IOException {
		out.write(set);
	}

	@Override
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

	@Override
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

	@Override
	public void writeBooleanArray(boolean[] array) throws IOException {
		writeInt(array.length);
		BitSet set = new BitSet();
		for (int i = 0; i < array.length; i++) {
			set.set(i, array[i]);
		}
		writeBitSet(set);
	}

	@Override
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

	@Override
	public void writeItemIdentifierStack(ItemIdentifierStack stack) throws IOException {
		writeItemIdentifier(stack.getItem());
		writeInt(stack.getStackSize());
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
	public <T extends Enum<T>> void writeEnum(T object) throws IOException {
		writeInt(object.ordinal());
	}

	@Override
	public void writeLinkedLogisticsOrderList(LinkedLogisticsOrderList orders) throws IOException {
		writeCollection(orders, LPDataOutput::writeOrderInfo);
		writeCollection(orders.getSubOrders(), LPDataOutput::writeLinkedLogisticsOrderList);
	}

	@Override
	public void writeResource(IResource res) throws IOException {
		ResourceNetwork.writeResource(this, res);
	}

	@Override
	public void writeByte(byte b) throws IOException {
		writeByte(b);
	}

	@Override
	public void writeShort(short s) throws IOException {
		writeShort(s);
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

	@Override
	@SuppressWarnings("Duplicates")
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

	@Override
	public void writeLongArray(long[] arr) throws IOException {
		if (arr == null) {
			writeInt(-1);
		} else {
			writeInt(arr.length);
			for (long i : arr) {
				writeLong(i);
			}
		}
	}

	public byte[] toByteArray() {
		return byteStream.toByteArray();
	}
}

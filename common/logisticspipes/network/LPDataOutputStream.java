package logisticspipes.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ResourceNetwork;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

public class LPDataOutputStream extends DataOutputStream {

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
			out.write(10);
		} else {
			out.write(dir.ordinal());
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
		this.writeList(route.filters, new IWriteListObject<IFilter>() {

			@Override
			public void writeObject(LPDataOutputStream data, IFilter filter) throws IOException {
				data.writeLPPosition(filter.getLPPosition());
			}
		});
		writeUTF(route.toString());
		writeBoolean(route.debug.isNewlyAddedCanidate);
		writeBoolean(route.debug.isTraced);
		writeInt(route.debug.index);
	}

	public void writeIRouter(IRouter router) throws IOException {
		if (router == null) {
			out.write(0);
		} else {
			out.write(1);
			writeLPPosition(router.getLPPosition());
		}
	}

	public void writeLPPosition(LPPosition pos) throws IOException {
		writeDouble(pos.getXD());
		writeDouble(pos.getYD());
		writeDouble(pos.getZD());
	}

	public <T extends Enum<T>> void writeEnumSet(EnumSet<T> types, Class<T> clazz) throws IOException {
		T[] parts = clazz.getEnumConstants();
		byte[] set = new byte[parts.length / 8 + (parts.length % 8 == 0 ? 0 : 1)];
		out.write(set.length);
		for (T part : parts) {
			if (types.contains(part)) {
				byte i = (byte) (1 << (part.ordinal() % 8));
				set[part.ordinal() / 8] |= i;
			}
		}
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
		this.write(bytes);
	}

	public void writeNBTTagCompound(NBTTagCompound tag) throws IOException {
		if (tag == null) {
			writeShort(-1);
		} else {
			byte[] var3 = CompressedStreamTools.compress(tag);
			writeShort((short) var3.length);
			this.write(var3);
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

	public void writeIntegerArray(int[] array) throws IOException {
		writeInt(array.length);
		for (int element : array) {
			writeInt(element);
		}
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
		for (int i = 0; i < list.size(); i++) {
			handler.writeObject(this, list.get(i));
		}
	}

	public <T> void writeCollection(Collection<T> collection, IWriteListObject<T> handler) throws IOException {
		this.writeList(new ArrayList<T>(collection), handler);
	}

	public void writeOrderInfo(IOrderInfoProvider order) throws IOException {
		writeItemIdentifierStack(order.getAsDisplayItem());
		writeInt(order.getRouterId());
		writeBoolean(order.isFinished());
		writeBoolean(order.isInProgress());
		this.writeEnum(order.getType());
		this.writeList(order.getProgresses(), new IWriteListObject<Float>() {

			@Override
			public void writeObject(LPDataOutputStream data, Float object) throws IOException {
				data.writeFloat(object);
			}
		});
		writeByte(order.getMachineProgress());
		if(order.getTargetPosition() != null) {
			writeBoolean(true);
			writeLPPosition(order.getTargetPosition());
			writeItemIdentifier(order.getTargetType());
		} else {
			writeBoolean(false);
		}
	}

	public <T extends Enum<T>> void writeEnum(T object) throws IOException {
		writeInt(object.ordinal());
	}

	public void writeLinkedLogisticsOrderList(LinkedLogisticsOrderList orders) throws IOException {
		this.writeList(orders, new IWriteListObject<IOrderInfoProvider>() {

			@Override
			public void writeObject(LPDataOutputStream data, IOrderInfoProvider order) throws IOException {
				data.writeOrderInfo(order);
			}
		});
		this.writeList(orders.getSubOrders(), new IWriteListObject<LinkedLogisticsOrderList>() {

			@Override
			public void writeObject(LPDataOutputStream data, LinkedLogisticsOrderList order) throws IOException {
				data.writeLinkedLogisticsOrderList(order);
			}
		});
	}

	public void writeByteArray(byte[] array) throws IOException {
		writeInt(array.length);
		for (byte element : array) {
			writeByte(element);
		}
	}

	public void writeByteBuf(ByteBuf buf) throws IOException {
		buf = buf.copy();
		buf.setIndex(0, 0);
		byte[] bytes;
		int length = buf.readableBytes();

		if (buf.hasArray()) {
			bytes = buf.array();
		} else {
			bytes = new byte[length];
			buf.getBytes(buf.readerIndex(), bytes);
		}
		writeByteArray(bytes);
	}

	public void writeLongArray(long[] array) throws IOException {
		writeInt(array.length);
		for (long element : array) {
			writeLong(element);
		}
	}

	public void writeIResource(IResource stack) throws IOException {
		ResourceNetwork.writeResource(this, stack);
	}
}

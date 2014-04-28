package logisticspipes.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.EnumSet;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class LPDataOutputStream extends DataOutputStream {

	private final ByteArrayOutputStream byteStream;
	
	public LPDataOutputStream() {
		super(new ByteArrayOutputStream());
		byteStream = (ByteArrayOutputStream) out;
	}
	
	public void writeForgeDirection(ForgeDirection dir) throws IOException {
		out.write(dir.ordinal());
	}
	
	public void writeExitRoute(ExitRoute route) throws IOException {
		this.writeIRouter(route.destination);
		this.writeIRouter(route.root);
		this.writeForgeDirection(route.exitOrientation);
		this.writeForgeDirection(route.insertOrientation);
		this.writeEnumSet(route.connectionDetails, PipeRoutingConnectionType.class);
		this.writeInt(route.distanceToDestination);
		this.writeInt(route.destinationDistanceToRoot);
		this.writeInt(route.filters.size());
		for(IFilter filter:route.filters) {
			this.writeLPPosition(filter.getLPPosition());
		}
		this.writeUTF(route.toString());
		this.writeBoolean(route.debug.isNewlyAddedCanidate);
		this.writeBoolean(route.debug.isTraced);
		this.writeInt(route.debug.index);
	}

	public void writeIRouter(IRouter router) throws IOException {
		if(router == null) {
			out.write(0);
		} else {
			out.write(1);
			writeLPPosition(router.getLPPosition());
		}
	}
	
	public void writeLPPosition(LPPosition pos) throws IOException {
		this.writeDouble(pos.getXD());
		this.writeDouble(pos.getYD());
		this.writeDouble(pos.getZD());
	}
	
	public <T extends Enum<T>> void writeEnumSet(EnumSet<T> types, Class<T> clazz) throws IOException {
		T[] parts = clazz.getEnumConstants();
		byte[] set = new byte[parts.length / 8 + (parts.length%8==0?0:1)];
		out.write(set.length);
		for(T part: parts) {
			if(types.contains(part)) {
				byte i = (byte)(1 << (part.ordinal() % 8));
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
		this.writeByte(bytes.length);
		this.write(bytes);
	}
	
	public void writeNBTTagCompound(NBTTagCompound tag) throws IOException {
		if(tag == null) {
			this.writeShort(-1);
		} else {
			byte[] var3 = CompressedStreamTools.compress(tag);
			this.writeShort((short)var3.length);
			this.write(var3);
		}
	}
	
	public void writeBooleanArray(boolean[] array) throws IOException {
		this.writeByte(array.length);
		BitSet set = new BitSet();
		for(int i=0;i<array.length;i++) {
			set.set(i, array[i]);
		}
		writeBitSet(set);
	}
	
	public void writeIntegerArray(int[] array) throws IOException {
		this.writeByte(array.length);
		for(int i=0;i<array.length;i++) {
			this.writeInt(array[i]);
		}
	}

	public byte[] toByteArray() {
		return byteStream.toByteArray();
	}

	public void writeItemIdentifierStack(ItemIdentifierStack stack) throws IOException {
		this.writeInt(stack.getItem().itemID);
		this.writeInt(stack.getStackSize());
		this.writeInt(stack.getItem().itemDamage);
		this.writeNBTTagCompound(stack.getItem().tag);
	}
}

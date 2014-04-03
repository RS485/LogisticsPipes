package logisticspipes.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.EnumSet;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class LPDataOutputStream extends DataOutputStream {

	public LPDataOutputStream(OutputStream outputstream) {
		super(outputstream);
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
}

package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;

import logisticspipes.network.NetworkConstants;
import logisticspipes.proxy.MainProxy;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.transport.TileGenericPipe;

public class PacketLiquidUpdate extends PacketCoordinates {

	public LiquidStack[] renderCache = new LiquidStack[ForgeDirection.values().length];
	public BitSet delta;

	public PacketLiquidUpdate(int xCoord, int yCoord, int zCoord) {
		super(NetworkConstants.LIQUID_UPDATE_PACKET, xCoord, yCoord, zCoord);
	}

	public PacketLiquidUpdate(int xCoord, int yCoord, int zCoord, boolean chunkPacket) {
		super(NetworkConstants.LIQUID_UPDATE_PACKET, xCoord, yCoord, zCoord);
		this.isChunkDataPacket = chunkPacket;
	}

	public PacketLiquidUpdate() {
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);

		World world = MainProxy.getClientMainWorld();
		if (!world.blockExists(posX, posY, posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(posX, posY, posZ);
		if (!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null)
			return;

		if (!(pipe.pipe.transport instanceof PipeLiquidTransportLogistics))
			return;

		PipeLiquidTransportLogistics transLiq = ((PipeLiquidTransportLogistics) pipe.pipe.transport);

		renderCache = transLiq.renderCache;

		byte[] dBytes = new byte[3];
		data.read(dBytes);
		delta = fromByteArray(dBytes);

		// System.out.printf("read %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);

		for (ForgeDirection dir : ForgeDirection.values()) {
			if (renderCache[dir.ordinal()] == null) {
				renderCache[dir.ordinal()] = new LiquidStack(0, 0, 0);
			}

			if (delta.get(dir.ordinal() * 3 + 0)) {
				renderCache[dir.ordinal()].itemID = data.readShort();
			}
			if (delta.get(dir.ordinal() * 3 + 1)) {
				renderCache[dir.ordinal()].itemMeta = data.readShort();
			}
			if (delta.get(dir.ordinal() * 3 + 2)) {
				if(dir != ForgeDirection.UNKNOWN) {
					renderCache[dir.ordinal()].amount = Math.min(transLiq.getSideCapacity(), data.readShort());
				} else {
					renderCache[dir.ordinal()].amount = Math.min(transLiq.getInnerCapacity(), data.readShort());
				}
			}
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		byte[] dBytes = toByteArray(delta);
		// System.out.printf("write %d, %d, %d = %s, %s%n", posX, posY, posZ, Arrays.toString(dBytes), delta);
		data.write(dBytes);

		for (ForgeDirection dir : ForgeDirection.values()) {
			LiquidStack liquid = renderCache[dir.ordinal()];

			if (delta.get(dir.ordinal() * 3 + 0)) {
				if (liquid != null) {
					data.writeShort(liquid.itemID);
				} else {
					data.writeShort(0);
				}
			}
			if (delta.get(dir.ordinal() * 3 + 1)) {
				if (liquid != null) {
					data.writeShort(liquid.itemMeta);
				} else {
					data.writeShort(0);
				}
			}
			if (delta.get(dir.ordinal() * 3 + 2)) {
				if (liquid != null) {
					data.writeShort(liquid.amount);
				} else {
					data.writeShort(0);
				}
			}
		}
	}

	public static BitSet fromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	public static byte[] toByteArray(BitSet bits) {
		byte[] bytes = new byte[3];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
			}
		}
		return bytes;
	}
}

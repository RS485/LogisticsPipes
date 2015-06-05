package logisticspipes.pipes.basic;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.multiblock.MultiBlockCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.TileBuffer;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public class LogisticsTileGenericSubMultiBlock extends TileEntity {

	private LPPosition mainPipePos;
	private LogisticsTileGenericPipe mainPipe;
	private TileBuffer[] tileBuffer;

	@Deprecated
	public LogisticsTileGenericSubMultiBlock() {

	}

	public LogisticsTileGenericSubMultiBlock(LPPosition pos) {
		mainPipePos = pos;
	}

	public LogisticsTileGenericPipe getMainPipe() {
		if (mainPipe == null) {
			if (mainPipePos != null) {
				TileEntity tile = mainPipePos.getTileEntity(getWorldObj());
				if (tile instanceof LogisticsTileGenericPipe) {
					mainPipe = (LogisticsTileGenericPipe) tile;
				}
			}
		}
		if (mainPipe == null || mainPipe.isInvalid()) {
			getWorldObj().setBlockToAir(xCoord, yCoord, zCoord);
		}
		return mainPipe;
	}

	@Override
	public void updateEntity() {
		if (MainProxy.isClient(getWorldObj())) {
			return;
		}
		LogisticsTileGenericPipe pipe = getMainPipe();
		if (pipe != null) {
			pipe.subMultiBlock.add(new LPPosition(this));
		}
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		mainPipePos = LPPosition.readFromNBT("MainPipePos_", nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		mainPipePos.writeToNBT("MainPipePos_", nbt);
	}

	@Override
	public Packet getDescriptionPacket() {
		try {
			return PacketHandler.toFMLPacket(getLPDescriptionPacket());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ModernPacket getLPDescriptionPacket() {
		MultiBlockCoordinatesPacket packet = PacketHandler.getPacket(MultiBlockCoordinatesPacket.class);
		packet.setTilePos(this);
		packet.setTargetLPPos(mainPipePos);
		return packet;
	}

	public void setPosition(LPPosition lpPosition) {
		mainPipePos = lpPosition;
	}

	public TileEntity getTile(ForgeDirection to) {
		return getTile(to, false);
	}

	public TileEntity getTile(ForgeDirection to, boolean force) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			if (force) {
				cache[to.ordinal()].refresh();
			}
			return cache[to.ordinal()].getTile();
		} else {
			return null;
		}
	}

	public Block getBlock(ForgeDirection to) {
		TileBuffer[] cache = getTileCache();
		if (cache != null) {
			return cache[to.ordinal()].getBlock();
		} else {
			return null;
		}
	}

	public TileBuffer[] getTileCache() {
		if (tileBuffer == null) {
			tileBuffer = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, true);
		}
		return tileBuffer;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		tileBuffer = null;
	}

	@Override
	public void validate() {
		super.validate();
		tileBuffer = null;
	}

	public void scheduleNeighborChange() {
		tileBuffer = null;
	}
}

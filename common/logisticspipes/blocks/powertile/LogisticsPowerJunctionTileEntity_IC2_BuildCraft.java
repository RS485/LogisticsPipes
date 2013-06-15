package logisticspipes.blocks.powertile;

import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class LogisticsPowerJunctionTileEntity_IC2_BuildCraft extends LogisticsPowerJunctionTileEntity_BuildCraft implements IEnergySink {

	public final int IC2Multiplier = 2;
	
	private boolean addedToEnergyNet = false;
	private boolean doinit = false;
	
	private int internalBuffer = 0;
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
		return true;
	}

	@Override
	public boolean isAddedToEnergyNet() {
		return addedToEnergyNet;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if(addedToEnergyNet) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			addedToEnergyNet = false;
		}
	}

	@Override
	public void validate() {
		super.validate();
		if(!addedToEnergyNet) {
			doinit = true;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if(doinit) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			addedToEnergyNet = true;
			doinit = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(addedToEnergyNet) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			addedToEnergyNet = false;
		}
	}

	@Override
	public int demandsEnergy() {
		if(internalBuffer > 0 && freeSpace() > 0) {
			internalBuffer = injectEnergy(null, internalBuffer);
		}
		return freeSpace();
	}

	@Override
	public int injectEnergy(Direction directionFrom, int amount) {
		int addAmount = Math.min(amount, freeSpace() / IC2Multiplier);
		if(freeSpace() > 0 && addAmount == 0) {
			addAmount = 1;
		}
		addEnergy(addAmount * IC2Multiplier);
		if(addAmount == 0 && directionFrom != null) {
			internalBuffer += amount;
			return 0;
		}
		return amount - addAmount;
	}

	@Override
	public int getMaxSafeInput() {
		return Integer.MAX_VALUE;
	}
}

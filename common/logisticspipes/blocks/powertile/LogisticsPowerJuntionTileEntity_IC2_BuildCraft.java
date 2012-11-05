package logisticspipes.blocks.powertile;

import ic2.api.Direction;
import ic2.api.IEnergySink;
import ic2.common.EnergyNet;
import net.minecraft.src.TileEntity;

public class LogisticsPowerJuntionTileEntity_IC2_BuildCraft extends LogisticsPowerJuntionTileEntity_BuildCraft implements IEnergySink {

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
			EnergyNet.getForWorld(worldObj).removeTileEntity(this);
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
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			addedToEnergyNet = true;
			doinit = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(addedToEnergyNet) {
			EnergyNet.getForWorld(worldObj).removeTileEntity(this);
			addedToEnergyNet = false;
		}
	}

	@Override
	public boolean demandsEnergy() {
		if(internalBuffer > 0 && freeSpace() > 0) {
			internalBuffer = injectEnergy(null, internalBuffer);
		}
		return freeSpace() > 0;
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
}

package logisticspipes.blocks.powertile;

import ic2.api.Direction;
import ic2.api.IEnergySink;
import ic2.common.EnergyNet;
import net.minecraft.src.TileEntity;

public class LogisticsPowerJuntionTileEntity_IC2_BuildCraft extends LogisticsPowerJuntionTileEntity_BuildCraft implements IEnergySink {

	public final int IC2Multiplier = 2;
	
	private boolean addedToEnergyNet = false;
	private boolean doinit = false;
	
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
		return freeSpace() > 0;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int amount) {
		int addAmount = Math.min(amount, freeSpace() / IC2Multiplier);
		addEnergy(addAmount * IC2Multiplier);
		return amount - addAmount;
	}

}

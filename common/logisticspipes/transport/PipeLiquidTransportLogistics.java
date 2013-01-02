package logisticspipes.transport;

import buildcraft.transport.PipeTransport;
import logisticspipes.pipes.basic.liquid.LogisitcsLiquidConnectionTransport;
import logisticspipes.pipes.basic.liquid.LogisticsLiquidSection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;

public class PipeLiquidTransportLogistics extends PipeTransportLogistics implements ITankContainer {

	private LogisticsLiquidSection[] sideTanks = new LogisticsLiquidSection[ForgeDirection.VALID_DIRECTIONS.length];
	private LogisticsLiquidSection internalTank = new LogisticsLiquidSection(10000);
	
	public PipeLiquidTransportLogistics() {
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			sideTanks[dir.ordinal()] = new LogisticsLiquidSection(1000);
		}
	}
	
	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		if(from.ordinal() < ForgeDirection.VALID_DIRECTIONS.length) {
			return sideTanks[from.ordinal()].fill(resource, doFill);
		} else {
			return 0;//internalTank.fill(resource, doFill);
		}
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		return fill(ForgeDirection.getOrientation(tankIndex), resource, doFill);
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if(from.ordinal() < ForgeDirection.VALID_DIRECTIONS.length) {
			return sideTanks[from.ordinal()].drain(maxDrain, doDrain);
		} else {
			return null;//internalTank.drain(maxDrain, doDrain);
		}
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		return drain(ForgeDirection.getOrientation(tankIndex), maxDrain, doDrain);
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		if(direction.ordinal() < ForgeDirection.VALID_DIRECTIONS.length) {
			return new ILiquidTank[]{sideTanks[direction.ordinal()]};
		} else {
			return null;//new ILiquidTank[]{internalTank};
		}
	}


	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (nbttagcompound.hasKey("tank[" + direction.ordinal() + "]")) {
				sideTanks[direction.ordinal()].readFromNBT(nbttagcompound.getCompoundTag("tank[" + direction.ordinal() + "]"));
			}
		}
		if (nbttagcompound.hasKey("tank[middle]")) {
			internalTank.readFromNBT(nbttagcompound.getCompoundTag("tank[middle]"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			NBTTagCompound subTag = new NBTTagCompound();
			sideTanks[direction.ordinal()].writeToNBT(subTag);
			nbttagcompound.setTag("tank[" + direction.ordinal() + "]", subTag);
		}
		NBTTagCompound subTag = new NBTTagCompound();
		internalTank.writeToNBT(subTag);
		nbttagcompound.setTag("tank[middle]", subTag);
	}
	
	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		return null;
	}
	
	@Override
	public boolean allowsConnect(PipeTransport with) {
		return super.allowsConnect(with) || with instanceof LogisitcsLiquidConnectionTransport;
	}
}

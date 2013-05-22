package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import logisticspipes.utils.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

public class PipeLiquidInsertion extends LiquidRoutedPipe {
	
	private List<Pair<Integer, Integer>> localJamList = new ArrayList<Pair<Integer, Integer>>();
	private int[] nextSendMax = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private int[] nextSendMin = new int[ForgeDirection.VALID_DIRECTIONS.length];
	
	public PipeLiquidInsertion(int itemID) {
		super(itemID);
	}

	@Override
	public void enabledUpdateEntity() {
		List<Integer> tempJamList = new ArrayList<Integer>();
		if(!localJamList.isEmpty()) {
			List<Pair<Integer, Integer>> toRemove = new ArrayList<Pair<Integer, Integer>>();
			for(Pair<Integer, Integer> part: localJamList) {
				part.setValue2(part.getValue2() - 1);
				if(part.getValue2() <= 0) {
					toRemove.add(part);
				} else {
					tempJamList.add(part.getValue1());
				}
			}
			if(!toRemove.isEmpty()) {
				localJamList.removeAll(toRemove);
			}
		}
		PipeLiquidTransportLogistics transport = (PipeLiquidTransportLogistics) this.transport;
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			LiquidStack stack = transport.sideTanks[dir.ordinal()].getLiquid();
			if(stack == null) continue;
			stack = stack.copy();
			
			if(nextSendMax[dir.ordinal()] > 0 && stack.amount < transport.sideTanks[dir.ordinal()].getCapacity()) {
				nextSendMax[dir.ordinal()]--;
				continue;
			}
			if(nextSendMin[dir.ordinal()] > 0) {
				nextSendMin[dir.ordinal()]--;
				continue;
			}
			
			Pair<Integer, Integer> result = SimpleServiceLocator.logisticsLiquidManager.getBestReply(stack, getRouter(), tempJamList);
			if(result == null || result.getValue1() == null || result.getValue2() == 0) {
				nextSendMax[dir.ordinal()] = 100;
				nextSendMin[dir.ordinal()] = 10;
				continue;
			}
			
			if(!useEnergy((int) (0.01 * result.getValue2()))) {
				nextSendMax[dir.ordinal()] = 100;
				nextSendMin[dir.ordinal()] = 10;
				continue;
			}

			LiquidStack toSend = transport.sideTanks[dir.ordinal()].drain(result.getValue2(), true);
			ItemStack liquidContainer = SimpleServiceLocator.logisticsLiquidManager.getLiquidContainer(toSend);
			IRoutedItem routed = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(liquidContainer, worldObj);
			routed.setDestination(result.getValue1());
			routed.setTransportMode(TransportMode.Passive);
			this.queueRoutedItem(routed, dir);
			nextSendMax[dir.ordinal()] = 100;
			nextSendMin[dir.ordinal()] = 10;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setIntArray("nextSendMax", nextSendMax);
		nbttagcompound.setIntArray("nextSendMin", nextSendMin);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		nextSendMax = nbttagcompound.getIntArray("nextSendMax");
		if(nextSendMax.length < 6) {
			nextSendMax = new int[6];
		}
		nextSendMin = nbttagcompound.getIntArray("nextSendMin");
		if(nextSendMin.length < 6) {
			nextSendMin = new int[6];
		}
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_INSERTION;
	}

	@Override
	public boolean canInsertToTanks() {
		return false;
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return false;
	}

}
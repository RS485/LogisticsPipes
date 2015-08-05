package logisticspipes.pipes;

import java.util.LinkedList;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.AdjacentTile;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class PipeFluidExtractor extends PipeFluidInsertion {

	private int[] liquidToExtract = new int[6];

	private static final int flowRate = 500;
	private static final int energyPerFlow = 5;

	public PipeFluidExtractor(Item item) {
		super(item);
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (!isNthTick(10)) {
			return;
		}
		LinkedList<AdjacentTile> connected = getConnectedEntities();
		for (AdjacentTile tile : connected) {
			if (tile.tile instanceof IFluidHandler && SimpleServiceLocator.pipeInformationManager.isNotAPipe(tile.tile)) {
				extractFrom((IFluidHandler) tile.tile, tile.orientation);
			}
		}
	}

	private void extractFrom(IFluidHandler container, ForgeDirection side) {
		int i = side.ordinal();
		FluidStack contained = ((PipeFluidTransportLogistics) transport).getTankInfo(side)[0].fluid;
		int amountMissing = ((PipeFluidTransportLogistics) transport).getSideCapacity() - (contained != null ? contained.amount : 0);
		if (liquidToExtract[i] < Math.min(PipeFluidExtractor.flowRate, amountMissing)) {
			if (this.useEnergy(PipeFluidExtractor.energyPerFlow)) {
				liquidToExtract[i] += Math.min(PipeFluidExtractor.flowRate, amountMissing);
			}
		}
		FluidStack extracted = container.drain(side.getOpposite(), liquidToExtract[i] > PipeFluidExtractor.flowRate ? PipeFluidExtractor.flowRate : liquidToExtract[i], false);

		int inserted = 0;
		if (extracted != null) {
			inserted = ((PipeFluidTransportLogistics) transport).fill(side, extracted, true);
			container.drain(side.getOpposite(), inserted, true);
		}
		liquidToExtract[i] -= inserted;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setIntArray("liquidToExtract", liquidToExtract);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		liquidToExtract = nbttagcompound.getIntArray("liquidToExtract");
		if (liquidToExtract.length < 6) {
			liquidToExtract = new int[6];
		}
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_EXTRACTOR;
	}
}

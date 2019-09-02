package logisticspipes.pipes;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.interfaces.ITankUtil;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.FluidIdentifierStack;

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

		getAdjacentTanksAdvanced(false).forEach(tankData -> extractFrom(tankData.getValue1(), tankData.getValue3()));
	}

	private void extractFrom(ITankUtil container, EnumFacing side) {
		int sideID = side.ordinal();
		FluidStack contained = ((PipeFluidTransportLogistics) transport).getTankProperties(side)[0].getContents();
		int amountMissing = ((PipeFluidTransportLogistics) transport).getSideCapacity() - (contained != null ? contained.amount : 0);
		if (liquidToExtract[sideID] < Math.min(PipeFluidExtractor.flowRate, amountMissing)) {
			if (this.useEnergy(PipeFluidExtractor.energyPerFlow)) {
				liquidToExtract[sideID] += Math.min(PipeFluidExtractor.flowRate, amountMissing);
			}
		}
		FluidIdentifierStack extracted = container.drain(Math.min(liquidToExtract[sideID], PipeFluidExtractor.flowRate), false);

		int inserted = 0;
		if (extracted != null) {
			inserted = ((PipeFluidTransportLogistics) transport).fill(side, extracted.makeFluidStack(), true);
			container.drain(inserted, true);
		}
		liquidToExtract[sideID] -= inserted;
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

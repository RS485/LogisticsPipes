package logisticspipes.pipes;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeFluidTransportLogistics;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.EnumFacing;
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

		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(container);
		worldCoordinates.getAdjacentTileEntities()
				.filter(adjacent -> adjacent.tileEntity instanceof IFluidHandler && SimpleServiceLocator.pipeInformationManager.isNotAPipe(adjacent.tileEntity))
				.forEach(adjacent -> extractFrom((IFluidHandler) adjacent.tileEntity, adjacent.direction));
	}

	private void extractFrom(IFluidHandler container, EnumFacing side) {
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

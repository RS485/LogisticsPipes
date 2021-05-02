package logisticspipes.pipes;

import logisticspipes.utils.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidTank;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITankUtil;
import logisticspipes.interfaces.routing.IFluidSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.tuples.Triplet;

public class PipeFluidBasic extends FluidRoutedPipe implements IFluidSink {

	public ItemIdentifierInventory filterInv = new ItemIdentifierInventory(1, "Dummy", 1, true);
	private PlayerCollectionList guiOpenedBy = new PlayerCollectionList();
	private final FluidSinkReply.FixedFluidPriority _priority = FluidSinkReply.FixedFluidPriority.ItemSink;

	public PipeFluidBasic(Item item) {
		super(item);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_BASIC;
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return true;
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Fluid_Basic_ID, getWorld(), getX(), getY(), getZ());
	}

	@Override
	public FluidSinkReply sinkAmount(FluidIdentifierStack stack, int bestPriority) {
		if (!guiOpenedBy.isEmpty()) {
			return null; //Don't sink when the gui is open
		}
		FluidIdentifier ident = stack.getFluid();
		if (filterInv.getIDStackInSlot(0) == null) {
			return null;
		}
		if (!ident.equals(FluidIdentifier.get(filterInv.getIDStackInSlot(0).getItem()))) {
			return null;
		}
		if(bestPriority > _priority.ordinal() || bestPriority == _priority.ordinal()){
			return null;
		}
		int onTheWay = this.countOnRoute(ident);
		long freeSpace = -onTheWay;
		for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : getAdjacentTanksAdvanced(true)) {
			FluidTank tank = ((PipeFluidTransportLogistics) transport).sideTanks[pair.getValue3().ordinal()];
			freeSpace += pair.getValue1().getFreeSpaceInsideTank(ident);
			freeSpace += ident.getFreeSpaceInsideTank(tank);
			if (freeSpace >= stack.getAmount()) {
				return new FluidSinkReply(FluidSinkReply.FixedFluidPriority.ItemSink, stack.getAmount());
			}
		}
		return new FluidSinkReply(FluidSinkReply.FixedFluidPriority.ItemSink, freeSpace > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) freeSpace);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		filterInv.writeToNBT(nbttagcompound);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		filterInv.readFromNBT(nbttagcompound);
	}

	@Override
	public boolean canInsertToTanks() {
		return true;
	}

	public void guiOpenedByPlayer(EntityPlayer player) {
		guiOpenedBy.add(player);
	}

	public void guiClosedByPlayer(EntityPlayer player) {
		guiOpenedBy.remove(player);
	}

	@Override
	public boolean canReceiveFluid() {
		return false;
	}
}

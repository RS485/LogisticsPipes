package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITankUtil;
import logisticspipes.interfaces.routing.IFluidSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.*;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.tuples.Triplet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTank;

public class PipeFluidTerminus extends FluidRoutedPipe implements IFluidSink {
    public ItemIdentifierInventory filterInv = new ItemIdentifierInventory(9, "Dummy", 1, true);
    private PlayerCollectionList guiOpenedBy = new PlayerCollectionList();
    private final FluidSinkReply.FixedFluidPriority _priority = FluidSinkReply.FixedFluidPriority.Terminus;

    public PipeFluidTerminus(Item item) {
        super(item);
    }

    @Override
    public Textures.TextureType getCenterTexture() {
        return Textures.LOGISTICSPIPE_LIQUID_TERMINUS;
    }

    @Override
    public boolean canInsertFromSideToTanks() {
        return true;
    }

    @Override
    public void onWrenchClicked(EntityPlayer entityplayer) {
        entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Fluid_Terminus_ID, getWorld(), getX(), getY(), getZ());
    }

    @Override
    public FluidSinkReply sinkAmount(FluidIdentifierStack stack, int bestPriority) {
        if (!guiOpenedBy.isEmpty()) {
            return null; //Don't sink when the gui is open
        }
        FluidIdentifier ident = stack.getFluid();

        for (int i = 0; i<9; i++) {
            if (filterInv.getIDStackInSlot(i) == null) {
                continue;
            }
            if (!ident.equals(FluidIdentifier.get(filterInv.getIDStackInSlot(i).getItem()))) {
                continue;
            }
            if( bestPriority > _priority.ordinal() || bestPriority == _priority.ordinal()) {
                continue;
            }
            int onTheWay = this.countOnRoute(ident);
            long freeSpace = -onTheWay;
            for (Triplet<ITankUtil, TileEntity, EnumFacing> pair : getAdjacentTanksAdvanced(true)) {
                FluidTank tank = ((PipeFluidTransportLogistics) transport).sideTanks[pair.getValue3().ordinal()];
                freeSpace += pair.getValue1().getFreeSpaceInsideTank(ident);
                freeSpace += ident.getFreeSpaceInsideTank(tank);
                if (freeSpace >= stack.getAmount()) {
                    return new FluidSinkReply(FluidSinkReply.FixedFluidPriority.Terminus, stack.getAmount());
                }
            }
            return new FluidSinkReply(FluidSinkReply.FixedFluidPriority.Terminus, freeSpace > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) freeSpace);
        }
        return null;
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

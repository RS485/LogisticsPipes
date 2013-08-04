package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IFluidSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import net.minecraftforge.fluids.FluidTank;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeFluidTransportLogistics;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.Pair;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.FluidStack;

public class PipeFluidBasic extends FluidRoutedPipe implements IFluidSink {
	
	public SimpleInventory filterInv = new SimpleInventory(1, "Dummy", 1);
	private PlayerCollectionList guiOpenedBy = new PlayerCollectionList();
	
	public PipeFluidBasic(int itemID) {
		super(itemID);
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
	public boolean wrenchClicked(EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(getWorld())) {
			if (settings == null || settings.openGui) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Fluid_Basic_ID, getWorld(), getX(), getY(), getZ());
			} else {
				entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
			}
		}
		return true;
	}

	@Override
	public int sinkAmount(FluidStack stack) {
		if(!guiOpenedBy.isEmpty()) return 0; //Don't sink when the gui is open
		FluidIdentifier ident = FluidIdentifier.get(stack);
		if(filterInv.getStackInSlot(0) == null) return 0;
		if(ident != filterInv.getIDStackInSlot(0).getItem().getFluidIdentifier()) return 0;
		int onTheWay = this.countOnRoute(ident);
		int freeSpace = -onTheWay;
		for(Pair<TileEntity,ForgeDirection> pair:getAdjacentTanks(true)) {
			FluidTank tank = ((PipeFluidTransportLogistics)this.transport).sideTanks[pair.getValue2().ordinal()];
			freeSpace += ident.getFreeSpaceInsideTank((IFluidHandler)pair.getValue1(), pair.getValue2());
			freeSpace += tank.getCapacity()-tank.getFluidAmount();
			if(freeSpace >= stack.amount) {
				return stack.amount;
			}
		}
 		return freeSpace;
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
}

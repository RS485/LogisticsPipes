package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ILiquidSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.pipes.basic.liquid.LogisticsLiquidSection;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

public class PipeLiquidBasic extends LiquidRoutedPipe implements ILiquidSink {
	
	public SimpleInventory filterInv = new SimpleInventory(1, "Dummy", 1);
	
	public PipeLiquidBasic(int itemID) {
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
	public boolean wrenchClicked(World world, int i, int j, int k, EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(world)) {
			if (settings == null || settings.openGui) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Liquid_Basic_ID, world, getX(), getY(), getZ());
			} else {
				entityplayer.sendChatToPlayer("Permission denied");
			}
		}
		return true;
	}

	@Override
	public int sinkAmount(LiquidStack stack) {
		LiquidIdentifier ident = LiquidIdentifier.get(stack);
		if(filterInv.getStackInSlot(0) == null) return 0;
		if(ident != ItemIdentifier.get(filterInv.getStackInSlot(0)).getLiquidIdentifier()) return 0;
		
		int amount = 0;
		for(Pair<TileEntity,ForgeDirection> pair:getAdjacentTanks(true)) {
			LogisticsLiquidSection tank = ((PipeLiquidTransportLogistics)this.transport).sideTanks[pair.getValue2().ordinal()];
			amount += tank.fill(stack, false);
			if(amount == stack.amount) {
				return amount;
			}
		}
		return amount;
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

}

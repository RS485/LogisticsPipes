package logisticspipes.pipes;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ILiquidSink;
import logisticspipes.items.LogisticsLiquidContainer;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.pipes.basic.liquid.LogisticsLiquidSection;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.core.utils.SimpleInventory;
import buildcraft.transport.EntityData;
import buildcraft.transport.PipeTransportItems;

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
	public void enabledUpdateEntity() {
		int validDirections = 0;
		List<Pair<TileEntity,ForgeDirection>> list = getAdjacentTanks(true);
		for(Pair<TileEntity,ForgeDirection> pair:list) {
			LogisticsLiquidSection tank = ((PipeLiquidTransportLogistics)this.transport).sideTanks[pair.getValue2().ordinal()];
			validDirections++;
			if(tank.getLiquid() == null) continue;
			int filled = ((ITankContainer)pair.getValue1()).fill(pair.getValue2().getOpposite(), tank.getLiquid(), true);
			if(filled == 0) continue;
			LiquidStack drain = tank.drain(filled, true);
			if(drain == null || filled != drain.amount) {
				if(LogisticsPipes.DEBUG) {
					throw new UnsupportedOperationException("Liquid Multiplication");
				}
			}
		}
		if(validDirections == 0) return;
		LogisticsLiquidSection tank = ((PipeLiquidTransportLogistics)this.transport).internalTank;
		LiquidStack stack = tank.getLiquid();
		if(stack == null) return;
		for(Pair<TileEntity,ForgeDirection> pair:list) {
			LogisticsLiquidSection tankSide = ((PipeLiquidTransportLogistics)this.transport).sideTanks[pair.getValue2().ordinal()];
			stack = tank.getLiquid();
			if(stack == null) continue;
			stack = stack.copy();
			int filled = tankSide.fill(stack , true);
			if(filled == 0) continue;
			LiquidStack drain = tank.drain(filled, true);
			if(drain == null || filled != drain.amount) {
				if(LogisticsPipes.DEBUG) {
					throw new UnsupportedOperationException("Liquid Multiplication");
				}
			}
		}
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		if(SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer)) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Liquid_Basic_ID, world, xCoord, yCoord, zCoord);
			return true;
		}
		return super.blockActivated(world, i, j, k, entityplayer);
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
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		if(!isConnectableTank(tile, data.output, true)) return;
		if(data.output.ordinal() >= ForgeDirection.VALID_DIRECTIONS.length) return;
		if(!(data.item instanceof IRoutedItem) || data.item.getItemStack() == null || !(data.item.getItemStack().getItem() instanceof LogisticsLiquidContainer)) return;
		if(this.getRouter().getSimpleID() != ((IRoutedItem)data.item).getDestination()) return;
		((PipeTransportItems)this.transport).scheduleRemoval(data.item);
		LiquidStack liquid = SimpleServiceLocator.logisticsLiquidManager.getLiquidFromContainer(data.item.getItemStack());
		if(liquid != null) {
			int filled = ((PipeLiquidTransportLogistics)this.transport).sideTanks[data.output.ordinal()].fill(liquid, true);
			if(filled != liquid.amount) {
				liquid.amount -= filled;
				filled = ((PipeLiquidTransportLogistics)this.transport).internalTank.fill(liquid, true);
				if(filled != liquid.amount) {
					if(data.item instanceof IRoutedItem) {
						liquid.amount -= filled;
						IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(SimpleServiceLocator.logisticsLiquidManager.getLiquidContainer(liquid), worldObj);
						routedItem.setDestination(((IRoutedItem)data.item).getSource());
						routedItem.setTransportMode(TransportMode.Passive);
						this.queueRoutedItem(routedItem, data.output.getOpposite());
					}
				}
			}
		}
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
}

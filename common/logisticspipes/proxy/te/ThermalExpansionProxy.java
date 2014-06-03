package logisticspipes.proxy.te;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.VersionNotSupportedException;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import logisticspipes.recipes.CraftingDependency;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.RecipeManager.LocalCraftingManager;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thermalexpansion.block.TEBlocks;
import thermalexpansion.block.ender.TileTesseract;
import thermalexpansion.item.TEItems;
import thermalexpansion.part.conduit.ConduitBase;
import thermalexpansion.part.conduit.IConduit;
import thermalexpansion.part.conduit.PropsConduit;
import thermalexpansion.part.conduit.item.ConduitItem;
import thermalexpansion.part.conduit.item.ConduitItem.routeInfo;
import thermalexpansion.part.conduit.item.ItemRoute;
import cofh.api.energy.IEnergyHandler;
import cofh.api.transport.IEnderAttuned;
import cpw.mods.fml.common.Mod;

public class ThermalExpansionProxy implements IThermalExpansionProxy {

	public ThermalExpansionProxy() {
		if(Configs.TE_PIPE_SUPPORT) {
			//Check TE Version
			String TEVersion = null;
			try {
				TEVersion = Class.forName("thermalexpansion.ThermalExpansion").getAnnotation(Mod.class).version();
			} catch(Exception e) {
				e.printStackTrace();
			}
			String expectedTEVersion = "3.0.0.5";
			if(TEVersion != null) {
				if(!TEVersion.contains(expectedTEVersion)) {
					throw new VersionNotSupportedException("TE", TEVersion, expectedTEVersion, " when you have TE Conduit support enabled");
				}
			} else {
				LogisticsPipes.log.info("Couldn't check the TE Version.");
			}
			SimpleServiceLocator.pipeInformaitonManager.registerProvider(IConduit.class, TEPipeInformationProvider.class);
		}
	}
	
	@Override
	public boolean isTesseract(TileEntity tile) {
		return tile instanceof TileTesseract;
	}

	@Override
	public List<TileEntity> getConnectedTesseracts(TileEntity tile) {
		List<IEnderAttuned> interfaces = ((TileTesseract)tile).getValidItemOutputs();
	    List<TileEntity> validOutputs = new LinkedList<TileEntity>();
	    for (IEnderAttuned object: interfaces) {
	    	if(object instanceof TileEntity) {
	    		validOutputs.add((TileEntity) object);
	    	}
	    }
	    return validOutputs;
	}

	@Override
	public boolean isItemConduit(TileEntity tile) {
		if(tile instanceof IConduit) {
			return ((IConduit)tile).isItemConduit();
		}
		return false;
	}

	@Override
	public boolean isTE() {
		return true;
	}

	@Override
	public void handleLPInternalConduitChunkUnload(LogisticsTileGenericPipe pipe) {
		if(!Configs.TE_PIPE_SUPPORT) return;
		if(MainProxy.isClient(pipe.getWorldObj())) return;
		for(int i=0;i<6;i++) {
			LPConduitItem conduit = pipe.getTEConduit(i);
			if(conduit.gridBase != null) {
				conduit.tileUnloading();
				conduit.gridBase.removeConduit(conduit);
				conduit.setGrid(null);
			}
		}
	}

	@Override
	public void handleLPInternalConduitRemove(LogisticsTileGenericPipe pipe) {
		if(!Configs.TE_PIPE_SUPPORT) return;
		for(int i=0;i<6;i++) {
			LPConduitItem conduit = pipe.getTEConduit(i);
			conduit.onRemoved();
		}
	}

	@Override
	public void handleLPInternalConduitNeighborChange(LogisticsTileGenericPipe pipe) {
		if(!Configs.TE_PIPE_SUPPORT) return;
		for(int i=0;i<6;i++) {
			LPConduitItem conduit = pipe.getTEConduit(i);
			conduit.onNeighborChanged();
		}
	}

	@Override
	public void handleLPInternalConduitUpdate(LogisticsTileGenericPipe pipe) {
		if(!Configs.TE_PIPE_SUPPORT) return;
		for(int i=0;i<6;i++) {
			LPConduitItem conduit = pipe.getTEConduit(i);
			conduit.updateLPStatus();
		}
	}

	@Override
	public boolean insertIntoConduit(LPTravelingItemServer arrivingItem, TileEntity tile, CoreRoutedPipe pipe) {
		if(!Configs.TE_PIPE_SUPPORT) return false;
		if(MainProxy.isClient(pipe.getWorld())) return true;
		ConduitItem conduitItem = ((IConduit)tile).getConduitItem();
		return routeItem(conduitItem, arrivingItem.getItemIdentifierStack().makeNormalStack(), arrivingItem.getInfo(), arrivingItem.output);
	}
	
	private boolean routeItem(ConduitItem conduit, ItemStack stack, ItemRoutingInformation itemRoutingInformation, ForgeDirection dir) {
		if(!Configs.TE_PIPE_SUPPORT) return false;
		conduit.cacheRoutes();
		routeInfo curInfo = null;
		for(Iterator<ItemRoute> i = conduit.validOutputs.iterator(); i.hasNext();) {
			ItemRoute aRoute = (ItemRoute)i.next();
			if(itemRoutingInformation != null) {
				int result = doRouteRoutedLPItem(aRoute, curInfo, stack, conduit, dir, itemRoutingInformation);
				if(result != -1) return true;
			} else {
				int result = conduit.doRouteItem(aRoute, curInfo, stack, dir.ordinal());
				if(result != -1) return true;
			}
		}
		LPConduitItem.dontCheckRoutes = true;
		for(Iterator<ItemRoute> i = conduit.validOutputs.iterator(); i.hasNext();) {
			ItemRoute aRoute = (ItemRoute)i.next();
			if(itemRoutingInformation != null) {
				int result = doRouteRoutedLPItem(aRoute, curInfo, stack, conduit, dir, itemRoutingInformation);
				if(result != -1) {
					LPConduitItem.dontCheckRoutes = false;
					return true;
				}
			} else {
				int result = conduit.doRouteItem(aRoute, curInfo, stack, dir.ordinal());
				if(result != -1) {
					LPConduitItem.dontCheckRoutes = false;
					return true;
				}
			}
		}
		LPConduitItem.dontCheckRoutes = false;
		return false;
	}
	
	private int doRouteRoutedLPItem(ItemRoute aRoute, routeInfo curInfo, ItemStack theItem, ConduitItem conduit, ForgeDirection dir, ItemRoutingInformation itemRoutingInformation) {
		if(!Configs.TE_PIPE_SUPPORT) return -1;
		if(((ConduitBase)(aRoute.endPoint)).isNode) {
			ItemStack stack = theItem.copy();
			if(aRoute.endPoint instanceof LPConduitItem) {
				curInfo = ((LPConduitItem)aRoute.endPoint).canRouteLPItem(stack, itemRoutingInformation, aRoute);
			}
			if(curInfo != null && curInfo.canRoute) {
				theItem = theItem.copy();
				theItem.stackSize -= curInfo.stackSize;
				ItemRoute itemRoute = aRoute.copy();
				itemRoute.pathDirections.add(Byte.valueOf(curInfo.side));
				thermalexpansion.part.conduit.item.TravelingItem travelingItem = new thermalexpansion.part.conduit.item.TravelingItem(theItem, conduit.x(), conduit.y(), conduit.z(), itemRoute, dir.ordinal());
				travelingItem.routedLPInfo = itemRoutingInformation;
				conduit.insertItem(travelingItem);
				return curInfo.stackSize;
			}
		}
		return -1;
	}

	@Override
	public boolean isSideFree(TileEntity tile, int side) {
		if(!Configs.TE_PIPE_SUPPORT) return false;
		return ((IConduit)tile).getConduit().tile().occlusionTest(((IConduit)tile).getConduit().tile().partList(), PropsConduit.CONDUIT_OCCLUSION[side]);
	}

	@Override
	public int getMaxEnergyStored(TileEntity tile, ForgeDirection opposite) {
		return ((IEnergyHandler)tile).getMaxEnergyStored(opposite);
	}

	@Override
	public boolean isEnergyHandler(TileEntity tile) {
		return tile instanceof IEnergyHandler;
	}

	@Override
	public int getEnergyStored(TileEntity tile, ForgeDirection opposite) {
		return ((IEnergyHandler)tile).getEnergyStored(opposite);
	}

	@Override
	public boolean canInterface(TileEntity tile, ForgeDirection opposite) {
		return ((IEnergyHandler)tile).canInterface(opposite);
	}

	@Override
	public int receiveEnergy(TileEntity tile, ForgeDirection opposite, int i, boolean b) {
		return ((IEnergyHandler)tile).receiveEnergy(opposite, i, b);
	}

	@Override
	public void addCraftingRecipes() {
		LocalCraftingManager craftingManager = RecipeManager.craftingManager;
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_RF_SUPPLIER), CraftingDependency.Power_Distribution, new Object[] { 
			false, 
			"PEP", 
			"RBR", 
			"PTP", 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_TRANSPORTATION),
			Character.valueOf('P'), Items.paper,
			Character.valueOf('E'), new ItemStack(TEBlocks.blockDynamo, 1, 0), 
			Character.valueOf('T'), TEItems.powerCoilSilver, 
			Character.valueOf('R'), TEItems.powerCoilGold
		});
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_RF_POWERPROVIDER), CraftingDependency.Power_Distribution, new Object[] { 
			false, 
			"PEP", 
			"RBR", 
			"PTP", 
			Character.valueOf('B'), Blocks.redstone_block,
			Character.valueOf('P'), Items.paper,
			Character.valueOf('E'), new ItemStack(TEBlocks.blockDynamo, 1, 0), 
			Character.valueOf('T'), TEItems.powerCoilSilver, 
			Character.valueOf('R'), TEItems.powerCoilGold
		});
	}
}

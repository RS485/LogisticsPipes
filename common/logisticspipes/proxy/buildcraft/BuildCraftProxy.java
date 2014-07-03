/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeFluidBasic;
import logisticspipes.pipes.PipeFluidExtractor;
import logisticspipes.pipes.PipeFluidInsertion;
import logisticspipes.pipes.PipeFluidProvider;
import logisticspipes.pipes.PipeFluidRequestLogistics;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.pipes.PipeItemsApiaristAnalyser;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk2;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsProviderLogisticsMk2;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeLogisticsChassiMk1;
import logisticspipes.pipes.PipeLogisticsChassiMk2;
import logisticspipes.pipes.PipeLogisticsChassiMk3;
import logisticspipes.pipes.PipeLogisticsChassiMk4;
import logisticspipes.pipes.PipeLogisticsChassiMk5;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.fluid.LogisticsFluidConnectorPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.gates.ActionDisableLogistics;
import logisticspipes.proxy.buildcraft.gates.LogisticsTriggerProvider;
import logisticspipes.proxy.buildcraft.gates.TriggerCrafting;
import logisticspipes.proxy.buildcraft.gates.TriggerHasDestination;
import logisticspipes.proxy.buildcraft.gates.TriggerNeedsPower;
import logisticspipes.proxy.buildcraft.gates.TriggerSupplierFailed;
import logisticspipes.renderer.LogisticsPipeBlockRenderer;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.CoreConstants;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.TransportProxyClient;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.render.PipeRendererTESR;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class BuildCraftProxy {
	
	public static List<Item> pipelist = new ArrayList<Item>();

	public static ITrigger LogisticsFailedTrigger;
	public static ITrigger LogisticsCraftingTrigger;
	public static ITrigger LogisticsNeedPowerTrigger;
	public static ITrigger LogisticsHasDestinationTrigger;
	public static IAction LogisticsDisableAction;
	
	private Method canPipeConnect;
	
	public boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way) {
		return checkPipesConnections(from, to, way, false);
	}
	
	//TODO generalise more for TE support
	public boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way, boolean ignoreSystemDisconnection) {
		if(from instanceof TileGenericPipe && to instanceof TileGenericPipe && (((TileGenericPipe)from).pipe instanceof CoreRoutedPipe || ((TileGenericPipe)to).pipe instanceof CoreRoutedPipe)) {
			if(((TileGenericPipe)from).pipe instanceof CoreRoutedPipe) {
				if (!((CoreRoutedPipe)((TileGenericPipe)from).pipe).canPipeConnect(to, way, ignoreSystemDisconnection)) {
					return false;
				}
			} else {
				((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = true;
				if (!canPipeConnect((TileGenericPipe) from, to, way)) {
					((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = false;
					return false;
				}
				((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = false;
			}
			if(((TileGenericPipe)to).pipe instanceof CoreRoutedPipe) {
				if (!((CoreRoutedPipe)((TileGenericPipe) to).pipe).canPipeConnect(from, way.getOpposite(), ignoreSystemDisconnection)) {
					return false;
				}
			} else {
				((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = true;
				if (!canPipeConnect((TileGenericPipe) to, from, way.getOpposite())) {
					((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = false;
					return false;
				}
				((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = false;
			}
			return true;
		} else if(from instanceof TileGenericPipe && ((TileGenericPipe)from).pipe instanceof CoreRoutedPipe) {
			if (!((CoreRoutedPipe)((TileGenericPipe)from).pipe).canPipeConnect(to, way, ignoreSystemDisconnection)) {
				return false;
			}
			return true;
		} else if(to instanceof TileGenericPipe && ((TileGenericPipe)to).pipe instanceof CoreRoutedPipe) {
			if (!((CoreRoutedPipe)((TileGenericPipe) to).pipe).canPipeConnect(from, way.getOpposite(), ignoreSystemDisconnection)) {
				return false;
			}
			return true;
		} else {
			return Utils.checkPipesConnections(from, to);
		}
	}

	public boolean initProxyAndCheckVersion() {
		try {
			canPipeConnect = TileGenericPipe.class.getDeclaredMethod("canPipeConnect", new Class[]{TileEntity.class, ForgeDirection.class});
			canPipeConnect.setAccessible(true);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("You seem to have an outdated Buildcraft version. Please update to the newest BuildCraft version to run LogisticsPipes.");
		}
	}

	public boolean canPipeConnect(TileGenericPipe tile, TileEntity with, ForgeDirection side) {
		try {
			return (Boolean) canPipeConnect.invoke(tile, with, side);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void dropItems(World world, IInventory inventory, int x, int y, int z) {
		InvUtils.dropItems(world, inventory, x, y, z);
	}

	public void dropItems(World world, ItemStack stack, int x, int y, int z) {
		InvUtils.dropItems(world, stack, x, y, z);
	}
	
	//TODO
	/*
	 * Save all routing information into another field that is than added to the TravelingItem form BC (or TE)
	 * Don't extend the TravalingItem to store all information. Just extend it to link the new Routing information field (and save those informaiton).
	 */

	public void registerTrigger() {
		ActionManager.registerTriggerProvider(new LogisticsTriggerProvider());
		
		/* Triggers */
		LogisticsFailedTrigger = new TriggerSupplierFailed();
		LogisticsNeedPowerTrigger = new TriggerNeedsPower();
		LogisticsCraftingTrigger = new TriggerCrafting();
		LogisticsHasDestinationTrigger = new TriggerHasDestination();
		
		/* Actions */
		LogisticsDisableAction = new ActionDisableLogistics();
	}

	public void registerPipes(Side side) {
		LogisticsPipes.LogisticsBasicPipe = createPipe(PipeItemsBasicLogistics.class, "Basic Logistics Pipe", side);
		LogisticsPipes.LogisticsRequestPipeMk1 = createPipe(PipeItemsRequestLogistics.class, "Request Logistics Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMk1 = createPipe(PipeItemsProviderLogistics.class, "Provider Logistics Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMk1 = createPipe(PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe", side);
		LogisticsPipes.LogisticsSatellitePipe = createPipe(PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe", side);
		LogisticsPipes.LogisticsSupplierPipe = createPipe(PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe", side);
		LogisticsPipes.LogisticsChassisPipeMk1 = createPipe(PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1", side);
		LogisticsPipes.LogisticsChassisPipeMk2 = createPipe(PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2", side);
		LogisticsPipes.LogisticsChassisPipeMk3 = createPipe(PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3", side);
		LogisticsPipes.LogisticsChassisPipeMk4 = createPipe(PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4", side);
		LogisticsPipes.LogisticsChassisPipeMk5 = createPipe(PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5", side);
		LogisticsPipes.LogisticsCraftingPipeMk2 = createPipe(PipeItemsCraftingLogisticsMk2.class, "Crafting Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRequestPipeMk2 = createPipe(PipeItemsRequestLogisticsMk2.class, "Request Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRemoteOrdererPipe = createPipe(PipeItemsRemoteOrdererLogistics.class, "Remote Orderer Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMk2 = createPipe(PipeItemsProviderLogisticsMk2.class, "Provider Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsApiaristAnalyzerPipe = createPipe(PipeItemsApiaristAnalyser.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsApiaristSinkPipe = createPipe(PipeItemsApiaristSink.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsInvSysConPipe = createPipe(PipeItemsInvSysConnector.class, "Logistics Inventory System Connector", side);
		LogisticsPipes.LogisticsEntrancePipe = createPipe(PipeItemsSystemEntranceLogistics.class, "Logistics System Entrance Pipe", side);
		LogisticsPipes.LogisticsDestinationPipe = createPipe(PipeItemsSystemDestinationLogistics.class, "Logistics System Destination Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMk3 = createPipe(PipeItemsCraftingLogisticsMk3.class, "Crafting Logistics Pipe MK3", side);
		LogisticsPipes.LogisticsFirewallPipe = createPipe(PipeItemsFirewall.class, "Firewall Logistics Pipe", side);
		
		LogisticsPipes.LogisticsFluidSupplierPipeMk1 = createPipe(PipeItemsFluidSupplier.class, "Fluid Supplier Logistics Pipe", side);
		
		LogisticsPipes.LogisticsFluidConnectorPipe = createPipe(LogisticsFluidConnectorPipe.class, "Logistics Fluid Connector Pipe", side);
		LogisticsPipes.LogisticsFluidBasicPipe = createPipe(PipeFluidBasic.class, "Basic Logistics Fluid Pipe", side);
		LogisticsPipes.LogisticsFluidInsertionPipe = createPipe(PipeFluidInsertion.class, "Logistics Fluid Insertion Pipe", side);
		LogisticsPipes.LogisticsFluidProviderPipe = createPipe(PipeFluidProvider.class, "Logistics Fluid Provider Pipe", side);
		LogisticsPipes.LogisticsFluidRequestPipe = createPipe(PipeFluidRequestLogistics.class, "Logistics Fluid Request Pipe", side);
		LogisticsPipes.LogisticsFluidExtractorPipe = createPipe(PipeFluidExtractor.class, "Logistics Fluid Extractor Pipe", side);
		LogisticsPipes.LogisticsFluidSatellitePipe = createPipe(PipeFluidSatellite.class, "Logistics Fluid Satellite Pipe", side);
		LogisticsPipes.LogisticsFluidSupplierPipeMk2 = createPipe(PipeFluidSupplierMk2.class, "Logistics Fluid Supplier Pipe Mk2", side);
	
		LogisticsPipes.logisticsRequestTable = createPipe(PipeBlockRequestTable.class, "Request Table", side);
	}

	/**
	 * Registers a new logistics pipe with buildcraft. The buildcraft implementation does not allow for a new item
	 * implementation (only the block)
	 *
	 * @param key   buildcraft key for the pipe
	 * @param clas  Class name of the pipe block
	 * @return the pipe
	 */
	@SuppressWarnings("unchecked")
	public static ItemLogisticsPipe registerPipe(Class<? extends Pipe<?>> clas) {
		ItemLogisticsPipe item = new ItemLogisticsPipe();

		Map pipes = null;
		
		try {
			pipes = BlockGenericPipe.pipes;
		} catch(NoSuchFieldError e) {
			try {
				pipes = (Map) BlockGenericPipe.class.getDeclaredField("pipes").get(null);
			} catch (Exception e2) {
				return null;
			}
		}
		
		pipes.put(item, clas);

		Pipe<?> dummyPipe = BlockGenericPipe.createPipe(item);
		if (dummyPipe != null) {
			item.setPipeIconIndex(dummyPipe.getIconIndexForItem());
			MainProxy.proxy.setIconProviderFromPipe(item, dummyPipe);
		}

		return item;
	}
	
	protected Item createPipe(Class <? extends Pipe<?>> clas, String descr, Side side) {
		ItemLogisticsPipe res = registerPipe(clas);
		res.setCreativeTab(LogisticsPipes.LPCreativeTab);
		res.setUnlocalizedName(clas.getSimpleName());
		Pipe<?> pipe = BlockGenericPipe.createPipe(res);
		if(pipe instanceof CoreRoutedPipe) {
			res.setPipeIconIndex(((CoreRoutedPipe)pipe).getTextureType(ForgeDirection.UNKNOWN).normal);
		}
		
		if(side.isClient()) {
			if(pipe instanceof PipeBlockRequestTable) {
				MinecraftForgeClient.registerItemRenderer(res, new LogisticsPipeBlockRenderer());
			} else {
				MinecraftForgeClient.registerItemRenderer(res, TransportProxyClient.pipeItemRenderer);
			}
		}
		if(clas != PipeItemsBasicLogistics.class && clas != PipeFluidBasic.class) {
			registerShapelessResetRecipe(res,0,LogisticsPipes.LogisticsBasicPipe,0);
		}
		pipelist.add(res);
		GameRegistry.registerItem(res, res.getUnlocalizedName());
		return res;
	}
	
	protected void registerShapelessResetRecipe(Item fromItem, int fromData, Item toItem, int toData) {
		for(int j=1;j < 10; j++) {
			Object[] obj = new Object[j];
			for(int k=0;k<j;k++) {
				obj[k] = new ItemStack(fromItem, 1, toData);
			}
			CraftingManager.getInstance().addShapelessRecipe(new ItemStack(toItem, j, fromData), obj);
		}
	}

	//IToolWrench interaction
	public boolean isWrenchEquipped(EntityPlayer entityplayer) {
		return (entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench);
	}

	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z) {
		if ((entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))
			return ((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).canWrench(entityplayer, x, y, z);
		return false;
	}

	public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z) {
		if ((entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))
			((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);
	}


	public boolean isUpgradeManagerEquipped(EntityPlayer entityplayer) {
		return entityplayer != null && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsUpgradeManager;
	}
	
	public void resetItemRotation(PipeRendererTESR renderer) {
		try {
			Field f = PipeRendererTESR.class.getDeclaredField("dummyEntityItem");
			f.setAccessible(true);
			EntityItem item = (EntityItem) f.get(renderer);
			item.hoverStart = 0;
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		} catch(SecurityException e) {
			e.printStackTrace();
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformaitonManager.registerProvider(TileGenericPipe.class, BCPipeInformationProvider.class);
	}
	
	public boolean insertIntoBuildcraftPipe(TileEntity tile, LPTravelingItem item) {
		if(tile instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe)tile;
			if(BlockGenericPipe.isValid(pipe.pipe) && pipe.pipe.transport instanceof PipeTransportItems) {
				TravelingItem bcItem = null;
				if(item instanceof LPTravelingItemServer) {
					LPRoutedBCTravelingItem lpBCItem = new LPRoutedBCTravelingItem();
					lpBCItem.setRoutingInformation(((LPTravelingItemServer)item).getInfo());
					lpBCItem.saveToExtraNBTData();
					bcItem = lpBCItem;
				} else {
					//TODO is this needed ClientSide ?
					//bcItem = TravelingItem.make();
				}
				LPPosition p = new LPPosition(tile.xCoord + 0.5F, tile.yCoord + CoreConstants.PIPE_MIN_POS, tile.zCoord + 0.5F);
				if(item.output.getOpposite() == ForgeDirection.DOWN) {
					p.moveForward(item.output.getOpposite(), 0.24F);
				} else if(item.output.getOpposite() == ForgeDirection.UP) {
					p.moveForward(item.output.getOpposite(), 0.74F);
				} else {
					p.moveForward(item.output.getOpposite(), 0.49F);
				}
				bcItem.setPosition(p.getXD(), p.getYD(), p.getZD());
				bcItem.setSpeed(item.getSpeed());
				if(item.getItemIdentifierStack() != null) {
					bcItem.setItemStack(item.getItemIdentifierStack().makeNormalStack());
				}
				((PipeTransportItems)pipe.pipe.transport).injectItem(bcItem, item.output);
				return true;
			}
		}
		return false;
	}
}

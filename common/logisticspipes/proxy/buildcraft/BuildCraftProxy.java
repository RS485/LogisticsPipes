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

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.gates.ActionDisableLogistics;
import logisticspipes.gates.LogisticsTriggerProvider;
import logisticspipes.gates.TriggerCrafting;
import logisticspipes.gates.TriggerHasDestination;
import logisticspipes.gates.TriggerNeedsPower;
import logisticspipes.gates.TriggerSupplierFailed;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.logisticspipes.IRoutedItem;
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
import logisticspipes.renderer.LogisticsPipeBlockRenderer;
import logisticspipes.routing.RoutedEntityItem;
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
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.utils.Localization;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.TransportProxyClient;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.render.RenderPipe;
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

	public IRoutedItem GetOrCreateRoutedItem(TravelingItem itemData) {
		if (!isRoutedItem(itemData)){
			RoutedEntityItem newItem = new RoutedEntityItem(itemData);
			itemData = newItem;
			return newItem;
		}
		return (IRoutedItem) itemData; 
	}
	
	public boolean isRoutedItem(TravelingItem item) {
		return (item instanceof RoutedEntityItem);
	}
	
	public IRoutedItem GetRoutedItem(TravelingItem item) {
		return (IRoutedItem) item;
	}
	
	public IRoutedItem CreateRoutedItem(TravelingItem item) {
		RoutedEntityItem newItem = new RoutedEntityItem(item);
		return newItem;
	}

	public IRoutedItem CreateRoutedItem(TileEntity container, ItemStack payload) {
		TravelingItem entityItem = new TravelingItem( 0, 0, 0, payload);
		entityItem.setContainer(container);
		return CreateRoutedItem(entityItem);
	}

	public void registerTrigger() {
		ActionManager.registerTriggerProvider(new LogisticsTriggerProvider());
		
		/* Triggers */
		LogisticsFailedTrigger = new TriggerSupplierFailed(700);
		LogisticsNeedPowerTrigger = new TriggerNeedsPower(701);
		LogisticsCraftingTrigger = new TriggerCrafting(702);
		LogisticsHasDestinationTrigger = new TriggerHasDestination(703);
		
		/* Actions */
		LogisticsDisableAction = new ActionDisableLogistics(700);
	}

	public void registerLocalization() {
		Localization.addLocalization("/lang/logisticspipes/", "en_US");
	}

	public void registerPipes(Side side) {
		LogisticsPipes.LogisticsBasicPipe = createPipe(Configs.LOGISTICSPIPE_BASIC_ID, PipeItemsBasicLogistics.class, "Basic Logistics Pipe", side);
		LogisticsPipes.LogisticsRequestPipeMk1 = createPipe(Configs.LOGISTICSPIPE_REQUEST_ID, PipeItemsRequestLogistics.class, "Request Logistics Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMk1 = createPipe(Configs.LOGISTICSPIPE_PROVIDER_ID, PipeItemsProviderLogistics.class, "Provider Logistics Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMk1 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_ID, PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe", side);
		LogisticsPipes.LogisticsSatellitePipe = createPipe(Configs.LOGISTICSPIPE_SATELLITE_ID, PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe", side);
		LogisticsPipes.LogisticsSupplierPipe = createPipe(Configs.LOGISTICSPIPE_SUPPLIER_ID, PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe", side);
		LogisticsPipes.LogisticsChassisPipeMk1 = createPipe(Configs.LOGISTICSPIPE_CHASSI1_ID, PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1", side);
		LogisticsPipes.LogisticsChassisPipeMk2 = createPipe(Configs.LOGISTICSPIPE_CHASSI2_ID, PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2", side);
		LogisticsPipes.LogisticsChassisPipeMk3 = createPipe(Configs.LOGISTICSPIPE_CHASSI3_ID, PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3", side);
		LogisticsPipes.LogisticsChassisPipeMk4 = createPipe(Configs.LOGISTICSPIPE_CHASSI4_ID, PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4", side);
		LogisticsPipes.LogisticsChassisPipeMk5 = createPipe(Configs.LOGISTICSPIPE_CHASSI5_ID, PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5", side);
		LogisticsPipes.LogisticsCraftingPipeMk2 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_MK2_ID, PipeItemsCraftingLogisticsMk2.class, "Crafting Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRequestPipeMk2 = createPipe(Configs.LOGISTICSPIPE_REQUEST_MK2_ID, PipeItemsRequestLogisticsMk2.class, "Request Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRemoteOrdererPipe = createPipe(Configs.LOGISTICSPIPE_REMOTE_ORDERER_ID, PipeItemsRemoteOrdererLogistics.class, "Remote Orderer Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMk2 = createPipe(Configs.LOGISTICSPIPE_PROVIDER_MK2_ID, PipeItemsProviderLogisticsMk2.class, "Provider Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsApiaristAnalyzerPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_ANALYSER_ID, PipeItemsApiaristAnalyser.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsApiaristSinkPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_SINK_ID, PipeItemsApiaristSink.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsInvSysConPipe = createPipe(Configs.LOGISTICSPIPE_INVSYSCON_ID, PipeItemsInvSysConnector.class, "Logistics Inventory System Connector", side);
		LogisticsPipes.LogisticsEntrancePipe = createPipe(Configs.LOGISTICSPIPE_ENTRANCE_ID, PipeItemsSystemEntranceLogistics.class, "Logistics System Entrance Pipe", side);
		LogisticsPipes.LogisticsDestinationPipe = createPipe(Configs.LOGISTICSPIPE_DESTINATION_ID, PipeItemsSystemDestinationLogistics.class, "Logistics System Destination Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMk3 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_MK3_ID, PipeItemsCraftingLogisticsMk3.class, "Crafting Logistics Pipe MK3", side);
		LogisticsPipes.LogisticsFirewallPipe = createPipe(Configs.LOGISTICSPIPE_FIREWALL_ID, PipeItemsFirewall.class, "Firewall Logistics Pipe", side);
		
		LogisticsPipes.LogisticsFluidSupplierPipeMk1 = createPipe(Configs.LOGISTICSPIPE_LIQUIDSUPPLIER_ID, PipeItemsFluidSupplier.class, "Fluid Supplier Logistics Pipe", side);
		
		LogisticsPipes.LogisticsFluidConnectorPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_CONNECTOR, LogisticsFluidConnectorPipe.class, "Logistics Fluid Connector Pipe", side);
		LogisticsPipes.LogisticsFluidBasicPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_BASIC, PipeFluidBasic.class, "Basic Logistics Fluid Pipe", side);
		LogisticsPipes.LogisticsFluidInsertionPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_INSERTION, PipeFluidInsertion.class, "Logistics Fluid Insertion Pipe", side);
		LogisticsPipes.LogisticsFluidProviderPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_PROVIDER, PipeFluidProvider.class, "Logistics Fluid Provider Pipe", side);
		LogisticsPipes.LogisticsFluidRequestPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_REQUEST, PipeFluidRequestLogistics.class, "Logistics Fluid Request Pipe", side);
		LogisticsPipes.LogisticsFluidExtractorPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_EXTRACTOR, PipeFluidExtractor.class, "Logistics Fluid Extractor Pipe", side);
		LogisticsPipes.LogisticsFluidSatellitePipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_SATELLITE, PipeFluidSatellite.class, "Logistics Fluid Satellite Pipe", side);
		LogisticsPipes.LogisticsFluidSupplierPipeMk2 = createPipe(Configs.LOGISTICSPIPE_LIQUID_SUPPLIER_MK2, PipeFluidSupplierMk2.class, "Logistics Fluid Supplier Pipe Mk2", side);
	
		LogisticsPipes.logisticsRequestTable = createPipe(Configs.LOGISTICSPIPE_REQUEST_TABLE_ID, PipeBlockRequestTable.class, "Request Table", side);
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
	public static ItemPipe registerPipe(int key, Class<? extends Pipe> clas) {
		ItemPipe item = new ItemLogisticsPipe(key, clas);

		Map<Integer, Class<? extends Pipe>> pipes = null;
		
		try {
			pipes = BlockGenericPipe.pipes;
		} catch(NoSuchFieldError e) {
			try {
				pipes = (Map<Integer, Class<? extends Pipe>>) BlockGenericPipe.class.getDeclaredField("pipes").get(null);
			} catch (Exception e2) {
				return null;
			}
		}
		
		pipes.put(item.itemID, clas);

		Pipe dummyPipe = BlockGenericPipe.createPipe(item.itemID);
		if (dummyPipe != null) {
			item.setPipeIconIndex(dummyPipe.getIconIndexForItem());
			TransportProxy.proxy.setIconProviderFromPipe(item, dummyPipe);
		}

		return item;
	}
	
	protected Item createPipe(int defaultID, Class <? extends Pipe> clas, String descr, Side side) {
		ItemPipe res = registerPipe (defaultID, clas);
		res.setCreativeTab(LogisticsPipes.LPCreativeTab);
		res.setUnlocalizedName(clas.getSimpleName());
		Pipe pipe = BlockGenericPipe.createPipe(res.itemID);
		if(pipe instanceof CoreRoutedPipe) {
			res.setPipeIconIndex(((CoreRoutedPipe)pipe).getTextureType(ForgeDirection.UNKNOWN).normal);
		}
		
		if(side.isClient()) {
			if(pipe instanceof PipeBlockRequestTable) {
				MinecraftForgeClient.registerItemRenderer(res.itemID, new LogisticsPipeBlockRenderer());
			} else {
			MinecraftForgeClient.registerItemRenderer(res.itemID, TransportProxyClient.pipeItemRenderer);
		}
		}
		if(defaultID != Configs.LOGISTICSPIPE_BASIC_ID && defaultID != Configs.LOGISTICSPIPE_LIQUID_CONNECTOR) {
			registerShapelessResetRecipe(res,0,LogisticsPipes.LogisticsBasicPipe,0);
		}
		pipelist.add(res);
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
	
	public boolean checkMaxItems() {
		//TODO: where's this gone ....
		return true;// 		BuildCraftTransport.instance.maxItemsInPipes >= 1000;
	}
	
	public boolean isWrenchEquipped(EntityPlayer entityplayer) {
		return (entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench);
	}
	
	public boolean isUpgradeManagerEquipped(EntityPlayer entityplayer) {
		return entityplayer != null && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.LogisticsUpgradeManager.itemID;
	}
	
	public void resetItemRotation(RenderPipe renderer) {
		try {
			Field f = RenderPipe.class.getDeclaredField("dummyEntityItem");
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

	public void replaceBlockGenericPipe() {
		if(Block.blocksList[BuildCraftTransport.genericPipeBlock.blockID] == BuildCraftTransport.genericPipeBlock) {
			Block.blocksList[BuildCraftTransport.genericPipeBlock.blockID] = null;
			BuildCraftTransport.genericPipeBlock = new LogisticsBlockGenericPipe(BuildCraftTransport.genericPipeBlock.blockID);
		}
	}
}

/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft;

import java.util.ArrayList;
import java.util.List;

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
import logisticspipes.pipes.PipeItemsApiaristAnalyser;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsBuilderSupplierLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk2;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsProviderLogisticsMk2;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeLiquidBasic;
import logisticspipes.pipes.PipeLiquidInsertion;
import logisticspipes.pipes.PipeLiquidProvider;
import logisticspipes.pipes.PipeLiquidRequestLogistics;
import logisticspipes.pipes.PipeLogisticsChassiMk1;
import logisticspipes.pipes.PipeLogisticsChassiMk2;
import logisticspipes.pipes.PipeLogisticsChassiMk3;
import logisticspipes.pipes.PipeLogisticsChassiMk4;
import logisticspipes.pipes.PipeLogisticsChassiMk5;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.liquid.LogisticsLiquidConnectorPipe;
import logisticspipes.routing.RoutedEntityItem;
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
import buildcraft.api.gates.Action;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.Trigger;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.utils.Localization;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.EntityData;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TransportProxyClient;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

public class BuildCraftProxy {
	
	public static Class<? extends TileGenericPipe> logisticsTileGenericPipe = TileGenericPipe.class;

	public static List<Item> pipelist = new ArrayList<Item>();

	public static Trigger LogisticsFailedTrigger;
	public static Trigger LogisticsCraftingTrigger;
	public static Trigger LogisticsNeedPowerTrigger;
	public static Trigger LogisticsHasDestinationTrigger;
	public static Action LogisticsDisableAction;
	
	public boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way) {
		return checkPipesConnections(from, to, way, false);
	}
	
	public boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way, boolean ignoreSystemDisconnection) {
		//Yes, the direction in TileGenericPipe.isPipeConnected(tile, direction) is the reverse of where tile is as seen from this...
		if(from instanceof TileGenericPipe && to instanceof TileGenericPipe && (((TileGenericPipe)from).pipe instanceof CoreRoutedPipe || ((TileGenericPipe)to).pipe instanceof CoreRoutedPipe)) {
			if(((TileGenericPipe)from).pipe instanceof CoreRoutedPipe) {
				if (!((CoreRoutedPipe)((TileGenericPipe)from).pipe).isPipeConnected(to, way, ignoreSystemDisconnection)) {
					return false;
				}
			} else {
				((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = true;
				if (!((TileGenericPipe) from).isPipeConnected(to, way.getOpposite())) {
					((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = false;
					return false;
				}
				((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = false;
			}
			if(((TileGenericPipe)to).pipe instanceof CoreRoutedPipe) {
				if (!((CoreRoutedPipe)((TileGenericPipe) to).pipe).isPipeConnected(from, way.getOpposite(), ignoreSystemDisconnection)) {
					return false;
				}
			} else {
				((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = true;
				if (!((TileGenericPipe) to).isPipeConnected(from, way)) {
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

	public void dropItems(World world, IInventory inventory, int x, int y, int z) {
		Utils.dropItems(world, inventory, x, y, z);
	}

	public void dropItems(World world, ItemStack stack, int x, int y, int z) {
		Utils.dropItems(world, stack, x, y, z);
	}

	public IRoutedItem GetOrCreateRoutedItem(World worldObj, EntityData itemData) {
		if (!isRoutedItem(itemData.item)){
			RoutedEntityItem newItem = new RoutedEntityItem(worldObj, itemData.item);
			itemData.item = newItem;
			return newItem;
		}
		return (IRoutedItem) itemData.item; 
	}
	
	public boolean isRoutedItem(IPipedItem item) {
		return (item instanceof RoutedEntityItem);
	}
	
	public IRoutedItem GetRoutedItem(IPipedItem item) {
		return (IRoutedItem) item;
	}
	
	public IRoutedItem CreateRoutedItem(World worldObj, IPipedItem item) {
		RoutedEntityItem newItem = new RoutedEntityItem(worldObj, item);
		return newItem;
	}

	public IRoutedItem CreateRoutedItem(ItemStack payload, World worldObj) {
		EntityPassiveItem entityItem = new EntityPassiveItem(worldObj, 0, 0, 0, payload);
		return CreateRoutedItem(worldObj, entityItem);
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
		LogisticsPipes.LogisticsRequestPipe = createPipe(Configs.LOGISTICSPIPE_REQUEST_ID, PipeItemsRequestLogistics.class, "Request Logistics Pipe", side);
		LogisticsPipes.LogisticsProviderPipe = createPipe(Configs.LOGISTICSPIPE_PROVIDER_ID, PipeItemsProviderLogistics.class, "Provider Logistics Pipe", side);
		LogisticsPipes.LogisticsCraftingPipe = createPipe(Configs.LOGISTICSPIPE_CRAFTING_ID, PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe", side);
		LogisticsPipes.LogisticsSatellitePipe = createPipe(Configs.LOGISTICSPIPE_SATELLITE_ID, PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe", side);
		LogisticsPipes.LogisticsSupplierPipe = createPipe(Configs.LOGISTICSPIPE_SUPPLIER_ID, PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe", side);
		LogisticsPipes.LogisticsChassiPipe1 = createPipe(Configs.LOGISTICSPIPE_CHASSI1_ID, PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1", side);
		LogisticsPipes.LogisticsChassiPipe2 = createPipe(Configs.LOGISTICSPIPE_CHASSI2_ID, PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2", side);
		LogisticsPipes.LogisticsChassiPipe3 = createPipe(Configs.LOGISTICSPIPE_CHASSI3_ID, PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3", side);
		LogisticsPipes.LogisticsChassiPipe4 = createPipe(Configs.LOGISTICSPIPE_CHASSI4_ID, PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4", side);
		LogisticsPipes.LogisticsChassiPipe5 = createPipe(Configs.LOGISTICSPIPE_CHASSI5_ID, PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5", side);
		LogisticsPipes.LogisticsCraftingPipeMK2 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_MK2_ID, PipeItemsCraftingLogisticsMk2.class, "Crafting Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRequestPipeMK2 = createPipe(Configs.LOGISTICSPIPE_REQUEST_MK2_ID, PipeItemsRequestLogisticsMk2.class, "Request Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRemoteOrdererPipe = createPipe(Configs.LOGISTICSPIPE_REMOTE_ORDERER_ID, PipeItemsRemoteOrdererLogistics.class, "Remote Orderer Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMK2 = createPipe(Configs.LOGISTICSPIPE_PROVIDER_MK2_ID, PipeItemsProviderLogisticsMk2.class, "Provider Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsApiaristAnalyserPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_ANALYSER_ID, PipeItemsApiaristAnalyser.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsApiaristSinkPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_SINK_ID, PipeItemsApiaristSink.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsInvSysCon = createPipe(Configs.LOGISTICSPIPE_INVSYSCON_ID, PipeItemsInvSysConnector.class, "Logistics Inventory System Connector", side);
		LogisticsPipes.LogisticsEntrance = createPipe(Configs.LOGISTICSPIPE_ENTRANCE_ID, PipeItemsSystemEntranceLogistics.class, "Logistics System Entrance Pipe", side);
		LogisticsPipes.LogisticsDestination = createPipe(Configs.LOGISTICSPIPE_DESTINATION_ID, PipeItemsSystemDestinationLogistics.class, "Logistics System Destination Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMK3 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_MK3_ID, PipeItemsCraftingLogisticsMk3.class, "Crafting Logistics Pipe MK3", side);
		LogisticsPipes.LogisticsFirewall = createPipe(Configs.LOGISTICSPIPE_FIREWALL_ID, PipeItemsFirewall.class, "Firewall Logistics Pipe", side);
		
		LogisticsPipes.LogisticsBuilderSupplierPipe = createPipe(Configs.LOGISTICSPIPE_BUILDERSUPPLIER_ID, PipeItemsBuilderSupplierLogistics.class, "Builder Supplier Logistics Pipe", side);
		LogisticsPipes.LogisticsLiquidSupplierPipe = createPipe(Configs.LOGISTICSPIPE_LIQUIDSUPPLIER_ID, PipeItemsLiquidSupplier.class, "Liquid Supplier Logistics Pipe", side);
		
		if(LogisticsPipes.DEBUG) {
			LogisticsPipes.LogisticsLiquidConnector = createPipe(Configs.LOGISTICSPIPE_LIQUID_CONNECTOR, LogisticsLiquidConnectorPipe.class, "Logistics Liquid Connector Pipe", side);
			LogisticsPipes.LogisticsLiquidBasic = createPipe(Configs.LOGISTICSPIPE_LIQUID_BASIC, PipeLiquidBasic.class, "Basic Logistics Liquid Pipe", side);
			LogisticsPipes.LogisticsLiquidInsertion = createPipe(Configs.LOGISTICSPIPE_LIQUID_INSERTION, PipeLiquidInsertion.class, "Logistics Liquid Insertion Pipe", side);
			LogisticsPipes.LogisticsLiquidProvider = createPipe(Configs.LOGISTICSPIPE_LIQUID_PROVIDER, PipeLiquidProvider.class, "Logistics Liquid Provider Pipe", side);
			LogisticsPipes.LogisticsLiquidRequest = createPipe(Configs.LOGISTICSPIPE_LIQUID_REQUEST, PipeLiquidRequestLogistics.class, "Logistics Liquid Request Pipe", side);
		}
	}

	/**
	 * Registers a new logistics pipe with buildcraft. The buildcraft implementation does not allow for a new item
	 * implementation (only the block)
	 *
	 * @param key   buildcraft key for the pipe
	 * @param clas  Class name of the pipe block
	 * @return the pipe
	 */
	public static ItemPipe registerPipe(int key, Class<? extends Pipe> clas) {
		ItemPipe item = new ItemLogisticsPipe(key, clas);

		BlockGenericPipe.pipes.put(item.itemID, clas);

		Pipe dummyPipe = BlockGenericPipe.createPipe(item.itemID);
		if (dummyPipe != null) {
			item.setTextureFile(dummyPipe.getTextureFile());
			item.setTextureIndex(dummyPipe.getTextureIndexForItem());
		}

		return item;
	}
	
	protected Item createPipe(int defaultID, Class <? extends Pipe> clas, String descr, Side side) {
		ItemPipe res = registerPipe (defaultID, clas);
		
		Pipe pipe = BlockGenericPipe.createPipe(res.itemID);
		if(pipe instanceof CoreRoutedPipe) {
			res.setTextureIndex(((CoreRoutedPipe)pipe).getTextureType(ForgeDirection.UNKNOWN).normal);
		}
		
		if(side.isClient()) {
			LanguageRegistry.addName(res, descr);
			MinecraftForgeClient.registerItemRenderer(res.itemID, TransportProxyClient.pipeItemRenderer);
		}
		if(defaultID != Configs.LOGISTICSPIPE_BASIC_ID) {
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
		return BuildCraftTransport.maxItemsInPipes >= 1000;
	}
	
	public boolean isWrenchEquipped(EntityPlayer entityplayer) {
		return (entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench);
	}
	
	public boolean isUpgradeManagerEquipped(EntityPlayer entityplayer) {
		return entityplayer != null && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.LogisticsUpgradeManager.itemID;
	}
}

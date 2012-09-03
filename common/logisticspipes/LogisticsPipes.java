/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
/*
 
ChangeLog:
	TODO: 
		* REMOVE DEBUG CODE (always! ALL OF IT!)
		* 
		*
		*
  - 0.2.5
  	Bug Fixes:
  		* Fixed the delayed send by modules, hopefully this causes no other problems, let me know if it does!
  		* Another attempt at fixing the concurrent iterator modification crash, hopefully the last time.
  	New stuff:
  		* Added Provider Modules - Like provider pipes, but modules! 
  		* Added upgraded extractors - extracts once per second
   
  - 0.2.4
  	Bug Fixes:
  		* Repaired integration with AdditionalPipes (teleport pipes) 
  		* Fixed the bottom 3 slots of MK5 chassis to not be configurable
  		* Fixed chassis pipes not using the configured item ids
  		* 
  - 0.2.3
	New stuff:
		* Ported to MC 1.2.5
	Bug fixes:
		* Fixed a stackoverflow crash when itemstacks heading into a almost full default route couldn't fully fit

	Known issues:
		* There is probably still a ConcurrentModification crash in there somewhere, but I have not been able to reproduce it  
  		
  - 0.2.2
  	New stuff:
  		* Ported to MC 1.2.3
  		* (3.x ONLY) Added liquid supplier pipes
  		* Added chassis mk 5 - 8 module slots
  		* Added Minor "Easter Egg"
  		* 
  	Bug fixes:
  		* (2.x) Fixed 0 stack items being dropped after being buffered
  		* Fixed the concurrent collection modification exception when splitting itemstacks that should be re-routed
  		* Fixed Chassis pipes being disabled by the "disable pipe" gate action
  		* Chassis now check ISidedInventory when checking if items will fit (when placed on top of a furnace it will hold a maximum of one stack)
  	
  - 0.2.1
  
  	New Stuff:
  		* Added Terminus module. Identical in every aspect to the ItemSink, but has a lower priority, but still higher than a default route. I'm going to use this one on my recyclers/AMR
  		* Renamed ChestItemSink to Polymorphic ItemSink
  		* Added "sneaky" functionality to extractor modules. You can now specify from which direction they should extract, regardless of which direction it is actually connected.
  		* Items are now slowed down if being dropped from a logisticspipe.
  	Bug fixes:
  		* Speed is now correct. No more lightningspeed for everything. Default routed items should be slowest, passive items faster and requested items fastest.
  		* Items are sped up at the first pipe, as intended.

  - 0.2.0 (Requires BC 2.1.12 or 3.1.3)
  	
  	New Stuff:
  		* Yet again, massive massive internal rewrite of routing (different part though) so expect them bugs!
  		* (3.x ONLY) Added alternative recipe for the basic logistics pipe. Can now be crafter using 2 golden chipsets instead of golden gears.
  		* Logipipes will no longer instantly drop items it don't know where to send. It will now buffer them internally for 2 seconds and then retry.
  		* 	If the re-send also fails, the item will be dropped. Also items arriving at their destination but have nowhere to go from there will be instantly dropped
  		* 	This will hopefully fix the items being dropped on world loads.
  		* Repurposed the requester pipe (also changed recipe to 2 gold gears and basic pipe). It will now open the remote orderer gui.
  		* Requester pipe now also track item statistics over the last 20 minutes of play (resolution: once a minute). A graph can be viewed from the requester pipe GUi by pressing 'stats'. This info is not saved and is on a session basis only
  		* (3.x ONLY) Added alternative recipe for requester pipe using golden chipsets instead of golden gears
  		* Added chassis system
  		* 	Unlike "normal" logipipes a chassi pipe can hold 1-4 modules (see below). Each chassis pipe needs to be aligned with an IInventory (shift-click with wrench to change) indicated by the orange stripe.
  		* 	Chassis pipes will do a best-effort to avoid overflowing chests, and reroute as needed. It is however always a good idea to keep a default route in a non-overflowing chest 
  		* Added chassis modules (working on names)
  		* 	ItemSink module - Works like the basic logistics pipe (Internal prio: 1, Default route: 0)
  		* 		- Use: Sort stuff
  		* 	ChestItemSinkModule - Works like the basic logistics pipe, but without the need to configure it. Instead it will accept all items found in the aligned chest (Internal prio 1)
  		* 		- Use: Sort stuff
  		* 	Passive supplier - Works like the normal supplier, but will not activly request items, but it will keep a stock from passive item inflow (Internal prio 2)
  		* 		- Use: Keep x amount of items saved, before they go to the normal ItemSinks
  		* Additional modules will be added over time
  		* Slightly increased the extraction speed of suppliers
  		* Added different extraction modes to providers.
  		* 	Leave first stack, leave last stack, leave first & last and leave 1 item per stack
  		* Increased the extraction speed of crafting pipes to match the speed of suppliers
  		* Added extractor module - This module will extract 1 item (not stacks) every 5 seconds from the adjacent inventory. This respects normal pipe-side operations (ISpecialInventory/ISidedInventory). Items will not be extracted if there is no route (Ie a specific or default) present in the network
  		* Added quicksort module, this expensive, yet powerful, yet limited module will every 5 seconds pull all itemstacks from an inventory with size > 26. Only items that has a specific destination will be pulled, default routes does not count. It will not respect ISpecialInventory/ISidedinventory and will ruthlessly grab everything that has a route. 
  		*	The limits are there on purpose as I don't want to see this module as a general pull-stuff-out automation module. It is intended to be used in combination with a chest to allow you a place to dump stuff for sorting. Any other shenanigans will make me nerf it/remove it.
  		* Added 2 additional display modes to the order/requester GUI, you can now view Supply only or Craft only in addition to Both 
 
  	Bug fixes:
  		* Fixed NPE when breaking a logisticspipe that has incoming items
  		* (3.x ONLY) Adapt action code to new BC action code 


  - 0.1.8
  	New Stuff:
  		* (3.x ONLY) Added custom actions to all logisticspipes that will disable them. They will still route and finish what they were doing, but will not provide/craft/supply/accept new items. Actions are accessed through gates. 
  	Bug fixes:
  		* Added ModLoader.getPriorities() that will hopefully resolve any remaining loading order problems and some blank texture issues.
  		* Fixed several crashes related to removing logisticspipes while items are traveling
  	
  - 0.1.7
  	New Stuff:
  		* Massive refactoring of Routing code, expect bugs!
  		* (3.x ONLY) Builder supplier pipe, a pipe that automatically supplies attached builder with items needed for the current section
  		* Holding shift and scrolling the mouse wheel will now change pages in the orderer
  		* Added config settings to invert mouse wheel scrolling direction for both pages and request count
  		* (3.x ONLY) Supplier pipes should have a trigger for when they fail a request. This can be used with gates.
  	Bug fixes:
  		* Fixed crafting pipe GUI to not import when you click paint.
  	Other:
  		* Displaying the routes now requires holding shift (and without item equipped)
  		* Shift-rightclicking on a dummy stack with an empty hand will now double the size (up to 127)
  		* The remote orderer will now refresh item list on request, and will correctly not display items that have been reserved.
  		* The remote orderer gui will now only clear the selected item if clicked within the darker square (roughly, not pixel accurate)
  	
  - 0.1.6
  	Bug fixes:
  		* Fixed incompatability with NEI on shift-clicking in configuration GUIs
  		* Replaced client hang on crafting loops with a lag spike and error message
  		* Changed max stack size in configuration guis to 127 as that is the maximum stack size that can be saved to world file.
  		* 
  	
  - 0.1.5
 
 	New stuff:
 		* The provider can now be configured to provide all items EXCEPT the ones in the filter
 		* The crafting pipe can now import recipes. (ONLY works with automatic crafting tables)
 		* The supplier can now be configured to do partial requests. If configured for it, it will first try requesting the full amount. Failing that it will request half the missing items, then half of that and so on down to 1 
 	Bug fixes:
 		* Fixed an issue with logisticsmanager not using extras/leftovers properly
 		* Possible fix for nullpointerexception in sendFailed()
 		* Suppliers will no longer try to supply inventories with size 0 and are explicitly forbidden to supply redstone engines.
 	Other:
 		* You can now use the mouse wheel to change the requested number of items in the GUI for RemoteOrderer (experimental)
 		* The supplier will no longer supply while its GUI is open.
 
 - 0.1.4
 
 	New stuff:
 		* RemoteOrderer - Instead of a GUI for requester pipe you get a portable item. Point at any logisticspipe and you get your GUI. Lists all items and stock of all items in the current network that is connected to a provider. Items with a 0 are out of stock, but they might be able to be crafted. Items requested will be delivered to the logistics pipe you clicked on. Be aware that normal delivery rules apply. 1) Deliver to chest/inventory 2) Deliver to unrouted connection 3) Drop it 
 		* Basic logisticspipe GUI and crafting GUI converted to use fake items. Upgrading to this version will convert your current items to fake ones and you will not get them back.
 		* 
 	Bug fixes:
 		* Fixed nasty router bug (crash) due to lingering statics.
 		* 
 	Other:
 		* All requests will now favor providers/crafters closer (in terms of number of pipes) to the point of the request. Previously this was a bit undefined(random).
 		* Changed the Network Monitor sprite
 		* Replaced diamond with glowstone dust in the recipes for provider/requester/crafter pipes (yes, anyone playing legit may TMI exchange the appropriate ammount of glowstone to diamonds as compensation)
 		* Slightly increased the extraction speed of providers

 - 0.1.3
 
 	New stuff:
 		* Provider pipes now have a filter gui. Putting items in it will ONLY provide those items. If empty it will provide all available items
 		* Logisticspipes now detect and use additionalpipes' teleport pipes.

- 0.1.2
		
	New stuff:
		* Added Supplier pipe. Will try to keep an inventory supplied with the configured materials by requesting them from the network. Checks every 5 seconds 
			(Do not place a provider pipe on the same chest and same network. It will not work!)
		* Added network monitor. Using this on any logistic pipe will bring up some network statistics
		* Testing different a type of interface for supplier pipe. You do not need to use real items to configure suppliers.
		* Requester pipes and crafting pipes will now attempt to re-request any items that go missing
		* Added a network monitor to track some stats on the pipes.,
		* Some behind the scenes tech improvements to be closer to saving the routed items to the world file.

	Bug fixes:
		* Changed the route table calculations to lazy-load (on demand). Didn't see a point to completely 
			recalculate the route table for every single router whenever the network changed. This should hopefully remove 
			the massive lag spike some players are experiencing on maps with many pipes.
		* Fixed provider pipes so they now work with double chests.	
		* Fixed the speed that non-requested extras from crafting pipes are extracted at.
		* Fixed routing for items tagged with a destination that cannot be reached. It will now attempt to route it somewhere else or drop it. 
			This is to prevent items randomly flowing around the logistics network.
	
	Other:
		* Items going to satellite pipes are now directly routed there
		* Items crafted in furnace (or anything except crafting table) will now be picked up as soon as it is ready.
		* Obsidian pipes will now (in addition to iron pipes) separate two networks 
		* Routing is now much more strict. Items in logistics pipes will no longer sometimes default to buildcraft's standard way of directing items. 
			If a pipe can not find a valid destination it will be dropped.

  	Remaining Issues:
		* Its possible to cause circular recursion by have a crafting pipe craft A using B, at the same time as having another crafting pipe crafting B using A. This will crash the game
		* Multiple providers on same chest.
		* Supplier and provider connected to same chest and network.
		* 


 - 0.1.1
	New stuff:
		* Added support in the crafting pipe to most IInventories (furnaces, chests etc)
		* Added an output message stating why a request failed. Easier to know what you ran out of, easier for me to debug.
		* Added new pipe, satellite pipe, these can be connected to crafting pipes and, if enabled, will send incoming materials in the 3 rightmost slots to the satellite instead of through the crafting pipe.
		* 
 		
 	Bug fixes:
 		* Renamed zip file to solve issues on *nix (including Mac) when logisticspipes was loaded before buildcraft
 		* Renamed zip file to solve texture issues with other mods, only appearing when the other mod loaded after buildcraft, but before logisticspipes
 		* Did the right thing and added initiation of buildcraft, just in case this mod loads before buildcraft core.
 		* Fixed recipe for crafting pipe now its "dPd", d = Diamond, P =Basic Logistics Pipe 
 		* Routing no longer assumes that pipes next to each other actually are connected. (Cobblestone, smoothstone)
 		* Routing also makes additional checks to ensure that we don't try and route over waterproof or conductive pipes.
 		* Route painting has been moved to the same method that calculates the paths for routes making it more robust and accurate
 		* Remove debug code accidently left in provider pipes (to set time to day)
 		* Fixed bug where provider pipes extracts from connected pipes with inventory.
 		* Logisticsmanager will now properly make use of extra resources produced within a request
 		* Logisticsmanager will now make use of any extras from previous request that have not been crafted yet.
 		* Redid some routing logic. All routers now share a single map of all routers instead of having its own. Let me know how this works out
 		* Rightclicking on all all logisticspipes (except provider) will now allow placing of pipes
 		
 	Other:
 		* Redesigned craftingpipe gui
 		* Changed the default routed speed to 10x normal speed, up from 5x. Upgrading users will have to update their config file.
 		
 - 0.1.0
 - Alternate texture for routed exits
 - Performance related fix that caused recalculation of shortest even when paths had not changed.
 - Fixed bug that was could cause items to slow down if it was inputed at speeds higher than the speed the router would send them.
 - "Fixed" routing bug - network wasn't converging when loading world
 - Iron pipes separate network segments
 - Crafting, need specify input->output for logistics in gui (dynamic recepies)
 - Logs to modloader.txt which texture ids it gets from buildcraft

 
 
TODO later, maybe....
 - Status screen (in transit, waiting for craft, ready etc)
 - RoutedEntityItem, targetTile - specify which "chest" it should be delivered to
 - RoutedEntityItem, travel time
 - Change recipes to chip-sets in 3.0.0.0
 - Add in-game item for network management (turn on/off link detection, poke link detection etc) ?
 - Context sensitive textures. Flashing routers on deliveries?
 - Track deliveries / en route ?
 - Save stuff, like destinations
 - Texture improvement
 - Route liquids (in container)?
 - Persistance:
 	- Save logistics to file. Save coordinates so they can be resolved later. Also save items in transit and count them as not delivered
 - SMP:
	- Peering, transport other peoples items. Need hook to set owner of PassiveEntity
 */

package logisticspipes;

import java.lang.reflect.Method;

import logisticspipes.blocks.CraftingSignRenderer;
import logisticspipes.blocks.LogisticsSignBlock;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.config.Configs;
import logisticspipes.config.SolderingStationRecipes;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ILogisticsManager;
import logisticspipes.items.CraftingSignCreator;
import logisticspipes.items.ItemDisk;
import logisticspipes.items.ItemModule;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.main.ActionDisableLogistics;
import logisticspipes.main.LogisticsItem;
import logisticspipes.main.LogisticsManager;
import logisticspipes.main.LogisticsTriggerProvider;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.main.TriggerSupplierFailed;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketHandler;
import logisticspipes.pipes.PipeItemsApiaristAnalyser;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsBuilderSupplierLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk2;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsProviderLogisticsMk2;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeLogisticsChassiMk1;
import logisticspipes.pipes.PipeLogisticsChassiMk2;
import logisticspipes.pipes.PipeLogisticsChassiMk3;
import logisticspipes.pipes.PipeLogisticsChassiMk4;
import logisticspipes.pipes.PipeLogisticsChassiMk5;
import logisticspipes.proxy.buildcraft.BuildCraftProxy3;
import logisticspipes.proxy.forestry.ForestryProxy;
import logisticspipes.proxy.ic2.ElectricItemProxy;
import logisticspipes.proxy.interfaces.IElectricItemProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.proxy.recipeproviders.AutoWorkbench;
import logisticspipes.proxy.recipeproviders.RollingMachine;
import logisticspipes.routing.RouterManager;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.client.MinecraftForgeClient;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.Action;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.Trigger;
import buildcraft.core.ProxyCore;
import buildcraft.core.utils.Localization;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TransportProxyClient;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(name="Logistics Pipes", version="0.5.@(Build_Number)", useMetadata = false, modid = "LP|MAIN")
@NetworkMod(channels = {NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
public class LogisticsPipes {
	
	
	public static LogisticsPipes instance;

	//Log Requests
	public static boolean DisplayRequests;

	public static boolean DEBUG = "%DEBUG%".equals("%" + "DEBUG" + "%") || "%DEBUG%".equals("true");

	// Items
	public static Item LogisticsBasicPipe;
	public static Item LogisticsRequestPipe;
	public static Item LogisticsProviderPipe;
	public static Item LogisticsCraftingPipe;
	public static Item LogisticsSatellitePipe;
	public static Item LogisticsSupplierPipe;
	public static Item LogisticsBuilderSupplierPipe;
	public static Item LogisticsLiquidSupplierPipe;
	public static Item LogisticsChassiPipe1;
	public static Item LogisticsChassiPipe2;
	public static Item LogisticsChassiPipe3;
	public static Item LogisticsChassiPipe4;
	public static Item LogisticsChassiPipe5;
	public static Item LogisticsCraftingPipeMK2;
	public static Item LogisticsRequestPipeMK2;
	public static Item LogisticsProviderPipeMK2;
	public static Item LogisticsRemoteOrdererPipe;
	public static Item LogisticsApiaristAnalyserPipe;
	public static Item LogisticsApiaristSinkPipe;
	public static Item LogisticsInvSysCon;
	
	
	public static Item LogisticsNetworkMonitior;
	public static Item LogisticsRemoteOrderer;
	public static Item LogisticsCraftingSignCreator;
	public static ItemDisk LogisticsItemDisk;
	public static Item LogisticsItemCard;
	
	public static ItemModule ModuleItem;
	
	public static Trigger LogisticsFailedTrigger;
	
	public static Action LogisticsDisableAction;
	
	private Textures textures = new Textures();
	
	//Blocks
	Block logisticsSign;
	Block logisticsSolidBlock;
	
	@Deprecated
	public static ILogisticsManager logisticsManager = new LogisticsManager();
	
	/** Support for teleport pipes **/
	public static boolean teleportPipeDetected = false;
	public static Class<? extends Pipe> PipeItemTeleport;
	public static Method teleportPipeMethod;

	public LogisticsPipes() {
		SimpleServiceLocator.setBuildCraftProxy(new BuildCraftProxy3());
		instance = this;
		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setDirectConnectionManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManagerV2());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
	}
	
	public String getPriorities() {
		return "after:mod_BuildCraftCore;after:mod_BuildCraftTransport;after:mod_Forestry;after:mod_IC2";
	}
	
	@Init
	public void init(FMLInitializationEvent event) {
		textures.load(event);
		if(event.getSide().isClient()) {
			Localization.addLocalization("/lang/logisticspipes/", "en_US");
		}
		NetworkRegistry.instance().registerGuiHandler(LogisticsPipes.instance, new GuiHandler());
	}
	
	@PreInit
	public void LoadConfig(FMLPreInitializationEvent evt) {
		Configs.load();
	}
	
	@PostInit
	public void PostLoad(FMLPostInitializationEvent event) {
		if(Loader.isModLoaded("mod_Forestry")) {
			SimpleServiceLocator.setForestryProxy(new ForestryProxy());
		} else {
			//DummyProxy
			SimpleServiceLocator.setForestryProxy(new IForestryProxy() {
				@Override public boolean isBee(ItemStack item) {return false;}
				@Override public boolean isBee(ItemIdentifier item) {return false;}
				@Override public boolean isAnalysedBee(ItemStack item) {return false;}
				@Override public boolean isAnalysedBee(ItemIdentifier item) {return false;}
				@Override public boolean isTileAnalyser(TileEntity tile) {return false;}
				@Override public boolean forestryEnabled() {return false;}
				@Override public boolean isKnownAlleleId(String uid, World world) {return false;}
				@Override public String getAlleleName(String uid) {return "";}
				@Override public String getFirstAlleleId(ItemStack bee) {return "";}
				@Override public String getSecondAlleleId(ItemStack bee) {return "";}
				@Override public boolean isDrone(ItemStack bee) {return false;}
				@Override public boolean isFlyer(ItemStack bee) {return false;}
				@Override public boolean isPrincess(ItemStack bee) {return false;}
				@Override public boolean isQueen(ItemStack bee) {return false;}
				@Override public boolean isPurebred(ItemStack bee) {return false;}
				@Override public boolean isNocturnal(ItemStack bee) {return false;}
				@Override public boolean isPureNocturnal(ItemStack bee) {return false;}
				@Override public boolean isPureFlyer(ItemStack bee) {return false;}
				@Override public boolean isCave(ItemStack bee) {return false;}
				@Override public boolean isPureCave(ItemStack bee) {return false;}
				@Override public String getForestryTranslation(String input) {return input.substring(input.lastIndexOf(".") + 1).toLowerCase().replace("_", " ");}
				@Override public int getIconIndexForAlleleId(String id, int phase) {return 0;}
				@Override public int getColorForAlleleId(String id, int phase) {return 0;}
				@Override public int getRenderPassesForAlleleId(String id) {return 0;}
				@Override public void addCraftingRecipes() {}
				@Override public String getNextAlleleId(String uid) {return null;}
				@Override public String getPrevAlleleId(String uid) {return null;}
			});
		}
		if(Loader.isModLoaded("mod_IC2")) {
			SimpleServiceLocator.setElectricItemProxy(new ElectricItemProxy());
		} else {
			//DummyProxy
			SimpleServiceLocator.setElectricItemProxy(new IElectricItemProxy() {
				@Override public boolean isElectricItem(ItemStack stack) {return false;}
				@Override public int getCharge(ItemStack stack) {return 0;}
				@Override public int getMaxCharge(ItemStack stack) {return 0;}
				@Override public boolean isDischarged(ItemStack stack, boolean partial) {return false;}
				@Override public boolean isCharged(ItemStack stack, boolean partial) {return false;}
				@Override public boolean isDischarged(ItemStack stack, boolean partial, Item electricItem) {return false;}
				@Override public boolean isCharged(ItemStack stack, boolean partial, Item electricItem) {return false;}
				@Override public void addCraftingRecipes() {}
			});
		}

		try {
			PipeItemTeleport = (Class<? extends Pipe>) Class.forName("buildcraft.additionalpipes.pipes.PipeItemTeleport");
			//PipeItemTeleport = (Class<? extends Pipe>) Class.forName("net.minecraft.src.buildcraft.additionalpipes.pipes.PipeItemTeleport");
			teleportPipeMethod = PipeItemTeleport.getMethod("getConnectedPipes", boolean.class);
			teleportPipeDetected = true;
			ModLoader.getLogger().fine("Additional pipes detected, adding compatibility");

		} catch (Exception e) {
			try {
				//PipeItemTeleport = (Class<? extends Pipe>) Class.forName("buildcraft.additionalpipes.pipes.PipeItemTeleport");
				PipeItemTeleport = (Class<? extends Pipe>) Class.forName("net.minecraft.src.buildcraft.additionalpipes.pipes.PipeItemTeleport");
				teleportPipeMethod = PipeItemTeleport.getMethod("getConnectedPipes", boolean.class);
				teleportPipeDetected = true;
				ModLoader.getLogger().fine("Additional pipes detected, adding compatibility");

			} catch (Exception e1) {
				ModLoader.getLogger().fine("Additional pipes not detected: " + e1.getMessage());
			}
		}
		
		//BuildCraftCore.initialize();
		//BuildCraftTransport.initialize();
		//BuildCraftBuilders.initialize();
		//BuildCraftSilicon.initialize();
		
		LogisticsNetworkMonitior = new LogisticsItem(Configs.LOGISTICSNETWORKMONITOR_ID);
		LogisticsNetworkMonitior.setIconIndex(Textures.LOGISTICSNETWORKMONITOR_ICONINDEX);
		LogisticsNetworkMonitior.setItemName("networkMonitorItem");
		
		LogisticsItemCard = new LogisticsItem(Configs.ItemCardId);
		LogisticsItemCard.setIconIndex(Textures.LOGISTICSITEMCARD_ICONINDEX);
		LogisticsItemCard.setItemName("logisticsItemCard");
		//LogisticsItemCard.setTabToDisplayOn(CreativeTabs.tabRedstone);
		
		LogisticsRemoteOrderer = new RemoteOrderer(Configs.LOGISTICSREMOTEORDERER_ID);
		//LogisticsRemoteOrderer.setIconIndex(LOGISTICSREMOTEORDERER_ICONINDEX);
		LogisticsRemoteOrderer.setItemName("remoteOrdererItem");

		LogisticsCraftingSignCreator = new CraftingSignCreator(Configs.LOGISTICSCRAFTINGSIGNCREATOR_ID);
		LogisticsCraftingSignCreator.setIconIndex(Textures.LOGISTICSCRAFTINGSIGNCREATOR_ICONINDEX);
		LogisticsCraftingSignCreator.setItemName("CraftingSignCreator");

		LogisticsPipes.LogisticsFailedTrigger = new TriggerSupplierFailed(700);
		ActionManager.registerTriggerProvider(new LogisticsTriggerProvider());
		
		LogisticsPipes.LogisticsDisableAction = new ActionDisableLogistics(700);
		
		ModuleItem = new ItemModule(Configs.ItemModuleId);
		ModuleItem.setItemName("itemModule");
		ModuleItem.loadModules();
		
		LogisticsItemDisk = new ItemDisk(Configs.ItemDiskId);
		LogisticsItemDisk.setItemName("itemDisk");
		LogisticsItemDisk.setIconIndex(3);
		
		LogisticsBasicPipe = createPipe(Configs.LOGISTICSPIPE_BASIC_ID, PipeItemsBasicLogistics.class, "Basic Logistics Pipe", event.getSide());
		LogisticsRequestPipe = createPipe(Configs.LOGISTICSPIPE_REQUEST_ID, PipeItemsRequestLogistics.class, "Request Logistics Pipe", event.getSide());
		LogisticsProviderPipe = createPipe(Configs.LOGISTICSPIPE_PROVIDER_ID, PipeItemsProviderLogistics.class, "Provider Logistics Pipe", event.getSide());
		LogisticsCraftingPipe = createPipe(Configs.LOGISTICSPIPE_CRAFTING_ID, PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe", event.getSide());
		LogisticsSatellitePipe = createPipe(Configs.LOGISTICSPIPE_SATELLITE_ID, PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe", event.getSide());
		LogisticsSupplierPipe = createPipe(Configs.LOGISTICSPIPE_SUPPLIER_ID, PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe", event.getSide());
		LogisticsChassiPipe1 = createPipe(Configs.LOGISTICSPIPE_CHASSI1_ID, PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1", event.getSide());
		LogisticsChassiPipe2 = createPipe(Configs.LOGISTICSPIPE_CHASSI2_ID, PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2", event.getSide());
		LogisticsChassiPipe3 = createPipe(Configs.LOGISTICSPIPE_CHASSI3_ID, PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3", event.getSide());
		LogisticsChassiPipe4 = createPipe(Configs.LOGISTICSPIPE_CHASSI4_ID, PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4", event.getSide());
		LogisticsChassiPipe5 = createPipe(Configs.LOGISTICSPIPE_CHASSI5_ID, PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5", event.getSide());
		LogisticsCraftingPipeMK2 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_MK2_ID, PipeItemsCraftingLogisticsMk2.class, "Crafting Logistics Pipe MK2", event.getSide());
		LogisticsRequestPipeMK2 = createPipe(Configs.LOGISTICSPIPE_REQUEST_MK2_ID, PipeItemsRequestLogisticsMk2.class, "Request Logistics Pipe MK2", event.getSide());
		LogisticsRemoteOrdererPipe = createPipe(Configs.LOGISTICSPIPE_REMOTE_ORDERER_ID, PipeItemsRemoteOrdererLogistics.class, "Remote Orderer Pipe", event.getSide());
		LogisticsProviderPipeMK2 = createPipe(Configs.LOGISTICSPIPE_PROVIDER_MK2_ID, PipeItemsProviderLogisticsMk2.class, "Provider Logistics Pipe MK2", event.getSide());
		LogisticsApiaristAnalyserPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_ANALYSER_ID, PipeItemsApiaristAnalyser.class, "Apiarist Logistics Analyser Pipe", event.getSide());
		LogisticsApiaristSinkPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_SINK_ID, PipeItemsApiaristSink.class, "Apiarist Logistics Analyser Pipe", event.getSide());
		LogisticsInvSysCon = createPipe(Configs.LOGISTICSPIPE_INVSYSCON_ID, PipeItemsInvSysConnector.class, "Logistics Inventory System Connector", event.getSide());

		ModLoader.addName(LogisticsNetworkMonitior, "Network monitor");
		ModLoader.addName(LogisticsItemCard, "Logistics Item Card");
		ModLoader.addName(LogisticsRemoteOrderer, "Remote Orderer");
		ModLoader.addName(LogisticsCraftingSignCreator, "Crafting Sign Creator");
		ModLoader.addName(ModuleItem, "BlankModule");
		ModLoader.addName(LogisticsItemDisk, "Logistics Disk");
		
		/*
		LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE);
		*/
		
		LogisticsBuilderSupplierPipe = createPipe(Configs.LOGISTICSPIPE_BUILDERSUPPLIER_ID, PipeItemsBuilderSupplierLogistics.class, "Builder Supplier Logistics Pipe", event.getSide());
		LogisticsLiquidSupplierPipe = createPipe(Configs.LOGISTICSPIPE_LIQUIDSUPPLIER_ID, PipeItemsLiquidSupplier.class, "Liquid Supplier Logistics Pipe", event.getSide());
		
		CraftingManager craftingManager = CraftingManager.getInstance();
		craftingManager.addRecipe(new ItemStack(LogisticsBuilderSupplierPipe, 1), new Object[]{"iPy", Character.valueOf('i'), new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('y'), new ItemStack(Item.dyePowder, 1,11)});
		//craftingManager.addRecipe(new ItemStack(LogisticsNetworkMonitior, 1), new Object[] { "g g", " G ", " g ", Character.valueOf('g'), Item.ingotGold, Character.valueOf('G'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsLiquidSupplierPipe, 1), new Object[]{" B ", "lPl", " B ", Character.valueOf('l'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('B'), Item.bucketEmpty});
		craftingManager.addRecipe(new ItemStack(LogisticsRemoteOrderer, 1), new Object[] { "gg", "gg", "DD", Character.valueOf('g'), Block.glass, Character.valueOf('D'), BuildCraftCore.diamondGearItem});
		
		
		craftingManager.addRecipe(new ItemStack(LogisticsBasicPipe, 8), new Object[] { "grg", "GdG", "grg", Character.valueOf('g'), Block.glass, 
								   Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
								   Character.valueOf('d'), BuildCraftTransport.pipeItemsDiamond, 
								   Character.valueOf('r'), Block.torchRedstoneActive});
		craftingManager.addRecipe(new ItemStack(LogisticsBasicPipe, 8), new Object[] { "grg", "GdG", "grg", Character.valueOf('g'), Block.glass, 
								   Character.valueOf('G'), BuildCraftCore.goldGearItem,
								   Character.valueOf('d'), BuildCraftTransport.pipeItemsDiamond, 
								   Character.valueOf('r'), Block.torchRedstoneActive});

		craftingManager.addRecipe(new ItemStack(LogisticsProviderPipe, 1), new Object[] { "d", "P", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('d'), Item.lightStoneDust});
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingPipe, 1), new Object[] { "dPd", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('d'), Item.lightStoneDust});
		craftingManager.addRecipe(new ItemStack(LogisticsSatellitePipe, 1), new Object[] { "rPr", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('r'), Item.redstone});
		craftingManager.addRecipe(new ItemStack(LogisticsSupplierPipe, 1), new Object[] { "lPl", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('l'), new ItemStack(Item.dyePowder, 1, 4)});

		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipe, 1), new Object[] { "gPg", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('g'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipe, 1), new Object[] { "gPg", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('g'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsRequestPipe, Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsRequestPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsRequestPipe, Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});

		craftingManager.addRecipe(new ItemStack(LogisticsCraftingPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsCraftingPipe, Character.valueOf('U'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsCraftingPipe, Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(LogisticsRemoteOrdererPipe, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsBasicPipe, Character.valueOf('U'), Item.enderPearl});
		
		craftingManager.addRecipe(new ItemStack(LogisticsItemDisk, 1), new Object[] { "igi", "grg", "igi", Character.valueOf('i'), new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('r'), Item.redstone, Character.valueOf('g'), Item.goldNugget});
		
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.BLANK), new Object[] { "prp", "prp", "pgp", Character.valueOf('p'), Item.paper, Character.valueOf('r'), Item.redstone, Character.valueOf('g'), Item.goldNugget});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ITEMSINK), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 2),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ITEMSINK), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 2),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 1),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 1),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), Item.redstone});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
									Character.valueOf('U'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
									Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
									Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
									Character.valueOf('U'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 14),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 14),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.QUICKSORT), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.diamondGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.QUICKSORT), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.TERMINUS), new Object[] { "CGD", "rBr", "DrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 0),
									Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.TERMINUS), new Object[] { " G ", "rBr", "CrD", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 0),
									Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PROVIDER), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.goldGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		craftingManager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PROVIDER), new Object[] { " G ", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2), 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		for(int i=0; i<1000;i++) {
			ILogisticsModule module = ((ItemModule)ModuleItem).getModuleForItem(new ItemStack(ModuleItem, 1, i), null, null, null, null);
			if(module != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				boolean force = false;
				try {
					module.writeToNBT(nbt, "");
				} catch(Exception e) {
					force = true;
				}
				if(!nbt.equals(new NBTTagCompound())) {
					registerShapelessResetRecipe(ModuleItem, i, ModuleItem, i);
				}
			}
		}
		
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe1, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.redstone});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe1, 1), new Object[] { " i ","iPi", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 0)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe2, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.ingotIron});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe2, 1), new Object[] { " i ","iPi", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe3, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.ingotGold});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe3, 1), new Object[] { " i ","iPi", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe4, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.diamond});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe4, 1), new Object[] { " i ","iPi", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		craftingManager.addRecipe(new ItemStack(LogisticsChassiPipe5, 1), new Object[] { "gig", "iPi", "gig", Character.valueOf('P'), LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Block.blockDiamond, Character.valueOf('g'), Block.blockGold});

		craftingManager.addRecipe(new ItemStack(LogisticsNetworkMonitior, 1), new Object[] { "g g", " G ", " g ", Character.valueOf('g'), Item.ingotGold, Character.valueOf('G'), BuildCraftCore.goldGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsNetworkMonitior, 1), new Object[] { "g g", " G ", " g ", Character.valueOf('g'), Item.ingotGold, Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2)});
		
		craftingManager.addRecipe(new ItemStack(LogisticsRemoteOrderer, 1), new Object[] { "gg", "gg", "DD", Character.valueOf('g'), Block.glass, Character.valueOf('D'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsRemoteOrderer, 1), new Object[] { "gg", "gg", "DD", Character.valueOf('g'), Block.glass, Character.valueOf('D'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingSignCreator, 1), new Object[] {"G G", " S ", " D ", Character.valueOf('G'), BuildCraftCore.goldGearItem, Character.valueOf('S'), Item.sign, Character.valueOf('D'), BuildCraftCore.diamondGearItem});
		craftingManager.addRecipe(new ItemStack(LogisticsCraftingSignCreator, 1), new Object[] {"G G", " S ", " D ", Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2), Character.valueOf('S'), Item.sign, Character.valueOf('D'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3)});
		
		SimpleServiceLocator.electricItemProxy.addCraftingRecipes();
		SimpleServiceLocator.forestryProxy.addCraftingRecipes();
		SimpleServiceLocator.addCraftingRecipeProvider(new AutoWorkbench());
		if (RollingMachine.load())
			SimpleServiceLocator.addCraftingRecipeProvider(new RollingMachine());
		
		SolderingStationRecipes.loadRecipe();
		
		//Blocks
		logisticsSign = new LogisticsSignBlock(Configs.LOGISTICS_SIGN_ID);
		ModLoader.registerBlock(logisticsSign);
		logisticsSolidBlock = new LogisticsSolidBlock(Configs.LOGISTICS_SOLID_BLOCK_ID);
		ModLoader.registerBlock(logisticsSolidBlock, LogisticsSolidBlockItem.class);
		ModLoader.registerTileEntity(LogisticsSignTileEntity.class, "net.minecraft.src.buildcraft.logisticspipes.blocks.LogisticsTileEntiy", new CraftingSignRenderer());
		ModLoader.registerTileEntity(LogisticsSignTileEntity.class, "logisticspipes.blocks.LogisticsSignTileEntity", new CraftingSignRenderer());
		ModLoader.registerTileEntity(LogisticsSolderingTileEntity.class, "logisticspipes.blocks.LogisticsSolderingTileEntity");
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
	
	protected Item createPipe(int defaultID, Class <? extends Pipe> clas, String descr, Side side) {
		ItemPipe res =  BlockGenericPipe.registerPipe (defaultID, clas);
		res.setItemName(clas.getSimpleName());
		
		if(side.isClient()) {
			ProxyCore.proxy.addName(res, descr);
			MinecraftForgeClient.registerItemRenderer(res.shiftedIndex, TransportProxyClient.pipeItemRenderer);
		}
		if(defaultID != Configs.LOGISTICSPIPE_BASIC_ID) {
			registerShapelessResetRecipe(res,0,LogisticsPipes.LogisticsBasicPipe,0);
		}
		return res;
	}
}

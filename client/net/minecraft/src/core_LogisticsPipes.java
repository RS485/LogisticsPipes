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

package net.minecraft.src;

import java.io.File;
import java.lang.reflect.Method;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.GuiHandler;
import net.minecraft.src.buildcraft.krapht.IBuildCraftProxy;
import net.minecraft.src.buildcraft.krapht.ILogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsItem;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.logistics.LogisticsManagerV2;
import net.minecraft.src.buildcraft.krapht.network.ConnectionHandler;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsBasicLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsCraftingLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsCraftingLogisticsMK2;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsProviderLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogisticsMK2;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsSatelliteLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsSupplierLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassiMk1;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassiMk2;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassiMk3;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassiMk4;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassiMk5;
import net.minecraft.src.buildcraft.krapht.routing.RouterManager;
import net.minecraft.src.buildcraft.logisticspipes.ItemModule;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.NetworkMod;
import net.minecraft.src.forge.Property;
import net.minecraft.src.krapht.InventoryUtilFactory;

public abstract class core_LogisticsPipes extends NetworkMod {
	
	public static boolean DEBUG = false;
	
	//Items
	public static Item LogisticsBasicPipe;
	public static Item LogisticsRequestPipe;
	public static Item LogisticsProviderPipe;
	public static Item LogisticsCraftingPipe;
	public static Item LogisticsSatellitePipe;
	public static Item LogisticsSupplierPipe;
	public static Item LogisticsChassiPipe1;
	public static Item LogisticsChassiPipe2;
	public static Item LogisticsChassiPipe3;
	public static Item LogisticsChassiPipe4;
	public static Item LogisticsChassiPipe5;
	public static Item LogisticsCraftingPipeMK2;
	public static Item LogisticsRequestPipeMK2;
	
	
	public static Item LogisticsNetworkMonitior;
	public static Item LogisticsRemoteOrderer;
	
	public static Item ModuleItem;
	
	
	//Ids
	
	public static int ItemModuleId									= 6871;
	public static int LOGISTICSREMOTEORDERER_ID						= 6872;
	public static int LOGISTICSNETWORKMONITOR_ID					= 6873;
	
	public static int LOGISTICSPIPE_BASIC_ID						= 6874;
	public static int LOGISTICSPIPE_REQUEST_ID						= 6875;
	public static int LOGISTICSPIPE_PROVIDER_ID						= 6876;
	public static int LOGISTICSPIPE_CRAFTING_ID						= 6877;
	public static int LOGISTICSPIPE_SATELLITE_ID					= 6878;
	public static int LOGISTICSPIPE_SUPPLIER_ID						= 6879;
																	//6880 - 3.x BuilderSupplier
	public static int LOGISTICSPIPE_CHASSI1_ID						= 6881;
	public static int LOGISTICSPIPE_CHASSI2_ID						= 6882;
	public static int LOGISTICSPIPE_CHASSI3_ID						= 6883;
	public static int LOGISTICSPIPE_CHASSI4_ID						= 6884;
	public static int LOGISTICSPIPE_CHASSI5_ID						= 6885;
																	// 6886 - 3.x LiquidSupplier;
	public static int LOGISTICSPIPE_CRAFTING_MK2_ID					= 6887;
	public static int LOGISTICSPIPE_REQUEST_MK2_ID					= 6888;
	
	
	
	//Texture #
	
	public static final int LOGISTICSNETWORKMONITOR_ICONINDEX = 0 * 16 + 0;
	public static final int LOGISTICSREMOTEORDERER_ICONINDEX = 0 * 16 + 1;
	
	public static int LOGISTICSPIPE_TEXTURE							= 0;
	public static int LOGISTICSPIPE_PROVIDER_TEXTURE				= 0;
	public static int LOGISTICSPIPE_REQUESTER_TEXTURE				= 0;
	public static int LOGISTICSPIPE_CRAFTER_TEXTURE					= 0;
	public static int LOGISTICSPIPE_SATELLITE_TEXTURE				= 0;
	public static int LOGISTICSPIPE_SUPPLIER_TEXTURE				= 0;
	public static int LOGISTICSPIPE_ROUTED_TEXTURE					= 0;
	public static int LOGISTICSPIPE_NOTROUTED_TEXTURE				= 0;
	public static int LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE			= 0;
	public static int LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE		= 0;
	public static int LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE		= 0;
	public static int LOGISTICSPIPE_CHASSI1_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI2_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI3_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI4_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI5_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CRAFTERMK2_TEXTURE				= 0;
	public static int LOGISTICSPIPE_REQUESTERMK2_TEXTURE			= 0;
	
		
	//Texture files
	
	public static final String LOGISTICSITEMS_TEXTURE_FILE = "/net/minecraft/src/buildcraft/krapht/gui/item_textures.png";
	public static final String LOGISTICSACTIONTRIGGERS_TEXTURE_FILE = "/net/minecraft/src/buildcraft/krapht/gui/actiontriggers_textures.png";
	
	public static final String LOGISTICSPIPE_TEXTURE_FILE					= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipe.png";
	public static final String LOGISTICSPIPE_PROVIDER_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipeprovider.png";
	public static final String LOGISTICSPIPE_REQUESTER_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspiperequester.png";
	public static final String LOGISTICSPIPE_CRAFTER_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipecrafter.png";
	public static final String LOGISTICSPIPE_SATELLITE_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipesatellite.png";
	public static final String LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipesupplier.png";
	public static final String LOGISTICSPIPE_ROUTED_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspiperouted.png";
	public static final String LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipenotrouted.png";
	public static final String LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE		= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipechassirouted.png";
	public static final String LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE	= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipechassinotrouted.png";
	public static final String LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE	= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipechassidirection.png";
	public static final String LOGISTICSPIPE_CHASSI1_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipechassi1.png";
	public static final String LOGISTICSPIPE_CHASSI2_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipechassi2.png";
	public static final String LOGISTICSPIPE_CHASSI3_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipechassi3.png";
	public static final String LOGISTICSPIPE_CHASSI4_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipechassi4.png";
	public static final String LOGISTICSPIPE_CHASSI5_TEXTURE_FILE			= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipechassi5.png";
	public static final String LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE		= "/net/minecraft/src/buildcraft/krapht/gui/logisticspipecrafterMK2.png";
	public static final String LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE		= "/net/minecraft/src/buildcraft/krapht/gui/logisticspiperequesterMK2.png";
	
	//Configrables
	public static int LOGISTICS_DETECTION_LENGTH	= 50;
	public static int LOGISTICS_DETECTION_COUNT		= 100;
	public static int LOGISTICS_DETECTION_FREQUENCY = 20;
	public static boolean LOGISTICS_ORDERER_COUNT_INVERTWHEEL = false;
	public static boolean LOGISTICS_ORDERER_PAGE_INVERTWHEEL = false;
	
	public static final float LOGISTICS_ROUTED_SPEED_MULTIPLIER	= 20F;
	public static final float LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER = 10F;
	
	protected static Configuration configuration;
	
	/** stuff for testing **/
	
	@Deprecated
	public static ILogisticsManager logisticsManager = new LogisticsManager();
	
	/** Support for teleport pipes **/
	public static boolean teleportPipeDetected = false;
	public static Class<? extends Pipe> PipeItemTeleport;
	public static Method teleportPipeMethod;

	public core_LogisticsPipes() {
		SimpleServiceLocator.setRouterManager(new RouterManager());
		SimpleServiceLocator.setLogisticsManager(new LogisticsManagerV2());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
	}
	
	@Override
	public String getPriorities() {
		return "after:mod_BuildCraftCore;after:mod_BuildCraftTransport";
	}
	
	@Override
	public void modsLoaded() {
		super.modsLoaded();
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
		BuildCraftCore.initialize();
		BuildCraftTransport.initialize();
		
		File configFile = new File(CoreProxy.getBuildCraftBase(), "config/LogisticsPipes.cfg");
		configuration = new Configuration(configFile);
		configuration.load();
		
		Property logisticRemoteOrdererIdProperty = configuration.getOrCreateIntProperty("logisticsRemoteOrderer.id", Configuration.CATEGORY_ITEM, LOGISTICSREMOTEORDERER_ID);
		logisticRemoteOrdererIdProperty.comment = "The item id for the remote orderer";
		
		Property logisticNetworkMonitorIdProperty = configuration.getOrCreateIntProperty("logisticsNetworkMonitor.id", Configuration.CATEGORY_ITEM, LOGISTICSNETWORKMONITOR_ID);
		logisticNetworkMonitorIdProperty.comment = "The item id for the network monitor";
		
		Property logisticPipeIdProperty = configuration.getOrCreateIntProperty("logisticsPipe.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_BASIC_ID);
		logisticPipeIdProperty.comment = "The item id for the basic logistics pipe";
		
		Property logisticPipeRequesterIdProperty = configuration.getOrCreateIntProperty("logisticsPipeRequester.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_REQUEST_ID);
		logisticPipeRequesterIdProperty.comment = "The item id for the requesting logistics pipe";
		
		Property logisticPipeProviderIdProperty = configuration.getOrCreateIntProperty("logisticsPipeProvider.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_PROVIDER_ID);
		logisticPipeProviderIdProperty.comment = "The item id for the providing logistics pipe";
		
		Property logisticPipeCraftingIdProperty = configuration.getOrCreateIntProperty("logisticsPipeCrafting.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CRAFTING_ID);
		logisticPipeCraftingIdProperty.comment = "The item id for the crafting logistics pipe";

		Property logisticPipeSatelliteIdProperty = configuration.getOrCreateIntProperty("logisticsPipeSatellite.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_SATELLITE_ID);
		logisticPipeSatelliteIdProperty.comment = "The item id for the crafting satellite pipe";
		
		Property logisticPipeSupplierIdProperty = configuration.getOrCreateIntProperty("logisticsPipeSupplier.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_SUPPLIER_ID);
		logisticPipeSupplierIdProperty.comment = "The item id for the supplier pipe";
		
		Property logisticPipeChassi1IdProperty = configuration.getOrCreateIntProperty("logisticsPipeChassi1.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI1_ID);
		logisticPipeChassi1IdProperty.comment = "The item id for the chassi1";
		
		Property logisticPipeChassi2IdProperty = configuration.getOrCreateIntProperty("logisticsPipeChassi2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI2_ID);
		logisticPipeChassi2IdProperty.comment = "The item id for the chassi2";
		
		Property logisticPipeChassi3IdProperty = configuration.getOrCreateIntProperty("logisticsPipeChassi3.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI3_ID);
		logisticPipeChassi3IdProperty.comment = "The item id for the chassi3";
		
		Property logisticPipeChassi4IdProperty = configuration.getOrCreateIntProperty("logisticsPipeChassi4.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI4_ID);
		logisticPipeChassi4IdProperty.comment = "The item id for the chassi4";
		
		Property logisticPipeChassi5IdProperty = configuration.getOrCreateIntProperty("logisticsPipeChassi5.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CHASSI5_ID);
		logisticPipeChassi5IdProperty.comment = "The item id for the chassi5";

		Property logisticPipeCraftingMK2IdProperty = configuration.getOrCreateIntProperty("logisticsPipeCraftingMK2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_CRAFTING_MK2_ID);
		logisticPipeCraftingMK2IdProperty.comment = "The item id for the crafting logistics pipe MK2";
		
		Property logisticPipeRequesterMK2IdProperty = configuration.getOrCreateIntProperty("logisticsPipeRequesterMK2.id", Configuration.CATEGORY_ITEM, LOGISTICSPIPE_REQUEST_MK2_ID);
		logisticPipeRequesterMK2IdProperty.comment = "The item id for the requesting logistics pipe MK2";
		
		Property logisticModuleIdProperty = configuration.getOrCreateIntProperty("logisticsModules.id", Configuration.CATEGORY_ITEM, ItemModuleId);
		logisticModuleIdProperty.comment = "The item id for the modules";

		
		
		
		
		Property detectionLength = configuration.getOrCreateIntProperty("detectionLength", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_LENGTH);
		detectionLength.comment = "The maximum shortest length between logistics pipes. This is an indicator on the maxim depth of the recursion algorithm to discover logistics neighbours. A low value might use less CPU, a high value will allow longer pipe sections";
		
		Property detectionCount = configuration.getOrCreateIntProperty("detectionCount", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_COUNT);
		detectionCount.comment = "The maximum number of buildcraft pipees (including forks) between logistics pipes. This is an indicator of the maximum ammount of nodes the recursion algorithm will visit before giving up. As it is possible to fork a pipe connection using standard BC pipes the algorithm will attempt to discover all available destinations through that pipe. Do note that the logistics system will not interfere with the operation of non-logistics pipes. So a forked pipe will usually be sup-optimal, but it is possible. A low value might reduce CPU usage, a high value will be able to handle more complex pipe setups. If you never fork your connection between the logistics pipes this has the same meaning as detectionLength and the lower of the two will be used";
		
		Property detectionFrequency = configuration.getOrCreateIntProperty("detectionFrequency", Configuration.CATEGORY_GENERAL, LOGISTICS_DETECTION_FREQUENCY);
		detectionFrequency.comment = "The amount of time that passes between checks to see if it is still connected to its neighbours. A low value will mean that it will detect changes faster but use more CPU. A high value means detection takes longer, but CPU consumption is reduced. A value of 20 will check about every second";
		
		Property countInvertWheelProperty = configuration.getOrCreateBooleanProperty("ordererCountInvertWheel", Configuration.CATEGORY_GENERAL, LOGISTICS_ORDERER_COUNT_INVERTWHEEL);
		countInvertWheelProperty.comment = "Inverts the the mouse wheel scrolling for remote order number of items"; 
		
		Property pageInvertWheelProperty = configuration.getOrCreateBooleanProperty("ordererPageInvertWheel", Configuration.CATEGORY_GENERAL, LOGISTICS_ORDERER_PAGE_INVERTWHEEL);
		pageInvertWheelProperty.comment = "Inverts the the mouse wheel scrolling for remote order pages";
				
		configuration.save();
		
		LOGISTICSNETWORKMONITOR_ID		= Integer.parseInt(logisticNetworkMonitorIdProperty.value);
		LOGISTICSREMOTEORDERER_ID		= Integer.parseInt(logisticRemoteOrdererIdProperty.value);
		ItemModuleId					= Integer.parseInt(logisticModuleIdProperty.value);
		 
		LOGISTICSPIPE_BASIC_ID 			= Integer.parseInt(logisticPipeIdProperty.value);
		LOGISTICSPIPE_REQUEST_ID		= Integer.parseInt(logisticPipeRequesterIdProperty.value);
		LOGISTICSPIPE_PROVIDER_ID		= Integer.parseInt(logisticPipeProviderIdProperty.value);
		LOGISTICSPIPE_CRAFTING_ID		= Integer.parseInt(logisticPipeCraftingIdProperty.value);
		LOGISTICSPIPE_SATELLITE_ID		= Integer.parseInt(logisticPipeSatelliteIdProperty.value);
		LOGISTICSPIPE_SUPPLIER_ID		= Integer.parseInt(logisticPipeSupplierIdProperty.value);
		LOGISTICSPIPE_CHASSI1_ID		= Integer.parseInt(logisticPipeChassi1IdProperty.value);
		LOGISTICSPIPE_CHASSI2_ID		= Integer.parseInt(logisticPipeChassi2IdProperty.value);
		LOGISTICSPIPE_CHASSI3_ID		= Integer.parseInt(logisticPipeChassi3IdProperty.value);
		LOGISTICSPIPE_CHASSI4_ID		= Integer.parseInt(logisticPipeChassi4IdProperty.value);
		LOGISTICSPIPE_CHASSI5_ID		= Integer.parseInt(logisticPipeChassi5IdProperty.value);
		LOGISTICSPIPE_CRAFTING_MK2_ID	= Integer.parseInt(logisticPipeCraftingMK2IdProperty.value);
		LOGISTICSPIPE_REQUEST_MK2_ID	= Integer.parseInt(logisticPipeRequesterMK2IdProperty.value);
		LOGISTICS_DETECTION_LENGTH		= Integer.parseInt(detectionLength.value);
		LOGISTICS_DETECTION_COUNT		= Integer.parseInt(detectionCount.value);
		LOGISTICS_DETECTION_FREQUENCY 	= Math.max(Integer.parseInt(detectionFrequency.value), 1);
		LOGISTICS_ORDERER_COUNT_INVERTWHEEL = Boolean.parseBoolean(countInvertWheelProperty.value);
		LOGISTICS_ORDERER_PAGE_INVERTWHEEL = Boolean.parseBoolean(pageInvertWheelProperty.value);
		
		
		LogisticsNetworkMonitior = new LogisticsItem(LOGISTICSNETWORKMONITOR_ID);
		LogisticsNetworkMonitior.setIconIndex(LOGISTICSNETWORKMONITOR_ICONINDEX);
		LogisticsNetworkMonitior.setItemName("networkMonitorItem");
		
		LogisticsRemoteOrderer = new LogisticsItem(LOGISTICSREMOTEORDERER_ID);
		LogisticsRemoteOrderer.setIconIndex(LOGISTICSREMOTEORDERER_ICONINDEX);
		LogisticsRemoteOrderer.setItemName("remoteOrdererItem");
		
		ModuleItem						= new ItemModule(ItemModuleId).setItemName("itemModule");
		
		LOGISTICSPIPE_TEXTURE 			= CoreProxy.addCustomTexture(LOGISTICSPIPE_TEXTURE_FILE);
		LOGISTICSPIPE_PROVIDER_TEXTURE 	= CoreProxy.addCustomTexture(LOGISTICSPIPE_PROVIDER_TEXTURE_FILE);
		LOGISTICSPIPE_REQUESTER_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_REQUESTER_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTER_TEXTURE	= CoreProxy.addCustomTexture(LOGISTICSPIPE_CRAFTER_TEXTURE_FILE);
		LOGISTICSPIPE_ROUTED_TEXTURE 	= CoreProxy.addCustomTexture(LOGISTICSPIPE_ROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_NOTROUTED_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_SATELLITE_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_SATELLITE_TEXTURE_FILE);
		LOGISTICSPIPE_SUPPLIER_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK2_TEXTURE	= CoreProxy.addCustomTexture(LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_REQUESTERMK2_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE);
		
		LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI1_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_CHASSI1_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI2_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_CHASSI2_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI3_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_CHASSI3_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI4_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_CHASSI4_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI5_TEXTURE = CoreProxy.addCustomTexture(LOGISTICSPIPE_CHASSI5_TEXTURE_FILE);

		
		LogisticsBasicPipe = createPipe(LOGISTICSPIPE_BASIC_ID, PipeItemsBasicLogistics.class, "Basic Logistics Pipe");
		LogisticsRequestPipe = createPipe(LOGISTICSPIPE_REQUEST_ID, PipeItemsRequestLogistics.class, "Request Logistics Pipe");
		LogisticsProviderPipe = createPipe(LOGISTICSPIPE_PROVIDER_ID, PipeItemsProviderLogistics.class, "Provider Logistics Pipe");
		LogisticsCraftingPipe = createPipe(LOGISTICSPIPE_CRAFTING_ID, PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe");
		LogisticsSatellitePipe = createPipe(LOGISTICSPIPE_SATELLITE_ID, PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe");
		LogisticsSupplierPipe = createPipe(LOGISTICSPIPE_SUPPLIER_ID, PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe");
		LogisticsChassiPipe1 = createPipe(LOGISTICSPIPE_CHASSI1_ID, PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1");
		LogisticsChassiPipe2 = createPipe(LOGISTICSPIPE_CHASSI2_ID, PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2");
		LogisticsChassiPipe3 = createPipe(LOGISTICSPIPE_CHASSI3_ID, PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3");
		LogisticsChassiPipe4 = createPipe(LOGISTICSPIPE_CHASSI4_ID, PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4");
		LogisticsChassiPipe5 = createPipe(LOGISTICSPIPE_CHASSI5_ID, PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5");
		LogisticsCraftingPipeMK2 = createPipe(LOGISTICSPIPE_CRAFTING_MK2_ID, PipeItemsCraftingLogisticsMK2.class, "Crafting Logistics Pipe MK2");
		LogisticsRequestPipeMK2 = createPipe(LOGISTICSPIPE_REQUEST_MK2_ID, PipeItemsRequestLogisticsMK2.class, "Request Logistics Pipe MK2");
		
		ModLoader.addName(LogisticsNetworkMonitior, "Network monitor");
		ModLoader.addName(LogisticsRemoteOrderer, "Remote Orderer");
		ModLoader.addName(ModuleItem, "BlankModule");
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		craftingmanager.addRecipe(new ItemStack(LogisticsBasicPipe, 8), new Object[] { "grg", "GdG", "grg", Character.valueOf('g'), Block.glass, 
																									   Character.valueOf('G'), BuildCraftCore.goldGearItem,
																									   Character.valueOf('d'), BuildCraftTransport.pipeItemsDiamond, 
																									   Character.valueOf('r'), Block.torchRedstoneActive});
		
		craftingmanager.addRecipe(new ItemStack(LogisticsRequestPipe, 1), new Object[] { "gPg", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('g'), BuildCraftCore.goldGearItem}); 
		craftingmanager.addRecipe(new ItemStack(LogisticsProviderPipe, 1), new Object[] { "d", "P", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('d'), Item.lightStoneDust});
		craftingmanager.addRecipe(new ItemStack(LogisticsCraftingPipe, 1), new Object[] { "dPd", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('d'), Item.lightStoneDust});
		craftingmanager.addRecipe(new ItemStack(LogisticsSatellitePipe, 1), new Object[] { "rPr", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('r'), Item.redstone});
		craftingmanager.addRecipe(new ItemStack(LogisticsSupplierPipe, 1), new Object[] { "lPl", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('l'), new ItemStack(Item.dyePowder, 1, 4)});
			
		craftingmanager.addRecipe(new ItemStack(LogisticsChassiPipe1, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.redstone});
		craftingmanager.addRecipe(new ItemStack(LogisticsChassiPipe2, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.ingotIron});
		craftingmanager.addRecipe(new ItemStack(LogisticsChassiPipe3, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.ingotGold});
		craftingmanager.addRecipe(new ItemStack(LogisticsChassiPipe4, 1), new Object[] { "iii", "iPi", "iii", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Item.diamond});
		craftingmanager.addRecipe(new ItemStack(LogisticsChassiPipe5, 1), new Object[] { "gig", "iPi", "gig", Character.valueOf('P'), core_LogisticsPipes.LogisticsBasicPipe, Character.valueOf('i'), Block.blockDiamond, Character.valueOf('g'), Block.blockGold});
		
		craftingmanager.addRecipe(new ItemStack(LogisticsCraftingPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsCraftingPipe, Character.valueOf('U'), BuildCraftCore.goldGearItem});
		craftingmanager.addRecipe(new ItemStack(LogisticsRequestPipeMK2, 1), new Object[] {"U", "B", Character.valueOf('B'), LogisticsRequestPipe, Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		
		craftingmanager.addRecipe(new ItemStack(LogisticsNetworkMonitior, 1), new Object[] { "g g", " G ", " g ", Character.valueOf('g'), Item.ingotGold, Character.valueOf('G'), BuildCraftCore.goldGearItem});
		craftingmanager.addRecipe(new ItemStack(LogisticsRemoteOrderer, 1), new Object[] { "gg", "gg", "DD", Character.valueOf('g'), Block.glass, Character.valueOf('D'), BuildCraftCore.diamondGearItem});
		
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.BLANK), new Object[] { "prp", "prp", "pgp", Character.valueOf('p'), Item.paper, Character.valueOf('r'), Item.redstone, Character.valueOf('g'), Item.goldNugget});
		
		//ItemSink
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ITEMSINK), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 2),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		//Passive supplier
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PASSIVE_SUPPLIER), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 1),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//Extractor module
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		//Advanced Extractor module
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), Item.redstone});
		
		//ChestItemSink
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.POLYMORPHIC_ITEMSINK), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 14),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//QuickSort
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.QUICKSORT), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.diamondGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		//Terminus
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.TERMINUS), new Object[] { "CGD", "rBr", "DrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 0),
									Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5),
									Character.valueOf('G'), BuildCraftCore.ironGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});

		//PASSIVE MK 2
		//Extractor module MK 2
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR),
									Character.valueOf('U'), BuildCraftCore.goldGearItem});
		
		//Advanced Extractor module MK 2
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR),
									Character.valueOf('U'), BuildCraftCore.goldGearItem});
		

		//PASSIVE MK 3
		//Extractor module MK 3
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.EXTRACTOR_MK2),
									Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		
		//Advanced Extractor module MK 3
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK3), new Object[] {"U", "B",
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.ADVANCED_EXTRACTOR_MK2),
									Character.valueOf('U'), BuildCraftCore.diamondGearItem});
		

		//ACTIVE
		
		//Supplier module
		craftingmanager.addRecipe(new ItemStack(ModuleItem, 1, ItemModule.PROVIDER), new Object[] { "CGC", "rBr", "CrC", 
									Character.valueOf('C'), new ItemStack(Item.dyePowder, 1, 4),
									Character.valueOf('G'), BuildCraftCore.goldGearItem, 
									Character.valueOf('r'), Item.redstone, 
									Character.valueOf('B'), new ItemStack(ModuleItem, 1, ItemModule.BLANK)});
		
		
		//ModuleSupplier in separate files

		
		
		
		if (core_LogisticsPipes.DEBUG) {
			craftingmanager.addRecipe(new ItemStack(LogisticsRemoteOrderer, 1), new Object[] {"dd", "dd", Character.valueOf('d'), Block.dirt});
			craftingmanager.addRecipe(new ItemStack(LogisticsNetworkMonitior, 1), new Object[] { "d d", " d ", Character.valueOf('d'), Block.dirt});
			craftingmanager.addRecipe(new ItemStack(LogisticsBasicPipe, 64), new Object[] { "wd", Character.valueOf('w'), Block.wood, Character.valueOf('d'), Block.dirt}); 
			craftingmanager.addRecipe(new ItemStack(LogisticsRequestPipe, 64), new Object[] {"ww", Character.valueOf('w'), Block.wood});
			craftingmanager.addRecipe(new ItemStack(LogisticsProviderPipe, 64), new Object[] {"dd", Character.valueOf('d'), Block.dirt});
			craftingmanager.addRecipe(new ItemStack(LogisticsCraftingPipe, 64), new Object[] {"ddd", Character.valueOf('d'), Block.dirt});
			craftingmanager.addRecipe(new ItemStack(LogisticsSatellitePipe, 64), new Object[] {"d","d", Character.valueOf('d'), Block.dirt});
			craftingmanager.addRecipe(new ItemStack(LogisticsSupplierPipe, 64), new Object[] { "d", "d", "d", Character.valueOf('d'), Block.dirt});
			craftingmanager.addRecipe(new ItemStack(Item.diamond, 39), new Object[] {"s", Character.valueOf('s'), Block.sand});
		}
	}
	
	protected static Item createPipe (int defaultID, Class <? extends Pipe> clas, String descr) {
//		String name = Character.toLowerCase(clas.getSimpleName().charAt(0))
//				+ clas.getSimpleName().substring(1);
		
		Item res =  BlockGenericPipe.registerPipe (defaultID, clas);
		res.setItemName(clas.getSimpleName());
		CoreProxy.addName(res, descr);
		MinecraftForgeClient.registerItemRenderer(res.shiftedIndex, mod_BuildCraftTransport.instance);
	
		return res;
	}

	@Override
	public void load() {
		MinecraftForge.registerConnectionHandler(new ConnectionHandler());
		
		MinecraftForge.setGuiHandler(this,new GuiHandler());
		
		MinecraftForgeClient.preloadTexture(LOGISTICSITEMS_TEXTURE_FILE);
		MinecraftForgeClient.preloadTexture(LOGISTICSACTIONTRIGGERS_TEXTURE_FILE);
		
//		
//		MinecraftForgeClient.preloadTexture(LOGISTICSPIPE_TEXTURE_FILE);
//		MinecraftForgeClient.preloadTexture(LOGISTICSPIPE_PROVIDER_TEXTURE_FILE);
//		MinecraftForgeClient.preloadTexture(LOGISTICSPIPE_REQUESTER_TEXTURE_FILE);
//		MinecraftForgeClient.preloadTexture(LOGISTICSPIPE_CRAFTER_TEXTURE_FILE);
//		MinecraftForgeClient.preloadTexture(LOGISTICSPIPE_SATELLITE_TEXTURE_FILE);
//		MinecraftForgeClient.preloadTexture(LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE);
//		MinecraftForgeClient.preloadTexture(LOGISTICSPIPE_ROUTED_TEXTURE_FILE);
//		MinecraftForgeClient.preloadTexture(LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE);
	}
}

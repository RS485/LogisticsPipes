package logisticspipes.textures;

import buildcraft.api.core.IIconProvider;
import logisticspipes.items.LogisticsItem;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLStateEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Textures {
	private int index = 0;
	private static TextureType empty = new TextureType();
	static {
		empty.normal = 0;
		empty.powered = 0;
		empty.unpowered = 0;
	}
	public Textures()
	{
		LPactionIconProvider = new LPActionTriggerIconProvider();
	}
	
	public class dummyIconProvider implements IIconProvider {

		@Override
		@SideOnly(Side.CLIENT)
		public Icon getIcon(int iconIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void registerIcons(IconRegister iconRegister) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static TextureType LOGISTICSPIPE_TEXTURE							= empty;
	public static TextureType LOGISTICSPIPE_PROVIDER_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_REQUESTER_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_CRAFTER_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_SATELLITE_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_SUPPLIER_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_ROUTED_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_NOTROUTED_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_POWERED_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_SECURITY_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_LIQUID_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE		= empty;
	public static TextureType LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE		= empty;
	public static TextureType LOGISTICSPIPE_CHASSI1_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI2_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI3_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI4_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI5_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CRAFTERMK2_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_REQUESTERMK2_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_PROVIDERMK2_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE		= empty;
	public static TextureType LOGISTICSPIPE_APIARIST_SINK_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_INVSYSCON_CON_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_ENTRANCE_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_DESTINATION_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_CRAFTERMK3_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_FIREWALL_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_LIQUID_BASIC					= empty;
	public static TextureType LOGISTICSPIPE_LIQUID_INSERTION				= empty;
	public static TextureType LOGISTICSPIPE_LIQUID_PROVIDER					= empty;
	public static TextureType LOGISTICSPIPE_LIQUID_REQUEST					= empty;
	
	public static int LOGISTICSPIPE_LIQUID_CONNECTOR						= 0;
	public static Icon LOGISTICSACTIONTRIGGERS_DISABLED ;
	public static Icon LOGISTICSACTIONTRIGGERS_CRAFTING_ICON;
	public static Icon LOGISTICSACTIONTRIGGERS_TEXTURE_FILE;
	public static Icon LOGISTICSACTIONTRIGGERS_NEEDS_POWER_ICON;
	public static Icon LOGISTICSACTIONTRIGGERS_SUPPLIER_FAILED_ICON;
	public static Icon[] LOGISTICS_UPGRADES_DISCONECT_ICONINDEX;
	public static Icon[] LOGISTICS_UPGRADES_SNEAKY_ICONINDEX;
	public static Icon[] LOGISTICS_UPGRADES_ICONINDEX;
	public static Icon LOGISTICSITEMS_ITEMHUD_ICON;
	public static Icon LOGISTICSITEMTEXTURE_FOR_DISK;
	
	// Standalone pipes
	public static String LOGISTICSPIPE_TEXTURE_FILE					;
	public static String LOGISTICSPIPE_PROVIDER_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE		;
	public static String LOGISTICSPIPE_REQUESTER_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE		;
	public static String LOGISTICSPIPE_CRAFTER_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE		;
	public static String LOGISTICSPIPE_SATELLITE_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE	;
	public static String LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE	;
	public static String LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE_FILE	;
	public static String LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE_FILE ;
	public static String LOGISTICSPIPE_APIARIST_SINK_TEXTURE_FILE 	;
	public static String LOGISTICSPIPE_INVSYSCON_CON_TEXTURE_FILE 	;
	public static String LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE_FILE 	;
	public static String LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE_FILE 	;
	public static String LOGISTICSPIPE_ENTRANCE_TEXTURE_FILE 			;
	public static String LOGISTICSPIPE_DESTINATION_TEXTURE_FILE 		;
	public static String LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE		;
	public static String LOGISTICSPIPE_FIREWALL_TEXTURE_FILE			;
	
	// Liquid Pipes
	public static String LOGISTICSPIPE_LIQUID_CONNECTOR_TEXTURE_FILE	;
	public static String LOGISTICSPIPE_LIQUID_BASIC_FILE				;
	public static String LOGISTICSPIPE_LIQUID_INSERTION_FILE			;
	public static String LOGISTICSPIPE_LIQUID_PROVIDER_FILE			;
	public static String LOGISTICSPIPE_LIQUID_REQUEST_FILE			;
	
	// Status overlay
	public static String LOGISTICSPIPE_ROUTED_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_POWERED_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_SECURITY_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_LIQUID_TEXTURE_FILE			;
	// Chassi pipes
	public static String LOGISTICSPIPE_CHASSI1_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_CHASSI2_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_CHASSI3_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_CHASSI4_TEXTURE_FILE			;
	public static String LOGISTICSPIPE_CHASSI5_TEXTURE_FILE			;
	// Chassi status overlay
	public static String LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE		;
	public static String LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE	;
	public static String LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE	;
	
	//Pipe Power Overlays
	public static String LOGISTICSPIPE_OVERLAY_POWERED_TEXTURE_FILE	;
	public static String LOGISTICSPIPE_OVERLAY_UNPOWERED_TEXTURE_FILE	;
	public static String LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE		;
	
	//Armor
	public static final String LOGISTICSPIPE_HUD_TEXTURE_FILE="/logisticspipes/HUD.png";
	
	/*static {
		//BROKEN CODE -- CAN NOT DO THIS STATIC, MUST INIT AFTER MINECRAFT
		TextureMap itemTextures = Minecraft.getMinecraft().renderEngine.textureMapItems;
		// Standalone pipes
		 LOGISTICSPIPE_TEXTURE_FILE					=  itemTextures.registerIcon("/logisticspipes/pipes/basic.png");
		 LOGISTICSPIPE_PROVIDER_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/provider.png");
		 LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE		=  itemTextures.registerIcon("/logisticspipes/pipes/provider_mk2.png");
		 LOGISTICSPIPE_REQUESTER_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/request.png");
		 LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE		=  itemTextures.registerIcon("/logisticspipes/pipes/request_mk2.png");
		 LOGISTICSPIPE_CRAFTER_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/crafting.png");
		 LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE		=  itemTextures.registerIcon("/logisticspipes/pipes/crafting_mk2.png");
		 LOGISTICSPIPE_SATELLITE_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/satellite.png");
		 LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/supplier.png");
		 LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE	=  itemTextures.registerIcon("/logisticspipes/pipes/builder_supplier.png");
		 LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE	=  itemTextures.registerIcon("/logisticspipes/pipes/liquid_supplier.png");
		 LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE_FILE	=  itemTextures.registerIcon("/logisticspipes/pipes/remote_orderer.png");
		 LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE_FILE =  itemTextures.registerIcon("/logisticspipes/pipes/analyzer.png");
		 LOGISTICSPIPE_APIARIST_SINK_TEXTURE_FILE 	=  itemTextures.registerIcon("/logisticspipes/pipes/beesink.png");
		 LOGISTICSPIPE_INVSYSCON_CON_TEXTURE_FILE 	=  itemTextures.registerIcon("/logisticspipes/pipes/invsyscon_con.png");
		 LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE_FILE 	=  itemTextures.registerIcon("/logisticspipes/pipes/invsyscon_dis.png");
		 LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE_FILE 	=  itemTextures.registerIcon("/logisticspipes/pipes/invsyscon_mis.png");
		 LOGISTICSPIPE_ENTRANCE_TEXTURE_FILE 			=  itemTextures.registerIcon("/logisticspipes/pipes/entrance.png");
		 LOGISTICSPIPE_DESTINATION_TEXTURE_FILE 		=  itemTextures.registerIcon("/logisticspipes/pipes/destination.png");
		 LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE		=  itemTextures.registerIcon("/logisticspipes/pipes/crafting_mk3.png");
		 LOGISTICSPIPE_FIREWALL_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/firewall.png");
		
		// Liquid Pipes
		 LOGISTICSPIPE_LIQUID_CONNECTOR_TEXTURE_FILE	=  itemTextures.registerIcon("/logisticspipes/pipes/liquid_connector.png");
		 LOGISTICSPIPE_LIQUID_BASIC_FILE				=  itemTextures.registerIcon("/logisticspipes/pipes/liquid_basic.png");
		 LOGISTICSPIPE_LIQUID_INSERTION_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/liquid_insertion.png");
		 LOGISTICSPIPE_LIQUID_PROVIDER_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/liquid_provider.png");
		 LOGISTICSPIPE_LIQUID_REQUEST_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/liquid_request.png");
		
		// Status overlay
		 LOGISTICSPIPE_ROUTED_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/status_overlay/routed.png");
		 LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/status_overlay/not_routed.png");
		 LOGISTICSPIPE_POWERED_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/status_overlay/powered.png");
		 LOGISTICSPIPE_SECURITY_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/status_overlay/security.png");
		 LOGISTICSPIPE_LIQUID_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/status_overlay/liquid_connection.png");
		// Chassi pipes
		 LOGISTICSPIPE_CHASSI1_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/chassi/chassi_mk1.png");
		 LOGISTICSPIPE_CHASSI2_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/chassi/chassi_mk2.png");
		 LOGISTICSPIPE_CHASSI3_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/chassi/chassi_mk3.png");
		 LOGISTICSPIPE_CHASSI4_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/chassi/chassi_mk4.png");
		 LOGISTICSPIPE_CHASSI5_TEXTURE_FILE			=  itemTextures.registerIcon("/logisticspipes/pipes/chassi/chassi_mk5.png");
		// Chassi status overlay
		 LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE		=  itemTextures.registerIcon("/logisticspipes/pipes/chassi/status_overlay/routed.png");
		 LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE	=  itemTextures.registerIcon("/logisticspipes/pipes/chassi/status_overlay/not_routed.png");
		 LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE	=  itemTextures.registerIcon("/logisticspipes/pipes/chassi/status_overlay/direction.png");
		
		//Pipe Power Overlays
		 LOGISTICSPIPE_OVERLAY_POWERED_TEXTURE_FILE	=  itemTextures.registerIcon("/logisticspipes/pipes/status_overlay/powered-pipe.png");
		 LOGISTICSPIPE_OVERLAY_UNPOWERED_TEXTURE_FILE	=  itemTextures.registerIcon("/logisticspipes/pipes/status_overlay/un-powered-pipe.png");
		 LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE		=  itemTextures.registerIcon("/logisticspipes/pipes/status_overlay/un-overlayed.png");
		
		//Armor
		 LOGISTICSPIPE_HUD_TEXTURE_FILE				=  itemTextures.registerIcon("/logisticspipes/HUD.png");

	}
	public static Icon LOGISTICSNETWORKMONITOR_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSREMOTEORDERER_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon[] LOGISTICSREMOTEORDERERCOLORED_ICONINDEX = new Icon[16];//8 * 16 + 0;
	public static Icon LOGISTICSCRAFTINGSIGNCREATOR_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSITEMCARD_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSITEMHUD_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSITEMHUD_PART1_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSITEMHUD_PART2_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSITEMHUD_PART3_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSITEM_NANOHOPPER_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSITEM_UPGRADEMANAGER_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;
	public static Icon LOGISTICSITEM_LIQUIDCONTAINER_ICONINDEX=LOGISTICSPIPE_TEXTURE_FILE;*/

	//Overrider
	@Deprecated
	public static Icon BASE_TEXTURE_FILE;

	// Misc -- now split
//	public static Icon LOGISTICSITEMS_TEXTURE_FILE = "/logisticspipes/item_textures.png";
//	public static Icon LOGISTICSACTIONTRIGGERS_TEXTURE_FILE = "/logisticspipes/actiontriggers_textures.png";

	public static String LOGISTICS_SOLID_BLOCK=LOGISTICSPIPE_TEXTURE_FILE;
	public static IIconProvider LPactionIconProvider;
	public void registerItemIcons(IconRegister par1IconRegister) {
		
		BASE_TEXTURE_FILE=par1IconRegister.registerIcon("logisticspipes:unknown");
		LPactionIconProvider.registerIcons(par1IconRegister);
		//BASE_TEXTURE_FILE = "/logisticspipes/empty.png"
		//LOGISTICS_SOLID_BLOCK="/logisticspipes/blocks/logistics_solid_block.png"
		LOGISTICSPIPE_TEXTURE 					= registerTexture(LOGISTICSPIPE_TEXTURE_FILE);
		LOGISTICSPIPE_PROVIDER_TEXTURE 				= registerTexture(LOGISTICSPIPE_PROVIDER_TEXTURE_FILE);
		LOGISTICSPIPE_REQUESTER_TEXTURE 			= registerTexture(LOGISTICSPIPE_REQUESTER_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTER_TEXTURE				= registerTexture(LOGISTICSPIPE_CRAFTER_TEXTURE_FILE);
		LOGISTICSPIPE_ROUTED_TEXTURE 				= registerTexture(LOGISTICSPIPE_ROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_NOTROUTED_TEXTURE 			= registerTexture(LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_POWERED_TEXTURE 				= registerTexture(LOGISTICSPIPE_POWERED_TEXTURE_FILE, false);
		LOGISTICSPIPE_SECURITY_TEXTURE 				= registerTexture(LOGISTICSPIPE_SECURITY_TEXTURE_FILE, false);
		LOGISTICSPIPE_LIQUID_TEXTURE 				= registerTexture(LOGISTICSPIPE_LIQUID_TEXTURE_FILE, false);
		LOGISTICSPIPE_SATELLITE_TEXTURE 			= registerTexture(LOGISTICSPIPE_SATELLITE_TEXTURE_FILE);
		LOGISTICSPIPE_SUPPLIER_TEXTURE 				= registerTexture(LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE		= registerTexture(LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE		= registerTexture(LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK2_TEXTURE			= registerTexture(LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_REQUESTERMK2_TEXTURE 			= registerTexture(LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_PROVIDERMK2_TEXTURE 			= registerTexture(LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE 		= registerTexture(LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE_FILE);
		LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE 	= registerTexture(LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE_FILE);
		LOGISTICSPIPE_APIARIST_SINK_TEXTURE 		= registerTexture(LOGISTICSPIPE_APIARIST_SINK_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_CON_TEXTURE 		= registerTexture(LOGISTICSPIPE_INVSYSCON_CON_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE 		= registerTexture(LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE 		= registerTexture(LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE_FILE);
		LOGISTICSPIPE_ENTRANCE_TEXTURE 				= registerTexture(LOGISTICSPIPE_ENTRANCE_TEXTURE_FILE);
		LOGISTICSPIPE_DESTINATION_TEXTURE	 		= registerTexture(LOGISTICSPIPE_DESTINATION_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK3_TEXTURE			= registerTexture(LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE);
		LOGISTICSPIPE_FIREWALL_TEXTURE				= registerTexture(LOGISTICSPIPE_FIREWALL_TEXTURE_FILE);

		LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE 		= registerTexture(LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE 		= registerTexture(LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE 		= registerTexture(LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI1_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI1_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI2_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI2_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI3_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI3_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI4_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI4_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI5_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI5_TEXTURE_FILE);
		
		LOGISTICSPIPE_LIQUID_CONNECTOR				= registerSingleTexture(LOGISTICSPIPE_LIQUID_CONNECTOR_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUID_BASIC					= registerTexture(LOGISTICSPIPE_LIQUID_BASIC_FILE);
		LOGISTICSPIPE_LIQUID_INSERTION				= registerTexture(LOGISTICSPIPE_LIQUID_INSERTION_FILE);
		LOGISTICSPIPE_LIQUID_PROVIDER				= registerTexture(LOGISTICSPIPE_LIQUID_PROVIDER_FILE);
		LOGISTICSPIPE_LIQUID_REQUEST				= registerTexture(LOGISTICSPIPE_LIQUID_REQUEST_FILE);
		if (index > 256) {
			throw new UnsupportedOperationException("Too many Textures.");
		}
	}
	
	public TextureType registerTexture(String fileName) {
		return registerTexture(fileName, true);
	}
	
	public TextureType registerTexture(String fileName, boolean flag) {
		TextureType texture = new TextureType();
		texture.normal = index++;
		texture.powered = index++;
		texture.unpowered = index++;
		if(FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			//MinecraftForgeClient.preloadTexture(fileName);
			MainProxy.proxy.addLogisticsPipesOverride(texture.normal, fileName, LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE);
			if(flag) {
				MainProxy.proxy.addLogisticsPipesOverride(texture.powered, fileName, LOGISTICSPIPE_OVERLAY_POWERED_TEXTURE_FILE);
				MainProxy.proxy.addLogisticsPipesOverride(texture.unpowered, fileName, LOGISTICSPIPE_OVERLAY_UNPOWERED_TEXTURE_FILE);
			} else {
				MainProxy.proxy.addLogisticsPipesOverride(texture.powered, fileName, LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE);
				MainProxy.proxy.addLogisticsPipesOverride(texture.unpowered, fileName, LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE);	
			}
		}
		return texture;
	}
	
	public int registerSingleTexture(String fileName) {
		int texture = index++;
		if(FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			//MinecraftForgeClient.preloadTexture(fileName);
			//MainProxy.proxy.addLogisticsPipesOverride(texture, fileName, LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE);
		}
		return texture;
	}
	
	public static class TextureType {
		public int normal;
		public int powered;
		public int unpowered;
	}
}
/*\item_textures\
HUDGlasses.png
HUD_part_arm.png
HUD_part_frame.png
HUD_part_lense.png
itemCraftingGenerator.png
itemNetworkAnalyzer.png
itemRemoteOrder.png
itemRemoteOrder_c0.png
itemRemoteOrder_c1.png
itemRemoteOrder_c2.png
itemRemoteOrder_c3.png
itemRemoteOrder_c4.png
itemRemoteOrder_c5.png
itemRemoteOrder_c6.png
itemRemoteOrder_c7.png
itemRemoteOrder_c8.png
itemRemoteOrder_c9.png
itemRemoteOrder_cA.png
itemRemoteOrder_cB.png
itemRemoteOrder_cC.png
itemRemoteOrder_cD.png
itemRemoteOrder_cE.png
itemRemoteOrder_cF.png
itemUnknowenModule.png
itemUnknown2.png
itemUpgradeDetach_down.png
itemUpgradeDetach_east.png
itemUpgradeDetach_north.png
itemUpgradeDetach_south.png
itemUpgradeDetach_up.png
itemUpgradeDetach_west.png
itemUpgradeManager.png
itemUpgradeSneaky_down.png
itemUpgradeSneaky_east.png
itemUpgradeSneaky_north.png
itemUpgradeSneaky_south.png
itemUpgradeSneaky_up.png
itemUpgradeSneaky_west.png
itemUpgrade_1.png
itemUpgrade_2.png
itemUpgrade_3.png
logisticsDisk.png
mod_bee1.png
mod_bee2.png
mod_bee3.png
mod_bee4.png
mod_blank.png
mod_electric1.png
mod_extract_2.png
mod_extract_3.png
mod_extract_advanced_1.png
mod_extract_advanced_2.png
mod_extract_advanced_3.png
mod_modSink.png
mod_passiveSupplier.png
mod_polySink.png
mod_provider1.png
mod_provider2.png
mod_quickSort.png
mod_sink.png
mod_terminus.png
mod_thaumic.png
nanoHopper.png

actionTriggers\ActionTrigger1.png
actionTriggers\ActionTrigger17.png
actionTriggers\CraftingWaiting.png
actionTriggers\DisablePipe.png
actionTriggers\PowerDischarging.png
actionTriggers\PowerNeeded.png*/
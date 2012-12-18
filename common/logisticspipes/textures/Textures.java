package logisticspipes.textures;

import logisticspipes.proxy.MainProxy;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLStateEvent;

public class Textures {
	private int index = 0;
	private static TextureType empty = new TextureType();
	static {
		empty.normal = 0;
		empty.powered = 0;
		empty.unpowered = 0;
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
	public static TextureType LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE		= empty;
	public static TextureType LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE		= empty;
	public static TextureType LOGISTICSPIPE_CHASSI1_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI2_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI3_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI4_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CHASSI5_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_CRAFTERMK2_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_CRAFTERMK2_TEXTURE_DIS 			= empty;
	public static TextureType LOGISTICSPIPE_REQUESTERMK2_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_PROVIDERMK2_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_PROVIDERMK2_TEXTURE_DIS 		= empty;
	public static TextureType LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE		= empty;
	public static TextureType LOGISTICSPIPE_APIARIST_SINK_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_INVSYSCON_CON_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_ENTRANCE_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_DESTINATION_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_CRAFTERMK3_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_CRAFTERMK3_TEXTURE_DIS			= empty;
	
	// Standalone pipes
	public static final String LOGISTICSPIPE_TEXTURE_FILE					= "/logisticspipes/pipes/basic.png";
	public static final String LOGISTICSPIPE_PROVIDER_TEXTURE_FILE			= "/logisticspipes/pipes/provider.png";
	public static final String LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE		= "/logisticspipes/pipes/provider_mk2.png";
	public static final String LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE_DIS	= "/logisticspipes/pipes/provider_mk2_dis.png";
	public static final String LOGISTICSPIPE_REQUESTER_TEXTURE_FILE			= "/logisticspipes/pipes/request.png";
	public static final String LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE		= "/logisticspipes/pipes/request_mk2.png";
	public static final String LOGISTICSPIPE_CRAFTER_TEXTURE_FILE			= "/logisticspipes/pipes/crafting.png";
	public static final String LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE		= "/logisticspipes/pipes/crafting_mk2.png";
	public static final String LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE_DIS	= "/logisticspipes/pipes/crafting_mk2_dis.png";
	public static final String LOGISTICSPIPE_SATELLITE_TEXTURE_FILE			= "/logisticspipes/pipes/satellite.png";
	public static final String LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE			= "/logisticspipes/pipes/supplier.png";
	public static final String LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE	= "/logisticspipes/pipes/builder_supplier.png";
	public static final String LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE	= "/logisticspipes/pipes/liquid_supplier.png";
	public static final String LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE_FILE	= "/logisticspipes/pipes/remote_orderer.png";
	public static final String LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE_FILE = "/logisticspipes/pipes/analyzer.png";
	public static final String LOGISTICSPIPE_APIARIST_SINK_TEXTURE_FILE 	= "/logisticspipes/pipes/beesink.png";
	public static final String LOGISTICSPIPE_INVSYSCON_CON_TEXTURE_FILE 	= "/logisticspipes/pipes/invsyscon_con.png";
	public static final String LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE_FILE 	= "/logisticspipes/pipes/invsyscon_dis.png";
	public static final String LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE_FILE 	= "/logisticspipes/pipes/invsyscon_mis.png";
	public static final String LOGISTICSPIPE_ENTRANCE_TEXTURE_FILE 			= "/logisticspipes/pipes/entrance.png";
	public static final String LOGISTICSPIPE_DESTINATION_TEXTURE_FILE 		= "/logisticspipes/pipes/destination.png";
	public static final String LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE		= "/logisticspipes/pipes/crafting_mk3.png";
	public static final String LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE_DIS	= "/logisticspipes/pipes/crafting_mk3_dis.png";
	// Status overlay
	public static final String LOGISTICSPIPE_ROUTED_TEXTURE_FILE			= "/logisticspipes/pipes/status_overlay/routed.png";
	public static final String LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE			= "/logisticspipes/pipes/status_overlay/not_routed.png";
	public static final String LOGISTICSPIPE_POWERED_TEXTURE_FILE			= "/logisticspipes/pipes/status_overlay/powered.png";
	// Chassi pipes
	public static final String LOGISTICSPIPE_CHASSI1_TEXTURE_FILE			= "/logisticspipes/pipes/chassi/chassi_mk1.png";
	public static final String LOGISTICSPIPE_CHASSI2_TEXTURE_FILE			= "/logisticspipes/pipes/chassi/chassi_mk2.png";
	public static final String LOGISTICSPIPE_CHASSI3_TEXTURE_FILE			= "/logisticspipes/pipes/chassi/chassi_mk3.png";
	public static final String LOGISTICSPIPE_CHASSI4_TEXTURE_FILE			= "/logisticspipes/pipes/chassi/chassi_mk4.png";
	public static final String LOGISTICSPIPE_CHASSI5_TEXTURE_FILE			= "/logisticspipes/pipes/chassi/chassi_mk5.png";
	// Chassi status overlay
	public static final String LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE		= "/logisticspipes/pipes/chassi/status_overlay/routed.png";
	public static final String LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE	= "/logisticspipes/pipes/chassi/status_overlay/not_routed.png";
	public static final String LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE	= "/logisticspipes/pipes/chassi/status_overlay/direction.png";
	
	//Pipe Power Overlays
	public static final String LOGISTICSPIPE_OVERLAY_POWERED_TEXTURE_FILE	= "/logisticspipes/pipes/status_overlay/powered-pipe.png";
	public static final String LOGISTICSPIPE_OVERLAY_UNPOWERED_TEXTURE_FILE	= "/logisticspipes/pipes/status_overlay/un-powered-pipe.png";
	public static final String LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE		= "/logisticspipes/pipes/status_overlay/un-overlayed.png";
	
	//Armor
	public static final String LOGISTICSPIPE_HUD_TEXTURE_FILE				= "/logisticspipes/HUD.png";
	
	public static final int LOGISTICSNETWORKMONITOR_ICONINDEX = 0 * 16 + 0;
	public static final int LOGISTICSREMOTEORDERER_ICONINDEX = 0 * 16 + 1;
	public static final int LOGISTICSREMOTEORDERERCOLORED_ICONINDEX = 8 * 16 + 0;
	public static final int LOGISTICSCRAFTINGSIGNCREATOR_ICONINDEX = 0 * 16 + 2;
	public static final int LOGISTICSITEMCARD_ICONINDEX = 0 * 16 + 4;
	public static final int LOGISTICSITEMHUD_ICONINDEX = 0 * 16 + 5;
	public static final int LOGISTICSITEMHUD_PART1_ICONINDEX = 0 * 16 + 6;
	public static final int LOGISTICSITEMHUD_PART2_ICONINDEX = 0 * 16 + 7;
	public static final int LOGISTICSITEMHUD_PART3_ICONINDEX = 0 * 16 + 8;
	public static final int LOGISTICSITEM_NANOHOPPER_ICONINDEX = 0 * 16 + 9;

	//Overrider
	public static final String BASE_TEXTURE_FILE = "/logisticspipes/empty.png";

	// Misc
	public static final String LOGISTICSITEMS_TEXTURE_FILE = "/logisticspipes/item_textures.png";
	public static final String LOGISTICSACTIONTRIGGERS_TEXTURE_FILE = "/logisticspipes/actiontriggers_textures.png";

	public static final String LOGISTICS_SOLID_BLOCK = "/logisticspipes/blocks/logistics_solid_block.png";
	
	public void load(FMLStateEvent event) {
		if(event.getSide().isClient()) {
			MinecraftForgeClient.preloadTexture(LOGISTICSITEMS_TEXTURE_FILE);
			MinecraftForgeClient.preloadTexture(LOGISTICSACTIONTRIGGERS_TEXTURE_FILE);
			MinecraftForgeClient.preloadTexture(LOGISTICS_SOLID_BLOCK);
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/itemsink.png");
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/extractor.png");
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/supplier.png");
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/soldering_station.png");
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/satellite.png");
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/power_junction.png");
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/crafting.png");
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/crafting.png");
			for(int i=1;i<=4;i++) {
				MinecraftForgeClient.preloadTexture("/logisticspipes/gui/chassipipe_size"+ i +".png");
			}
			MinecraftForgeClient.preloadTexture("/logisticspipes/gui/chassipipe_size8.png");
		}
		LOGISTICSPIPE_TEXTURE 						= registerTexture(LOGISTICSPIPE_TEXTURE_FILE);
		LOGISTICSPIPE_PROVIDER_TEXTURE 				= registerTexture(LOGISTICSPIPE_PROVIDER_TEXTURE_FILE);
		LOGISTICSPIPE_REQUESTER_TEXTURE 			= registerTexture(LOGISTICSPIPE_REQUESTER_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTER_TEXTURE				= registerTexture(LOGISTICSPIPE_CRAFTER_TEXTURE_FILE);
		LOGISTICSPIPE_ROUTED_TEXTURE 				= registerTexture(LOGISTICSPIPE_ROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_NOTROUTED_TEXTURE 			= registerTexture(LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_POWERED_TEXTURE 				= registerTexture(LOGISTICSPIPE_POWERED_TEXTURE_FILE, false);
		LOGISTICSPIPE_SATELLITE_TEXTURE 			= registerTexture(LOGISTICSPIPE_SATELLITE_TEXTURE_FILE);
		LOGISTICSPIPE_SUPPLIER_TEXTURE 				= registerTexture(LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE		= registerTexture(LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE		= registerTexture(LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK2_TEXTURE			= registerTexture(LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK2_TEXTURE_DIS		= registerTexture(LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE_DIS);
		LOGISTICSPIPE_REQUESTERMK2_TEXTURE 			= registerTexture(LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_PROVIDERMK2_TEXTURE 			= registerTexture(LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_PROVIDERMK2_TEXTURE_DIS 		= registerTexture(LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE_DIS);
		LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE 		= registerTexture(LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE_FILE);
		LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE 	= registerTexture(LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE_FILE);
		LOGISTICSPIPE_APIARIST_SINK_TEXTURE 		= registerTexture(LOGISTICSPIPE_APIARIST_SINK_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_CON_TEXTURE 		= registerTexture(LOGISTICSPIPE_INVSYSCON_CON_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE 		= registerTexture(LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE 		= registerTexture(LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE_FILE);
		LOGISTICSPIPE_ENTRANCE_TEXTURE 				= registerTexture(LOGISTICSPIPE_ENTRANCE_TEXTURE_FILE);
		LOGISTICSPIPE_DESTINATION_TEXTURE	 		= registerTexture(LOGISTICSPIPE_DESTINATION_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK3_TEXTURE			= registerTexture(LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK3_TEXTURE_DIS		= registerTexture(LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE_DIS);

		LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE 		= registerTexture(LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE 		= registerTexture(LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE 		= registerTexture(LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI1_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI1_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI2_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI2_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI3_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI3_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI4_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI4_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI5_TEXTURE 				= registerTexture(LOGISTICSPIPE_CHASSI5_TEXTURE_FILE);
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
			MinecraftForgeClient.preloadTexture(fileName);
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
	
	public static class TextureType {
		public int normal;
		public int powered;
		public int unpowered;
	}
}

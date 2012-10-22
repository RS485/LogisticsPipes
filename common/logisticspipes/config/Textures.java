package logisticspipes.config;

import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.SpriteHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLStateEvent;

public class Textures {
	private int index = 0;

	public static int LOGISTICSPIPE_TEXTURE							= 0;
	public static int LOGISTICSPIPE_PROVIDER_TEXTURE				= 0;
	public static int LOGISTICSPIPE_REQUESTER_TEXTURE				= 0;
	public static int LOGISTICSPIPE_CRAFTER_TEXTURE					= 0;
	public static int LOGISTICSPIPE_SATELLITE_TEXTURE				= 0;
	public static int LOGISTICSPIPE_SUPPLIER_TEXTURE				= 0;
	public static int LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE			= 0;
	public static int LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE			= 0;
	public static int LOGISTICSPIPE_ROUTED_TEXTURE					= 0;
	public static int LOGISTICSPIPE_NOTROUTED_TEXTURE				= 0;
	public static int LOGISTICSPIPE_POWERED_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE			= 0;
	public static int LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE		= 0;
	public static int LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE		= 0;
	public static int LOGISTICSPIPE_CHASSI1_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI2_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI3_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI4_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CHASSI5_TEXTURE					= 0;
	public static int LOGISTICSPIPE_CRAFTERMK2_TEXTURE				= 0;
	public static int LOGISTICSPIPE_CRAFTERMK2_TEXTURE_DIS 			= 0;
	public static int LOGISTICSPIPE_REQUESTERMK2_TEXTURE			= 0;
	public static int LOGISTICSPIPE_PROVIDERMK2_TEXTURE				= 0;
	public static int LOGISTICSPIPE_PROVIDERMK2_TEXTURE_DIS 		= 0;
	public static int LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE			= 0;
	public static int LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE		= 0;
	public static int LOGISTICSPIPE_APIARIST_SINK_TEXTURE			= 0;
	public static int LOGISTICSPIPE_INVSYSCON_CON_TEXTURE			= 0;
	public static int LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE			= 0;
	public static int LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE			= 0;
	public static int LOGISTICSPIPE_ENTRANCE_TEXTURE				= 0;
	public static int LOGISTICSPIPE_DESTINATION_TEXTURE				= 0;
	public static int LOGISTICSPIPE_CRAFTERMK3_TEXTURE				= 0;
	public static int LOGISTICSPIPE_CRAFTERMK3_TEXTURE_DIS			= 0;
	
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
	
	//Armor
	public static final String LOGISTICSPIPE_HUD_TEXTURE_FILE				= "/logisticspipes/HUD.png";
	
	public static final int LOGISTICSNETWORKMONITOR_ICONINDEX = 0 * 16 + 0;
	public static final int LOGISTICSREMOTEORDERER_ICONINDEX = 0 * 16 + 1;
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
			initTextures();
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
		LOGISTICSPIPE_POWERED_TEXTURE 				= registerTexture(LOGISTICSPIPE_POWERED_TEXTURE_FILE);
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
	}

	public int registerTexture(String fileName) {
		if(FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			RenderingRegistry.addTextureOverride(Textures.BASE_TEXTURE_FILE, fileName, index);
			MinecraftForgeClient.preloadTexture(fileName);
		}
		return index++;
	}
	
	public void initTextures() {
		String spirt = 	"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111";
		SpriteHelper.registerSpriteMapForFile(Textures.BASE_TEXTURE_FILE, spirt);
	}
}

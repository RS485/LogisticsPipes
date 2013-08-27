package logisticspipes.textures;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import logisticspipes.textures.provider.LPPipeIconProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.common.FMLCommonHandler;

public class Textures {
	private int index = 0;
	public static TextureType empty = new TextureType();
	static {
		empty.normal = 0;
		empty.powered = 0;
		empty.unpowered = 0;
	}
	public static TextureType empty_1 = new TextureType();
	static {
		empty_1.normal = 1;
		empty_1.powered = 1;
		empty_1.unpowered = 1;
	}
	public static TextureType empty_2 = new TextureType();
	static {
		empty_2.normal = 2;
		empty_2.powered = 2;
		empty_2.unpowered = 2;
	}
	
	public Textures() {
		LPactionIconProvider = new LPActionTriggerIconProvider();
		LPpipeIconProvider = new LPPipeIconProvider();
	}

	public static TextureType LOGISTICSPIPE_TEXTURE							= empty;
	public static TextureType LOGISTICSPIPE_PROVIDER_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_REQUESTER_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_CRAFTER_TEXTURE					= empty;
	public static TextureType LOGISTICSPIPE_SATELLITE_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_SUPPLIER_TEXTURE				= empty;
	public static TextureType LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE			= empty;
	public static TextureType LOGISTICSPIPE_LIQUIDSUPPLIER_MK2_TEXTURE		= empty;
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
	public static TextureType LOGISTICSPIPE_LIQUID_EXTRACTOR				= empty;
	public static TextureType LOGISTICSPIPE_LIQUID_SATELLITE				= empty;
	
	public static Icon LOGISTICS_REQUEST_TABLE[]							= new Icon[0];
	
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
	public static String LOGISTICSPIPE_TEXTURE_FILE = "pipes/basic";
	public static String LOGISTICSPIPE_PROVIDER_TEXTURE_FILE ="pipes/provider";
	public static String LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE	="pipes/provider_mk2";
	public static String LOGISTICSPIPE_REQUESTER_TEXTURE_FILE =  "pipes/request";
	public static String LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE =  "pipes/request_mk2";
	public static String LOGISTICSPIPE_CRAFTER_TEXTURE_FILE	 =  "pipes/crafting";
	public static String LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE =  "pipes/crafting_mk2";
	public static String LOGISTICSPIPE_SATELLITE_TEXTURE_FILE =  "pipes/satellite";
	public static String LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE =  "pipes/supplier";
	public static String LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE	=  "pipes/liquid_supplier";
	public static String LOGISTICSPIPE_LIQUIDSUPPLIER_MK2_TEXTURE_FILE	=  "pipes/liquid_supplier_mk2";
	public static String LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE_FILE	=  "pipes/remote_orderer";
	public static String LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE_FILE =  "pipes/analyzer";
	public static String LOGISTICSPIPE_APIARIST_SINK_TEXTURE_FILE =  "pipes/beesink";
	public static String LOGISTICSPIPE_INVSYSCON_CON_TEXTURE_FILE =  "pipes/invsyscon_con";
	public static String LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE_FILE =  "pipes/invsyscon_dis";
	public static String LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE_FILE =  "pipes/invsyscon_mis";
	public static String LOGISTICSPIPE_ENTRANCE_TEXTURE_FILE  =  "pipes/entrance";
	public static String LOGISTICSPIPE_DESTINATION_TEXTURE_FILE =  "pipes/destination";
	public static String LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE =  "pipes/crafting_mk3";
	public static String LOGISTICSPIPE_FIREWALL_TEXTURE_FILE =  "pipes/firewall";
	
	// Fluid Pipes
	public static String LOGISTICSPIPE_LIQUID_CONNECTOR_TEXTURE_FILE =  "pipes/original/liquid_connector";
	public static String LOGISTICSPIPE_LIQUID_BASIC_FILE =  "pipes/liquid_basic";
	public static String LOGISTICSPIPE_LIQUID_INSERTION_FILE =  "pipes/liquid_insertion";
	public static String LOGISTICSPIPE_LIQUID_PROVIDER_FILE =  "pipes/liquid_provider";
	public static String LOGISTICSPIPE_LIQUID_REQUEST_FILE =  "pipes/liquid_request";
	public static String LOGISTICSPIPE_LIQUID_EXTRACTOR_FILE =  "pipes/liquid_extractor";
	public static String LOGISTICSPIPE_LIQUID_SATELLITE_FILE =  "pipes/liquid_satellite";
	
	// Status overlay
	public static String LOGISTICSPIPE_ROUTED_TEXTURE_FILE = "pipes/status_overlay/routed";
	public static String LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE = "pipes/status_overlay/not_routed";
	public static String LOGISTICSPIPE_POWERED_TEXTURE_FILE = "pipes/status_overlay/powered";
	public static String LOGISTICSPIPE_SECURITY_TEXTURE_FILE ="pipes/status_overlay/security";
	public static String LOGISTICSPIPE_LIQUID_TEXTURE_FILE ="pipes/status_overlay/liquid_connection";
	// Chassi pipes
	public static String LOGISTICSPIPE_CHASSI1_TEXTURE_FILE			=  "pipes/chassi/chassi_mk1";
	public static String LOGISTICSPIPE_CHASSI2_TEXTURE_FILE			=  "pipes/chassi/chassi_mk2";
	public static String LOGISTICSPIPE_CHASSI3_TEXTURE_FILE			=  "pipes/chassi/chassi_mk3";
	public static String LOGISTICSPIPE_CHASSI4_TEXTURE_FILE			=  "pipes/chassi/chassi_mk4";
	public static String LOGISTICSPIPE_CHASSI5_TEXTURE_FILE			=  "pipes/chassi/chassi_mk5";
	
	// Chassi status overlay
	public static String LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE		=  "pipes/chassi/status_overlay/routed";
	public static String LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE	=  "pipes/chassi/status_overlay/not_routed";
	public static String LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE	=  "pipes/chassi/status_overlay/direction";
	
	//Pipe Power Overlays
	public static String LOGISTICSPIPE_OVERLAY_POWERED_TEXTURE_FILE="pipes/status_overlay/powered-pipe";
	public static String LOGISTICSPIPE_OVERLAY_UNPOWERED_TEXTURE_FILE="pipes/status_overlay/un-powered-pipe";
	public static String LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE="pipes/status_overlay/un-overlayed";
	
	//Armor
	public static final String LOGISTICSPIPE_HUD_TEXTURE_FILE="/logisticspipes/HUD";
	
	public static String LOGISTICS_SOLID_BLOCK=LOGISTICSPIPE_TEXTURE_FILE;
	public static IIconProvider LPactionIconProvider;
	public static LPPipeIconProvider LPpipeIconProvider;

	//this gets called with null par1IconRegister from preinit, and later with non-null from textureprestitch
	public void registerBlockIcons(IconRegister par1IconRegister) {
		//Register Empty Texture for slot 0
		MainProxy.proxy.addLogisticsPipesOverride(par1IconRegister, 0, "empty", "", true);
		MainProxy.proxy.addLogisticsPipesOverride(par1IconRegister, 1, "empty", "", true);
		MainProxy.proxy.addLogisticsPipesOverride(par1IconRegister, 2, "empty", "", true);
		
		index = 3;
		
		// Standalone pipes
		LOGISTICSPIPE_TEXTURE 						= registerTexture(par1IconRegister, LOGISTICSPIPE_TEXTURE_FILE);
		LOGISTICSPIPE_PROVIDER_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_PROVIDER_TEXTURE_FILE);
		LOGISTICSPIPE_POWERED_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_POWERED_TEXTURE_FILE, 2);
		LOGISTICSPIPE_SECURITY_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_SECURITY_TEXTURE_FILE, 2);
		LOGISTICSPIPE_ROUTED_TEXTURE            	= registerTexture(par1IconRegister, LOGISTICSPIPE_ROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_NOTROUTED_TEXTURE         	= registerTexture(par1IconRegister, LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_REQUESTER_TEXTURE 			= registerTexture(par1IconRegister, LOGISTICSPIPE_REQUESTER_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTER_TEXTURE				= registerTexture(par1IconRegister, LOGISTICSPIPE_CRAFTER_TEXTURE_FILE);
		LOGISTICSPIPE_SATELLITE_TEXTURE 			= registerTexture(par1IconRegister, LOGISTICSPIPE_SATELLITE_TEXTURE_FILE);
		LOGISTICSPIPE_SUPPLIER_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_SUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE		= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUIDSUPPLIER_MK2_TEXTURE	= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUIDSUPPLIER_MK2_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK2_TEXTURE			= registerTexture(par1IconRegister, LOGISTICSPIPE_CRAFTERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_REQUESTERMK2_TEXTURE 			= registerTexture(par1IconRegister, LOGISTICSPIPE_REQUESTERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_PROVIDERMK2_TEXTURE 			= registerTexture(par1IconRegister, LOGISTICSPIPE_PROVIDERMK2_TEXTURE_FILE);
		LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE 		= registerTexture(par1IconRegister, LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE_FILE);
		LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE 	= registerTexture(par1IconRegister, LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE_FILE);
		LOGISTICSPIPE_APIARIST_SINK_TEXTURE 		= registerTexture(par1IconRegister, LOGISTICSPIPE_APIARIST_SINK_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_CON_TEXTURE 		= registerTexture(par1IconRegister, LOGISTICSPIPE_INVSYSCON_CON_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE 		= registerTexture(par1IconRegister, LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE_FILE);
		LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE 		= registerTexture(par1IconRegister, LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE_FILE);
		LOGISTICSPIPE_ENTRANCE_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_ENTRANCE_TEXTURE_FILE);
		LOGISTICSPIPE_DESTINATION_TEXTURE	 		= registerTexture(par1IconRegister, LOGISTICSPIPE_DESTINATION_TEXTURE_FILE);
		LOGISTICSPIPE_CRAFTERMK3_TEXTURE			= registerTexture(par1IconRegister, LOGISTICSPIPE_CRAFTERMK3_TEXTURE_FILE);
		LOGISTICSPIPE_FIREWALL_TEXTURE				= registerTexture(par1IconRegister, LOGISTICSPIPE_FIREWALL_TEXTURE_FILE);
		
		//Fluid
		LOGISTICSPIPE_LIQUID_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUID_TEXTURE_FILE, 2);
		LOGISTICSPIPE_LIQUID_CONNECTOR				= registerSingleTexture(par1IconRegister, LOGISTICSPIPE_LIQUID_CONNECTOR_TEXTURE_FILE);
		LOGISTICSPIPE_LIQUID_BASIC					= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUID_BASIC_FILE);
		LOGISTICSPIPE_LIQUID_INSERTION				= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUID_INSERTION_FILE);
		LOGISTICSPIPE_LIQUID_PROVIDER				= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUID_PROVIDER_FILE);
		LOGISTICSPIPE_LIQUID_REQUEST				= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUID_REQUEST_FILE);
		LOGISTICSPIPE_LIQUID_EXTRACTOR				= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUID_EXTRACTOR_FILE);
		LOGISTICSPIPE_LIQUID_SATELLITE				= registerTexture(par1IconRegister, LOGISTICSPIPE_LIQUID_SATELLITE_FILE);
		
		//Chassi
		LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE 		= registerTexture(par1IconRegister, LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE 		= registerTexture(par1IconRegister, LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE 		= registerTexture(par1IconRegister, LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI1_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_CHASSI1_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI2_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_CHASSI2_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI3_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_CHASSI3_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI4_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_CHASSI4_TEXTURE_FILE);
		LOGISTICSPIPE_CHASSI5_TEXTURE 				= registerTexture(par1IconRegister, LOGISTICSPIPE_CHASSI5_TEXTURE_FILE);
		
		if(MainProxy.isClient() && par1IconRegister != null) {
			LOGISTICS_REQUEST_TABLE = new Icon[5];
			for(int i = 0;i < 5;i++) {
				LOGISTICS_REQUEST_TABLE[i] = par1IconRegister.registerIcon("logisticspipes:requesttable/"+i);
			}
		}
		
		if(LogisticsPipes.DEBUG) {
			System.out.println("LP: pipetextures " + index);
		}
	}
	
	public void registerItemIcons(IconRegister par1IconRegister) {
		LPactionIconProvider.registerIcons(par1IconRegister);
	}
	
	private TextureType registerTexture(IconRegister par1IconRegister, String fileName) {
		return registerTexture(par1IconRegister, fileName, 1);
	}
	/**
	 * @param par1IconRegister - IconRegister
	 * @param fileName - name of texture
	 * @param flag - 2 - register single texture without overlay, 1/0 register with overlay
	 */
	
	private TextureType registerTexture(IconRegister par1IconRegister, String fileName, int flag) {
		TextureType texture = new TextureType();
			texture.normal = index++;
			texture.powered=texture.normal;
			texture.unpowered=texture.normal;
			boolean isClient = MainProxy.isClient();
			if(isClient) {
				MainProxy.proxy.addLogisticsPipesOverride(par1IconRegister, texture.normal,fileName,LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE,(flag==2));
			}
			if(flag==1) {
				texture.powered = index++;
				texture.unpowered = index++;
				if(isClient) {
					MainProxy.proxy.addLogisticsPipesOverride(par1IconRegister, texture.powered,fileName,LOGISTICSPIPE_OVERLAY_POWERED_TEXTURE_FILE,false);
					MainProxy.proxy.addLogisticsPipesOverride(par1IconRegister, texture.unpowered,fileName,LOGISTICSPIPE_OVERLAY_UNPOWERED_TEXTURE_FILE,false);
				}
			} 
		return texture;
	}
	
	private int registerSingleTexture(IconRegister par1IconRegister, String fileName) {
		int texture = index++;
		if(FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			MainProxy.proxy.addLogisticsPipesOverride(par1IconRegister, texture, fileName, LOGISTICSPIPE_UN_OVERLAY_TEXTURE_FILE,true);
		}
		return texture;
	}
	
	public static class TextureType {
		public int normal;
		public int powered;
		public int unpowered;
	}
}
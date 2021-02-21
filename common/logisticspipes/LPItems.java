package logisticspipes;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import logisticspipes.items.ItemBlankModule;
import logisticspipes.items.ItemDisk;
import logisticspipes.items.ItemHUDArmor;
import logisticspipes.items.ItemLogisticsChips;
import logisticspipes.items.ItemLogisticsProgrammer;
import logisticspipes.items.ItemPipeController;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.items.RemoteOrderer;
import network.rs485.logisticspipes.guidebook.ItemGuideBook;

public class LPItems {

	// Logistics Pipes
	@ObjectHolder("logisticspipes:pipe_basic")
	public static Item pipeBasic;

	@ObjectHolder("logisticspipes:pipe_request")
	public static Item pipeRequest;

	@ObjectHolder("logisticspipes:pipe_request_mk2")
	public static Item pipeRequestMk2;

	@ObjectHolder("logisticspipes:pipe_provider")
	public static Item pipeProvider;

	@ObjectHolder("logisticspipes:pipe_crafting")
	public static Item pipeCrafting;

	@ObjectHolder("logisticspipes:pipe_satellite")
	public static Item pipeSatellite;

	@ObjectHolder("logisticspipes:pipe_supplier")
	public static Item pipeSupplier;

	@ObjectHolder("logisticspipes:pipe_chassis_mk1")
	public static Item pipeChassisMk1;

	@ObjectHolder("logisticspipes:pipe_chassis_mk2")
	public static Item pipeChassisMk2;

	@ObjectHolder("logisticspipes:pipe_chassis_mk3")
	public static Item pipeChassisMk3;

	@ObjectHolder("logisticspipes:pipe_chassis_mk4")
	public static Item pipeChassisMk4;

	@ObjectHolder("logisticspipes:pipe_chassis_mk5")
	public static Item pipeChassisMk5;

	@ObjectHolder("logisticspipes:pipe_inventory_system_connector")
	public static Item pipeInvSystemConnector;

	@ObjectHolder("logisticspipes:pipe_system_entrance")
	public static Item pipeSystemEntrance;

	@ObjectHolder("logisticspipes:pipe_system_destination")
	public static Item pipeSystemDestination;

	@ObjectHolder("logisticspipes:pipe_firewall")
	public static Item pipeFirewall;

	@ObjectHolder("logisticspipes:pipe_remote_orderer")
	public static Item pipeRemoteOrderer;

	@ObjectHolder("logisticspipes:pipe_request_table")
	public static Item requestTable;

	// Logistics Fluid Pipes
	@ObjectHolder("logisticspipes:pipe_fluid_basic")
	public static Item pipeFluidBasic;

	@ObjectHolder("logisticspipes:pipe_fluid_request")
	public static Item pipeFluidRequest;

	@ObjectHolder("logisticspipes:pipe_fluid_provider")
	public static Item pipeFluidProvider;

	@ObjectHolder("logisticspipes:pipe_fluid_satellite")
	public static Item pipeFluidSatellite;

	@ObjectHolder("logisticspipes:pipe_fluid_supplier")
	public static Item pipeFluidSupplier;

	@ObjectHolder("logisticspipes:pipe_fluid_supplier_mk2")
	public static Item pipeFluidSupplierMk2;

	@ObjectHolder("logisticspipes:pipe_fluid_insertion")
	public static Item pipeFluidInsertion;

	@ObjectHolder("logisticspipes:pipe_fluid_extractor")
	public static Item pipeFluidExtractor;

	// Logistics Modules/Upgrades
	@ObjectHolder("logisticspipes:module_blank")
	public static ItemBlankModule blankModule;

	public static BiMap<String, ResourceLocation> modules = HashBiMap.create();
	public static BiMap<String, ResourceLocation> upgrades = HashBiMap.create();

	// Miscellaneous Items
	@ObjectHolder("logisticspipes:guide_book")
	public static ItemGuideBook itemGuideBook;

	@ObjectHolder("logisticspipes:remote_orderer")
	public static RemoteOrderer remoteOrderer;

	@ObjectHolder("logisticspipes:disk")
	public static ItemDisk disk;

	@ObjectHolder("logisticspipes:item_card")
	public static LogisticsItemCard itemCard;

	@ObjectHolder("logisticspipes:hud_glasses")
	public static ItemHUDArmor hudGlasses;

	@ObjectHolder("logisticspipes:fluid_container")
	public static LogisticsFluidContainer fluidContainer;

	@ObjectHolder("logisticspipes:pipe_controller")
	public static ItemPipeController pipeController;

	@ObjectHolder("logisticspipes:logistics_programmer")
	public static ItemLogisticsProgrammer logisticsProgrammer;

	@ObjectHolder("logisticspipes:chip_basic")
	public static ItemLogisticsChips chipBasic;

	@ObjectHolder("logisticspipes:chip_basic_raw")
	public static ItemLogisticsChips chipBasicRaw;

	@ObjectHolder("logisticspipes:chip_advanced")
	public static ItemLogisticsChips chipAdvanced;

	@ObjectHolder("logisticspipes:chip_advanced_raw")
	public static ItemLogisticsChips chipAdvancedRaw;

	@ObjectHolder("logisticspipes:chip_fpga")
	public static ItemLogisticsChips chipFPGA;

	@ObjectHolder("logisticspipes:chip_fpga_raw")
	public static ItemLogisticsChips chipFPGARaw;

}

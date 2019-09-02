package logisticspipes.datafixer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.google.common.collect.ImmutableMap;

import logisticspipes.LPConstants;

public class MissingMappingHandler {

	private Map<String, String> itemIDMap = ImmutableMap.<String, String>builder()
			// pipes
			.put("item.pipeitemsbasiclogistics", "pipe_basic")
			.put("item.pipeitemsbasictransport", "pipe_transport_basic")
			.put("item.pipelogisticschassimk1", "pipe_chassis_mk1")
			.put("item.pipelogisticschassimk2", "pipe_chassis_mk2")
			.put("item.pipelogisticschassimk3", "pipe_chassis_mk3")
			.put("item.pipelogisticschassimk4", "pipe_chassis_mk4")
			.put("item.pipelogisticschassimk5", "pipe_chassis_mk5")
			.put("item.pipeitemssupplierlogistics", "pipe_supplier")
			.put("item.pipeitemscraftinglogistics", "pipe_crafting")
			.put("item.pipeitemscraftinglogisticsmk2", "pipe_crafting_mk2")
			.put("item.pipeitemscraftinglogisticsmk3", "pipe_crafting_mk3")
			.put("item.pipeitemsfirewall", "pipe_firewall")
			.put("item.pipeitemsproviderlogisticsmk2", "pipe_provider_mk2")
			.put("item.pipeitemsrequestlogistics", "pipe_request")
			.put("item.pipeitemsremoteordererlogistics", "pipe_remote_orderer")
			.put("item.pipeitemssatellitelogistics", "pipe_satellite")
			.put("item.pipeblockrequesttable", "pipe_request_table")
			.put("item.pipeitemsproviderlogistics", "pipe_provider")
			.put("item.pipeitemsinvsysconnector", "pipe_inventory_system_connector")
			.put("item.pipeitemsrequestlogisticsmk2", "pipe_request_mk2")
			.put("item.pipeitemssystemdestinationlogistics", "pipe_system_destination")
			.put("item.pipeitemssystementrancelogistics", "pipe_system_entrance")
			.put("item.pipefluidbasic", "pipe_fluid_basic")
			.put("item.pipefluidprovider", "pipe_fluid_provider")
			.put("item.pipefluidinsertion", "pipe_fluid_insertion")
			.put("item.pipeitemsfluidsupplier", "pipe_fluid_supplier")
			.put("item.pipefluidsuppliermk2", "pipe_fluid_supplier_mk2")
			.put("item.pipefluidrequestlogistics", "pipe_fluid_request")
			.put("item.pipefluidextractor", "pipe_fluid_extractor")
			.put("item.pipefluidsatellite", "pipe_fluid_satellite")
			.put("item.hstubeline", "pipe_hs_line")
			.put("item.hstubegain", "pipe_hs_gain")
			.put("item.hstubescurve", "pipe_hs_s_curve")
			.put("item.hstubespeedup", "pipe_hs_speedup")
			.put("item.hstubecurve", "pipe_hs_curve")

			// modules
			.put("moduleblank", "module_blank")
			.put("itemmodule.moduleitemsink", "module_item_sink")
			.put("itemmodule.modulemodbaseditemsink", "module_item_sink_mod")
			.put("itemmodule.moduleextractor", "module_extractor")
			.put("itemmodule.moduleextractormk2", "module_extractor_mk2")
			.put("itemmodule.moduleextractormk3", "module_extractor_mk3")
			.put("itemmodule.modulecrafter", "module_crafter")
			.put("itemmodule.modulecraftermk2", "module_crafter_mk2")
			.put("itemmodule.modulecraftermk3", "module_crafter_mk3")
			.put("itemmodule.modulepassivesupplier", "module_passive_supplier")
			.put("itemmodule.modulepolymorphicitemsink", "module_item_sink_polymorphic")
			.put("itemmodule.moduleadvancedextractor", "module_extractor_advanced")
			.put("itemmodule.moduleadvancedextractormk2", "module_extractor_advanced_mk2")
			.put("itemmodule.moduleadvancedextractormk3", "module_extractor_advanced_mk3")
			.put("itemmodule.moduleccbasedquicksort", "module_quick_sort_cc")
			.put("itemmodule.moduleenchantmentsink", "module_enchantment_sink")
			.put("itemmodule.moduleenchantmentsinkmk2", "module_enchantment_sink_mk2")
			.put("itemmodule.modulequicksort", "module_quick_sort")
			.put("itemmodule.moduleprovider", "module_provider")
			.put("itemmodule.moduleprovidermk2", "module_provider_mk2")
			.put("itemmodule.moduleoredictitemsink", "module_item_sink_oredict")
			.put("itemmodule.modulecreativetabbaseditemsink", "module_item_sink_creativetab")
			.put("itemmodule.moduleterminus", "module_terminus")
			.put("itemmodule.moduleactivesupplier", "module_active_supplier")
			.put("itemmodule.moduleccbaseditemsink", "module_item_sink_cc")

			// upgrades
			.put("itemmodule.rfpowersupplierupgrade", "upgrade_power_supplier_rf")
			.put("itemmodule.bcpowersupplierupgrade", "upgrade_power_supplier_mj")
			.put("itemmodule.ic2lvpowersupplierupgrade", "upgrade_power_supplier_eu_lv")
			.put("itemmodule.ic2mvpowersupplierupgrade", "upgrade_power_supplier_eu_mv")
			.put("itemmodule.ic2hvpowersupplierupgrade", "upgrade_power_supplier_eu_hv")
			.put("itemmodule.ic2evpowersupplierupgrade", "upgrade_power_supplier_eu_ev")
			.put("itemmodule.fluidcraftingupgrade", "upgrade_fluid_crafting")
			.put("itemmodule.logiccontrollerupgrade", "upgrade_logic_controller")
			.put("itemmodule.connectionupgradeconfig", "upgrade_disconnection")
			.put("itemmodule.sneakyupgradeconfig", "upgrade_sneaky")
			.put("itemmodule.ccremotecontrolupgrade", "upgrade_cc_remote_control")
			.put("itemmodule.opaqueupgrade", "upgrade_opaque")
			.put("itemmodule.powertransportationupgrade", "upgrade_power_transportation")
			.put("itemmodule.combinedsneakyupgrade", "upgrade_sneaky_combination")
			.put("itemmodule.fuzzyupgrade", "upgrade_fuzzy")
			.put("itemmodule.advancedsatelliteupgrade", "upgrade_satellite_advanced")
			.put("itemmodule.craftingcleanupupgrade", "upgrade_crafting_cleanup")
			.put("itemmodule.craftingmonitoringupgrade", "upgrade_crafting_monitoring")
			.put("itemmodule.upgrademoduleupgrade", "upgrade_module_upgrade")
			.put("itemmodule.speedupgrade", "upgrade_speed")
			.put("itemmodule.patternupgrade", "upgrade_pattern")
			.put("itemmodule.craftingbyproductupgrade", "upgrade_crafting_byproduct")

			// misc
			.put("logisticsfluidcontainer", "fluid_container")
			.put("logisticsprogrammer", "logistics_programmer")
			.put("remoteordereritem", "remote_orderer")
			.put("logisticschips.0", "chip_basic")
			.put("logisticschips.1", "chip_basic_raw")
			.put("logisticschips.2", "chip_advanced")
			.put("logisticschips.3", "chip_advanced_raw")
			.put("logisticschips.4", "chip_fpga")
			.put("logisticschips.5", "chip_fpga_raw")
			.put("brokenitem", "broken_item")
			.put("logisticsparts", "parts")
			.put("pipecontroller", "pipe_controller")
			.put("pipemanager", "pipe_manager")
			.put("logisticshudglasses", "hud_glasses")
			.put("logisticsitemcard", "item_card")
			.put("itempipesigncreator", "sign_creator")
			.put("itemdisk", "disk")
			.build();

	private Map<String, String> blockIDMap = ImmutableMap.<String, String>builder()
			.put("tile.logisticssolidblock", "solid_block")
			.put("tile.logisticsblockgenericsubmultiblock", "sub_multiblock")
			.put("tile.logisticsblockgenericpipe", "pipe")
			.build();

	// handled by data fixer
	private List<String> ignoreItems = Arrays.asList(
			"solid_block", "tile.logisticssolidblock"
	);

	@SubscribeEvent
	public void onMissingBlocks(RegistryEvent.MissingMappings<Block> e) {
		for (RegistryEvent.MissingMappings.Mapping<Block> m : e.getMappings()) {
			String entry = blockIDMap.get(m.key.getResourcePath());
			if (entry == null) continue;
			Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(LPConstants.LP_MOD_ID, entry));
			if (value == null) continue;
			m.remap(value);
		}
	}

	@SubscribeEvent
	public void onMissingItems(RegistryEvent.MissingMappings<Item> e) {
		for (RegistryEvent.MissingMappings.Mapping<Item> m : e.getMappings()) {
			String old = m.key.getResourcePath();
			if (ignoreItems.contains(old)) {
				m.ignore();
				continue;
			}
			String entry = itemIDMap.get(old);
			if (entry == null) continue;
			Item value = ForgeRegistries.ITEMS.getValue(new ResourceLocation(LPConstants.LP_MOD_ID, entry));
			if (value == null) continue;
			m.remap(value);
		}
	}

}

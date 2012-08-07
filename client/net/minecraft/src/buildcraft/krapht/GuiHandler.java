package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.gui.GuiChassiPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiCraftingPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiLiquidSupplierPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiProviderPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiRoutingStats;
import net.minecraft.src.buildcraft.krapht.gui.GuiSatellitePipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiSupplierPipe;
import net.minecraft.src.buildcraft.krapht.gui.orderer.GuiOrderer;
import net.minecraft.src.buildcraft.krapht.gui.orderer.NormalGuiOrderer;
import net.minecraft.src.buildcraft.krapht.gui.orderer.NormalMk2GuiOrderer;
import net.minecraft.src.buildcraft.krapht.logic.BaseRoutingLogic;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.logic.LogicLiquidSupplier;
import net.minecraft.src.buildcraft.krapht.logic.LogicProvider;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogisticsMk2;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiAdvancedExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiElectricManager;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiLiquidSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiPassiveSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiTerminus;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiWithPreviousGuiContainer;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISneakyOrientationreceiver;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleElectricManager;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleLiquidSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModulePassiveSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleTerminus;
import net.minecraft.src.buildcraft.logisticspipes.statistics.GuiStatistics;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.PipeLogic;
import net.minecraft.src.forge.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if(!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe))
			return null;

		TileGenericPipe pipe = (TileGenericPipe)tile;
		
		if(ID > 10000) {
			ID -= 10000;
			if(ModLoader.getMinecraftInstance().currentScreen instanceof GuiWithPreviousGuiContainer) {
				GuiScreen prev = ((GuiWithPreviousGuiContainer)ModLoader.getMinecraftInstance().currentScreen).getprevGui();
				if(prev != null) {
					if(prev.getClass().equals(getGuiElement(ID,player,world,x,y,z).getClass())) {
						return prev;
					}
				}
			}
		}
		
		if(ID < 120) {
			switch(ID) {
			
			case GuiIDs.GUI_CRAFTINGPIPE_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicCrafting)) return null;
				return new GuiCraftingPipe(player, ((LogicCrafting)pipe.pipe.logic).getDummyInventory(), (LogicCrafting)pipe.pipe.logic);
			
			case GuiIDs.GUI_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplier)) return null;
				return new GuiLiquidSupplierPipe(player.inventory, ((LogicLiquidSupplier)pipe.pipe.logic).getDummyInventory(), (LogicLiquidSupplier)pipe.pipe.logic);
				
			case GuiIDs.GUI_ProviderPipe_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicProvider)) return null;
				return new GuiProviderPipe(player.inventory, ((LogicProvider)pipe.pipe.logic).getDummyInventory(), (LogicProvider)pipe.pipe.logic);
			
			case GuiIDs.GUI_SatelitePipe_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSatellite)) return null;
				return new GuiSatellitePipe((LogicSatellite)pipe.pipe.logic);
				
			case GuiIDs.GUI_SupplierPipe_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSupplier)) return null;
				return new GuiSupplierPipe(player.inventory, ((LogicSupplier)pipe.pipe.logic).getDummyInventory(), (LogicSupplier)pipe.pipe.logic);
				
				/*** Modules ***/
			case GuiIDs.GUI_Module_Extractor_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyOrientationreceiver)) return null;
				return new GuiExtractor(player.inventory, pipe.pipe, (ISneakyOrientationreceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) return null;
				return new GuiItemSink(player.inventory, pipe.pipe, (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleLiquidSupplier)) return null;
				return new GuiLiquidSupplier(player.inventory, pipe.pipe, (ModuleLiquidSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModulePassiveSupplier)) return null;
				return new GuiPassiveSupplier(player.inventory, pipe.pipe, (ModulePassiveSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleProvider)) return null;
				return new GuiProvider(player.inventory, pipe.pipe, (ModuleProvider) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleTerminus)) return null;
				return new GuiTerminus(player.inventory, pipe.pipe, (ModuleTerminus) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_ChassiModule_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) return null;
				return new GuiChassiPipe(player, (PipeLogisticsChassi)pipe.pipe);

			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleAdvancedExtractor)) return null;
				return new GuiAdvancedExtractor(player.inventory, pipe.pipe, (ModuleAdvancedExtractor) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);

			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleElectricManager)) return null;
				return new GuiElectricManager(player.inventory, pipe.pipe, (ModuleElectricManager) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);				
				
				/*** Basic ***/
			case GuiIDs.GUI_RoutingStats_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new GuiRoutingStats(((BaseRoutingLogic)pipe.pipe.logic).getRouter());
				
			case GuiIDs.GUI_Normal_Orderer_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new NormalGuiOrderer(((BaseRoutingLogic)pipe.pipe.logic).getRoutedPipe(), player);
				
			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new NormalMk2GuiOrderer(((PipeItemsRequestLogisticsMk2)pipe.pipe), player);
				
			// TODO To be client-sided
				/*case GuiIDs.GUI_OrdererStats_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogistics)) return null;
				return new GuiStatistics(((PipeItemsRequestLogistics)pipe.pipe).getHistory(), selectedItem, ModLoader.getMinecraftInstance().currentScreen, _entityPlayer)
				*/
			default:
				return null;
			}
		} else {
			int slot = ID / 100;
			slot--;
			switch(ID % 100) {
			case GuiIDs.GUI_Module_Extractor_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ISneakyOrientationreceiver)) return null;
				return new GuiExtractor(player.inventory, pipe.pipe, (ISneakyOrientationreceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleItemSink)) return null;
				return new GuiItemSink(player.inventory, pipe.pipe, (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleLiquidSupplier)) return null;
				return new GuiLiquidSupplier(player.inventory, pipe.pipe, (ModuleLiquidSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModulePassiveSupplier)) return null;
				return new GuiPassiveSupplier(player.inventory, pipe.pipe, (ModulePassiveSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleProvider)) return null;
				return new GuiProvider(player.inventory, pipe.pipe, (ModuleProvider) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleTerminus)) return null;
				return new GuiTerminus(player.inventory, pipe.pipe, (ModuleTerminus) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen);
			
			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleAdvancedExtractor)) return null;
				return new GuiAdvancedExtractor(player.inventory, pipe.pipe, (ModuleAdvancedExtractor) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);

			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleElectricManager)) return null;
				return new GuiElectricManager(player.inventory, pipe.pipe, (ModuleElectricManager) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				
			default:
				return null;
			}
		}
	}
}

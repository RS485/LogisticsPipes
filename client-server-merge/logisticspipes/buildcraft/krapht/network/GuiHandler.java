package logisticspipes.buildcraft.krapht.network;

import logisticspipes.buildcraft.krapht.CoreRoutedPipe;
import logisticspipes.buildcraft.krapht.GuiIDs;
import logisticspipes.buildcraft.krapht.gui.GuiChassiPipe;
import logisticspipes.buildcraft.krapht.gui.GuiCraftingPipe;
import logisticspipes.buildcraft.krapht.gui.GuiLiquidSupplierPipe;
import logisticspipes.buildcraft.krapht.gui.GuiProviderPipe;
import logisticspipes.buildcraft.krapht.gui.GuiRoutingStats;
import logisticspipes.buildcraft.krapht.gui.GuiSatellitePipe;
import logisticspipes.buildcraft.krapht.gui.GuiSupplierPipe;
import logisticspipes.buildcraft.krapht.gui.orderer.NormalGuiOrderer;
import logisticspipes.buildcraft.krapht.gui.orderer.NormalMk2GuiOrderer;
import logisticspipes.buildcraft.krapht.logic.BaseLogicCrafting;
import logisticspipes.buildcraft.krapht.logic.BaseLogicSatellite;
import logisticspipes.buildcraft.krapht.logic.BaseRoutingLogic;
import logisticspipes.buildcraft.krapht.logic.LogicLiquidSupplier;
import logisticspipes.buildcraft.krapht.logic.LogicProvider;
import logisticspipes.buildcraft.krapht.logic.LogicSupplier;
import logisticspipes.buildcraft.krapht.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.buildcraft.krapht.pipes.PipeLogisticsChassi;
import logisticspipes.buildcraft.logisticspipes.modules.GuiAdvancedExtractor;
import logisticspipes.buildcraft.logisticspipes.modules.GuiApiaristSink;
import logisticspipes.buildcraft.logisticspipes.modules.GuiElectricManager;
import logisticspipes.buildcraft.logisticspipes.modules.GuiExtractor;
import logisticspipes.buildcraft.logisticspipes.modules.GuiItemSink;
import logisticspipes.buildcraft.logisticspipes.modules.GuiLiquidSupplier;
import logisticspipes.buildcraft.logisticspipes.modules.GuiPassiveSupplier;
import logisticspipes.buildcraft.logisticspipes.modules.GuiProvider;
import logisticspipes.buildcraft.logisticspipes.modules.GuiTerminus;
import logisticspipes.buildcraft.logisticspipes.modules.GuiWithPreviousGuiContainer;
import logisticspipes.buildcraft.logisticspipes.modules.ISneakyOrientationreceiver;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleElectricManager;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleItemSink;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleLiquidSupplier;
import logisticspipes.buildcraft.logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleProvider;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleTerminus;
import logisticspipes.krapht.gui.DummyContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IInventory;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe))
			return null;

		TileGenericPipe pipe = (TileGenericPipe)tile;
		
		DummyContainer dummy;
		int xOffset;
		int yOffset;
		
		if(ID > 10000) {
			ID -= 10000;
		}
		
		if(ID < 120) {
			switch(ID) {
			
			case GuiIDs.GUI_CRAFTINGPIPE_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicCrafting)) return null;
				dummy = new DummyContainer(player.inventory, ((BaseLogicCrafting)pipe.pipe.logic).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				//Input slots
		        for(int l = 0; l < 9; l++) {
		        	dummy.addDummySlot(l, 18 + l * 18, 18);
		        }
		        
		        //Output slot
		        dummy.addDummySlot(9, 90, 64);
				return dummy;
			
			case GuiIDs.GUI_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((LogicLiquidSupplier)pipe.pipe.logic).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				
				xOffset = 72;
				yOffset = 18;
				
				for (int row = 0; row < 3; row++){
					for (int column = 0; column < 3; column++){
						dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
					}
				}
				return dummy;
				
			case GuiIDs.GUI_ProviderPipe_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicProvider)) return null;
				dummy = new DummyContainer(player.inventory, ((LogicProvider)pipe.pipe.logic).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				
				xOffset = 72;
				yOffset = 18;
				
				for (int row = 0; row < 3; row++){
					for (int column = 0; column < 3; column++){
						dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
					}
				}
				return dummy;
				
			case GuiIDs.GUI_SatelitePipe_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicSatellite)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_SupplierPipe_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((LogicSupplier)pipe.pipe.logic).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				
				xOffset = 72;
				yOffset = 18;
				
				for (int row = 0; row < 3; row++){
					for (int column = 0; column < 3; column++){
						dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
					}
				}
				return dummy;
				
				/*** Modules ***/
			case GuiIDs.GUI_Module_Extractor_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyOrientationreceiver)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    return dummy;
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleLiquidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleLiquidSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModulePassiveSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModulePassiveSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleProvider)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleProvider)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				
				xOffset = 72;
				yOffset = 18;
				
				for (int row = 0; row < 3; row++){
					for (int column = 0; column < 3; column++){
						dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
					}
				}
				return dummy;
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleTerminus)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleTerminus)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;

			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
				PacketDispatcher.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.xCoord,pipe.yCoord,pipe.zCoord,-1,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getPacket(), player);
				return new DummyContainer(player.inventory, null);
			    
			case GuiIDs.GUI_ChassiModule_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) return null;
				PipeLogisticsChassi _chassiPipe = (PipeLogisticsChassi)pipe.pipe;
				IInventory _moduleInventory = _chassiPipe.getModuleInventory();
				dummy = new DummyContainer(player.inventory, _moduleInventory);
				if (_chassiPipe.getChassiSize() < 5){
					dummy.addNormalSlotsForPlayerInventory(18, 97);
				} else {
					dummy.addNormalSlotsForPlayerInventory(18, 174);
				}
				if (_chassiPipe.getChassiSize() > 0) dummy.addModuleSlot(0, _moduleInventory, 19, 9, _chassiPipe);
				if (_chassiPipe.getChassiSize() > 1) dummy.addModuleSlot(1, _moduleInventory, 19, 29, _chassiPipe);
				if (_chassiPipe.getChassiSize() > 2) dummy.addModuleSlot(2, _moduleInventory, 19, 49, _chassiPipe);
				if (_chassiPipe.getChassiSize() > 3) dummy.addModuleSlot(3, _moduleInventory, 19, 69, _chassiPipe);
				if (_chassiPipe.getChassiSize() > 4) {
					dummy.addModuleSlot(4, _moduleInventory, 19, 89, _chassiPipe);
					dummy.addModuleSlot(5, _moduleInventory, 19, 109, _chassiPipe);
					dummy.addModuleSlot(6, _moduleInventory, 19, 129, _chassiPipe);
					dummy.addModuleSlot(7, _moduleInventory, 19, 149, _chassiPipe);
				}
				
				
				return dummy;
				
				/*** Basic ***/
			case GuiIDs.GUI_RoutingStats_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Normal_Orderer_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new DummyContainer(player.inventory, null);

			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new DummyContainer(player.inventory, null);
				
			default:
				return null;
			}
		} else {
			int slot = ID / 100;
			slot--;
			switch(ID % 100) {
			/*** Modules ***/
			case GuiIDs.GUI_Module_Extractor_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ISneakyOrientationreceiver)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleItemSink)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    return dummy;
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleLiquidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleLiquidSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModulePassiveSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModulePassiveSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleProvider)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleProvider)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				
				xOffset = 72;
				yOffset = 18;
				
				for (int row = 0; row < 3; row++){
					for (int column = 0; column < 3; column++){
						dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
					}
				}
				return dummy;
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleTerminus)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleTerminus)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;

			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleAdvancedExtractor)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleAdvancedExtractor)(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot))).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);

				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    return dummy;
			    
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristSink)) return null;
				PacketDispatcher.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.xCoord,pipe.yCoord,pipe.zCoord,slot,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getPacket(), player);
				return new DummyContainer(player.inventory, null);
			    
			default:
			    return null;
			}
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
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
					if(prev.getClass().equals(getClientGuiElement(ID,player,world,x,y,z).getClass())) {
						return prev;
					}
				}
			}
		}
		
		if(ID < 120) {
			switch(ID) {
			
			case GuiIDs.GUI_CRAFTINGPIPE_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicCrafting)) return null;
				return new GuiCraftingPipe(player, ((BaseLogicCrafting)pipe.pipe.logic).getDummyInventory(), (BaseLogicCrafting)pipe.pipe.logic);
			
			case GuiIDs.GUI_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplier)) return null;
				return new GuiLiquidSupplierPipe(player.inventory, ((LogicLiquidSupplier)pipe.pipe.logic).getDummyInventory(), (LogicLiquidSupplier)pipe.pipe.logic);
				
			case GuiIDs.GUI_ProviderPipe_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicProvider)) return null;
				return new GuiProviderPipe(player.inventory, ((LogicProvider)pipe.pipe.logic).getDummyInventory(), (LogicProvider)pipe.pipe.logic);
			
			case GuiIDs.GUI_SatelitePipe_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicSatellite)) return null;
				return new GuiSatellitePipe((BaseLogicSatellite)pipe.pipe.logic, player);
				
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
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
				return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), player, pipe.pipe, ModLoader.getMinecraftInstance().currentScreen, 0);
			
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
			
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristSink)) return null;
				return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), player, pipe.pipe, ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				
			default:
				return null;
			}
		}
	}

}

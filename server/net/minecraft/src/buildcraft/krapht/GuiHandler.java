package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.logic.BaseRoutingLogic;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.logic.LogicLiquidSupplier;
import net.minecraft.src.buildcraft.krapht.logic.LogicProvider;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogisticsMk2;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISneakyOrientationreceiver;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleLiquidSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModulePassiveSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleTerminus;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.IGuiHandler;
import net.minecraft.src.krapht.gui.DummyContainer;

public class GuiHandler implements IGuiHandler {
	
	@Override
	public Object getGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

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
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicCrafting)) return null;
				dummy = new DummyContainer(player.inventory, ((LogicCrafting)pipe.pipe.logic).getDummyInventory());
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
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSatellite)) return null;
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
			    
			default:
			    return null;
			}
		}
	}
}

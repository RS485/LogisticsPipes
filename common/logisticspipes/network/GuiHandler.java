package logisticspipes.network;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.gui.GuiChassiPipe;
import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.gui.GuiInvSysConnector;
import logisticspipes.gui.GuiLiquidSupplierPipe;
import logisticspipes.gui.GuiProviderPipe;
import logisticspipes.gui.GuiSatellitePipe;
import logisticspipes.gui.GuiSolderingStation;
import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.gui.modules.GuiAdvancedExtractor;
import logisticspipes.gui.modules.GuiApiaristSink;
import logisticspipes.gui.modules.GuiElectricManager;
import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.gui.modules.GuiItemSink;
import logisticspipes.gui.modules.GuiLiquidSupplier;
import logisticspipes.gui.modules.GuiPassiveSupplier;
import logisticspipes.gui.modules.GuiProvider;
import logisticspipes.gui.modules.GuiTerminus;
import logisticspipes.gui.modules.GuiWithPreviousGuiContainer;
import logisticspipes.gui.orderer.NormalGuiOrderer;
import logisticspipes.gui.orderer.NormalMk2GuiOrderer;
import logisticspipes.interfaces.ISneakyOrientationreceiver;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.main.CoreRoutedPipe;
import logisticspipes.main.GuiIDs;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleLiquidSupplier;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleTerminus;
import logisticspipes.network.packets.PacketModuleNBT;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(!world.blockExists(x, y, z))
			return null;
		
		
		
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		TileGenericPipe pipe = null;
		if(tile instanceof TileGenericPipe) {
			pipe = (TileGenericPipe)tile;
		}
		
		DummyContainer dummy;
		int xOffset;
		int yOffset;
		
		if(ID > 10000) {
			ID -= 10000;
		}
		
		if(ID < 120) {
			switch(ID) {
			
			case GuiIDs.GUI_CRAFTINGPIPE_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicCrafting)) return null;
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((LogicLiquidSupplier)pipe.pipe.logic).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				
				xOffset = 72;
				yOffset = 18;
				
				for (int row = 0; row < 3; row++){
					for (int column = 0; column < 3; column++){
						dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
					}
				}
				
				PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_PARTIALS, pipe.xCoord, pipe.yCoord, pipe.zCoord, (((LogicLiquidSupplier)pipe.pipe.logic).isRequestingPartials() ? 1 : 0)).getPacket(), (Player)player);
			    return dummy;
				
			case GuiIDs.GUI_ProviderPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicProvider)) return null;
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicSatellite)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_SupplierPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSupplier)) return null;
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyOrientationreceiver)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    return dummy;
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleLiquidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleLiquidSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModulePassiveSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModulePassiveSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleProvider)) return null;
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleTerminus)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleTerminus)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;

			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
				PacketDispatcher.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.xCoord,pipe.yCoord,pipe.zCoord,-1,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getPacket(), (Player)player);
				return new DummyContainer(player.inventory, null);
			    
			case GuiIDs.GUI_ChassiModule_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) return null;
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Normal_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new DummyContainer(player.inventory, null);

			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_INV_SYS_CONNECTOR:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsInvSysConnector)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeItemsInvSysConnector)pipe.pipe).inv);
				
				dummy.addRestrictedSlot(0, ((PipeItemsInvSysConnector)pipe.pipe).inv, 50, 10, Configs.ItemCardId + 256);
				
				dummy.addNormalSlotsForPlayerInventory(0, 50);
				
				return dummy;
			
			case GuiIDs.GUI_Soldering_Station:
				if(!(tile instanceof LogisticsSolderingTileEntity)) return null;
				return ((LogisticsSolderingTileEntity)tile).createContainer(player);
				
			default:
				return null;
			}
		} else {
			if(pipe == null) return null;
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
				PacketDispatcher.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.xCoord,pipe.yCoord,pipe.zCoord,slot,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getPacket(), (Player)player);
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
		TileGenericPipe pipe = null;
		if(tile instanceof TileGenericPipe) {
			pipe = (TileGenericPipe)tile;
		}

		
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicCrafting)) return null;
				return new GuiCraftingPipe(player, ((BaseLogicCrafting)pipe.pipe.logic).getDummyInventory(), (BaseLogicCrafting)pipe.pipe.logic);
			
			case GuiIDs.GUI_LiquidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplier)) return null;
				return new GuiLiquidSupplierPipe(player.inventory, ((LogicLiquidSupplier)pipe.pipe.logic).getDummyInventory(), (LogicLiquidSupplier)pipe.pipe.logic);
				
			case GuiIDs.GUI_ProviderPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicProvider)) return null;
				return new GuiProviderPipe(player.inventory, ((LogicProvider)pipe.pipe.logic).getDummyInventory(), (LogicProvider)pipe.pipe.logic);
			
			case GuiIDs.GUI_SatelitePipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicSatellite)) return null;
				return new GuiSatellitePipe((BaseLogicSatellite)pipe.pipe.logic, player);
				
			case GuiIDs.GUI_SupplierPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSupplier)) return null;
				return new GuiSupplierPipe(player.inventory, ((LogicSupplier)pipe.pipe.logic).getDummyInventory(), (LogicSupplier)pipe.pipe.logic);
				
				/*** Modules ***/
			case GuiIDs.GUI_Module_Extractor_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyOrientationreceiver)) return null;
				return new GuiExtractor(player.inventory, pipe.pipe, (ISneakyOrientationreceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) return null;
				return new GuiItemSink(player.inventory, pipe.pipe, (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleLiquidSupplier)) return null;
				return new GuiLiquidSupplier(player.inventory, pipe.pipe, (ModuleLiquidSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModulePassiveSupplier)) return null;
				return new GuiPassiveSupplier(player.inventory, pipe.pipe, (ModulePassiveSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleProvider)) return null;
				return new GuiProvider(player.inventory, pipe.pipe, (ModuleProvider) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleTerminus)) return null;
				return new GuiTerminus(player.inventory, pipe.pipe, (ModuleTerminus) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_ChassiModule_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) return null;
				return new GuiChassiPipe(player, (PipeLogisticsChassi)pipe.pipe);

			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleAdvancedExtractor)) return null;
				return new GuiAdvancedExtractor(player.inventory, pipe.pipe, (ModuleAdvancedExtractor) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);

			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleElectricManager)) return null;
				return new GuiElectricManager(player.inventory, pipe.pipe, (ModuleElectricManager) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), ModLoader.getMinecraftInstance().currentScreen, 0);				
				
			case GuiIDs.GUI_Normal_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new NormalGuiOrderer(((BaseRoutingLogic)pipe.pipe.logic).getRoutedPipe(), player);
				
			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new NormalMk2GuiOrderer(((PipeItemsRequestLogisticsMk2)pipe.pipe), player);
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
				return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), player, pipe.pipe, ModLoader.getMinecraftInstance().currentScreen, 0);
			
			case GuiIDs.GUI_INV_SYS_CONNECTOR:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsInvSysConnector)) return null;
				return new GuiInvSysConnector(player, (PipeItemsInvSysConnector)pipe.pipe);
			
			case GuiIDs.GUI_Soldering_Station:
				if(!(tile instanceof LogisticsSolderingTileEntity)) return null;
				return new GuiSolderingStation(player, (LogisticsSolderingTileEntity)tile);
				
			default:
				return null;
			}
		} else {
			if(pipe == null) return null;
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

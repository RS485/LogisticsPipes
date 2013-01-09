package logisticspipes.network;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.gui.GuiChassiPipe;
import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.gui.GuiFreqCardContent;
import logisticspipes.gui.GuiInvSysConnector;
import logisticspipes.gui.GuiLiquidSupplierPipe;
import logisticspipes.gui.GuiPowerJunction;
import logisticspipes.gui.GuiProviderPipe;
import logisticspipes.gui.GuiRoutingStats;
import logisticspipes.gui.GuiSatellitePipe;
import logisticspipes.gui.GuiSolderingStation;
import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.gui.GuiUpgradeManager;
import logisticspipes.gui.hud.GuiHUDSettings;
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
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISneakyOrientationreceiver;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleLiquidSupplier;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleTerminus;
import logisticspipes.network.packets.PacketModuleInteger;
import logisticspipes.network.packets.PacketModuleNBT;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.Player;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, final int x, final int y, final int z) {
		
		TileEntity tile = null;
		if(y != -1) {
			tile = world.getBlockTileEntity(x, y, z);
		}
		TileGenericPipe pipe = null;
		if(tile instanceof TileGenericPipe) {
			pipe = (TileGenericPipe)tile;
		}
		final TileGenericPipe fpipe = pipe;
		
		DummyContainer dummy;
		int xOffset;
		int yOffset;
		
		if(ID > 10000) {
			ID -= 10000;
		}
		
		//Handle Module Configuration
		if(ID == -1) {
			return getServerGuiElement(100 * 20 + x, player, world, 0, -1, z);
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
				
				MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_PARTIALS, pipe.xCoord, pipe.yCoord, pipe.zCoord, (((LogicLiquidSupplier)pipe.pipe.logic).isRequestingPartials() ? 1 : 0)).getPacket(), (Player)player);
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
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, pipe.xCoord, pipe.yCoord, pipe.zCoord, -1, ((ISneakyOrientationreceiver)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ITEM_SINK_STATUS, x, y, z, -1, ((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).isDefaultRoute() ? 1 : 0).getPacket(), (Player)player);
			    
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

			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleElectricManager)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);

				//Pipe slots
				for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
					dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
				}
				
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, pipe.xCoord, pipe.yCoord, pipe.zCoord, -1, ((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).isDischargeMode() ? 1 : 0).getPacket(), (Player)player);
				
				return dummy;
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
				MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.xCoord,pipe.yCoord,pipe.zCoord,-1,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getPacket(), (Player)player);
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
				return new DummyContainer(player, null, new IGuiOpenControler() {
					@Override
					public void guiOpenedByPlayer(EntityPlayer player) {
						((CoreRoutedPipe)fpipe.pipe).playerStartWatching(player, 0);
					}
					
					@Override
					public void guiClosedByPlayer(EntityPlayer player) {
						((CoreRoutedPipe)fpipe.pipe).playerStopWatching(player, 0);
					}
				});
				
			case GuiIDs.GUI_Normal_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new DummyContainer(player.inventory, null);

			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Inv_Sys_Connector_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsInvSysConnector)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeItemsInvSysConnector)pipe.pipe).inv);
				
				dummy.addRestrictedSlot(0, ((PipeItemsInvSysConnector)pipe.pipe).inv, 50, 10, LogisticsPipes.LogisticsItemCard.itemID);
				
				dummy.addNormalSlotsForPlayerInventory(0, 50);
				
				MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.INC_SYS_CON_RESISTANCE, pipe.xCoord, pipe.yCoord, pipe.zCoord, ((PipeItemsInvSysConnector)pipe.pipe).resistance).getPacket(), (Player)player);
				
				return dummy;
			
			case GuiIDs.GUI_Soldering_Station_ID:
				if(!(tile instanceof LogisticsSolderingTileEntity)) return null;
				return ((LogisticsSolderingTileEntity)tile).createContainer(player);
				
			case GuiIDs.GUI_Freq_Card_ID:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeItemsSystemEntranceLogistics) || (pipe.pipe instanceof PipeItemsSystemDestinationLogistics))) return null;
				IInventory inv = null;
				if(pipe.pipe instanceof PipeItemsSystemEntranceLogistics) {
					inv = ((PipeItemsSystemEntranceLogistics)pipe.pipe).inv;
				} else if(pipe.pipe instanceof PipeItemsSystemDestinationLogistics) {
					inv = ((PipeItemsSystemDestinationLogistics)pipe.pipe).inv;
				}
				
				dummy = new DummyContainer(player.inventory, inv);
				
				dummy.addRestrictedSlot(0, inv, 40, 40, LogisticsPipes.LogisticsItemCard.itemID);
				dummy.addNormalSlotsForPlayerInventory(0, 0);
				
				return dummy;
				
			case GuiIDs.GUI_Power_Junction_ID:
				if(!(tile instanceof LogisticsPowerJuntionTileEntity_BuildCraft)) return null;
				return ((LogisticsPowerJuntionTileEntity_BuildCraft)tile).createContainer(player);
				
			case GuiIDs.GUI_HUD_Settings:
				dummy = new DummyContainer(player.inventory, null);
				dummy.addRestrictedHotbarForPlayerInventory(8, 160);
				return dummy;
				
			case GuiIDs.GUI_Upgrade_Manager:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof CoreRoutedPipe))) return null;
				return ((CoreRoutedPipe)pipe.pipe).getUpgradeManager().getDummyContainer(player);
				
			default:break;
			}
		} else {
			int slot = ID / 100;
			if(pipe == null && slot != 20) return null;
			if(slot != 20) {
				slot--;
			}
			switch(ID % 100) {
			/*** Modules ***/
			case GuiIDs.GUI_Module_Extractor_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ISneakyOrientationreceiver)) return null;
					return new DummyContainer(player.inventory, null);
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ISneakyOrientationreceiver)) return null;
					return dummy;
				}
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleItemSink)) return null;
					dummy = new DummyContainer(player.inventory, ((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleItemSink)) return null;
					((DummyModuleContainer)dummy).setInventory(((ModuleItemSink)((DummyModuleContainer)dummy).getModule()).getFilterInventory());
				}
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    return dummy;
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(slot == 20) return null;
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleLiquidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleLiquidSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModulePassiveSupplier)) return null;
					dummy = new DummyContainer(player.inventory, ((ModulePassiveSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModulePassiveSupplier)) return null;
					((DummyModuleContainer)dummy).setInventory(((ModulePassiveSupplier)((DummyModuleContainer)dummy).getModule()).getFilterInventory());
				}
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleProvider)) return null;
					dummy = new DummyContainer(player.inventory, ((ModuleProvider)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleProvider)) return null;
					((DummyModuleContainer)dummy).setInventory(((ModuleProvider)((DummyModuleContainer)dummy).getModule()).getFilterInventory());	
				}
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
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleTerminus)) return null;
					dummy = new DummyContainer(player.inventory, ((ModuleTerminus)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleTerminus)) return null;
					((DummyModuleContainer)dummy).setInventory(((ModuleTerminus)((DummyModuleContainer)dummy).getModule()).getFilterInventory());	
				}
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;

			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleAdvancedExtractor)) return null;
					dummy = new DummyContainer(player.inventory, ((ModuleAdvancedExtractor)(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot))).getFilterInventory());
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleAdvancedExtractor)) return null;
					((DummyModuleContainer)dummy).setInventory(((ModuleAdvancedExtractor)((DummyModuleContainer)dummy).getModule()).getFilterInventory());
				}
				dummy.addNormalSlotsForPlayerInventory(8, 60);

				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    return dummy;
			    
			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleElectricManager)) return null;
					dummy = new DummyContainer(player.inventory, ((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleElectricManager)) return null;
					((DummyModuleContainer)dummy).setInventory(((ModuleElectricManager)((DummyModuleContainer)dummy).getModule()).getFilterInventory());
				}
				dummy.addNormalSlotsForPlayerInventory(8, 60);

				//Pipe slots
				for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
					dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
				}

				if(slot != 20) {
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, pipe.xCoord, pipe.yCoord, pipe.zCoord, slot, ((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).isDischargeMode() ? 1 : 0).getPacket(), (Player)player);
				}
				return dummy;
			
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristSink)) return null;
					MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.xCoord,pipe.yCoord,pipe.zCoord,slot,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getPacket(), (Player)player);
					return new DummyContainer(player.inventory, null);
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleApiaristSink)) return null;
					return dummy;
				}
				
			default:break;
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, final World world, int x, int y, int z) {
		
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		TileGenericPipe pipe = null;
		if(tile instanceof TileGenericPipe) {
			pipe = (TileGenericPipe)tile;
		}
		
		if(ID == -1) {
			return getClientGuiElement(100 * 20 + x, player, world, 0, -1, z);
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
				
			case GuiIDs.GUI_RoutingStats_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new GuiRoutingStats(((BaseRoutingLogic)pipe.pipe.logic).getRouter());

			case GuiIDs.GUI_Normal_Orderer_ID:
				return new NormalGuiOrderer(x, y, z, MainProxy.getDimensionForWorld(world), player);
				
			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new NormalMk2GuiOrderer(((PipeItemsRequestLogisticsMk2)pipe.pipe), player);
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
				return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), player, pipe.pipe, ModLoader.getMinecraftInstance().currentScreen, 0);
			
			case GuiIDs.GUI_Inv_Sys_Connector_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsInvSysConnector)) return null;
				return new GuiInvSysConnector(player, (PipeItemsInvSysConnector)pipe.pipe);
			
			case GuiIDs.GUI_Soldering_Station_ID:
				if(!(tile instanceof LogisticsSolderingTileEntity)) return null;
				return new GuiSolderingStation(player, (LogisticsSolderingTileEntity)tile);
				
			case GuiIDs.GUI_Freq_Card_ID:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeItemsSystemEntranceLogistics) || (pipe.pipe instanceof PipeItemsSystemDestinationLogistics))) return null;
				IInventory inv = null;
				if(pipe.pipe instanceof PipeItemsSystemEntranceLogistics) {
					inv = ((PipeItemsSystemEntranceLogistics)pipe.pipe).inv;
				} else if(pipe.pipe instanceof PipeItemsSystemDestinationLogistics) {
					inv = ((PipeItemsSystemDestinationLogistics)pipe.pipe).inv;
				}
				return new GuiFreqCardContent(player, inv);
				
			case GuiIDs.GUI_Power_Junction_ID:
				if(!(tile instanceof LogisticsPowerJuntionTileEntity_BuildCraft)) return null;
				return new GuiPowerJunction(player, (LogisticsPowerJuntionTileEntity_BuildCraft) tile);

			case GuiIDs.GUI_HUD_Settings:
				return new GuiHUDSettings(player, x);

				
			case GuiIDs.GUI_Upgrade_Manager:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof CoreRoutedPipe))) return null;
				return new GuiUpgradeManager(player, (CoreRoutedPipe) pipe.pipe);
				
			default:break;
			}
		} else {
			int slot = ID / 100;
			if(pipe == null && slot != 20) return null;
			if(slot != 20) {
				slot--;
			}
			switch(ID % 100) {
			case GuiIDs.GUI_Module_Extractor_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ISneakyOrientationreceiver)) return null;
					return new GuiExtractor(player.inventory, pipe.pipe, (ISneakyOrientationreceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					ILogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerPosition(0, -1, z, 20);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ISneakyOrientationreceiver)) return null;
					return new GuiExtractor(player.inventory, null, (ISneakyOrientationreceiver) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleItemSink)) return null;
					return new GuiItemSink(player.inventory, pipe.pipe, (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					ILogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerPosition(0, -1, z, 20);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleItemSink)) return null;
					return new GuiItemSink(player.inventory, null, (ModuleItemSink) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleLiquidSupplier)) return null;
				return new GuiLiquidSupplier(player.inventory, pipe.pipe, (ModuleLiquidSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen);
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModulePassiveSupplier)) return null;
					return new GuiPassiveSupplier(player.inventory, pipe.pipe, (ModulePassiveSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					ILogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerPosition(0, -1, z, 20);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModulePassiveSupplier)) return null;
					return new GuiPassiveSupplier(player.inventory, null, (ModulePassiveSupplier) module, null);	
				}
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleProvider)) return null;
					return new GuiProvider(player.inventory, pipe.pipe, (ModuleProvider) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					ILogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerPosition(0, -1, z, 20);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleProvider)) return null;
					return new GuiProvider(player.inventory, null, (ModuleProvider) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleTerminus)) return null;
					return new GuiTerminus(player.inventory, pipe.pipe, (ModuleTerminus) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					ILogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerPosition(0, -1, z, 20);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleTerminus)) return null;
					return new GuiTerminus(player.inventory, null, (ModuleTerminus) module, null);
				}
				
			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleAdvancedExtractor)) return null;
					return new GuiAdvancedExtractor(player.inventory, pipe.pipe, (ModuleAdvancedExtractor) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					ILogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerPosition(0, -1, z, 20);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleAdvancedExtractor)) return null;
					return new GuiAdvancedExtractor(player.inventory, null, (ModuleAdvancedExtractor) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleElectricManager)) return null;
					return new GuiElectricManager(player.inventory, pipe.pipe, (ModuleElectricManager) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					ILogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerPosition(0, -1, z, 20);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleElectricManager)) return null;
					return new GuiElectricManager(player.inventory, null, (ModuleElectricManager) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(slot != 20) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristSink)) return null;
					return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), player, pipe.pipe, ModLoader.getMinecraftInstance().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					ILogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, new IWorldProvider() {
						@Override
						public World getWorld() {
							return world;
						}}, null);
					module.registerPosition(0, -1, z, 20);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleApiaristSink)) return null;
					return new GuiApiaristSink((ModuleApiaristSink) module, player, null, null, slot);
				}
				
			default:break;
			}
		}
		return null;
	}

}

package logisticspipes.network;

import java.util.HashMap;
import java.util.Map;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.gui.GuiCardManager;
import logisticspipes.gui.GuiChassiPipe;
import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.gui.GuiFirewall;
import logisticspipes.gui.GuiFreqCardContent;
import logisticspipes.gui.GuiInvSysConnector;
import logisticspipes.gui.GuiLiquidBasic;
import logisticspipes.gui.GuiLiquidSupplierMk2Pipe;
import logisticspipes.gui.GuiLiquidSupplierPipe;
import logisticspipes.gui.GuiLogisticsCraftingTable;
import logisticspipes.gui.GuiPowerJunction;
import logisticspipes.gui.GuiProviderPipe;
import logisticspipes.gui.GuiRoutingStats;
import logisticspipes.gui.GuiSatellitePipe;
import logisticspipes.gui.GuiSecurityStation;
import logisticspipes.gui.GuiSolderingStation;
import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.gui.GuiUpgradeManager;
import logisticspipes.gui.hud.GuiHUDSettings;
import logisticspipes.gui.modules.GuiAdvancedExtractor;
import logisticspipes.gui.modules.GuiApiaristAnalyser;
import logisticspipes.gui.modules.GuiApiaristSink;
import logisticspipes.gui.modules.GuiElectricManager;
import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.gui.modules.GuiItemSink;
import logisticspipes.gui.modules.GuiLiquidSupplier;
import logisticspipes.gui.modules.GuiModBasedItemSink;
import logisticspipes.gui.modules.GuiPassiveSupplier;
import logisticspipes.gui.modules.GuiProvider;
import logisticspipes.gui.modules.GuiTerminus;
import logisticspipes.gui.modules.GuiThaumicAspectSink;
import logisticspipes.gui.modules.GuiWithPreviousGuiContainer;
import logisticspipes.gui.orderer.LiquidGuiOrderer;
import logisticspipes.gui.orderer.NormalGuiOrderer;
import logisticspipes.gui.orderer.NormalMk2GuiOrderer;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logic.BaseLogicLiquidSatellite;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.LogicLiquidSupplierMk2;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleLiquidSupplier;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleTerminus;
import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.network.oldpackets.PacketModuleInteger;
import logisticspipes.network.oldpackets.PacketModuleNBT;
import logisticspipes.network.oldpackets.PacketPipeInteger;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeLiquidBasic;
import logisticspipes.pipes.PipeLiquidRequestLogistics;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.CardManagmentInventory;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.Player;

public class GuiHandler implements IGuiHandler {
	
	public final static Map<Integer, Object[]> argumentQueue = new HashMap<Integer, Object[]>();

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
			return getServerGuiElement(100 * -20 + x, player, world, 0, -1, z);
		}
		
		
		if(ID < 120 && ID > 0) {
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
		        
		        for(int i=0;i<((CoreRoutedPipe)pipe.pipe).getUpgradeManager().getLiquidCrafter();i++) {
					int liquidLeft = -(i*40) - 40;
					dummy.addLiquidSlot(i, ((BaseLogicCrafting)pipe.pipe.logic).getLiquidInventory(), liquidLeft + 13, 42);
				}

		        if(((CoreRoutedPipe)pipe.pipe).getUpgradeManager().hasByproductExtractor()) {
		        	dummy.addDummySlot(10, 197, 104);
		        }
		        
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

			case GuiIDs.GUI_LiquidSupplier_MK2_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplierMk2)) return null;
				dummy = new DummyContainer(player.inventory, ((LogicLiquidSupplierMk2)pipe.pipe.logic).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				dummy.addLiquidSlot(0, ((LogicLiquidSupplierMk2)pipe.pipe.logic).getDummyInventory(), 0, 0);
				
				MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_PARTIALS, pipe.xCoord, pipe.yCoord, pipe.zCoord, (((LogicLiquidSupplierMk2)pipe.pipe.logic).isRequestingPartials() ? 1 : 0)).getPacket(), (Player)player);
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
				if(pipe != null && pipe.pipe != null && pipe.pipe.logic instanceof BaseLogicSatellite) {
					return new DummyContainer(player.inventory, null);
				}
				if(pipe != null && pipe.pipe != null && pipe.pipe.logic instanceof BaseLogicLiquidSatellite) {
					return new DummyContainer(player.inventory, null);
				}
				
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyDirectionReceiver)) return null;
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, pipe.xCoord, pipe.yCoord, pipe.zCoord, -1, ((ISneakyDirectionReceiver)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getSneakyDirection().ordinal()).getPacket(), (Player)player);
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
			
			case GuiIDs.GUI_Item_Manager:
				final CardManagmentInventory Cinv = new CardManagmentInventory();
				dummy = new DummyContainer(player, Cinv, new IGuiOpenControler() {
					@Override public void guiOpenedByPlayer(EntityPlayer player) {}
					@Override
					public void guiClosedByPlayer(EntityPlayer player) {
						Cinv.close(player);
					}
				});
				for(int i=0;i<2;i++) {
					dummy.addRestrictedSlot(i, Cinv, 0, 0, LogisticsPipes.ModuleItem.itemID);
				}
				dummy.addRestrictedSlot(2, Cinv, 0, 0, new ISlotCheck() {
					@Override public boolean isStackAllowed(ItemStack itemStack) {return false;}
				});
				dummy.addRestrictedSlot(3, Cinv, 0, 0, LogisticsPipes.LogisticsItemCard.itemID);
				for(int i=4;i<10;i++) {
					dummy.addColorSlot(i, Cinv, 0, 0);
				}
				dummy.addNormalSlotsForPlayerInventory(0, 0);
				return dummy;
			
			case GuiIDs.GUI_Normal_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new DummyContainer(player.inventory, null);

			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Liquid_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeLiquidRequestLogistics)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Inv_Sys_Connector_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsInvSysConnector)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeItemsInvSysConnector)pipe.pipe).inv);
				
				dummy.addRestrictedSlot(0, ((PipeItemsInvSysConnector)pipe.pipe).inv, 50, 10, new ISlotCheck() {
					@Override
					public boolean isStackAllowed(ItemStack itemStack) {
						if(itemStack == null) return false;
						if(itemStack.itemID != LogisticsPipes.LogisticsItemCard.itemID) return false;
						if(itemStack.getItemDamage() != LogisticsItemCard.FREQ_CARD) return false;
						return true;
					}
				});
				
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
				
				dummy.addRestrictedSlot(0, inv, 40, 40, new ISlotCheck() {
					@Override
					public boolean isStackAllowed(ItemStack itemStack) {
						if(itemStack == null) return false;
						if(itemStack.itemID != LogisticsPipes.LogisticsItemCard.itemID) return false;
						if(itemStack.getItemDamage() != LogisticsItemCard.FREQ_CARD) return false;
						return true;
					}
				});
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
				
			case GuiIDs.GUI_Liquid_Basic_ID:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeLiquidBasic))) return null;
				dummy = new DummyContainer(player.inventory, ((PipeLiquidBasic)pipe.pipe).filterInv);
				dummy.addLiquidSlot(0, ((PipeLiquidBasic)pipe.pipe).filterInv, 28, 15);
				dummy.addNormalSlotsForPlayerInventory(10, 45);
				return dummy;
				
			case GuiIDs.GUI_FIREWALL:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeItemsFirewall))) return null;
				dummy = new DummyContainer(player.inventory, ((PipeItemsFirewall)pipe.pipe).inv);
				dummy.addNormalSlotsForPlayerInventory(33, 147);
				for(int i = 0;i < 6;i++) {
					for(int j = 0;j < 6;j++) {
						dummy.addDummySlot(i*6 + j, 0, 0);
					}
				}
				return dummy;

			case GuiIDs.GUI_Security_Station_ID:
				if(!(tile instanceof LogisticsSecurityTileEntity)) return null;
				dummy = new DummyContainer(player, null, ((LogisticsSecurityTileEntity)tile));
				dummy.addRestrictedSlot(0, ((LogisticsSecurityTileEntity)tile).inv, 50, 50, -1);
				dummy.addNormalSlotsForPlayerInventory(10, 210);
				return dummy;

			case GuiIDs.GUI_Module_Apiarist_Analyzer:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristAnalyser)) return null;
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE, pipe.xCoord, pipe.yCoord, pipe.zCoord, 0, ((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getExtractMode()).getPacket(), (Player)player);
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Auto_Crafting_ID:
				if(!(tile instanceof LogisticsCraftingTableTileEntity)) return null;
				dummy = new DummyContainer(player.inventory, ((LogisticsCraftingTableTileEntity)tile).matrix);

				for(int X=0;X<3;X++) {
					for(int Y=0;Y<3;Y++) {
						dummy.addDummySlot(Y*3 + X, 35 + X*18, 10 + Y*18);
					}
				}
				dummy.addUnmodifiableSlot(0, ((LogisticsCraftingTableTileEntity)tile).resultInv, 125, 28);
				for(int X=0;X<9;X++) {
					for(int Y=0;Y<2;Y++) {
						dummy.addNormalSlot(Y*9 + X, ((LogisticsCraftingTableTileEntity)tile).inv, 8 + X*18, 80 + Y*18);
					}
				}
				dummy.addNormalSlotsForPlayerInventory(8, 135);
				return dummy;
				
			default:break;
			}
		} else {
			int slot = ID / 100;
			if(pipe == null && slot >= 0) return null;
			if(slot >= 0) {
				slot--;
			}
			switch(((ID % 100) + 100) % 100) {
			/*** Modules ***/
			case GuiIDs.GUI_Module_Extractor_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ISneakyDirectionReceiver)) return null;
					return new DummyContainer(player.inventory, null);
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ISneakyDirectionReceiver)) return null;
					return dummy;
				}
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(slot >= 0) {
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
				if(slot < 0) return null;
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleLiquidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleLiquidSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(slot >= 0) {
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
				if(slot >= 0) {
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
				if(slot >= 0) {
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
				if(slot >= 0) {
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
				if(slot >= 0) {
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

				if(slot >= 0) {
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, pipe.xCoord, pipe.yCoord, pipe.zCoord, slot, ((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).isDischargeMode() ? 1 : 0).getPacket(), (Player)player);
				}
				return dummy;
			
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristSink)) return null;
					MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.xCoord,pipe.yCoord,pipe.zCoord,slot,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getPacket(), (Player)player);
					return new DummyContainer(player.inventory, null);
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleApiaristSink)) return null;
					return dummy;
				}
				
			case GuiIDs.GUI_Module_ModBased_ItemSink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleModBasedItemSink)) return null;
					NBTTagCompound nbt = new NBTTagCompound();
					((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot).writeToNBT(nbt);
					MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, pipe.xCoord, pipe.yCoord, pipe.zCoord, slot, nbt).getPacket(), (Player)player);
					dummy = new DummyContainer(player.inventory, new SimpleInventory(1, "TMP", 1));
					dummy.addDummySlot(0, 0, 0);
					dummy.addNormalSlotsForPlayerInventory(0, 0);
					return dummy;
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleModBasedItemSink)) return null;
					((DummyModuleContainer)dummy).setInventory(new SimpleInventory(1, "TMP", 1));
					dummy.addDummySlot(0, 0, 0);
					dummy.addNormalSlotsForPlayerInventory(0, 0);
					return dummy;
				}
				
			case GuiIDs.GUI_Module_Thaumic_AspectSink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleThaumicAspectSink)) return null;
					NBTTagCompound nbt = new NBTTagCompound();
					((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot).writeToNBT(nbt);
					MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.THAUMICASPECTSINKLIST, pipe.xCoord, pipe.yCoord, pipe.zCoord, slot, nbt).getPacket(), (Player)player);
					dummy = new DummyContainer(player.inventory, new SimpleInventory(1, "TMP", 1));
					dummy.addDummySlot(0, 0, 0);
					dummy.addNormalSlotsForPlayerInventory(0, 0);
					return dummy;
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleThaumicAspectSink)) return null;
					((DummyModuleContainer)dummy).setInventory(new SimpleInventory(1, "TMP", 1));
					dummy.addDummySlot(0, 0, 0);
					dummy.addNormalSlotsForPlayerInventory(0, 0);
					return dummy;
				}
				
			case GuiIDs.GUI_Module_Apiarist_Analyzer:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristAnalyser)) return null;
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE, pipe.xCoord, pipe.yCoord, pipe.zCoord, slot, ((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getExtractMode()).getPacket(), (Player)player);
					return new DummyContainer(player.inventory, null);
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleApiaristAnalyser)) return null;
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
			return getClientGuiElement(-100 * 20 + x, player, world, 0, -1, z);
		}
		
		if(ID > 10000) {
			ID -= 10000;
			if(FMLClientHandler.instance().getClient().currentScreen instanceof GuiWithPreviousGuiContainer) {
				GuiScreen prev = ((GuiWithPreviousGuiContainer)FMLClientHandler.instance().getClient().currentScreen).getprevGui();
				if(prev != null) {
					if(prev.getClass().equals(getClientGuiElement(ID,player,world,x,y,z).getClass())) {
						return prev;
					}
				}
			}
		}
		
		Object[] args = argumentQueue.get(ID);
		
		if(ID < 120 && ID > 0) {
			switch(ID) {
			
			case GuiIDs.GUI_CRAFTINGPIPE_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof BaseLogicCrafting)) return null;
				if(args == null) {
					new UnsupportedOperationException("Arguments missing").printStackTrace();
					return null;
				}
				return new GuiCraftingPipe(player, ((BaseLogicCrafting)pipe.pipe.logic).getDummyInventory(), (BaseLogicCrafting)pipe.pipe.logic, (Boolean) args[0], (Integer) args[1], (int[]) args[2], (Boolean) args[3]);
			
			case GuiIDs.GUI_LiquidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplier)) return null;
				return new GuiLiquidSupplierPipe(player.inventory, ((LogicLiquidSupplier)pipe.pipe.logic).getDummyInventory(), (LogicLiquidSupplier)pipe.pipe.logic);
			
			case GuiIDs.GUI_LiquidSupplier_MK2_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicLiquidSupplierMk2)) return null;
				return new GuiLiquidSupplierMk2Pipe(player.inventory, ((LogicLiquidSupplierMk2)pipe.pipe.logic).getDummyInventory(), (LogicLiquidSupplierMk2)pipe.pipe.logic);
				
			case GuiIDs.GUI_ProviderPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicProvider)) return null;
				return new GuiProviderPipe(player.inventory, ((LogicProvider)pipe.pipe.logic).getDummyInventory(), (LogicProvider)pipe.pipe.logic);
			
			case GuiIDs.GUI_SatelitePipe_ID:
				if(pipe != null && pipe.pipe != null && pipe.pipe.logic instanceof BaseLogicSatellite) {
					return new GuiSatellitePipe((BaseLogicSatellite)pipe.pipe.logic, player);
				}
				if(pipe != null && pipe.pipe != null && pipe.pipe.logic instanceof BaseLogicLiquidSatellite) {
					return new GuiSatellitePipe((BaseLogicLiquidSatellite)pipe.pipe.logic, player);
				}
				return null;
				
			case GuiIDs.GUI_SupplierPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe.logic instanceof LogicSupplier)) return null;
				return new GuiSupplierPipe(player.inventory, ((LogicSupplier)pipe.pipe.logic).getDummyInventory(), (LogicSupplier)pipe.pipe.logic);
				
				/*** Modules ***/
			case GuiIDs.GUI_Module_Extractor_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyDirectionReceiver)) return null;
				return new GuiExtractor(player.inventory, pipe.pipe, (ISneakyDirectionReceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) return null;
				return new GuiItemSink(player.inventory, pipe.pipe, (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleLiquidSupplier)) return null;
				return new GuiLiquidSupplier(player.inventory, pipe.pipe, (ModuleLiquidSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen);
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModulePassiveSupplier)) return null;
				return new GuiPassiveSupplier(player.inventory, pipe.pipe, (ModulePassiveSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen);
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleProvider)) return null;
				return new GuiProvider(player.inventory, pipe.pipe, (ModuleProvider) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleTerminus)) return null;
				return new GuiTerminus(player.inventory, pipe.pipe, (ModuleTerminus) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen);
				
			case GuiIDs.GUI_ChassiModule_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) return null;
				return new GuiChassiPipe(player, (PipeLogisticsChassi)pipe.pipe);

			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleAdvancedExtractor)) return null;
				return new GuiAdvancedExtractor(player.inventory, pipe.pipe, (ModuleAdvancedExtractor) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);

			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleElectricManager)) return null;
				return new GuiElectricManager(player.inventory, pipe.pipe, (ModuleElectricManager) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);				
				
			case GuiIDs.GUI_RoutingStats_ID:
				if(pipe.pipe == null || !(pipe.pipe.logic instanceof BaseRoutingLogic)) return null;
				return new GuiRoutingStats(((BaseRoutingLogic)pipe.pipe.logic).getRoutedPipe().getRouter(), player);
			
			case GuiIDs.GUI_Item_Manager:
				return new GuiCardManager(player);
				
			case GuiIDs.GUI_Normal_Orderer_ID:
				return new NormalGuiOrderer(x, y, z, MainProxy.getDimensionForWorld(world), player);
				
			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new NormalMk2GuiOrderer(((PipeItemsRequestLogisticsMk2)pipe.pipe), player);
				
			case GuiIDs.GUI_Liquid_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeLiquidRequestLogistics)) return null;
				return new LiquidGuiOrderer(((PipeLiquidRequestLogistics)pipe.pipe), player);
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
				return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), player, pipe.pipe, FMLClientHandler.instance().getClient().currentScreen, 0);
			
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
			
			case GuiIDs.GUI_Liquid_Basic_ID:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeLiquidBasic))) return null;
				return new GuiLiquidBasic(player, ((PipeLiquidBasic)pipe.pipe).filterInv);

			case GuiIDs.GUI_FIREWALL:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeItemsFirewall))) return null;
				return new GuiFirewall((PipeItemsFirewall) pipe.pipe, player);

			case GuiIDs.GUI_Security_Station_ID:
				if(!(tile instanceof LogisticsSecurityTileEntity)) return null;
				return new GuiSecurityStation((LogisticsSecurityTileEntity)tile, player);

			case GuiIDs.GUI_Module_Apiarist_Analyzer:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristAnalyser)) return null;
				return new GuiApiaristAnalyser((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), pipe.pipe, FMLClientHandler.instance().getClient().currentScreen, player.inventory);
			
			case GuiIDs.GUI_Auto_Crafting_ID:
				if(!(tile instanceof LogisticsCraftingTableTileEntity)) return null;
				return new GuiLogisticsCraftingTable(player, (LogisticsCraftingTableTileEntity)tile);
	
			default:break;
			}
		} else {
			int slot = ID / 100;
			if(pipe == null && slot >= 0) return null;
			if(slot >= 0) {
				slot--;
			}
			switch(((ID % 100) + 100) % 100) {
			case GuiIDs.GUI_Module_Extractor_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ISneakyDirectionReceiver)) return null;
					return new GuiExtractor(player.inventory, pipe.pipe, (ISneakyDirectionReceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ISneakyDirectionReceiver)) return null;
					return new GuiExtractor(player.inventory, null, (ISneakyDirectionReceiver) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleItemSink)) return null;
					return new GuiItemSink(player.inventory, pipe.pipe, (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleItemSink)) return null;
					return new GuiItemSink(player.inventory, null, (ModuleItemSink) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_LiquidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleLiquidSupplier)) return null;
				return new GuiLiquidSupplier(player.inventory, pipe.pipe, (ModuleLiquidSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen);
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModulePassiveSupplier)) return null;
					return new GuiPassiveSupplier(player.inventory, pipe.pipe, (ModulePassiveSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModulePassiveSupplier)) return null;
					return new GuiPassiveSupplier(player.inventory, null, (ModulePassiveSupplier) module, null);	
				}
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleProvider)) return null;
					return new GuiProvider(player.inventory, pipe.pipe, (ModuleProvider) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleProvider)) return null;
					return new GuiProvider(player.inventory, null, (ModuleProvider) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleTerminus)) return null;
					return new GuiTerminus(player.inventory, pipe.pipe, (ModuleTerminus) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleTerminus)) return null;
					return new GuiTerminus(player.inventory, null, (ModuleTerminus) module, null);
				}
				
			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleAdvancedExtractor)) return null;
					return new GuiAdvancedExtractor(player.inventory, pipe.pipe, (ModuleAdvancedExtractor) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleAdvancedExtractor)) return null;
					return new GuiAdvancedExtractor(player.inventory, null, (ModuleAdvancedExtractor) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleElectricManager)) return null;
					return new GuiElectricManager(player.inventory, pipe.pipe, (ModuleElectricManager) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleElectricManager)) return null;
					return new GuiElectricManager(player.inventory, null, (ModuleElectricManager) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristSink)) return null;
					return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), player, pipe.pipe, FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, new IWorldProvider() {
						@Override
						public World getWorld() {
							return world;
						}}, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleApiaristSink)) return null;
					return new GuiApiaristSink((ModuleApiaristSink) module, player, null, null, slot);
				}
				
			case GuiIDs.GUI_Module_ModBased_ItemSink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleModBasedItemSink)) return null;
					return new GuiModBasedItemSink(player.inventory, pipe.pipe, (ModuleModBasedItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot),  FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, new IWorldProvider() {
						@Override
						public World getWorld() {
							return world;
						}}, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleModBasedItemSink)) return null;
					return new GuiModBasedItemSink(player.inventory, null, (ModuleModBasedItemSink) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_Thaumic_AspectSink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleThaumicAspectSink)) return null;
					return new GuiThaumicAspectSink(player.inventory, pipe.pipe, (ModuleThaumicAspectSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot),  FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, new IWorldProvider() {
						@Override
						public World getWorld() {
							return world;
						}}, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleThaumicAspectSink)) return null;
					return new GuiThaumicAspectSink(player.inventory, null, (ModuleThaumicAspectSink) module, null, slot);
				}
			
			case GuiIDs.GUI_Module_Apiarist_Analyzer:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristAnalyser)) return null;
					return new GuiApiaristAnalyser((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), pipe.pipe, FMLClientHandler.instance().getClient().currentScreen, player.inventory);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, new IWorldProvider() {
						@Override
						public World getWorld() {
							return world;
						}}, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleApiaristAnalyser)) return null;
					return new GuiApiaristAnalyser((ModuleApiaristAnalyser) module, null, null, player.inventory);
				}

				
				
			default:break;
			}
		}
		return null;
	}

}

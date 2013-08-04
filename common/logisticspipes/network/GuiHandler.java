package logisticspipes.network;

import java.util.HashMap;
import java.util.Map;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.gui.GuiCardManager;
import logisticspipes.gui.GuiChassiPipe;
import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.gui.GuiFirewall;
import logisticspipes.gui.GuiFreqCardContent;
import logisticspipes.gui.GuiInvSysConnector;
import logisticspipes.gui.GuiFluidBasic;
import logisticspipes.gui.GuiFluidSupplierMk2Pipe;
import logisticspipes.gui.GuiFluidSupplierPipe;
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
import logisticspipes.gui.modules.GuiFluidSupplier;
import logisticspipes.gui.modules.GuiModBasedItemSink;
import logisticspipes.gui.modules.GuiOreDictItemSink;
import logisticspipes.gui.modules.GuiPassiveSupplier;
import logisticspipes.gui.modules.GuiProvider;
import logisticspipes.gui.modules.GuiTerminus;
import logisticspipes.gui.modules.GuiThaumicAspectSink;
import logisticspipes.gui.modules.GuiWithPreviousGuiContainer;
import logisticspipes.gui.orderer.FluidGuiOrderer;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.gui.orderer.NormalGuiOrderer;
import logisticspipes.gui.orderer.NormalMk2GuiOrderer;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.interfaces.ISlotClick;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleFluidSupplier;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleTerminus;
import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.network.packets.module.ApiaristAnalyserMode;
import logisticspipes.network.packets.module.ElectricManagetMode;
import logisticspipes.network.packets.module.ModuleBasedItemSinkList;
import logisticspipes.network.packets.module.OreDictItemSinkList;
import logisticspipes.network.packets.module.ThaumicAspectsSinkList;
import logisticspipes.network.packets.modules.BeeModule;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
import logisticspipes.network.packets.modules.ItemSinkDefault;
import logisticspipes.network.packets.pipe.InvSysConResistance;
import logisticspipes.network.packets.pipe.FluidSupplierMode;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeFluidBasic;
import logisticspipes.pipes.PipeFluidRequestLogistics;
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsCraftingLogistics)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeItemsCraftingLogistics)pipe.pipe).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				//Input slots
		        for(int l = 0; l < 9; l++) {
		        	dummy.addDummySlot(l, 18 + l * 18, 18);
		        }
		        
		        //Output slot
		        dummy.addDummySlot(9, 90, 64);
		        
		        for(int i=0;i<((CoreRoutedPipe)pipe.pipe).getUpgradeManager().getFluidCrafter();i++) {
					int liquidLeft = -(i*40) - 40;
					dummy.addFluidSlot(i, ((PipeItemsCraftingLogistics)pipe.pipe).getFluidInventory(), liquidLeft + 13, 42);
				}

		        if(((CoreRoutedPipe)pipe.pipe).getUpgradeManager().hasByproductExtractor()) {
		        	dummy.addDummySlot(10, 197, 104);
		        }
		        
				return dummy;

			case GuiIDs.GUI_FluidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsFluidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeItemsFluidSupplier)pipe.pipe).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				
				xOffset = 72;
				yOffset = 18;
				
				for (int row = 0; row < 3; row++){
					for (int column = 0; column < 3; column++){
						dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
					}
				}
				
//TODO 			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_PARTIALS, pipe.getX(), pipe.getY(), pipe.getZ(), (((PipeItemsFluidSupplier)pipe.pipe).isRequestingPartials() ? 1 : 0)).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidSupplierMode.class).setInteger((((PipeItemsFluidSupplier)pipe.pipe).isRequestingPartials() ? 1 : 0)).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
			    return dummy;

			case GuiIDs.GUI_FluidSupplier_MK2_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeFluidSupplierMk2)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeFluidSupplierMk2)pipe.pipe).getDummyInventory());
				dummy.addNormalSlotsForPlayerInventory(18, 97);
				dummy.addFluidSlot(0, ((PipeFluidSupplierMk2)pipe.pipe).getDummyInventory(), 0, 0);
				
//TODO 			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_PARTIALS, pipe.getX(), pipe.getY(), pipe.getZ(), (((PipeFluidSupplierMk2)pipe.pipe).isRequestingPartials() ? 1 : 0)).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidSupplierMode.class).setInteger((((PipeFluidSupplierMk2)pipe.pipe).isRequestingPartials() ? 1 : 0)).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
			    return dummy;
				
			case GuiIDs.GUI_ProviderPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsProviderLogistics)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeItemsProviderLogistics)pipe.pipe).getprovidingInventory());
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
				if(pipe != null && pipe.pipe != null && pipe.pipe instanceof PipeItemsSatelliteLogistics) {
					return new DummyContainer(player.inventory, null);
				}
				if(pipe != null && pipe.pipe != null && pipe.pipe instanceof PipeFluidSatellite) {
					return new DummyContainer(player.inventory, null);
				}
				
			case GuiIDs.GUI_SupplierPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsSupplierLogistics)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeItemsSupplierLogistics)pipe.pipe).getDummyInventory());
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
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, pipe.getX(), pipe.getY(), pipe.getZ(), -1, ((ISneakyDirectionReceiver)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getSneakyDirection().ordinal()).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(-1).setInteger(((ISneakyDirectionReceiver)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getSneakyDirection().ordinal()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
				dummy.addNormalSlotsForPlayerInventory(8, 60);
	
				//Pipe slots
			    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
			    }
			    
//TODO 		    MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ITEM_SINK_STATUS, x, y, z, -1, ((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).isDefaultRoute() ? 1 : 0).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ItemSinkDefault.class).setInteger2(-1).setInteger(((ModuleItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).isDefaultRoute() ? 1 : 0).setPosX(x).setPosY(y).setPosZ(z), (Player)player);
			    
			    return dummy;
				
			case GuiIDs.GUI_Module_FluidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleFluidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleFluidSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getFilterInventory());
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
				
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, pipe.getX(), pipe.getY(), pipe.getZ(), -1, ((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).isDischargeMode() ? 1 : 0).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ElectricManagetMode.class).setInteger2(-1).setInteger(((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).isDischargeMode() ? 1 : 0).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
				
				return dummy;
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.getX(),pipe.getY(),pipe.getZ(),-1,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(BeeModule.class).setSlot(-1).readFromProvider((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) return null;
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
						Cinv.close(player,(int)player.posX, (int)player.posY, (int)player.posZ);
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) return null;
				return new DummyContainer(player.inventory, null);

			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new DummyContainer(player.inventory, null);
				
			case GuiIDs.GUI_Fluid_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeFluidRequestLogistics)) return null;
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
				
//TODO 			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.INC_SYS_CON_RESISTANCE, pipe.getX(), pipe.getY(), pipe.getZ(), ((PipeItemsInvSysConnector)pipe.pipe).resistance).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(InvSysConResistance.class).setInteger(((PipeItemsInvSysConnector)pipe.pipe).resistance).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
				
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
				if(!(tile instanceof LogisticsPowerJunctionTileEntity)) return null;
				return ((LogisticsPowerJunctionTileEntity)tile).createContainer(player);
				
			case GuiIDs.GUI_HUD_Settings:
				dummy = new DummyContainer(player.inventory, null);
				dummy.addRestrictedHotbarForPlayerInventory(8, 160);
				return dummy;
				
			case GuiIDs.GUI_Upgrade_Manager:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof CoreRoutedPipe))) return null;
				return ((CoreRoutedPipe)pipe.pipe).getUpgradeManager().getDummyContainer(player);
				
			case GuiIDs.GUI_Fluid_Basic_ID:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeFluidBasic))) return null;
				dummy = new DummyContainer(player, ((PipeFluidBasic)pipe.pipe).filterInv, new IGuiOpenControler() {
					@Override
					public void guiOpenedByPlayer(EntityPlayer player) {
						((PipeFluidBasic)fpipe.pipe).guiOpenedByPlayer(player);
					}

					@Override
					public void guiClosedByPlayer(EntityPlayer player) {
						((PipeFluidBasic)fpipe.pipe).guiClosedByPlayer(player);
					}
				});
				dummy.addFluidSlot(0, ((PipeFluidBasic)pipe.pipe).filterInv, 28, 15);
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
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE, pipe.getX(), pipe.getY(), pipe.getZ(), 0, ((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getExtractMode()).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ApiaristAnalyserMode.class).setInteger2(0).setInteger(((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).getExtractMode()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
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
				
			case GuiIDs.GUI_Request_Table_ID:
				if(pipe == null || !(pipe.pipe instanceof PipeBlockRequestTable)) return null;
				dummy = new DummyContainer(player.inventory, ((PipeBlockRequestTable)pipe.pipe).matrix);
				int i = 0;
				for(int Y = 0;Y < 3;Y++) {
					for(int X = 0;X < 9;X++) {
						dummy.addNormalSlot(i++, ((PipeBlockRequestTable)pipe.pipe).inv, 0, 0);
					}
				}
				i = 0;
				for(int Y = 0;Y < 3;Y++) {
					for(int X = 0;X < 3;X++) {
						dummy.addDummySlot(i++, 0, 0);
					}
				}
				dummy.addCallableSlotHandler(0, ((PipeBlockRequestTable)pipe.pipe).resultInv, 0, 0, new ISlotClick() {
					@Override
					public ItemStack getResultForClick() {
						((PipeBlockRequestTable)fpipe.pipe).inv.addCompressed(((PipeBlockRequestTable)fpipe.pipe).getOutput());
						return null;
					}
				});
				dummy.addNormalSlot(0, ((PipeBlockRequestTable)pipe.pipe).toSortInv, 0, 0);
				dummy.addNormalSlotsForPlayerInventory(0, 0);
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
				
			case GuiIDs.GUI_Module_FluidSupplier_ID:
				if(slot < 0) return null;
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleFluidSupplier)) return null;
				dummy = new DummyContainer(player.inventory, ((ModuleFluidSupplier)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getFilterInventory());
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
//TODO 				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, pipe.getX(), pipe.getY(), pipe.getZ(), slot, ((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).isDischargeMode() ? 1 : 0).getPacket(), (Player)player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ElectricManagetMode.class).setInteger2(slot).setInteger(((ModuleElectricManager)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).isDischargeMode() ? 1 : 0).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
				}
				return dummy;
			
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristSink)) return null;
//TODO 				MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.BEE_MODULE_CONTENT,pipe.getX(),pipe.getY(),pipe.getZ(),slot,(ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getPacket(), (Player)player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(BeeModule.class).setSlot(slot).readFromProvider((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
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
//TODO 				MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, pipe.getX(), pipe.getY(), pipe.getZ(), slot, nbt).getPacket(), (Player)player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setSlot(slot).setTag(nbt).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
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
//TODO 				MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.THAUMICASPECTSINKLIST, pipe.getX(), pipe.getY(), pipe.getZ(), slot, nbt).getPacket(), (Player)player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ThaumicAspectsSinkList.class).setSlot(slot).setTag(nbt).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
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
				
			case GuiIDs.GUI_Module_OreDict_ItemSink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleOreDictItemSink)) return null;
					NBTTagCompound nbt = new NBTTagCompound();
					((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot).writeToNBT(nbt);
//TODO 				MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, pipe.xCoord, pipe.yCoord, pipe.zCoord, slot, nbt).getPacket(), (Player)player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OreDictItemSinkList.class).setSlot(slot).setTag(nbt).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
					dummy = new DummyContainer(player.inventory, new SimpleInventory(1, "TMP", 1));
					dummy.addDummySlot(0, 0, 0);
					dummy.addNormalSlotsForPlayerInventory(0, 0);
					return dummy;
				} else {
					dummy = new DummyModuleContainer(player, z);
					if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleOreDictItemSink)) return null;
					((DummyModuleContainer)dummy).setInventory(new SimpleInventory(1, "TMP", 1));
					dummy.addDummySlot(0, 0, 0);
					dummy.addNormalSlotsForPlayerInventory(0, 0);
					return dummy;
				}
				
			case GuiIDs.GUI_Module_Apiarist_Analyzer:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristAnalyser)) return null;
//TODO 				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE, pipe.getX(), pipe.getY(), pipe.getZ(), slot, ((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getExtractMode()).getPacket(), (Player)player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ApiaristAnalyserMode.class).setInteger2(slot).setInteger(((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot)).getExtractMode()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
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
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsCraftingLogistics)) return null;
				if(args == null) {
					new UnsupportedOperationException("Arguments missing").printStackTrace();
					return null;
				}
				return new GuiCraftingPipe(player, ((PipeItemsCraftingLogistics)pipe.pipe).getDummyInventory(), (PipeItemsCraftingLogistics)pipe.pipe, (Boolean) args[0], (Integer) args[1], (int[]) args[2], (Boolean) args[3]);
			
			case GuiIDs.GUI_FluidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsFluidSupplier)) return null;
				return new GuiFluidSupplierPipe(player.inventory, ((PipeItemsFluidSupplier)pipe.pipe).getDummyInventory(), (PipeItemsFluidSupplier)pipe.pipe);
			
			case GuiIDs.GUI_FluidSupplier_MK2_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeFluidSupplierMk2)) return null;
				return new GuiFluidSupplierMk2Pipe(player.inventory, ((PipeFluidSupplierMk2)pipe.pipe).getDummyInventory(), (PipeFluidSupplierMk2)pipe.pipe);
				
			case GuiIDs.GUI_ProviderPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsProviderLogistics)) return null;
				return new GuiProviderPipe(player.inventory, ((PipeItemsProviderLogistics)pipe.pipe).getprovidingInventory(), (PipeItemsProviderLogistics)pipe.pipe);
			
			case GuiIDs.GUI_SatelitePipe_ID:
				if(pipe != null && pipe.pipe != null && pipe.pipe instanceof PipeItemsSatelliteLogistics) {
					return new GuiSatellitePipe((PipeItemsSatelliteLogistics)pipe.pipe, player);
				}
				if(pipe != null && pipe.pipe != null && pipe.pipe instanceof PipeFluidSatellite) {
					return new GuiSatellitePipe((PipeFluidSatellite)pipe.pipe, player);
				}
				return null;
				
			case GuiIDs.GUI_SupplierPipe_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsSupplierLogistics)) return null;
				return new GuiSupplierPipe(player.inventory, ((PipeItemsSupplierLogistics)pipe.pipe).getDummyInventory(), (PipeItemsSupplierLogistics)pipe.pipe);
				
				/*** Modules ***/
			case GuiIDs.GUI_Module_Extractor_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyDirectionReceiver)) return null;
				return new GuiExtractor(player.inventory, (CoreRoutedPipe) pipe.pipe, (ISneakyDirectionReceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);
				
			case GuiIDs.GUI_Module_ItemSink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) return null;
				return new GuiItemSink(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);
				
			case GuiIDs.GUI_Module_FluidSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleFluidSupplier)) return null;
				return new GuiFluidSupplier(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleFluidSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen);
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModulePassiveSupplier)) return null;
				return new GuiPassiveSupplier(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModulePassiveSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen);
				
			case GuiIDs.GUI_Module_Provider_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleProvider)) return null;
				return new GuiProvider(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleProvider) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);
				
			case GuiIDs.GUI_Module_Terminus_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleTerminus)) return null;
				return new GuiTerminus(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleTerminus) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen);
				
			case GuiIDs.GUI_ChassiModule_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) return null;
				return new GuiChassiPipe(player, (PipeLogisticsChassi)pipe.pipe);

			case GuiIDs.GUI_Module_Advanced_Extractor_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleAdvancedExtractor)) return null;
				return new GuiAdvancedExtractor(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleAdvancedExtractor) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);

			case GuiIDs.GUI_Module_ElectricManager_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleElectricManager)) return null;
				return new GuiElectricManager(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleElectricManager) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), FMLClientHandler.instance().getClient().currentScreen, 0);				
				
			case GuiIDs.GUI_RoutingStats_ID:
				//TODO: what was this supposed to be?
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) return null;
				return new GuiRoutingStats(((CoreRoutedPipe)pipe.pipe).getRouter(), player);
			
			case GuiIDs.GUI_Item_Manager:
				return new GuiCardManager(player);
				
			case GuiIDs.GUI_Normal_Orderer_ID:
				return new NormalGuiOrderer(x, y, z, MainProxy.getDimensionForWorld(world), player);
				
			case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) return null;
				return new NormalMk2GuiOrderer(((PipeItemsRequestLogisticsMk2)pipe.pipe), player);
				
			case GuiIDs.GUI_Fluid_Orderer_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeFluidRequestLogistics)) return null;
				return new FluidGuiOrderer(((PipeFluidRequestLogistics)pipe.pipe), player);
				
			case GuiIDs.GUI_Module_Apiarist_Sink_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristSink)) return null;
				return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), player, (CoreRoutedPipe) pipe.pipe, FMLClientHandler.instance().getClient().currentScreen, 0);
			
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
				if(!(tile instanceof LogisticsPowerJunctionTileEntity)) return null;
				return new GuiPowerJunction(player, (LogisticsPowerJunctionTileEntity) tile);

			case GuiIDs.GUI_HUD_Settings:
				return new GuiHUDSettings(player, x);

				
			case GuiIDs.GUI_Upgrade_Manager:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof CoreRoutedPipe))) return null;
				return new GuiUpgradeManager(player, (CoreRoutedPipe) pipe.pipe);
			
			case GuiIDs.GUI_Fluid_Basic_ID:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeFluidBasic))) return null;
				return new GuiFluidBasic(player, ((PipeFluidBasic)pipe.pipe).filterInv);

			case GuiIDs.GUI_FIREWALL:
				if(pipe == null || pipe.pipe == null || !((pipe.pipe instanceof PipeItemsFirewall))) return null;
				return new GuiFirewall((PipeItemsFirewall) pipe.pipe, player);

			case GuiIDs.GUI_Security_Station_ID:
				if(!(tile instanceof LogisticsSecurityTileEntity)) return null;
				return new GuiSecurityStation((LogisticsSecurityTileEntity)tile, player);

			case GuiIDs.GUI_Module_Apiarist_Analyzer:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristAnalyser)) return null;
				return new GuiApiaristAnalyser((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule(), (CoreRoutedPipe) pipe.pipe, FMLClientHandler.instance().getClient().currentScreen, player.inventory);
			
			case GuiIDs.GUI_Auto_Crafting_ID:
				if(!(tile instanceof LogisticsCraftingTableTileEntity)) return null;
				return new GuiLogisticsCraftingTable(player, (LogisticsCraftingTableTileEntity)tile);
	
			case GuiIDs.GUI_Request_Table_ID:
				if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof PipeBlockRequestTable)) return null;
				return new GuiRequestTable(player, ((PipeBlockRequestTable)pipe.pipe));
			
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
					return new GuiExtractor(player.inventory, (CoreRoutedPipe) pipe.pipe, (ISneakyDirectionReceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
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
					return new GuiItemSink(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
				} else {
					ItemStack item = player.inventory.mainInventory[z];
					if(item == null) return null;
					LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, null, null, null, null);
					module.registerSlot(-1-z);
					ItemModuleInformationManager.readInformation(item, module);
					if(!(module instanceof ModuleItemSink)) return null;
					return new GuiItemSink(player.inventory, null, (ModuleItemSink) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_FluidSupplier_ID:
				if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleFluidSupplier)) return null;
				return new GuiFluidSupplier(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleFluidSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen);
				
			case GuiIDs.GUI_Module_PassiveSupplier_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModulePassiveSupplier)) return null;
					return new GuiPassiveSupplier(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModulePassiveSupplier) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen);
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
					return new GuiProvider(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleProvider) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
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
					return new GuiTerminus(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleTerminus) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen);
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
					return new GuiAdvancedExtractor(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleAdvancedExtractor) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
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
					return new GuiElectricManager(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleElectricManager) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), FMLClientHandler.instance().getClient().currentScreen, slot + 1);
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
					return new GuiApiaristSink((ModuleApiaristSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), player, (CoreRoutedPipe) pipe.pipe, FMLClientHandler.instance().getClient().currentScreen, slot + 1);
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
					return new GuiModBasedItemSink(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleModBasedItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot),  FMLClientHandler.instance().getClient().currentScreen, slot + 1);
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
					return new GuiThaumicAspectSink(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleThaumicAspectSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot),  FMLClientHandler.instance().getClient().currentScreen, slot + 1);
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
			
			case GuiIDs.GUI_Module_OreDict_ItemSink_ID:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleOreDictItemSink)) return null;
					return new GuiOreDictItemSink(player.inventory, (CoreRoutedPipe) pipe.pipe, (ModuleOreDictItemSink)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot),  FMLClientHandler.instance().getClient().currentScreen, slot + 1);
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
					if(!(module instanceof ModuleOreDictItemSink)) return null;
					return new GuiOreDictItemSink(player.inventory, null, (ModuleOreDictItemSink) module, null, slot);
				}
				
			case GuiIDs.GUI_Module_Apiarist_Analyzer:
				if(slot >= 0) {
					if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot) instanceof ModuleApiaristAnalyser)) return null;
					return new GuiApiaristAnalyser((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(slot), (CoreRoutedPipe) pipe.pipe, FMLClientHandler.instance().getClient().currentScreen, player.inventory);
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

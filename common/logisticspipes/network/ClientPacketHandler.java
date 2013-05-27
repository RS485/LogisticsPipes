package logisticspipes.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.config.Configs;
import logisticspipes.gui.GuiInvSysConnector;
import logisticspipes.gui.GuiProviderPipe;
import logisticspipes.gui.GuiSecurityStation;
import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.gui.modules.GuiAdvancedExtractor;
import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.gui.modules.GuiProvider;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.gui.popup.GuiDiskPopup;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.interfaces.ISendQueueContentRecieiver;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.interfaces.PlayerListReciver;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logic.BaseLogicLiquidSatellite;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.nei.LoadingHelper;
import logisticspipes.network.packets.GuiArgumentPacket;
import logisticspipes.network.packets.PacketBufferTransfer;
import logisticspipes.network.packets.PacketCoordinatesUUID;
import logisticspipes.network.packets.PacketCraftingLoop;
import logisticspipes.network.packets.PacketInteger;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketItem;
import logisticspipes.network.packets.PacketItems;
import logisticspipes.network.packets.PacketLiquidUpdate;
import logisticspipes.network.packets.PacketModuleInteger;
import logisticspipes.network.packets.PacketModuleInvContent;
import logisticspipes.network.packets.PacketModuleNBT;
import logisticspipes.network.packets.PacketNBT;
import logisticspipes.network.packets.PacketNameUpdatePacket;
import logisticspipes.network.packets.PacketPipeBitSet;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.network.packets.PacketPipeUpdate;
import logisticspipes.network.packets.PacketRenderFX;
import logisticspipes.network.packets.PacketRequestGuiContent;
import logisticspipes.network.packets.PacketRoutingStats;
import logisticspipes.network.packets.PacketSimulate;
import logisticspipes.network.packets.PacketStringList;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemMessage;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler {
	
	public static void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		final DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		onPacketData(data, player);
	}
	
	public static void onPacketData(DataInputStream data, Player player) {
		try {

			final int packetID = data.read();
			switch (packetID) {
				case NetworkConstants.CRAFTING_PIPE_SATELLITE_ID:
					final PacketPipeInteger packetA1 = new PacketPipeInteger();
					packetA1.readData(data);
					onCraftingPipeSetSatellite(packetA1);
					break;
				case NetworkConstants.CRAFTING_PIPE_IMPORT_BACK:
					final PacketInventoryChange packetA = new PacketInventoryChange();
					packetA.readData(data);
					onCraftingPipeSetImport(packetA);
					break;
				case NetworkConstants.SATELLITE_PIPE_SATELLITE_ID:
					final PacketPipeInteger packetB = new PacketPipeInteger();
					packetB.readData(data);
					onSatellitePipeSetSatellite(packetB);
					break;
				case NetworkConstants.ORDERER_CONTENT_ANSWER:
					final PacketRequestGuiContent packetC = new PacketRequestGuiContent();
					packetC.readData(data);
					onOrdererRefreshAnswer(packetC);
					break;
				case NetworkConstants.MISSING_ITEMS:
					final PacketItems packetD = new PacketItems();
					packetD.readData(data);
					onItemsResponse(packetD);
					break;
				case NetworkConstants.CRAFTING_LOOP:
					final PacketCraftingLoop packetE = new PacketCraftingLoop();
					packetE.readData(data);
					onCraftingLoop(packetE);
					break;
				case NetworkConstants.ITEM_SINK_STATUS:
					final PacketModuleInteger packetH = new PacketModuleInteger();
					packetH.readData(data);
					onItemSinkStatusRecive(packetH);
					break;
				case NetworkConstants.PROVIDER_PIPE_MODE_CONTENT:
					final PacketPipeInteger packetF = new PacketPipeInteger();
					packetF.readData(data);
					onProviderPipeModeRecive(packetF);
					break;
				case NetworkConstants.PROVIDER_PIPE_INCLUDE_CONTENT:
					final PacketPipeInteger packetG = new PacketPipeInteger();
					packetG.readData(data);
					onProviderPipeIncludeRecive(packetG);
					break;
				case NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE:
					final PacketPipeInteger packetI = new PacketPipeInteger();
					packetI.readData(data);
					onSupplierPipeRecive(packetI);
					break;
				case NetworkConstants.EXTRACTOR_MODULE_RESPONSE:
					final PacketModuleInteger packetJ = new PacketModuleInteger();
					packetJ.readData(data);
					onModulePipeRecive(packetJ);
					break;
				case NetworkConstants.PROVIDER_MODULE_MODE_CONTENT:
					final PacketPipeInteger packetK = new PacketPipeInteger();
					packetK.readData(data);
					onProviderModuleModeRecive(packetK);
					break;
				case NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT:
					final PacketPipeInteger packetL = new PacketPipeInteger();
					packetL.readData(data);
					onProviderModuleIncludeRecive(packetL);
					break;
				case NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE:
					final PacketModuleInteger packetM = new PacketModuleInteger();
					packetM.readData(data);
					onAdvancedExtractorModuleIncludeRecive(packetM);
					break;
				case NetworkConstants.DISK_CONTENT:
					final PacketItem packetO = new PacketItem();
					packetO.readData(data);
					handleRequestMK2DiskItem(packetO);
					break;
				case NetworkConstants.PIPE_UPDATE:
					final PacketPipeUpdate packetOa = new PacketPipeUpdate();
					packetOa.readData(data);
					handlePacketPipeUpdate(packetOa);
					break;
				case NetworkConstants.DISK_MACRO_REQUEST_RESPONSE:
					final PacketItems packetP = new PacketItems();
					packetP.readData(data);
					handleMacroResponse(packetP);
					break;
				case NetworkConstants.BEE_MODULE_CONTENT:
					final PacketModuleNBT packetQ = new PacketModuleNBT();
					packetQ.readData(data);
					handleBeePacketNBT(packetQ);
					break;
				case NetworkConstants.LIQUID_SUPPLIER_PARTIALS:
					final PacketPipeInteger packetR = new PacketPipeInteger();
					packetR.readData(data);
					onLiquidSupplierPartials(player,packetR);
					break;
				case NetworkConstants.INC_SYS_CON_CONTENT:
					final PacketRequestGuiContent packetS = new PacketRequestGuiContent();
					packetS.readData(data);
					onInvSysConGuiData(player,packetS);
					break;
				case NetworkConstants.SOLDERING_UPDATE_HEAT:
					final PacketPipeInteger packetT = new PacketPipeInteger();
					packetT.readData(data);
					onSolderingUpdateHeat(player, packetT);
					break;
				case NetworkConstants.SOLDERING_UPDATE_PROGRESS:
					final PacketPipeInteger packetU = new PacketPipeInteger();
					packetU.readData(data);
					onSolderingUpdateProgress(player, packetU);
					break;
				case NetworkConstants.SOLDERING_UPDATE_INVENTORY:
					final PacketInventoryChange packetV = new PacketInventoryChange();
					packetV.readData(data);
					onSolderingUpdateInventory(player, packetV);
					break;
				case NetworkConstants.PIPE_CHEST_CONTENT:
					final PacketPipeInvContent packetW = new PacketPipeInvContent();
					packetW.readData(data);
					onSateliteChestInv(player, packetW);
					break;
				case NetworkConstants.ORDER_MANAGER_CONTENT:
					final PacketPipeInvContent packetX = new PacketPipeInvContent();
					packetX.readData(data);
					onOrderManagerContent(player, packetX);
					break;
				case NetworkConstants.BUFFERED_PACKET_TRANSFER:
					final PacketBufferTransfer packetZ = new PacketBufferTransfer();
					packetZ.readData(data);
					onBufferTransfer(packetZ);
					break;
				case NetworkConstants.INC_SYS_CON_RESISTANCE:
					final PacketPipeInteger packetAa = new PacketPipeInteger();
					packetAa.readData(data);
					onInvSysConResistance(player, packetAa);
					break;
				case NetworkConstants.CHASSIE_PIPE_MODULE_CONTENT:
					final PacketPipeInvContent packetAb = new PacketPipeInvContent();
					packetAb.readData(data);
					onChassieInvRecive(player, packetAb);
					break;
				case NetworkConstants.MODULE_INV_CONTENT:
					final PacketModuleInvContent packetAc = new PacketModuleInvContent();
					packetAc.readData(data);
					onModuleInvRecive(packetAc);
					break;
				case NetworkConstants.ELECTRIC_MANAGER_STATE:
					final PacketModuleInteger packetAj = new PacketModuleInteger();
					packetAj.readData(data);
					onElectricModuleStateChange(packetAj);
					break;
				case NetworkConstants.SEND_QUEUE_CONTENT:
					final PacketPipeInvContent packetAk = new PacketPipeInvContent();
					packetAk.readData(data);
					onSendQueueInventory(packetAk);
					break;
				case NetworkConstants.ROTATION_SET:
					final PacketPipeInteger packetAl = new PacketPipeInteger();
					packetAl.readData(data);
					onRotationSet(packetAl);
					break;
				case NetworkConstants.CRAFTING_PIPE_PRIORITY:
					final PacketPipeInteger packetAm = new PacketPipeInteger();
					packetAm.readData(data);
					onPrioritySet(packetAm);
					break;
				case NetworkConstants.POWER_JUNCTION_POWER_LEVEL:
					final PacketPipeInteger packetAn = new PacketPipeInteger();
					packetAn.readData(data);
					onPowerLevel(packetAn);
					break;
				case NetworkConstants.STAT_UPDATE:
					final PacketRoutingStats packetAo = new PacketRoutingStats();
					packetAo.readData(data);
					onStatUpdate(packetAo);
					break;
				case NetworkConstants.ACTIVATNBTDEBUG:
					enableNBTDEBUG();
					break;
				case NetworkConstants.REQUEST_GUI_DIMENSION:
					final PacketInteger packetAq = new PacketInteger();
					packetAq.readData(data);
					onRequestDimension(packetAq);
					break;
				case NetworkConstants.PARTICLE_FX_RENDER_DATA:
					final PacketRenderFX packetAr = new PacketRenderFX();
					packetAr.readData(data);
					onParticleRenderUpdate(packetAr);
					break;
				case NetworkConstants.COMPONENT_LIST:
					final PacketSimulate packetAs = new PacketSimulate();
					packetAs.readData(data);
					onComponentList(packetAs);
					break;
				case NetworkConstants.REQUEST_UPDATE_NAMES:
					sendNamesToServer();
					break;
				case NetworkConstants.UPDATE_NAMES:
					final PacketNameUpdatePacket packetAt = new PacketNameUpdatePacket();
					packetAt.readData(data);
					onItemNameRequest(packetAt);
					break;
				case NetworkConstants.LIQUID_UPDATE_PACKET:
					final PacketLiquidUpdate packetAu = new PacketLiquidUpdate();
					packetAu.readData(data);
					break;
				case NetworkConstants.MODBASEDITEMSINKLIST:
					final PacketModuleNBT packetAv = new PacketModuleNBT();
					packetAv.readData(data);
					onModBasedItemSinkList(packetAv);
					break;
				case NetworkConstants.FIREWALL_FLAG_SET:
					final PacketPipeBitSet packetAw = new PacketPipeBitSet();
					packetAw.readData(data);
					onFirewallFlags(packetAw);
					break;
				case NetworkConstants.PLAYER_LIST:
					final PacketStringList packetAx = new PacketStringList();
					packetAx.readData(data);
					onPlayerList(packetAx);
					break;
				case NetworkConstants.SECURITY_STATION_ID:
					final PacketCoordinatesUUID packetAy = new PacketCoordinatesUUID();
					packetAy.readData(data);
					onSecurityID(packetAy);
					break;
				case NetworkConstants.OPEN_SECURITY_PLAYER:
					final PacketNBT packetAz = new PacketNBT();
					packetAz.readData(data);
					onOpenSecurityPlayer(packetAz);
					break;
				case NetworkConstants.THAUMICASPECTSINKLIST:
					final PacketModuleNBT packetBa = new PacketModuleNBT();
					packetBa.readData(data);
					onThaumicAspectList(packetBa);
					break;
				case NetworkConstants.SET_SECURITY_CC:
					final PacketPipeInteger packetBb = new PacketPipeInteger();
					packetBb.readData(data);
					onSetSecurityCC(packetBb);
					break;
				case NetworkConstants.GUI_ARGUMENT_PACKET:
					final GuiArgumentPacket packetBc = new GuiArgumentPacket();
					packetBc.readData(data);
					GuiHandler.argumentQueue.put(packetBc.guiID, packetBc.args);
					break;
				case NetworkConstants.CRAFTING_PIPE_SATELLITE_ID_ADVANCED:
					final PacketModuleInteger packetBd = new PacketModuleInteger();
					packetBd.readData(data);
					onCraftingPipeSetSatelliteAdvanced(packetBd);
					break;
				case NetworkConstants.SECURITY_AUTHORIZEDLIST_UPDATE:
					final PacketStringList packetBe = new PacketStringList();
					packetBe.readData(data);
					onSecurityAuthorizationListUpdate(packetBe);
					break;
				case NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE:
					final PacketModuleInteger packetBf = new PacketModuleInteger();
					packetBf.readData(data);
					onApiaristAnalyserChangeExtract(packetBf);
					break;
				case NetworkConstants.SEND_CC_IDS:
					final PacketNBT packetBg = new PacketNBT();
					packetBg.readData(data);
					onCCIDs(packetBg);
					break;
				case NetworkConstants.SET_SECURITY_DESTROY:
					final PacketPipeInteger packetBh = new PacketPipeInteger();
					packetBh.readData(data);
					onSetSecurityDestroy(packetBh);
					break;
				case NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED:
					final PacketModuleInteger packetBi = new PacketModuleInteger();
					packetBi.readData(data);
					onCraftingPipeSetLiquidSatelliteAdvanced(packetBi);
					break;
				case NetworkConstants.LIQUID_CRAFTING_PIPE_AMOUNT:
					final PacketModuleInteger packetBn = new PacketModuleInteger();
					packetBn.readData(data);
					onCraftingPipeLiquidAmountChange(packetBn);
					break;
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void onCraftingPipeSetSatellite(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setSatelliteId(packet.integer, -1);
	}

	private static void onCraftingPipeSetImport(PacketInventoryChange packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		final BaseLogicCrafting craftingPipe = (BaseLogicCrafting) pipe.pipe.logic;
		for (int i = 0; i < packet.itemStacks.size(); i++) {
			craftingPipe.setDummyInventorySlot(i, packet.itemStacks.get(i));
		}
	}

	private static void onSatellitePipeSetSatellite(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (pipe.pipe.logic instanceof BaseLogicSatellite) {
			((BaseLogicSatellite) pipe.pipe.logic).setSatelliteId(packet.integer);
		}
		if (pipe.pipe.logic instanceof BaseLogicLiquidSatellite) {
			((BaseLogicLiquidSatellite) pipe.pipe.logic).setSatelliteId(packet.integer);
		}
	}

	private static void onOrdererRefreshAnswer(PacketRequestGuiContent packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).handlePacket(packet);
		}
	}

	private static void onItemsResponse(PacketItems packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(packet.items,!packet.error,(GuiOrderer)FMLClientHandler.instance().getClient().currentScreen,FMLClientHandler.instance().getClient().thePlayer);
		} else if(packet.error) {
			for (final ItemMessage items : packet.items) {
				FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Missing: " + items);
			}
		} else {
			for (final ItemMessage items : packet.items) {
				FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Requested: " + items);
			}
			FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Request successful!");
		}
	}

	private static void onCraftingLoop(PacketCraftingLoop packet) {
		final ItemIdentifier item = packet.items.get(0).getItemIdentifier();
		FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Logistics: Possible crafting loop while trying to craft " + item.getFriendlyName()
				+ " !! ABORTING !!");
	}

	private static void onItemSinkStatusRecive(PacketModuleInteger packet) {
		if(packet.slot < 0) {
			return;
		}
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		
		if(packet.slot == -1) {
			if (!(pipe.pipe instanceof CoreRoutedPipe)) {
				return;
			}
			if(!(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) {
				return;
			}
			ModuleItemSink module = (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule();
			module.setDefaultRoute(packet.integer == 1);
			return;
		}
		
		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}
		if(((PipeLogisticsChassi)pipe.pipe).getModules() == null) return;
		if(((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleItemSink) {
			ModuleItemSink module = (ModuleItemSink) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot);
			module.setDefaultRoute(packet.integer == 1);
		}
	}

	private static void onProviderPipeModeRecive(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicProvider)) {
			return;
		}
		
		((LogicProvider) pipe.pipe.logic).setExtractionMode(packet.integer);
	}

	private static void onProviderPipeIncludeRecive(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicProvider)) {
			return;
		}
		((LogicProvider) pipe.pipe.logic).setFilterExcluded(packet.integer == 1);
		
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiProviderPipe) {
			((GuiProviderPipe) FMLClientHandler.instance().getClient().currentScreen).refreshInclude();
		}
	}

	private static void onSupplierPipeRecive(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSupplier)) {
			return;
		}
		((LogicSupplier) pipe.pipe.logic).setRequestingPartials(packet.integer == 1);
		
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSupplierPipe) {
			((GuiSupplierPipe) FMLClientHandler.instance().getClient().currentScreen).refreshMode();
		}
	}

	private static void onModulePipeRecive(PacketModuleInteger packet) {
		if(packet.slot < 0) {
			if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiExtractor) {
				((GuiExtractor) FMLClientHandler.instance().getClient().currentScreen).setMode(ForgeDirection.getOrientation(packet.integer));
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		
		if(packet.slot == -1) {
			if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyDirectionReceiver) {
				((ISneakyDirectionReceiver)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).setSneakyDirection(ForgeDirection.getOrientation(packet.integer));
			}
			return;
		}
		
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ISneakyDirectionReceiver) {
			ISneakyDirectionReceiver recieiver = (ISneakyDirectionReceiver) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot);
			recieiver.setSneakyDirection(ForgeDirection.getOrientation(packet.integer));
		}
	}

	private static void onProviderModuleModeRecive(PacketPipeInteger packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiProvider) {
			((GuiProvider) FMLClientHandler.instance().getClient().currentScreen).handleModuleModeRecive(packet);
		}
	}

	private static void onProviderModuleIncludeRecive(PacketPipeInteger packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiProvider) {
			((GuiProvider) FMLClientHandler.instance().getClient().currentScreen).handleModuleIncludeRecive(packet);
		}
	}

	private static void onAdvancedExtractorModuleIncludeRecive(PacketModuleInteger packet) {
		if(packet.slot < 0) {
			if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiAdvancedExtractor) {
				((GuiAdvancedExtractor) FMLClientHandler.instance().getClient().currentScreen).setInclude(packet.integer == 1);
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		
		if(packet.slot == -1) {
			if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				((ModuleAdvancedExtractor)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).setItemsIncluded(packet.integer == 1);
			}
			return;
		}
		
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleAdvancedExtractor) {
			ModuleAdvancedExtractor recieiver = (ModuleAdvancedExtractor) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot);
			recieiver.setItemsIncluded(packet.integer == 1);
		}
	}

	private static void handlePacketPipeUpdate(PacketPipeUpdate packet) {
		TileGenericPipe tile = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(tile == null) {
			return;
		}
		if(tile.pipe == null) {
			return;
		}
		new TilePacketWrapper(new Class[] { TileGenericPipe.class, tile.pipe.transport.getClass(), tile.pipe.logic.getClass() }).fromPayload(new Object[] { tile.pipe.container, tile.pipe.transport, tile.pipe.logic },packet.getPayload());
	}

	private static void handleRequestMK2DiskItem(PacketItem packet) {
		final TileGenericPipe tile = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof PipeItemsRequestLogisticsMk2) {
			((PipeItemsRequestLogisticsMk2)tile.pipe).setDisk(packet.itemstack);
		}
	}
	
	private static void handleMacroResponse(PacketItems packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			if(((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).getSubGui() instanceof GuiDiskPopup) {
				((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(packet.items, packet.error, ((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).getSubGui(),FMLClientHandler.instance().getClient().thePlayer);
			}
		}
	}

	private static void handleBeePacketNBT(PacketModuleNBT packet) {
		if(packet.slot < 0) {
			//EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
			//ItemStack module = player.inventory.mainInventory[packet.posZ];
			return;
		}
		final TileGenericPipe tile = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(tile == null) {
			return;
		}
		ModuleApiaristSink sink;
		if(packet.slot == -1 && tile.pipe instanceof PipeItemsApiaristSink) {
			sink = (ModuleApiaristSink) ((PipeItemsApiaristSink)tile.pipe).getLogisticsModule();
		} else if(tile.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)tile.pipe).getLogisticsModule().getSubModule(packet.slot) instanceof ModuleApiaristSink) {
			sink = (ModuleApiaristSink) ((CoreRoutedPipe)tile.pipe).getLogisticsModule().getSubModule(packet.slot);
		} else {
			return;
		}
		packet.handle(sink);
	}

	private static void onLiquidSupplierPartials(Player player, PacketPipeInteger packet) {
		final TileGenericPipe tile = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof PipeItemsLiquidSupplier && tile.pipe.logic instanceof LogicLiquidSupplier) {
			((LogicLiquidSupplier)tile.pipe.logic).setRequestingPartials((packet.integer % 10) == 1);		
		}
	}
	
	private static void onInvSysConGuiData(Player player, PacketRequestGuiContent packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiInvSysConnector) {
			((GuiInvSysConnector) FMLClientHandler.instance().getClient().currentScreen).handleContentAnswer(packet._allItems);
		}
	}

	private static void onSolderingUpdateHeat(Player player, PacketPipeInteger packet) {
		final TileEntity tile = FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSolderingTileEntity) {
			LogisticsSolderingTileEntity station = (LogisticsSolderingTileEntity) tile;
			int old = station.heat;
			station.heat = packet.integer;
			if((station.heat == 0 && old != 0) || (station.heat != 0 && old == 0)) {
				FMLClientHandler.instance().getClient().theWorld.markBlockForUpdate(packet.posX, packet.posY, packet.posZ);
			}
		}
	}

	private static void onSolderingUpdateProgress(Player player, PacketPipeInteger packet) {
		final TileEntity tile = FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSolderingTileEntity) {
			LogisticsSolderingTileEntity station = (LogisticsSolderingTileEntity) tile;
			station.progress = packet.integer;
		}
	}

	private static void onSolderingUpdateInventory(Player player, PacketInventoryChange packet) {
		final TileEntity tile = FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSolderingTileEntity) {
			LogisticsSolderingTileEntity station = (LogisticsSolderingTileEntity) tile;
			for(int i=0;i<station.getSizeInventory();i++) {
				if(i >= packet.itemStacks.size()) break;
				ItemStack stack = packet.itemStacks.get(i);
				station.setInventorySlotContents(i, stack);
			}
		}
	}

	private static void onSateliteChestInv(Player player, PacketPipeInvContent packet) {
		final TileGenericPipe tile = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof IChestContentReceiver) {
			((IChestContentReceiver)tile.pipe).setReceivedChestContent(packet._allItems);
		}
	}

	private static void onOrderManagerContent(Player player, PacketPipeInvContent packet) {
		final TileGenericPipe tile = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof IOrderManagerContentReceiver) {
			((IOrderManagerContentReceiver)tile.pipe).setOrderManagerContent(packet._allItems);
		}
	}

	private static void onBufferTransfer(PacketBufferTransfer packet) {
		SimpleServiceLocator.clientBufferHandler.handlePacket(packet);
	}

	private static void onInvSysConResistance(Player player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsInvSysConnector) {
			PipeItemsInvSysConnector invCon = (PipeItemsInvSysConnector) pipe.pipe;
			invCon.resistance = packet.integer;
		}
	}

	private static void onChassieInvRecive(Player player, PacketPipeInvContent packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeLogisticsChassi) {
			PipeLogisticsChassi chassie = (PipeLogisticsChassi) pipe.pipe;
			chassie.handleModuleItemIdentifierList(packet._allItems);
		}
	}

	private static void onModuleInvRecive(PacketModuleInvContent packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof IModuleInventoryReceive) {
			IModuleInventoryReceive module = (IModuleInventoryReceive) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot);
			module.handleInvContent(packet._allItems);
		}
	}

	private static void onElectricModuleStateChange(PacketModuleInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleElectricManager) {
			ModuleElectricManager module = (ModuleElectricManager) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot);
			module.setDischargeMode(packet.integer == 1);
		}
	}

	private static void onSendQueueInventory(PacketPipeInvContent packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof ISendQueueContentRecieiver) {
			ISendQueueContentRecieiver receiver = (ISendQueueContentRecieiver) pipe.pipe;
			receiver.handleSendQueueItemIdentifierList(packet._allItems);
		}
	}

	private static void onRotationSet(PacketPipeInteger packet) {
		TileEntity tile = FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof IRotationProvider) {
			((IRotationProvider)tile).setRotation(packet.integer);
			FMLClientHandler.instance().getClient().theWorld.markBlockForUpdate(packet.posX, packet.posY, packet.posZ);
		}
	}

	private static void onPrioritySet(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setPriority(packet.integer);
	}

	private static void onPowerLevel(PacketPipeInteger packet) {
		TileEntity tile = FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsPowerJuntionTileEntity_BuildCraft) {
			((LogisticsPowerJuntionTileEntity_BuildCraft)tile).handlePowerPacket(packet);
		}
	}

	private static void onStatUpdate(PacketRoutingStats packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		CoreRoutedPipe cPipe = (CoreRoutedPipe) pipe.pipe;

		cPipe.stat_session_sent = packet.stat_session_sent;
		cPipe.stat_session_recieved = packet.stat_session_recieved;
		cPipe.stat_session_relayed = packet.stat_session_relayed;
		cPipe.stat_lifetime_sent = packet.stat_lifetime_sent;
		cPipe.stat_lifetime_recieved = packet.stat_lifetime_recieved;
		cPipe.stat_lifetime_relayed = packet.stat_lifetime_relayed;
		cPipe.server_routing_table_size = packet.server_routing_table_size;
	}

	private static void onParticleRenderUpdate(PacketRenderFX packet) {
		int x = packet.posX;
		int y = packet.posY;
		int z = packet.posZ;
		int particle = packet.particle;
		int amount = packet.amount;
		MainProxy.spawnParticle(particle, x, y, z, amount);
	}
	
	private static void enableNBTDEBUG() {
		try {
			Class.forName("codechicken.nei.forge.GuiContainerManager");
			Configs.TOOLTIP_INFO = true;
			LoadingHelper.LoadNeiNBTDebugHelper();
		} catch(ClassNotFoundException e) {
			
		} catch(Exception e1) {
			if(LogisticsPipes.DEBUG) {
				e1.printStackTrace();
			}
		}
	}

	private static void onRequestDimension(PacketInteger packet) {
		if(FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).dimension = packet.value;
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).refreshItems();
		} else {
			GuiOrderer.dimensioncache = packet.value;
			GuiOrderer.cachetime = System.currentTimeMillis();
		}
	}

	private static void onComponentList(PacketSimulate packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).handleSimulateAnswer(packet.used,packet.missing,(GuiOrderer)FMLClientHandler.instance().getClient().currentScreen,FMLClientHandler.instance().getClient().thePlayer);
		} else {
			for (final ItemMessage items : packet.used) {
				FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Used: " + items);
			}
			for (final ItemMessage items : packet.missing) {
				FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Missing: " + items);
			}
		}
	}

	private static void sendNamesToServer() {
		Item[] itemList = Item.itemsList;
		List<ItemIdentifier> identList = new LinkedList<ItemIdentifier>();
		for(Item item:itemList) {
			if(item != null) {
				for(CreativeTabs tab:item.getCreativeTabs()) {
					List<ItemStack> list = new ArrayList<ItemStack>();
					item.getSubItems(item.itemID, tab, list);
					if(list.size() > 0) {
						for(ItemStack stack:list) {
							identList.add(ItemIdentifier.get(stack));
						}
					} else {
						identList.add(ItemIdentifier.get(item.itemID, 0, null));
					}
				}
			}
		}
		SimpleServiceLocator.clientBufferHandler.setPause(true);
		for(ItemIdentifier item:identList) {
			MainProxy.sendCompressedPacketToServer((Packet250CustomPayload)new PacketNameUpdatePacket(item).getPacket());
		}
		SimpleServiceLocator.clientBufferHandler.setPause(false);
		FMLClientHandler.instance().getClient().thePlayer.addChatMessage("Names in send Queue");
	}

	private static void onItemNameRequest(PacketNameUpdatePacket packetAt) {
		MainProxy.sendCompressedPacketToServer((Packet250CustomPayload)new PacketNameUpdatePacket(packetAt.item).getPacket());
	}

	private static void onModBasedItemSinkList(PacketModuleNBT packet) {
		final TileGenericPipe pipe = getPipe(MainProxy.getClientMainWorld(), packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleModBasedItemSink) {
			((ModuleModBasedItemSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot)).readFromNBT(packet.tag);
			((ModuleModBasedItemSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot)).ModListChanged();
		}
	}

	private static void onThaumicAspectList(PacketModuleNBT packet) {
		final TileGenericPipe pipe = getPipe(MainProxy.getClientMainWorld(), packet.posX, packet.posY, packet.posZ);
		if (pipe == null) return;
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleThaumicAspectSink) {
			((ModuleThaumicAspectSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot)).readFromNBT(packet.tag);
			((ModuleThaumicAspectSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot)).aspectListChanged();
		}
	}

	private static void onFirewallFlags(PacketPipeBitSet packet) {
		final TileGenericPipe pipe = getPipe(MainProxy.getClientMainWorld(), packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeItemsFirewall) {
			PipeItemsFirewall firewall = (PipeItemsFirewall) pipe.pipe;
			firewall.setFlags(packet.flags);
		}
	}

	private static void onPlayerList(PacketStringList packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof PlayerListReciver) {
			((PlayerListReciver)FMLClientHandler.instance().getClient().currentScreen).recivePlayerList(packet.list);
		}
	}

	private static void onSecurityID(PacketCoordinatesUUID packet) {
		TileEntity tile = MainProxy.getClientMainWorld().getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSecurityTileEntity) {
			((LogisticsSecurityTileEntity)tile).setClientUUID(packet.uuid);
		}
	}

	private static void onOpenSecurityPlayer(PacketNBT packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSecurityStation) {
			SecuritySettings setting = new SecuritySettings(null);
			setting.readFromNBT(packet.tag);
			((GuiSecurityStation)FMLClientHandler.instance().getClient().currentScreen).handlePlayerSecurityOpen(setting);
		}
	}

	private static void onSetSecurityCC(PacketPipeInteger packet) {
		TileEntity tile = MainProxy.getClientMainWorld().getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSecurityTileEntity) {
			((LogisticsSecurityTileEntity)tile).setClientCC(packet.integer == 1);
			if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSecurityStation) {
				((GuiSecurityStation)FMLClientHandler.instance().getClient().currentScreen).refreshCheckBoxes();
			}
		}
	}

	private static void onSecurityAuthorizationListUpdate(PacketStringList packet) {
		SimpleServiceLocator.securityStationManager.setClientAuthorizationList(packet.list);
	}

	private static void onApiaristAnalyserChangeExtract(PacketModuleInteger packet) {
		final TileGenericPipe pipe = getPipe(MainProxy.getClientMainWorld(), packet.posX, packet.posY, packet.posZ);
		if (pipe == null) return;
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleApiaristAnalyser) {
			((ModuleApiaristAnalyser)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot)).setExtractMode(packet.integer);
		}
		if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristAnalyser) {
			((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).setExtractMode(packet.integer);
		}
	}

	private static void onCraftingPipeSetSatelliteAdvanced(PacketModuleInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setSatelliteId(packet.integer, packet.slot);
	}

	private static void onCCIDs(PacketNBT packet) {
		TileEntity tile = MainProxy.getClientMainWorld().getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSecurityTileEntity) {
			((LogisticsSecurityTileEntity)tile).handleListPacket(packet);
		}
	}

	private static void onSetSecurityDestroy(PacketPipeInteger packet) {
		TileEntity tile = MainProxy.getClientMainWorld().getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSecurityTileEntity) {
			((LogisticsSecurityTileEntity)tile).setClientDestroy(packet.integer == 1);
			if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSecurityStation) {
				((GuiSecurityStation)FMLClientHandler.instance().getClient().currentScreen).refreshCheckBoxes();
			}
		}
	}

	private static void onCraftingPipeSetLiquidSatelliteAdvanced(PacketModuleInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setLiquidSatelliteId(packet.integer, packet.slot);
	}

	private static void onCraftingPipeLiquidAmountChange(PacketModuleInteger packet) {
		final TileGenericPipe pipe = getPipe(FMLClientHandler.instance().getClient().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).defineLiquidAmount(packet.integer, packet.slot);
	}
	
	// BuildCraft method
	/**
	 * Retrieves pipe at specified coordinates if any.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static TileGenericPipe getPipe(World world, int x, int y, int z) {
		if(world == null) return null;
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		final TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe)) {
			return null;
		}

		return (TileGenericPipe) tile;
	}
	// BuildCraft method end

}

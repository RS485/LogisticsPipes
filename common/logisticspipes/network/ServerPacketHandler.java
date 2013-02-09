package logisticspipes.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.gui.PacketStringCoordinates;
import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.interfaces.ISneakyOrientationreceiver;
import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleApiaristSink.FilterType;
import logisticspipes.modules.ModuleCrafting;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.network.packets.PacketBufferTransfer;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketHUDSettings;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketItem;
import logisticspipes.network.packets.PacketModuleInteger;
import logisticspipes.network.packets.PacketModuleInventoryChange;
import logisticspipes.network.packets.PacketModuleNBT;
import logisticspipes.network.packets.PacketNameUpdatePacket;
import logisticspipes.network.packets.PacketPipeBeePacket;
import logisticspipes.network.packets.PacketPipeBitSet;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeString;
import logisticspipes.network.packets.PacketPipeUpdate;
import logisticspipes.network.packets.PacketRequestGuiContent;
import logisticspipes.network.packets.PacketRequestSubmit;
import logisticspipes.network.packets.PacketStringList;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestHandler;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SneakyOrientation;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler {

	public static void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		final DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		onPacketData(data, player);
	}
	
	public static void onPacketData(DataInputStream data, Player playerFML) {
		EntityPlayerMP player = (EntityPlayerMP) playerFML;
		
		try {
			final int packetID = data.read();
			switch (packetID) {

				case NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE:
					final PacketCoordinates packet = new PacketCoordinates();
					packet.readData(data);
					onCraftingPipeNextSatellite(player, packet);
					break;

				case NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE:
					final PacketCoordinates packetA = new PacketCoordinates();
					packetA.readData(data);
					onCraftingPipePrevSatellite(player, packetA);
					break;
				case NetworkConstants.CRAFTING_PIPE_IMPORT:
					final PacketCoordinates packetB = new PacketCoordinates();
					packetB.readData(data);
					onCraftingPipeImport(player, packetB);
					break;
				case NetworkConstants.CRAFTING_MODULE_NEXT_SATELLITE:
					final PacketPipeInteger packet1 = new PacketPipeInteger();
					packet1.readData(data);
					onCraftingModuleNextSatellite(player, packet1);
					break;

				case NetworkConstants.CRAFTING_MODULE_PREV_SATELLITE:
					final PacketPipeInteger packetA1 = new PacketPipeInteger();
					packetA1.readData(data);
					onCraftingModulePrevSatellite(player, packetA1);
					break;
				case NetworkConstants.CRAFTING_MODULE_IMPORT:
					final PacketPipeInteger packetB1 = new PacketPipeInteger();
					packetB1.readData(data);
					onCraftingModuleImport(player, packetB1);
					break;
				case NetworkConstants.CRAFTING_MODULE_OPEN_CONNECTED_GUI:
					final PacketPipeInteger packetB2 = new PacketPipeInteger();
					packetB2.readData(data);
					onCraftingModuleOpenConnectedGui(player, packetB2);
					break;
				case NetworkConstants.SATELLITE_PIPE_NEXT:
					final PacketCoordinates packetC = new PacketCoordinates();
					packetC.readData(data);
					onSatellitePipeNext(player, packetC);
					break;
				case NetworkConstants.SATELLITE_PIPE_PREV:
					final PacketCoordinates packetD = new PacketCoordinates();
					packetD.readData(data);
					onSatellitePipePrev(player, packetD);
					break;
				case NetworkConstants.CHASSI_GUI_PACKET_ID:
					final PacketPipeInteger packetE = new PacketPipeInteger();
					packetE.readData(data);
					onModuleGuiOpen(player, packetE);
					break;
				case NetworkConstants.GUI_BACK_PACKET:
					final PacketPipeInteger packetF = new PacketPipeInteger();
					packetF.readData(data);
					onGuiBackOpen(player, packetF);
					break;
				case NetworkConstants.REQUEST_SUBMIT:
					final PacketRequestSubmit packetG = new PacketRequestSubmit();
					packetG.readData(data);
					onRequestSubmit(player, packetG);
					break;
				case NetworkConstants.ORDERER_REFRESH_REQUEST:
					final PacketPipeInteger packetH = new PacketPipeInteger();
					packetH.readData(data);
					onRefreshRequest(player, packetH);
					break;
				case NetworkConstants.ITEM_SINK_DEFAULT:
					final PacketPipeInteger packetI = new PacketPipeInteger();
					packetI.readData(data);
					onItemSinkDefault(player, packetI);
					break;
				case NetworkConstants.PROVIDER_PIPE_NEXT_MODE:
					final PacketCoordinates packetJ = new PacketCoordinates();
					packetJ.readData(data);
					onProviderModeChange(player, packetJ);
					break;
				case NetworkConstants.PROVIDER_PIPE_CHANGE_INCLUDE:
					final PacketCoordinates packetK = new PacketCoordinates();
					packetK.readData(data);
					onProviderIncludeChange(player, packetK);
					break;
				case NetworkConstants.SUPPLIER_PIPE_MODE_CHANGE:
					final PacketCoordinates packetL = new PacketCoordinates();
					packetL.readData(data);
					onSupplierModeChange(player, packetL);
					break;
				case NetworkConstants.EXTRACTOR_MODULE_DIRECTION_SET:
					final PacketPipeInteger packetM = new PacketPipeInteger();
					packetM.readData(data);
					onExtractorModeChange(player, packetM);
					break;
				case NetworkConstants.PROVIDER_MODULE_NEXT_MODE:
					final PacketPipeInteger packetN = new PacketPipeInteger();
					packetN.readData(data);
					onProviderModuleModeChange(player, packetN);
					break;
				case NetworkConstants.PROVIDER_MODULE_CHANGE_INCLUDE:
					final PacketPipeInteger packetO = new PacketPipeInteger();
					packetO.readData(data);
					onProviderModuleIncludeChange(player, packetO);
					break;
				case NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_SET:
					final PacketPipeInteger packetP = new PacketPipeInteger();
					packetP.readData(data);
					onAdvancedExtractorModuleIncludeChange(player, packetP);
					break;
				case NetworkConstants.ADVANCED_EXTRACTOR_MODULE_SNEAKY_GUI:
					final PacketPipeInteger packetQ = new PacketPipeInteger();
					packetQ.readData(data);
					onAdvancedExtractorModuleGuiSneaky(player, packetQ);
					break;
				case NetworkConstants.REQUEST_PIPE_UPDATE:
					final PacketCoordinates packetR = new PacketCoordinates();
					packetR.readData(data);
					onPipeUpdateRequest(player, packetR);
					break;
				case NetworkConstants.REQUEST_CRAFTING_PIPE_UPDATE:
					final PacketCoordinates packetS = new PacketCoordinates();
					packetS.readData(data);
					onCraftingPipeUpdateRequest(player, packetS);
					break;
				case NetworkConstants.CRAFTING_PIPE_OPEN_CONNECTED_GUI:
					final PacketCoordinates packetT = new PacketCoordinates();
					packetT.readData(data);
					onCraftingPipeOpenConnectedGui(player, packetT);
					break;
				case NetworkConstants.DISK_REQUEST_CONTENT:
					final PacketCoordinates packetU = new PacketCoordinates();
					packetU.readData(data);
					onDiskContentRequest(player, packetU);
					break;
				case NetworkConstants.DISK_SET_NAME:
					final PacketPipeString packetV = new PacketPipeString();
					packetV.readData(data);
					onDiskSetName(player, packetV);
					break;
				case NetworkConstants.DISK_CONTENT:
					final PacketItem packetW = new PacketItem();
					packetW.readData(data);
					onDiskChangeClientSide(player, packetW);
					break;
				case NetworkConstants.DISK_DROP:
					final PacketCoordinates packetX = new PacketCoordinates();
					packetX.readData(data);
					onDiskDrop(player, packetX);
					break;
				case NetworkConstants.DISK_MACRO_REQUEST:
					final PacketPipeInteger packetY = new PacketPipeInteger();
					packetY.readData(data);
					onDiskMacroRequest(player, packetY);
					break;
				case NetworkConstants.BEE_MODULE_SET_BEE:
					final PacketPipeBeePacket packetZ = new PacketPipeBeePacket();
					packetZ.readData(data);
					onBeeModuleSetBee(player, packetZ);
					break;
				case NetworkConstants.LIQUID_SUPPLIER_PARTIALS:
					final PacketPipeInteger packetAa = new PacketPipeInteger();
					packetAa.readData(data);
					onLiquidSupplierPartials(player, packetAa);
					break;
				case NetworkConstants.INC_SYS_CON_CONTENT:
					final PacketCoordinates packetAb = new PacketCoordinates();
					packetAb.readData(data);
					onInvSysContentRequest(player, packetAb);
					break;
				case NetworkConstants.HUD_START_WATCHING:
					final PacketPipeInteger packetAc = new PacketPipeInteger();
					packetAc.readData(data);
					onHUDWatchingChange(player, packetAc, true);
					break;
				case NetworkConstants.HUD_STOP_WATCHING:
					final PacketPipeInteger packetAd = new PacketPipeInteger();
					packetAd.readData(data);
					onHUDWatchingChange(player, packetAd, false);
					break;
				case NetworkConstants.INC_SYS_CON_RESISTANCE:
					final PacketPipeInteger packetAf = new PacketPipeInteger();
					packetAf.readData(data);
					onInvSysConResistance(player, packetAf);
					break;
				case NetworkConstants.CRAFTING_PIPE_STACK_MOVE:
					final PacketPipeInteger packetAg = new PacketPipeInteger();
					packetAg.readData(data);
					onCraftingPipeStackMove(player, packetAg);
					break;
				case NetworkConstants.HUD_START_WATCHING_MODULE:
					final PacketPipeInteger packetAh = new PacketPipeInteger();
					packetAh.readData(data);
					onHUDModuleWatchingChange(player, packetAh, true);
					break;
				case NetworkConstants.HUD_STOP_WATCHING_MODULE:
					final PacketPipeInteger packetAi = new PacketPipeInteger();
					packetAi.readData(data);
					onHUDModuleWatchingChange(player, packetAi, false);
					break;
				case NetworkConstants.ELECTRIC_MANAGER_SET:
					final PacketModuleInteger packetAj = new PacketModuleInteger();
					packetAj.readData(data);
					onElectricModuleStateChange(player, packetAj);
					break;
				case NetworkConstants.ROTATION_REQUEST:
					final PacketCoordinates packetAk = new PacketCoordinates();
					packetAk.readData(data);
					onRotationRequest(player, packetAk);
					break;
				case NetworkConstants.CRAFTING_PIPE_PRIORITY_UP:
					final PacketCoordinates packetAl = new PacketCoordinates();
					packetAl.readData(data);
					onPriorityUp(player, packetAl);
					break;
				case NetworkConstants.CRAFTING_PIPE_PRIORITY_DOWN:
					final PacketCoordinates packetAm = new PacketCoordinates();
					packetAm.readData(data);
					onPriorityDown(player, packetAm);
					break;
				case NetworkConstants.CRAFTING_MODULE_PRIORITY_UP:
					final PacketPipeInteger packetAl1 = new PacketPipeInteger();
					packetAl1.readData(data);
					onCraftingModulePriorityUp(player, packetAl1);
					break;
				case NetworkConstants.CRAFTING_MODULE_PRIORITY_DOWN:
					final PacketPipeInteger packetAm1 = new PacketPipeInteger();
					packetAm1.readData(data);
					onCraftingModulePriorityDown(player, packetAm1);
					break;
				case NetworkConstants.HUD_START_WATCHING_BLOCK:
					final PacketCoordinates packetAn = new PacketCoordinates();
					packetAn.readData(data);
					onHUDBlockWatch(player, packetAn, true);
					break;
				case NetworkConstants.HUD_STOP_WATCHING_BLOCK:
					final PacketCoordinates packetAo = new PacketCoordinates();
					packetAo.readData(data);
					onHUDBlockWatch(player, packetAo, false);
					break;
				case NetworkConstants.HUD_SETTING_SET:
					final PacketHUDSettings packetAp = new PacketHUDSettings();
					packetAp.readData(data);
					onHUDSettings(player, packetAp);
					break;
				case NetworkConstants.REQUEST_COMPONENTS:
					final PacketRequestSubmit packetAq = new PacketRequestSubmit();
					packetAq.readData(data);
					onRequestComponents(player, packetAq);
					break;
				case NetworkConstants.BUFFERED_PACKET_TRANSFER:
					final PacketBufferTransfer packetAr = new PacketBufferTransfer();
					packetAr.readData(data);
					onBufferTransfer(packetAr, playerFML);
					break;
				case NetworkConstants.UPDATE_NAMES:
					final PacketNameUpdatePacket packetAs = new PacketNameUpdatePacket();
					packetAs.readData(data);
					onNameUpdate(packetAs);
					break;
				case NetworkConstants.ORDERER_LIQUID_REFRESH_REQUEST:
					final PacketPipeInteger packetAt = new PacketPipeInteger();
					packetAt.readData(data);
					onLiquidRefreshRequest(player, packetAt);
					break;
				case NetworkConstants.LIQUID_REQUEST_SUBMIT:
					final PacketRequestSubmit packetAu = new PacketRequestSubmit();
					packetAu.readData(data);
					onLiquidRequestSubmit(player, packetAu);
					break;
				case NetworkConstants.MODBASEDITEMSINKLIST:
					final PacketModuleNBT packetAv = new PacketModuleNBT();
					packetAv.readData(data);
					onModBasedItemSinkList(player, packetAv);
					break;
				case NetworkConstants.FIREWALL_FLAG_SET:
					final PacketPipeBitSet packetAw = new PacketPipeBitSet();
					packetAw.readData(data);
					onFirewallFlags(player, packetAw);
					break;
				case NetworkConstants.REQUEST_CRAFTING_MODULE_UPDATE:
					final PacketPipeInteger packetAx1 = new PacketPipeInteger();
					packetAx1.readData(data);
					onCraftingModuleUpdate(player, packetAx1);
					break;
				case NetworkConstants.SECURITY_CARD:
					final PacketPipeInteger packetAx = new PacketPipeInteger();
					packetAx.readData(data);
					onSecurityCardButton(player, packetAx);
					break;
				case NetworkConstants.THAUMICASPECTSINKLIST:
					final PacketModuleNBT packetAy = new PacketModuleNBT();
					packetAy.readData(data);
					onThaumicAspectSinkList(player, packetAy);
					break;
				case NetworkConstants.CHEATJUNCTIONPOWER:
					if (!LogisticsPipes.DEBUG) break;
					final PacketCoordinates packetAz = new PacketCoordinates();
					packetAz.readData(data);
					onCheatJunctionPower(player, packetAz);
					break;
				case NetworkConstants.PLAYER_LIST:
					onPlayerListRequest(player);
					break;
				case NetworkConstants.OPEN_SECURITY_PLAYER:
					final PacketStringCoordinates packetBa = new PacketStringCoordinates();
					packetBa.readData(data);
					onOpenSecurityPlayer(player, packetBa);
					break;
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
	private static void onCraftingModulePriorityUp(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleCrafting) {
					final ModuleCrafting module = (ModuleCrafting)dummy.getModule();
					int satID = module.getNextConnectSatelliteId(false);
					module.priorityUp();
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
			
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule();
				int satID = module.getNextConnectSatelliteId(false);
				module.priorityUp();
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule().getSubModule(slot - 1);
				int satID = module.getNextConnectSatelliteId(false);
				module.priorityUp();
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onCraftingModulePriorityDown(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleCrafting) {
					final ModuleCrafting module = (ModuleCrafting)dummy.getModule();
					int satID = module.getNextConnectSatelliteId(false);
					module.priorityDown();
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
			
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule();
				int satID = module.getNextConnectSatelliteId(false);
				module.priorityDown();
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule().getSubModule(slot - 1);
				int satID = module.getNextConnectSatelliteId(false);
				module.priorityDown();
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
				return;
			}
		}
	}
	private static void onCraftingModuleNextSatellite(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleCrafting) {
					final ModuleCrafting module = (ModuleCrafting)dummy.getModule();
					int satID = module.getNextConnectSatelliteId(false);
					module.setSatelliteId(satID);
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
			
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule();
				int satID = module.getNextConnectSatelliteId(false);
				module.setSatelliteId(satID);
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule().getSubModule(slot - 1);
				int satID = module.getNextConnectSatelliteId(false);
				module.setSatelliteId(satID);
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onCraftingModulePrevSatellite(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleCrafting) {
					final ModuleCrafting module = (ModuleCrafting)dummy.getModule();
					int satID = module.getNextConnectSatelliteId(true);
					module.setSatelliteId(satID);
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
			
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule();
				int satID = module.getNextConnectSatelliteId(true);
				module.setSatelliteId(satID);
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule().getSubModule(slot - 1);
				int satID = module.getNextConnectSatelliteId(true);
				module.setSatelliteId(satID);
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onCraftingModuleImport(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleCrafting) {
					final ModuleCrafting module = (ModuleCrafting)dummy.getModule();
					module.importFromCraftingTable();
					MainProxy.sendPacketToPlayer(new PacketModuleInventoryChange(NetworkConstants.CRAFTING_MODULE_IMPORT_BACK, packet.posX, packet.posY, packet.posZ, slot, module._dummyInventory).getPacket(), (Player)player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
			
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule();
				int satID = module.getNextConnectSatelliteId(true);
				module.importFromCraftingTable();
				MainProxy.sendPacketToPlayer(new PacketModuleInventoryChange(NetworkConstants.CRAFTING_MODULE_IMPORT_BACK, packet.posX, packet.posY, packet.posZ, slot, module._dummyInventory).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule().getSubModule(slot - 1);
				int satID = module.getNextConnectSatelliteId(true);
				module.importFromCraftingTable();
				MainProxy.sendPacketToPlayer(new PacketModuleInventoryChange(NetworkConstants.CRAFTING_MODULE_IMPORT_BACK, packet.posX, packet.posY, packet.posZ, slot, module._dummyInventory).getPacket(), (Player)player);
				return;
			}
		}
	}
	
	private static void onCraftingModuleOpenConnectedGui(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleCrafting) {
					final ModuleCrafting module = (ModuleCrafting)dummy.getModule();
					module.openAttachedGui(player);
					
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
			
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule();
				int satID = module.getNextConnectSatelliteId(true);
				module.openAttachedGui(player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule().getSubModule(slot - 1);
				int satID = module.getNextConnectSatelliteId(true);
				module.openAttachedGui(player);
				return;
			}
		}
	}
	
	private static void onCraftingModuleUpdate(EntityPlayerMP player, PacketPipeInteger packet)
	{
		
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleCrafting) {
					final ModuleCrafting module = (ModuleCrafting)dummy.getModule();
					if(module.isSatelliteConnected())
						module.satelliteId = 0;
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
					MainProxy.sendPacketToPlayer(new PacketModuleInventoryChange(NetworkConstants.CRAFTING_MODULE_IMPORT_BACK, packet.posX, packet.posY, packet.posZ, slot, module._dummyInventory).getPacket(), (Player)player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
			
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule();
				if(module.isSatelliteConnected())
					module.satelliteId = 0;
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(new PacketModuleInventoryChange(NetworkConstants.CRAFTING_MODULE_IMPORT_BACK, packet.posX, packet.posY, packet.posZ, slot, module._dummyInventory).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleCrafting) {
				final ModuleCrafting module = (ModuleCrafting)piperouted.getLogisticsModule().getSubModule(slot - 1);
				if(module.isSatelliteConnected())
					module.satelliteId = 0;
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_SATELLITE_ID, packet.posX, packet.posY, packet.posZ, slot, module.satelliteId).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY, packet.posX, packet.posY, packet.posZ, slot, module.priority).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(new PacketModuleInventoryChange(NetworkConstants.CRAFTING_MODULE_IMPORT_BACK, packet.posX, packet.posY, packet.posZ, slot, module._dummyInventory).getPacket(), (Player)player);
				return;
			}
		}
	}
	private static void onCraftingPipeNextSatellite(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setNextSatellite(player);
	}

	private static void onCraftingPipePrevSatellite(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setPrevSatellite(player);
	}

	private static void onCraftingPipeImport(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).importFromCraftingTable(player);
	}

	private static void onSatellitePipeNext(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicSatellite)) {
			return;
		}

		((BaseLogicSatellite) pipe.pipe.logic).setNextId(player);
	}

	private static void onSatellitePipePrev(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicSatellite)) {
			return;
		}

		((BaseLogicSatellite) pipe.pipe.logic).setPrevId(player);
	}

	private static void onModuleGuiOpen(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}

		final PipeLogisticsChassi cassiPipe = (PipeLogisticsChassi) pipe.pipe;
		
		if(!(cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ILogisticsGuiModule)) return;
		
		player.openGui(LogisticsPipes.instance, ((ILogisticsGuiModule)cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getGuiHandlerID()
				+ (100 * (packet.integer + 1)), player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleItemSink) {
			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ITEM_SINK_STATUS, packet.posX, packet.posY, packet.posZ, packet.integer,
					(((ModuleItemSink) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).isDefaultRoute() ? 1 : 0)).getPacket(), (Player)player);
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleExtractor) {
			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, packet.integer,
					(((ModuleExtractor) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getSneakyOrientation().ordinal())).getPacket(), (Player)player);
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleProvider) {
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, (((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).isExcludeFilter() ? 1 : 0)).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, (((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getExtractionMode().ordinal())).getPacket(), (Player)player);
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleAdvancedExtractor) {
			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, packet.integer, (((ModuleAdvancedExtractor) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).areItemsIncluded() ? 1 : 0)).getPacket(), (Player)player);
		}
	}

	private static void onGuiBackOpen(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}

		player.openGui(LogisticsPipes.instance, packet.integer, player.worldObj, packet.posX, packet.posY, packet.posZ);
	}

	private static void onRequestSubmit(EntityPlayerMP player, PacketRequestSubmit packet) {
		final TileGenericPipe pipe = getPipe(DimensionManager.getWorld(packet.dimension), packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		
		RequestHandler.request(player, packet, (CoreRoutedPipe) pipe.pipe);
	}
	
	private static void onRequestComponents(EntityPlayerMP player, PacketRequestSubmit packet) {
		final TileGenericPipe pipe = getPipe(DimensionManager.getWorld(packet.dimension), packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		
		RequestHandler.simulate(player, packet, (CoreRoutedPipe) pipe.pipe);
	}

	private static void onRefreshRequest(EntityPlayerMP player, PacketPipeInteger packet) {
		int dimension = (packet.integer - (packet.integer % 10)) / 10;
		final TileGenericPipe pipe = getPipe(DimensionManager.getWorld(dimension), packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		RequestHandler.DisplayOptions option;
		switch (packet.integer % 10) {
			case 0:
				option = RequestHandler.DisplayOptions.Both;
				break;
			case 1:
				option = RequestHandler.DisplayOptions.SupplyOnly;
				break;
			case 2:
				option = RequestHandler.DisplayOptions.CraftOnly;
				break;
			default:
				option = RequestHandler.DisplayOptions.Both;
				break;
		}
		RequestHandler.refresh(player, (CoreRoutedPipe) pipe.pipe, option);
	}

	private static void onItemSinkDefault(EntityPlayerMP player, PacketPipeInteger packet) {
		final int value = packet.integer % 10;
		final int slot = packet.integer / 10;
		
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleItemSink) {
					((ModuleItemSink)dummy.getModule()).setDefaultRoute(value == 1);
				}
			}
			return;
		}
		
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;


		if (piperouted.getLogisticsModule() == null) {
			return;
		}

		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleItemSink) {
				final ModuleItemSink module = (ModuleItemSink) piperouted.getLogisticsModule();
				module.setDefaultRoute(value == 1);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleItemSink) {
				final ModuleItemSink module = (ModuleItemSink) piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setDefaultRoute(value == 1);
				return;
			}
		}
	}

	private static void onProviderModeChange(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		final PipeItemsProviderLogistics providerpipe = (PipeItemsProviderLogistics) pipe.pipe;
		final LogicProvider logic = (LogicProvider)providerpipe.logic;
		logic.nextExtractionMode();
		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, logic.getExtractionMode().ordinal()).getPacket(), (Player)player);
	}

	private static void onProviderIncludeChange(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		final PipeItemsProviderLogistics providerpipe = (PipeItemsProviderLogistics) pipe.pipe;
		final LogicProvider logic = (LogicProvider)providerpipe.logic;
		logic.setFilterExcluded(!logic.isExcludeFilter());
		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, logic.isExcludeFilter() ? 1 : 0).getPacket(), (Player)player);
	}

	private static void onSupplierModeChange(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSupplier)) {
			return;
		}
		final LogicSupplier logic = (LogicSupplier) pipe.pipe.logic;
		logic.setRequestingPartials(!logic.isRequestingPartials());
		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE, packet.posX, packet.posY, packet.posZ, logic.isRequestingPartials() ? 1 : 0).getPacket(), (Player)player);
	}

	private static void onExtractorModeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final int value = packet.integer % 10;
		final int slot = packet.integer / 10;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ISneakyOrientationreceiver) {
					final ISneakyOrientationreceiver module = (ISneakyOrientationreceiver) dummy.getModule();
					switch (value){
					case 0:
						module.setSneakyOrientation(SneakyOrientation.Top);
						break;
					
					case 1:
						module.setSneakyOrientation(SneakyOrientation.Side);
						break;
					
					case 2:
						module.setSneakyOrientation(SneakyOrientation.Bottom);
						break;
						
					case 3:
						module.setSneakyOrientation(SneakyOrientation.Default);
						break;
					}
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, -1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
					
				}
			}
			return;
		}
		
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;


		if (piperouted.getLogisticsModule() == null) {
			return;
		}

		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ISneakyOrientationreceiver) {
				final ISneakyOrientationreceiver module = (ISneakyOrientationreceiver) piperouted.getLogisticsModule();
				switch (value){
				case 0:
					module.setSneakyOrientation(SneakyOrientation.Top);
					break;
				
				case 1:
					module.setSneakyOrientation(SneakyOrientation.Side);
					break;
				
				case 2:
					module.setSneakyOrientation(SneakyOrientation.Bottom);
					break;
					
				case 3:
					module.setSneakyOrientation(SneakyOrientation.Default);
					break;
				}
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, -1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ISneakyOrientationreceiver) {
				final ISneakyOrientationreceiver module = (ISneakyOrientationreceiver) piperouted.getLogisticsModule().getSubModule(slot - 1);
				switch (value){
				case 0:
					module.setSneakyOrientation(SneakyOrientation.Top);
					break;
				
				case 1:
					module.setSneakyOrientation(SneakyOrientation.Side);
					break;
				
				case 2:
					module.setSneakyOrientation(SneakyOrientation.Bottom);
					break;
					
				case 3:
					module.setSneakyOrientation(SneakyOrientation.Default);
					break;
				}
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, slot - 1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onProviderModuleModeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleProvider) {
					final ModuleProvider module = (ModuleProvider)dummy.getModule();
					module.nextExtractionMode();
					MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()).getPacket(), (Player)player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;
		
		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule();
				module.nextExtractionMode();
				MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.nextExtractionMode();
				MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onProviderModuleIncludeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleProvider) {
					final ModuleProvider module = (ModuleProvider)dummy.getModule();
					module.setFilterExcluded(!module.isExcludeFilter());
					MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, module.isExcludeFilter() ? 1 : 0).getPacket(), (Player)player);
				}
			}
			return;
		}
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule();
				module.setFilterExcluded(!module.isExcludeFilter());
				MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, module.isExcludeFilter() ? 1 : 0).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setFilterExcluded(!module.isExcludeFilter());
				MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, (module.isExcludeFilter() ? 1 : 0)).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onAdvancedExtractorModuleIncludeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer / 10;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleAdvancedExtractor) {
					((ModuleAdvancedExtractor)dummy.getModule()).setItemsIncluded(!((ModuleAdvancedExtractor)dummy.getModule()).areItemsIncluded());
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, 20, ((ModuleAdvancedExtractor)dummy.getModule()).areItemsIncluded() ? 1 : 0).getPacket(), (Player)player);
				}
			}
			return;
		}
		
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;


		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule();
				module.setItemsIncluded(!module.areItemsIncluded());
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, -1, module.areItemsIncluded() ? 1 : 0).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setItemsIncluded(!module.areItemsIncluded());
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, slot - 1, (module.areItemsIncluded() ? 1 : 0)).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onAdvancedExtractorModuleGuiSneaky(EntityPlayerMP player, PacketPipeInteger packet) {
		final int slot = packet.integer;
		if(slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleAdvancedExtractor) {
					player.closeScreen();
					player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
					MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, -1, ((ModuleAdvancedExtractor)dummy.getModule()).getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				}
			}
			return;
		}
		
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;


		if (piperouted.getLogisticsModule() == null) {
			return;
		}

		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule();
				player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, -1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule().getSubModule(slot - 1);
				player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
				MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, slot - 1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onPipeUpdateRequest(EntityPlayerMP playerEntity, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(playerEntity.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		MainProxy.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,packet.posX,packet.posY,packet.posZ,((CoreRoutedPipe)pipe.pipe).getLogisticsNetworkPacket()).getPacket(), (Player) playerEntity);
		((CoreRoutedPipe)pipe.pipe).refreshRender(true);
	}

	private static void onCraftingPipeUpdateRequest(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		if(!(pipe.pipe instanceof CoreRoutedPipe)) return;
		MainProxy.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,packet.posX,packet.posY,packet.posZ,((CoreRoutedPipe)pipe.pipe).getLogisticsNetworkPacket()).getPacket(), (Player) player);
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(pipe.pipe.logic instanceof BaseLogicCrafting) {
				final PacketInventoryChange newpacket = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, pipe.xCoord, pipe.yCoord, pipe.zCoord, ((BaseLogicCrafting)pipe.pipe.logic).getDummyInventory());
				MainProxy.sendPacketToPlayer(newpacket.getPacket(), (Player)player);
			}
		}
	}

	private static void onCraftingPipeOpenConnectedGui(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(pipe.pipe.logic instanceof BaseLogicCrafting) {
				((BaseLogicCrafting)pipe.pipe.logic).openAttachedGui(player);
			}
		}
	}

	private static void onDiskContentRequest(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			if(((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk() != null) {
				if(!((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().hasTagCompound()) {
					((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().setTagCompound(new NBTTagCompound("tag"));
				}
			}
			MainProxy.sendPacketToPlayer(new PacketItem(NetworkConstants.DISK_CONTENT, pipe.xCoord, pipe.yCoord, pipe.zCoord, ((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk()).getPacket(), (Player)player);
		}		
	}

	private static void onDiskSetName(EntityPlayerMP player, PacketPipeString packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			if(((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk() == null) {
				return;
			}
			if(!((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().hasTagCompound()) {
				((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().setTagCompound(new NBTTagCompound("tag"));
			}
			NBTTagCompound nbt = ((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().getTagCompound();
			nbt.setString("name", packet.string);
		}
	}

	private static void onDiskChangeClientSide(EntityPlayerMP player, PacketItem packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			((PipeItemsRequestLogisticsMk2)pipe.pipe).setDisk(packet.itemstack);
		}
		onDiskContentRequest(player, packet);
	}

	private static void onDiskDrop(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			((PipeItemsRequestLogisticsMk2)pipe.pipe).dropDisk();
		}
		onDiskContentRequest(player, packet);
	}

	private static void onDiskMacroRequest(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			if(((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk() == null) {
				return;
			}
			if(!((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().hasTagCompound()) {
				return;
			}
			NBTTagCompound nbt = ((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().getTagCompound();
			if(!nbt.hasKey("macroList")) {
				NBTTagList list = new NBTTagList();
				nbt.setTag("macroList", list);
			}
			
			NBTTagList list = nbt.getTagList("macroList");
			
			
			for(int i = 0;i < list.tagCount();i++) {
				if(i == packet.integer) {
					NBTTagCompound itemlist = (NBTTagCompound) list.tagAt(i);
					RequestHandler.requestMacrolist(itemlist, (PipeItemsRequestLogisticsMk2)pipe.pipe,player);
					//mainGui.handleRequestAnswer(reply.items, reply.suceed, this, player);
					break;
				}
			}
		}
	}

	private static void onBeeModuleSetBee(EntityPlayerMP player, PacketPipeBeePacket packet) {
		ModuleApiaristSink sink;
		if(packet.integer1 == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleApiaristSink) {
					sink = (ModuleApiaristSink) dummy.getModule();
				} else {
					return;
				}
			} else {
				return;
			}
		} else {
			final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
			if(pipe == null) {
				return;
			}
			if(pipe.pipe instanceof PipeItemsApiaristSink) {
				sink = (ModuleApiaristSink) ((PipeItemsApiaristSink)pipe.pipe).getLogisticsModule();
			} else if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(packet.integer1 - 1) instanceof ModuleApiaristSink) {
				sink = (ModuleApiaristSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(packet.integer1 - 1);
			} else {
				return;
			}
		}
		if(packet.integer2 >= sink.filter.length) return;
		switch(packet.integer3) {
		case 0:
			sink.filter[packet.integer2].firstBee = packet.string1;
			break;
		case 1:
			sink.filter[packet.integer2].secondBee = packet.string1;
			break;
		case 2:
			sink.filter[packet.integer2].filterGroup = packet.integer4;
			break;
		case 3:
			if(packet.integer4 >= FilterType.values().length) return;
			sink.filter[packet.integer2].filterType = FilterType.values()[packet.integer4];
			break;
		}
	}

	private static void onLiquidSupplierPartials(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeItemsLiquidSupplier) {
			PipeItemsLiquidSupplier liquid = (PipeItemsLiquidSupplier) pipe.pipe;
			((LogicLiquidSupplier)liquid.logic).setRequestingPartials((packet.integer % 10) == 1);
		}
	}

	private static void onInvSysContentRequest(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeItemsInvSysConnector) {
			PipeItemsInvSysConnector connector = (PipeItemsInvSysConnector) pipe.pipe;
			Collection<ItemIdentifierStack> allItems = connector.getExpectedItems();
			PacketRequestGuiContent packetContent = new PacketRequestGuiContent(allItems, NetworkConstants.INC_SYS_CON_CONTENT);
			MainProxy.sendPacketToPlayer(packetContent.getPacket(), (Player)player);
		}
	}

	private static void onHUDWatchingChange(EntityPlayerMP player, PacketPipeInteger packet, boolean add) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof IWatchingHandler) {
			IWatchingHandler handler = (IWatchingHandler) pipe.pipe;
			if(add) {
				handler.playerStartWatching(player, packet.integer);
			} else {
				handler.playerStopWatching(player, packet.integer);
			}
		}
	}

	private static void onInvSysConResistance(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsInvSysConnector) {
			PipeItemsInvSysConnector invCon = (PipeItemsInvSysConnector) pipe.pipe;
			invCon.resistance = packet.integer;
			invCon.getRouter().update(true);
		}
	}

	private static void onCraftingPipeStackMove(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(((PipeItemsCraftingLogistics)pipe.pipe).logic instanceof BaseLogicCrafting) {
				BaseLogicCrafting logic = (BaseLogicCrafting) ((PipeItemsCraftingLogistics)pipe.pipe).logic;
				logic.handleStackMove(packet.integer);
			}
		}
	}

	private static void onHUDModuleWatchingChange(EntityPlayerMP player, PacketPipeInteger packet, boolean flag) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.integer) instanceof IModuleWatchReciver) {
			
			IModuleWatchReciver handler = (IModuleWatchReciver) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.integer);
			if(flag) {
				handler.startWatching(player);
			} else {
				handler.stopWatching(player);
			}
		}
	}

	private static void onElectricModuleStateChange(EntityPlayerMP player, PacketModuleInteger packet) {
		if(packet.slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleElectricManager) {
					ModuleElectricManager module = (ModuleElectricManager) dummy.getModule();
					module.setDischargeMode(packet.integer == 1);
				}
			}
			return;
		}
		
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleElectricManager) {
			ModuleElectricManager module = (ModuleElectricManager) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot);
			module.setDischargeMode(packet.integer == 1);
		}
	}

	private static void onRotationRequest(EntityPlayerMP player, PacketCoordinates packet) {
		TileEntity tile = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof IRotationProvider) {
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.ROTATION_SET, packet.posX, packet.posY, packet.posZ, ((IRotationProvider)tile).getRotation()).getPacket(), (Player)player);
		}
	}

	private static void onPriorityUp(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).priorityUp(player);
	}

	private static void onPriorityDown(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).priorityDown(player);
	}

	private static void onHUDBlockWatch(EntityPlayerMP player, PacketCoordinates packet, boolean flag) {
		TileEntity tile = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof IBlockWatchingHandler) {
			if(flag) {
				((IBlockWatchingHandler)tile).playerStartWatching(player);
			} else {
				((IBlockWatchingHandler)tile).playerStopWatching(player);	
			}
		}
	}

	private static void onHUDSettings(EntityPlayerMP player, PacketHUDSettings packet) {
		if(player.inventory.getStackInSlot(packet.slot) == null) return;
		HUDConfig config = new HUDConfig(player.inventory.getStackInSlot(packet.slot));
		switch(packet.buttonId) {
		case 0:
			config.setHUDChassie(packet.state);
			if(config.isHUDChassie()) {
				player.sendChatToPlayer("Enabled Chassie.");
			} else {
				player.sendChatToPlayer("Disabled Chassie.");
			}
			break;
		case 1:
			config.setHUDCrafting(packet.state);
			if(config.isHUDCrafting()) {
				player.sendChatToPlayer("Enabled Crafting.");
			} else {
				player.sendChatToPlayer("Disabled Crafting.");
			}
			break;
		case 2:
			config.setHUDInvSysCon(packet.state);
			if(config.isHUDInvSysCon()) {
				player.sendChatToPlayer("Enabled InvSysCon.");
			} else {
				player.sendChatToPlayer("Disabled InvSysCon.");
			}
			break;
		case 3:
			config.setHUDPowerJunction(packet.state);
			if(config.isHUDPowerJunction()) {
				player.sendChatToPlayer("Enabled Power Junction.");
			} else {
				player.sendChatToPlayer("Disabled Power Junction.");
			}
			break;
		case 4:
			config.setHUDProvider(packet.state);
			if(config.isHUDProvider()) {
				player.sendChatToPlayer("Enabled Provider.");
			} else {
				player.sendChatToPlayer("Disabled Provider.");
			}
			break;
		case 5:
			config.setHUDSatellite(packet.state);
			if(config.isHUDSatellite()) {
				player.sendChatToPlayer("Enabled Satellite.");
			} else {
				player.sendChatToPlayer("Disabled Satellite.");
			}
			break;
		}
		if(player.inventoryContainer != null) {
			player.inventoryContainer.detectAndSendChanges();
		}
	}

	private static void onBufferTransfer(PacketBufferTransfer packet, Player player) {
		SimpleServiceLocator.serverBufferHandler.handlePacket(packet, player);
	}

	private static void onNameUpdate(PacketNameUpdatePacket packetAs) {
		MainProxy.proxy.updateNames(packetAs.item, packetAs.name);
	}

	private static void onLiquidRefreshRequest(EntityPlayerMP player, PacketPipeInteger packet) {
		int dimension = packet.integer;
		final TileGenericPipe pipe = getPipe(DimensionManager.getWorld(dimension), packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		
		RequestHandler.refreshLiquid(player, (CoreRoutedPipe) pipe.pipe);
	}

	private static void onLiquidRequestSubmit(EntityPlayerMP player, PacketRequestSubmit packet) {
		final TileGenericPipe pipe = getPipe(DimensionManager.getWorld(packet.dimension), packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		if (!(pipe.pipe instanceof IRequestLiquid)) {
			return;
		}
		
		RequestHandler.requestLiquid(player, packet, (CoreRoutedPipe) pipe.pipe, (IRequestLiquid) pipe.pipe);
	}

	private static void onModBasedItemSinkList(EntityPlayerMP player, PacketModuleNBT packet) {
		if(packet.slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleModBasedItemSink) {
					((ModuleModBasedItemSink)dummy.getModule()).readFromNBT(packet.tag);
					return;
				}
			}
		}

		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleModBasedItemSink) {
			((ModuleModBasedItemSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot)).readFromNBT(packet.tag);
		}
	}

	private static void onThaumicAspectSinkList(EntityPlayerMP player, PacketModuleNBT packet) {
		if(packet.slot == 20) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleThaumicAspectSink) {
					ModuleThaumicAspectSink module = (ModuleThaumicAspectSink) dummy.getModule();
					module.readFromNBT(packet.tag);
				}
			}
			return;
		}

		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) return;
	
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleThaumicAspectSink) {
			((ModuleThaumicAspectSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot)).readFromNBT(packet.tag);
		}		
	}

	private static void onCheatJunctionPower(EntityPlayerMP player, PacketCoordinates packet) {
		World world = player.worldObj;
		if (!world.blockExists(packet.posX, packet.posY, packet.posZ)) {
			return;
		}

		final TileEntity tile = world.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if (tile instanceof LogisticsPowerJuntionTileEntity_BuildCraft) {
			((LogisticsPowerJuntionTileEntity_BuildCraft) tile).addEnergy(100000);
		}
	}

	private static void onFirewallFlags(EntityPlayerMP player, PacketPipeBitSet packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeItemsFirewall) {
			PipeItemsFirewall firewall = (PipeItemsFirewall) pipe.pipe;
			firewall.setFlags(packet.flags);
		}
	}

	private static void onSecurityCardButton(EntityPlayerMP player, PacketPipeInteger packet) {
		TileEntity tile = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSecurityTileEntity) {
			((LogisticsSecurityTileEntity)tile).buttonFreqCard(packet.integer);
		}
	}

	private static void onPlayerListRequest(EntityPlayerMP player) {
		List<String> list = new LinkedList<String>();
		File root = DimensionManager.getCurrentSaveRootDirectory();
		if(root == null) return;
		if(!root.exists()) return;
		File players = new File(root, "players");
		if(!players.exists()) return;
		for(String names:players.list()) {
			if(names.endsWith(".dat") && new File(players, names).isFile()) {
				list.add(names.substring(0, names.length() - 4));
			}
		}
		MainProxy.sendPacketToPlayer(new PacketStringList(NetworkConstants.PLAYER_LIST, list).getPacket(), (Player) player);
	}

	private static void onOpenSecurityPlayer(EntityPlayerMP player, PacketStringCoordinates packet) {
		TileEntity tile = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(tile instanceof LogisticsSecurityTileEntity) {
			((LogisticsSecurityTileEntity)tile).handleOpenSecurityPlayer(player, packet.string);
		}
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
		if(world == null) {
			return null;
		}
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

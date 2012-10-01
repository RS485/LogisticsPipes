package logisticspipes.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISneakyOrientationreceiver;
import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.logisticspipes.modules.SneakyOrientation;
import logisticspipes.main.CoreRoutedPipe;
import logisticspipes.main.GuiIDs;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleApiaristSink.FilterType;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketItem;
import logisticspipes.network.packets.PacketItems;
import logisticspipes.network.packets.PacketModuleInteger;
import logisticspipes.network.packets.PacketPipeBeePacket;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeString;
import logisticspipes.network.packets.PacketPipeUpdate;
import logisticspipes.network.packets.PacketRequestGuiContent;
import logisticspipes.network.packets.PacketRequestSubmit;
import logisticspipes.network.packets.PacketRouterInformation;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestHandler;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ServerRouter;
import logisticspipes.ticks.PacketBufferHandlerThread;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler {
	
	public static void onPacketData(NetworkManager manager, Packet250CustomPayload packet250, Player playerFML) {
		
		EntityPlayerMP player = (EntityPlayerMP) playerFML;
		
		final DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet250.data));
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
				case NetworkConstants.REQUEST_ROUTER_UPDATE:
					final PacketPipeInteger packetAe = new PacketPipeInteger();
					packetAe.readData(data);
					onRouterUpdateRequest(player, packetAe);
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
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
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

		player.openGui(LogisticsPipes.instance, cassiPipe.getLogisticsModule().getSubModule(packet.integer).getGuiHandlerID()
				+ (100 * (packet.integer + 1)), player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleItemSink) {
			PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ITEM_SINK_STATUS, packet.posX, packet.posY, packet.posZ, packet.integer,
					(((ModuleItemSink) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).isDefaultRoute() ? 1 : 0)).getPacket(), (Player)player);
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleExtractor) {
			PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, packet.integer,
					(((ModuleExtractor) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getSneakyOrientation().ordinal())).getPacket(), (Player)player);
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleProvider) {
			PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, (((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).isExcludeFilter() ? 1 : 0)).getPacket(), (Player)player);
			PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, (((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getExtractionMode().ordinal())).getPacket(), (Player)player);
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleAdvancedExtractor) {
			PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, packet.integer, (((ModuleAdvancedExtractor) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).areItemsIncluded() ? 1 : 0)).getPacket(), (Player)player);
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
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		RequestHandler.request(player, packet, (CoreRoutedPipe) pipe.pipe);
	}

	private static void onRefreshRequest(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		RequestHandler.DisplayOptions option;
		switch (packet.integer) {
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
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int value = packet.integer % 10;
		final int slot = packet.integer / 10;

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
		PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, logic.getExtractionMode().ordinal()).getPacket(), (Player)player);
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
		PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, logic.isExcludeFilter() ? 1 : 0).getPacket(), (Player)player);
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
		PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE, packet.posX, packet.posY, packet.posZ, logic.isRequestingPartials() ? 1 : 0).getPacket(), (Player)player);
	}

	private static void onExtractorModeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int value = packet.integer % 10;
		final int slot = packet.integer / 10;

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
				PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, -1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
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
				PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, slot - 1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onProviderModuleModeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int slot = packet.integer;
		
		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule();
				module.nextExtractionMode();
				PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.nextExtractionMode();
				PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onProviderModuleIncludeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int slot = packet.integer;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule();
				module.setFilterExcluded(!module.isExcludeFilter());
				PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, module.isExcludeFilter() ? 1 : 0).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setFilterExcluded(!module.isExcludeFilter());
				PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, (module.isExcludeFilter() ? 1 : 0)).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onAdvancedExtractorModuleIncludeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int slot = packet.integer / 10;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule();
				module.setItemsIncluded(!module.areItemsIncluded());
				PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, -1, module.areItemsIncluded() ? 1 : 0).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setItemsIncluded(!module.areItemsIncluded());
				PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, slot - 1, (module.areItemsIncluded() ? 1 : 0)).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onAdvancedExtractorModuleGuiSneaky(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int slot = packet.integer;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}

		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule();
				player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
				PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, -1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule().getSubModule(slot - 1);
				player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
				PacketDispatcher.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, slot - 1, module.getSneakyOrientation().ordinal()).getPacket(), (Player)player);
				return;
			}
		}
	}

	private static void onPipeUpdateRequest(EntityPlayerMP playerEntity, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(playerEntity.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		PacketDispatcher.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,packet.posX,packet.posY,packet.posZ,((CoreRoutedPipe)pipe.pipe).getLogisticsNetworkPacket()).getPacket(), (Player) playerEntity);
	}

	private static void onCraftingPipeUpdateRequest(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		if(!(pipe.pipe instanceof CoreRoutedPipe)) return;
		PacketDispatcher.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,packet.posX,packet.posY,packet.posZ,((CoreRoutedPipe)pipe.pipe).getLogisticsNetworkPacket()).getPacket(), (Player) player);
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(pipe.pipe.logic instanceof BaseLogicCrafting) {
				final PacketInventoryChange newpacket = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, pipe.xCoord, pipe.yCoord, pipe.zCoord, ((BaseLogicCrafting)pipe.pipe.logic).getDummyInventory());
				PacketDispatcher.sendPacketToPlayer(newpacket.getPacket(), (Player)player);
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
					((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().setTagCompound(new NBTTagCompound());
				}
			}
			PacketDispatcher.sendPacketToPlayer(new PacketItem(NetworkConstants.DISK_CONTENT, pipe.xCoord, pipe.yCoord, pipe.zCoord, ((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk()).getPacket(), (Player)player);
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
				((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().setTagCompound(new NBTTagCompound());
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
			
			
			boolean flag = false;
			
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
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		ModuleApiaristSink sink;
		if(pipe.pipe instanceof PipeItemsApiaristSink) {
			sink = (ModuleApiaristSink) ((PipeItemsApiaristSink)pipe.pipe).getLogisticsModule();
		} else if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(packet.integer1 - 1) instanceof ModuleApiaristSink) {
			sink = (ModuleApiaristSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(packet.integer1 - 1);
		} else {
			return;
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
			LinkedList<ItemIdentifierStack> allItems = connector.getExpectedItems();
			PacketRequestGuiContent packetContent = new PacketRequestGuiContent(allItems, NetworkConstants.INC_SYS_CON_CONTENT);
			PacketDispatcher.sendPacketToPlayer(packetContent.getPacket(), (Player)player);
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

	private static void onRouterUpdateRequest(EntityPlayerMP player, PacketPipeInteger packet) {
		World world = MainProxy.getWorld(packet.integer);
		final TileGenericPipe pipe = getPipe(world, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof CoreRoutedPipe) {
			IRouter router = ((CoreRoutedPipe)pipe.pipe).getRouter();
			if(router instanceof ServerRouter) {
				PacketBufferHandlerThread.addPacketToCompressor((Packet250CustomPayload) new PacketRouterInformation(NetworkConstants.ROUTER_UPDATE_CONTENT, packet.posX, packet.posY, packet.posZ, packet.integer, (ServerRouter)router).getPacket(), (Player) player);
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
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot) instanceof ModuleElectricManager) {
			ModuleElectricManager module = (ModuleElectricManager) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(packet.slot);
			module.setDischargeMode(packet.integer == 1);
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

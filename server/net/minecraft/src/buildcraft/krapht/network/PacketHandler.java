package net.minecraft.src.buildcraft.krapht.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.zip.ZipEntry;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.mod_BuildCraftCore;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.core.CoreProxy;
import buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.logic.LogicProvider;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsCraftingLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsProviderLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogisticsMk2;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.krapht.routing.NormalOrdererRequests;
import net.minecraft.src.buildcraft.logisticspipes.macros.RequestHandler;
import net.minecraft.src.buildcraft.logisticspipes.macros.RequestHandler.RequestReply;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISneakyOrientationreceiver;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.SneakyOrientation;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.IPacketHandler;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] bytes) {
		final DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
		try {
			final NetServerHandler net = (NetServerHandler) network.getNetHandler();
			final int packetID = data.read();
			switch (packetID) {

				case NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE:
					final PacketCoordinates packet = new PacketCoordinates();
					packet.readData(data);
					onCraftingPipeNextSatellite(net.getPlayerEntity(), packet);
					break;

				case NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE:
					final PacketCoordinates packetA = new PacketCoordinates();
					packetA.readData(data);
					onCraftingPipePrevSatellite(net.getPlayerEntity(), packetA);
					break;
				case NetworkConstants.CRAFTING_PIPE_IMPORT:
					final PacketCoordinates packetB = new PacketCoordinates();
					packetB.readData(data);
					onCraftingPipeImport(net.getPlayerEntity(), packetB);
					break;
				case NetworkConstants.SATELLITE_PIPE_NEXT:
					final PacketCoordinates packetC = new PacketCoordinates();
					packetC.readData(data);
					onSatellitePipeNext(net.getPlayerEntity(), packetC);
					break;
				case NetworkConstants.SATELLITE_PIPE_PREV:
					final PacketCoordinates packetD = new PacketCoordinates();
					packetD.readData(data);
					onSatellitePipePrev(net.getPlayerEntity(), packetD);
					break;
				case NetworkConstants.CHASSI_GUI_PACKET_ID:
					final PacketPipeInteger packetE = new PacketPipeInteger();
					packetE.readData(data);
					onModuleGuiOpen(net.getPlayerEntity(), packetE);
					break;
				case NetworkConstants.GUI_BACK_PACKET:
					final PacketPipeInteger packetF = new PacketPipeInteger();
					packetF.readData(data);
					onGuiBackOpen(net.getPlayerEntity(), packetF);
					break;
				case NetworkConstants.REQUEST_SUBMIT:
					final PacketRequestSubmit packetG = new PacketRequestSubmit();
					packetG.readData(data);
					onRequestSubmit(net.getPlayerEntity(), packetG);
					break;
				case NetworkConstants.ORDERER_REFRESH_REQUEST:
					final PacketPipeInteger packetH = new PacketPipeInteger();
					packetH.readData(data);
					onRefreshRequest(net.getPlayerEntity(), packetH);
					break;
				case NetworkConstants.ITEM_SINK_DEFAULT:
					final PacketPipeInteger packetI = new PacketPipeInteger();
					packetI.readData(data);
					onItemSinkDefault(net.getPlayerEntity(), packetI);
					break;
				case NetworkConstants.PROVIDER_PIPE_NEXT_MODE:
					final PacketCoordinates packetJ = new PacketCoordinates();
					packetJ.readData(data);
					onProviderModeChange(net.getPlayerEntity(), packetJ);
					break;
				case NetworkConstants.PROVIDER_PIPE_CHANGE_INCLUDE:
					final PacketCoordinates packetK = new PacketCoordinates();
					packetK.readData(data);
					onProviderIncludeChange(net.getPlayerEntity(), packetK);
					break;
				case NetworkConstants.SUPPLIER_PIPE_MODE_CHANGE:
					final PacketCoordinates packetL = new PacketCoordinates();
					packetL.readData(data);
					onSupplierModeChange(net.getPlayerEntity(), packetL);
					break;
				case NetworkConstants.EXTRACTOR_MODULE_DIRECTION_SET:
					final PacketPipeInteger packetM = new PacketPipeInteger();
					packetM.readData(data);
					onExtractorModeChange(net.getPlayerEntity(), packetM);
					break;
				case NetworkConstants.PROVIDER_MODULE_NEXT_MODE:
					final PacketPipeInteger packetN = new PacketPipeInteger();
					packetN.readData(data);
					onProviderModuleModeChange(net.getPlayerEntity(), packetN);
					break;
				case NetworkConstants.PROVIDER_MODULE_CHANGE_INCLUDE:
					final PacketPipeInteger packetO = new PacketPipeInteger();
					packetO.readData(data);
					onProviderModuleIncludeChange(net.getPlayerEntity(), packetO);
					break;
				case NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_SET:
					final PacketPipeInteger packetP = new PacketPipeInteger();
					packetP.readData(data);
					onAdvancedExtractorModuleIncludeChange(net.getPlayerEntity(), packetP);
					break;
				case NetworkConstants.ADVANCED_EXTRACTOR_MODULE_SNEAKY_GUI:
					final PacketPipeInteger packetQ = new PacketPipeInteger();
					packetQ.readData(data);
					onAdvancedExtractorModuleGuiSneaky(net.getPlayerEntity(), packetQ);
					break;
				case NetworkConstants.REQUEST_PIPE_UPDATE:
					final PacketCoordinates packetR = new PacketCoordinates();
					packetR.readData(data);
					onPipeUpdateRequest(net.getPlayerEntity(), packetR);
					break;
				case NetworkConstants.REQUEST_CRAFTING_PIPE_UPDATE:
					final PacketCoordinates packetS = new PacketCoordinates();
					packetS.readData(data);
					onCraftingPipeUpdateRequest(net.getPlayerEntity(), packetS);
					break;
				case NetworkConstants.CRAFTING_PIPE_OPEN_CONNECTED_GUI:
					final PacketCoordinates packetT = new PacketCoordinates();
					packetT.readData(data);
					onCraftingPipeOpenConnectedGui(net.getPlayerEntity(), packetT);
					break;
				case NetworkConstants.DISK_REQUEST_CONTENT:
					final PacketCoordinates packetU = new PacketCoordinates();
					packetU.readData(data);
					onDiskContentRequest(net.getPlayerEntity(), packetU);
					break;
				case NetworkConstants.DISK_SET_NAME:
					final PacketPipeString packetV = new PacketPipeString();
					packetV.readData(data);
					onDiskSetName(net.getPlayerEntity(), packetV);
					break;
				case NetworkConstants.DISK_CONTENT:
					final PacketItem packetW = new PacketItem();
					packetW.readData(data);
					onDiskChangeClientSide(net.getPlayerEntity(), packetW);
					break;
				case NetworkConstants.DISK_DROP:
					final PacketCoordinates packetX = new PacketCoordinates();
					packetX.readData(data);
					onDiskDrop(net.getPlayerEntity(), packetX);
					break;
				case NetworkConstants.DISK_MACRO_REQUEST:
					final PacketPipeInteger packetY = new PacketPipeInteger();
					packetY.readData(data);
					onDiskMacroRequest(net.getPlayerEntity(), packetY);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private void onCraftingPipeNextSatellite(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicCrafting)) {
			return;
		}

		((LogicCrafting) pipe.pipe.logic).setNextSatellite(player);
	}

	private void onCraftingPipePrevSatellite(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicCrafting)) {
			return;
		}

		((LogicCrafting) pipe.pipe.logic).setPrevSatellite(player);
	}

	private void onCraftingPipeImport(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicCrafting)) {
			return;
		}

		((LogicCrafting) pipe.pipe.logic).importFromCraftingTable(player);
	}

	private void onSatellitePipeNext(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSatellite)) {
			return;
		}

		((LogicSatellite) pipe.pipe.logic).setNextId(player);
	}

	private void onSatellitePipePrev(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSatellite)) {
			return;
		}

		((LogicSatellite) pipe.pipe.logic).setPrevId(player);
	}

	private void onModuleGuiOpen(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}

		final PipeLogisticsChassi cassiPipe = (PipeLogisticsChassi) pipe.pipe;

		player.openGui(mod_LogisticsPipes.instance, cassiPipe.getLogisticsModule().getSubModule(packet.integer).getGuiHandlerID()
				+ (100 * (packet.integer + 1)), player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleItemSink) {
			CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.ITEM_SINK_STATUS, packet.posX, packet.posY, packet.posZ,
					(((ModuleItemSink) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).isDefaultRoute() ? 1 : 0)));
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleExtractor) {
			CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ,
					(((ModuleExtractor) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getSneakyOrientation().ordinal())));
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleProvider) {
			CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, (((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).isExcludeFilter() ? 1 : 0)));
			CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, (((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getExtractionMode().ordinal())));
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleAdvancedExtractor) {
			CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, (((ModuleAdvancedExtractor) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).areItemsIncluded() ? 1 : 0)));
		}
	}

	private void onGuiBackOpen(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}

		player.openGui(mod_LogisticsPipes.instance, packet.integer, player.worldObj, packet.posX, packet.posY, packet.posZ);
	}

	private void onRequestSubmit(EntityPlayerMP player, PacketRequestSubmit packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		NormalOrdererRequests.request(player, packet, (CoreRoutedPipe) pipe.pipe);
	}

	private void onRefreshRequest(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		NormalOrdererRequests.DisplayOptions option;
		switch (packet.integer) {
			case 0:
				option = NormalOrdererRequests.DisplayOptions.Both;
				break;
			case 1:
				option = NormalOrdererRequests.DisplayOptions.SupplyOnly;
				break;
			case 2:
				option = NormalOrdererRequests.DisplayOptions.CraftOnly;
				break;
			default:
				option = NormalOrdererRequests.DisplayOptions.Both;
				break;
		}

		NormalOrdererRequests.refresh(player, (CoreRoutedPipe) pipe.pipe, option);
	}

	private void onItemSinkDefault(EntityPlayerMP player, PacketPipeInteger packet) {
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

	private void onProviderModeChange(EntityPlayerMP player, PacketCoordinates packet) {
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
		CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, logic.getExtractionMode().ordinal()));
	}

	private void onProviderIncludeChange(EntityPlayerMP player, PacketCoordinates packet) {
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
		CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, logic.isExcludeFilter() ? 1 : 0));
	}

	private void onSupplierModeChange(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSupplier)) {
			return;
		}
		final LogicSupplier logic = (LogicSupplier) pipe.pipe.logic;
		logic.setRequestingPartials(!logic.isRequestingPartials());
		CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE, packet.posX, packet.posY, packet.posZ, logic.isRequestingPartials() ? 1 : 0));
	}

	private void onExtractorModeChange(EntityPlayerMP player, PacketPipeInteger packet) {
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
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, module.getSneakyOrientation().ordinal()));
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
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, module.getSneakyOrientation().ordinal()));
				return;
			}
		}
	}

	private void onProviderModuleModeChange(EntityPlayerMP player, PacketPipeInteger packet) {
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
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()));
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.nextExtractionMode();
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()));
				return;
			}
		}
	}

	private void onProviderModuleIncludeChange(EntityPlayerMP player, PacketPipeInteger packet) {
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
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, module.isExcludeFilter() ? 1 : 0));
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setFilterExcluded(!module.isExcludeFilter());
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, (module.isExcludeFilter() ? 1 : 0)));
				return;
			}
		}
	}

	private void onAdvancedExtractorModuleIncludeChange(EntityPlayerMP player, PacketPipeInteger packet) {
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
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, module.areItemsIncluded() ? 1 : 0));
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setItemsIncluded(!module.areItemsIncluded());
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, (module.areItemsIncluded() ? 1 : 0)));
				return;
			}
		}
	}

	private void onAdvancedExtractorModuleGuiSneaky(EntityPlayerMP player, PacketPipeInteger packet) {
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
				player.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, module.getSneakyOrientation().ordinal()));
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule().getSubModule(slot - 1);
				player.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
				CoreProxy.sendToPlayer(player, new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, module.getSneakyOrientation().ordinal()));
				return;
			}
		}
	}

	private void onPipeUpdateRequest(EntityPlayerMP playerEntity, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(playerEntity.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		playerEntity.playerNetServerHandler.sendPacket(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,packet.posX,packet.posY,packet.posZ,((CoreRoutedPipe)pipe.pipe).getLogisticsNetworkPacket()).getPacket());
	}

	private void onCraftingPipeUpdateRequest(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		if(!(pipe.pipe instanceof CoreRoutedPipe)) return;
		player.playerNetServerHandler.sendPacket(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,packet.posX,packet.posY,packet.posZ,((CoreRoutedPipe)pipe.pipe).getLogisticsNetworkPacket()).getPacket());
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(pipe.pipe.logic instanceof LogicCrafting) {
				final PacketInventoryChange newpacket = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, pipe.xCoord, pipe.yCoord, pipe.zCoord, ((LogicCrafting)pipe.pipe.logic).getDummyInventory());
				CoreProxy.sendToPlayer(player, newpacket);
			}
		}
	}

	private void onCraftingPipeOpenConnectedGui(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(pipe.pipe.logic instanceof LogicCrafting) {
				((LogicCrafting)pipe.pipe.logic).openAttachedGui(player);
			}
		}
	}

	private void onDiskContentRequest(EntityPlayerMP player, PacketCoordinates packet) {
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
			CoreProxy.sendToPlayer(player, new PacketItem(NetworkConstants.DISK_CONTENT, pipe.xCoord, pipe.yCoord, pipe.zCoord, ((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk()));
		}		
	}

	private void onDiskSetName(EntityPlayerMP player, PacketPipeString packet) {
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

	private void onDiskChangeClientSide(EntityPlayerMP player, PacketItem packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			((PipeItemsRequestLogisticsMk2)pipe.pipe).setDisk(packet.itemstack);
		}
		onDiskContentRequest(player, packet);
	}

	private void onDiskDrop(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			((PipeItemsRequestLogisticsMk2)pipe.pipe).dropDisk();
		}
		onDiskContentRequest(player, packet);
	}

	private void onDiskMacroRequest(EntityPlayerMP player, PacketPipeInteger packet) {
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
					RequestReply reply = RequestHandler.requestMacrolist(itemlist, (PipeItemsRequestLogisticsMk2)pipe.pipe,player);
					CoreProxy.sendToPlayer(player, new PacketItems(reply.items, !reply.suceed));
					//mainGui.handleRequestAnswer(reply.items, reply.suceed, this, player);
					break;
				}
			}
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
	private TileGenericPipe getPipe(World world, int x, int y, int z) {
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

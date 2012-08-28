package logisticspipes.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import logisticspipes.gui.GuiLiquidSupplierPipe;
import logisticspipes.gui.GuiProviderPipe;
import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.gui.modules.GuiAdvancedExtractor;
import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.gui.modules.GuiItemSink;
import logisticspipes.gui.modules.GuiProvider;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.gui.popup.GuiDiskPopup;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.LogicProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.main.CoreRoutedPipe;
import logisticspipes.main.ItemMessage;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler {
	
	public static void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {

		final DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {

			final int packetID = data.read();
			switch (packetID) {
				case NetworkConstants.CRAFTING_PIPE_SATELLITE_ID:
					final PacketPipeInteger packetAa = new PacketPipeInteger();
					packetAa.readData(data);
					onCraftingPipeSetSatellite(packetAa);
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
					final PacketPipeInteger packetH = new PacketPipeInteger();
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
					final PacketPipeInteger packetJ = new PacketPipeInteger();
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
					final PacketPipeInteger packetM = new PacketPipeInteger();
					packetM.readData(data);
					onAdvancedExtractorModuleIncludeRecive(packetM);
					break;
				//case NetworkConstants.NON_CONTAINER_GUI:
				//	final PacketPipeInteger packetN = new PacketPipeInteger();
				//	packetN.readData(data);
				//	handleNonContainerGui(packetN);
				//	break;
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

		((BaseLogicCrafting) pipe.pipe.logic).setSatelliteId(packet.integer);
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

		if (!(pipe.pipe.logic instanceof BaseLogicSatellite)) {
			return;
		}

		((BaseLogicSatellite) pipe.pipe.logic).setSatelliteId(packet.integer);
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

	private static void onItemSinkStatusRecive(PacketPipeInteger packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiItemSink) {
			((GuiItemSink) FMLClientHandler.instance().getClient().currentScreen).handleDefaultRoutePackage(packet);
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
		
		ExtractionMode mode = ((LogicProvider) pipe.pipe.logic).getExtractionMode();
		int modeint = mode.ordinal();
		while(modeint != packet.integer) {
			((LogicProvider) pipe.pipe.logic).nextExtractionMode();
			modeint = ((LogicProvider) pipe.pipe.logic).getExtractionMode().ordinal();
			if(mode.ordinal() == modeint) {
				//loop break
				break;
			}
		}
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

	private static void onModulePipeRecive(PacketPipeInteger packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiExtractor) {
			((GuiExtractor) FMLClientHandler.instance().getClient().currentScreen).handlePackat(packet);
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

	private static void onAdvancedExtractorModuleIncludeRecive(PacketPipeInteger packet) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiAdvancedExtractor) {
			((GuiAdvancedExtractor) FMLClientHandler.instance().getClient().currentScreen).handleIncludeRoutePackage(packet);
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

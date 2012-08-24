package logisticspipes.buildcraft.krapht.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import logisticspipes.buildcraft.krapht.CoreRoutedPipe;
import logisticspipes.buildcraft.krapht.ItemMessage;
import logisticspipes.buildcraft.krapht.gui.GuiProviderPipe;
import logisticspipes.buildcraft.krapht.gui.GuiSupplierPipe;
import logisticspipes.buildcraft.krapht.gui.orderer.GuiOrderer;
import logisticspipes.buildcraft.krapht.gui.popup.GuiDiskPopup;
import logisticspipes.buildcraft.krapht.logic.BaseLogicCrafting;
import logisticspipes.buildcraft.krapht.logic.BaseLogicSatellite;
import logisticspipes.buildcraft.krapht.logic.LogicProvider;
import logisticspipes.buildcraft.krapht.logic.LogicSupplier;
import logisticspipes.buildcraft.krapht.pipes.PipeItemsApiaristSink;
import logisticspipes.buildcraft.krapht.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.buildcraft.logisticspipes.ExtractionMode;
import logisticspipes.buildcraft.logisticspipes.modules.GuiAdvancedExtractor;
import logisticspipes.buildcraft.logisticspipes.modules.GuiExtractor;
import logisticspipes.buildcraft.logisticspipes.modules.GuiItemSink;
import logisticspipes.buildcraft.logisticspipes.modules.GuiProvider;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.krapht.ItemIdentifier;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.transport.TileGenericPipe;
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
					
				case NetworkConstants.DISK_MACRO_REQUEST_RESPONSE:
					final PacketItems packetP = new PacketItems();
					packetP.readData(data);
					handleMacroResponse(packetP);
				case NetworkConstants.BEE_MODULE_CONTENT:
					final PacketModuleNBT packetQ = new PacketModuleNBT();
					packetQ.readData(data);
					handleBeePacketNBT(packetQ);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void onCraftingPipeSetSatellite(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}

		((BaseLogicCrafting) pipe.pipe.logic).setSatelliteId(packet.integer);
	}

	private static void onCraftingPipeSetImport(PacketInventoryChange packet) {
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
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
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof BaseLogicSatellite)) {
			return;
		}

		((BaseLogicSatellite) pipe.pipe.logic).setSatelliteId(packet.integer);
	}

	private static void onOrdererRefreshAnswer(PacketRequestGuiContent packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiOrderer) {
			((GuiOrderer) ModLoader.getMinecraftInstance().currentScreen).handlePacket(packet);
		}
	}

	private static void onItemsResponse(PacketItems packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)ModLoader.getMinecraftInstance().currentScreen).handleRequestAnswer(packet.items,!packet.error,(GuiOrderer)ModLoader.getMinecraftInstance().currentScreen,ModLoader.getMinecraftInstance().thePlayer);
		} else if(packet.error) {
			for (final ItemMessage items : packet.items) {
				ModLoader.getMinecraftInstance().thePlayer.addChatMessage("Missing: " + items);
			}
		} else {
			for (final ItemMessage items : packet.items) {
				ModLoader.getMinecraftInstance().thePlayer.addChatMessage("Requested: " + items);
			}
			ModLoader.getMinecraftInstance().thePlayer.addChatMessage("Request successful!");
		}
	}

	private static void onCraftingLoop(PacketCraftingLoop packet) {
		final ItemIdentifier item = packet.items.get(0).getItemIdentifier();
		ModLoader.getMinecraftInstance().thePlayer.addChatMessage("Logistics: Possible crafting loop while trying to craft " + item.getFriendlyName()
				+ " !! ABORTING !!");
	}

	private static void onItemSinkStatusRecive(PacketPipeInteger packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiItemSink) {
			((GuiItemSink) ModLoader.getMinecraftInstance().currentScreen).handleDefaultRoutePackage(packet);
		}
	}

	private static void onProviderPipeModeRecive(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
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
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicProvider)) {
			return;
		}
		((LogicProvider) pipe.pipe.logic).setFilterExcluded(packet.integer == 1);
		
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiProviderPipe) {
			((GuiProviderPipe) ModLoader.getMinecraftInstance().currentScreen).refreshInclude();
		}
	}

	private static void onSupplierPipeRecive(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSupplier)) {
			return;
		}
		((LogicSupplier) pipe.pipe.logic).setRequestingPartials(packet.integer == 1);
		
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiSupplierPipe) {
			((GuiSupplierPipe) ModLoader.getMinecraftInstance().currentScreen).refreshMode();
		}
	}

	private static void onModulePipeRecive(PacketPipeInteger packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiExtractor) {
			((GuiExtractor) ModLoader.getMinecraftInstance().currentScreen).handlePackat(packet);
		}
	}

	private static void onProviderModuleModeRecive(PacketPipeInteger packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiProvider) {
			((GuiProvider) ModLoader.getMinecraftInstance().currentScreen).handleModuleModeRecive(packet);
		}
	}

	private static void onProviderModuleIncludeRecive(PacketPipeInteger packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiProvider) {
			((GuiProvider) ModLoader.getMinecraftInstance().currentScreen).handleModuleIncludeRecive(packet);
		}
	}

	private static void onAdvancedExtractorModuleIncludeRecive(PacketPipeInteger packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiAdvancedExtractor) {
			((GuiAdvancedExtractor) ModLoader.getMinecraftInstance().currentScreen).handleIncludeRoutePackage(packet);
		}
	}

	//private void handleNonContainerGui(PacketPipeInteger packet) {
	//	Object gui = new GuiHandler().getGuiElement(packet.integer, ModLoader.getMinecraftInstance().thePlayer, ModLoader.getMinecraftInstance().theWorld,packet.posX,packet.posY,packet.posZ);
	//	if(gui instanceof GuiScreen) {
	//		ModLoader.openGUI(ModLoader.getMinecraftInstance().thePlayer, (GuiScreen)gui);
	//	}
	//}

	private static void handlePacketPipeUpdate(PacketPipeUpdate packet) {
		TileGenericPipe tile = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if(tile == null) {
			return;
		}
		if(tile.pipe == null) {
			return;
		}
		new TilePacketWrapper(new Class[] { TileGenericPipe.class, tile.pipe.transport.getClass(), tile.pipe.logic.getClass() }).fromPayload(new Object[] { tile.pipe.container, tile.pipe.transport, tile.pipe.logic },packet.getPayload());
	}

	private static void handleRequestMK2DiskItem(PacketItem packet) {
		final TileGenericPipe tile = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if(tile == null) {
			return;
		}
		if(tile.pipe instanceof PipeItemsRequestLogisticsMk2) {
			((PipeItemsRequestLogisticsMk2)tile.pipe).setDisk(packet.itemstack);
		}
	}
	
	private static void handleMacroResponse(PacketItems packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiOrderer) {
			if(((GuiOrderer) ModLoader.getMinecraftInstance().currentScreen).getSubGui() instanceof GuiDiskPopup) {
				((GuiOrderer) ModLoader.getMinecraftInstance().currentScreen).handleRequestAnswer(packet.items, packet.error, ((GuiOrderer) ModLoader.getMinecraftInstance().currentScreen).getSubGui(),ModLoader.getMinecraftInstance().thePlayer);
			}
		}
	}

	private static void handleBeePacketNBT(PacketModuleNBT packet) {
		final TileGenericPipe tile = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
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

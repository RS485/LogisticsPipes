package net.minecraft.src.buildcraft.krapht.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.ModLoader;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.ErrorMessage;
import net.minecraft.src.buildcraft.krapht.gui.GuiOrderer;
import net.minecraft.src.buildcraft.krapht.gui.GuiProviderPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiSupplierPipe;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.logic.LogicProvider;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.logisticspipes.ExtractionMode;
import net.minecraft.src.buildcraft.logisticspipes.modules.GuiItemSink;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.IPacketHandler;
import net.minecraft.src.krapht.ItemIdentifier;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] bytes) {

		final DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
		try {
			network.getNetHandler();

			final int packetID = data.read();
			switch (packetID) {
				case NetworkConstants.CRAFTING_PIPE_SATELLITE_ID:
					final PacketPipeInteger packet = new PacketPipeInteger();
					packet.readData(data);
					onCraftingPipeSetSatellite(packet);
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
					final PacketMissingItems packetD = new PacketMissingItems();
					packetD.readData(data);
					onMissingItems(packetD);
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
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private void onCraftingPipeSetSatellite(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicCrafting)) {
			return;
		}

		((LogicCrafting) pipe.pipe.logic).setSatelliteId(packet.integer);
	}

	private void onCraftingPipeSetImport(PacketInventoryChange packet) {
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicCrafting)) {
			return;
		}

		final LogicCrafting craftingPipe = (LogicCrafting) pipe.pipe.logic;
		for (int i = 0; i < packet.itemStacks.size(); i++) {
			craftingPipe.setDummyInventorySlot(i, packet.itemStacks.get(i));
		}
	}

	private void onSatellitePipeSetSatellite(PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSatellite)) {
			return;
		}

		((LogicSatellite) pipe.pipe.logic).setSatelliteId(packet.integer);
	}

	private void onOrdererRefreshAnswer(PacketRequestGuiContent packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiOrderer) {
			((GuiOrderer) ModLoader.getMinecraftInstance().currentScreen).handlePacket(packet);
		}
	}

	private void onMissingItems(PacketMissingItems packet) {
		for (final ErrorMessage error : packet.errors) {
			ModLoader.getMinecraftInstance().thePlayer.addChatMessage("Missing: " + error);
		}
	}

	private void onCraftingLoop(PacketCraftingLoop packet) {
		final ItemIdentifier item = packet.errors.get(0).getItemIdentifier();
		ModLoader.getMinecraftInstance().thePlayer.addChatMessage("Logistics: Possible crafting loop while trying to craft " + item.getFriendlyName()
				+ " !! ABORTING !!");
	}

	private void onItemSinkStatusRecive(PacketPipeInteger packet) {
		if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiItemSink) {
			((GuiItemSink) ModLoader.getMinecraftInstance().currentScreen).handleDefaultRoutePackage(packet);
		}
	}

	private void onProviderPipeModeRecive(PacketPipeInteger packet) {
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
			modeint = mode.ordinal();
			if(mode.ordinal() == modeint) {
				//loop break
				break;
			}
		}
	}

	private void onProviderPipeIncludeRecive(PacketPipeInteger packet) {
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

	private void onSupplierPipeRecive(PacketPipeInteger packet) {
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

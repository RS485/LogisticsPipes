package net.minecraft.src.buildcraft.krapht.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.network.PacketSlotChange;
import net.minecraft.src.buildcraft.krapht.gui.GuiOrderer;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.transport.PipeLogicDiamond;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.IPacketHandler;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] bytes) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
		try
		{
			NetClientHandler net = (NetClientHandler)network.getNetHandler();

			int packetID = data.read();
			switch (packetID) {
			case NetworkConstants.CRAFTING_PIPE_SATELLITE_ID:
				PacketPipeInteger packet = new PacketPipeInteger();
				packet.readData(data);
				onCraftingPipeSetSatellite(packet);
				break;
			case NetworkConstants.CRAFTING_PIPE_IMPORT_BACK:
				PacketInventoryChange packetA = new PacketInventoryChange();
				packetA.readData(data);
				onCraftingPipeSetImport(packetA);
				break;
			case NetworkConstants.SATELLITE_PIPE_SATELLITE_ID:
				PacketPipeInteger packetB = new PacketPipeInteger();
				packetB.readData(data);
				onSatellitePipeSetSatellite(packetB);
				break;
			case NetworkConstants.ORDERER_CONTENT_ANSWER:
				PacketRequestGuiContent packetC = new PacketRequestGuiContent();
				packetC.readData(data);
				onOrdererRefreshAnswer(packetC);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void onCraftingPipeSetSatellite(PacketPipeInteger packet) {
		TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof LogicCrafting))
			return;
		
		((LogicCrafting) pipe.pipe.logic).setSatelliteId(packet.integer);
	}

	private void onCraftingPipeSetImport(PacketInventoryChange packet) {
		TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;

		if(!(pipe.pipe.logic instanceof LogicCrafting))
			return;
		
		LogicCrafting craftingPipe = (LogicCrafting) pipe.pipe.logic;
		for(int i=0; i < packet.itemStacks.size(); i++) {
			craftingPipe.setDummyInventorySlot(i, packet.itemStacks.get(i));
		}
	}
	
	private void onSatellitePipeSetSatellite(PacketPipeInteger packet) {
		TileGenericPipe pipe = getPipe(ModLoader.getMinecraftInstance().theWorld, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof LogicSatellite))
			return;
		
		((LogicSatellite) pipe.pipe.logic).setSatelliteId(packet.integer);
	}

	private void onOrdererRefreshAnswer(PacketRequestGuiContent packet) {
		if(ModLoader.getMinecraftInstance().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)ModLoader.getMinecraftInstance().currentScreen).handlePacket(packet);
		}
	}

	// BuildCraft method
	/**
	 * Retrieves pipe at specified coordinates if any.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private TileGenericPipe getPipe(World world, int x, int y, int z) {
		if(!world.blockExists(x, y, z))
			return null;
		
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe))
			return null;

		return (TileGenericPipe)tile;
	}
	// BuildCraft method end
}

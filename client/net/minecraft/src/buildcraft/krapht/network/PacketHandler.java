package net.minecraft.src.buildcraft.krapht.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
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
				PacketCraftingPipeSatelliteId packet = new PacketCraftingPipeSatelliteId();
				packet.readData(data);
				onCraftingPipeSetSatellite(packet);
				break;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void onCraftingPipeSetSatellite(PacketCraftingPipeSatelliteId packet) {
		World world = ModLoader.getMinecraftInstance().theWorld;
		TileGenericPipe pipe = getPipe(world, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof LogicCrafting))
			return;
		
		((LogicCrafting) pipe.pipe.logic).setSatelliteId(packet.satelliteId);
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

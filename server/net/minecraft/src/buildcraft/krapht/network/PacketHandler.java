package net.minecraft.src.buildcraft.krapht.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.LinkedList;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.krapht.routing.OrdererRequests;
import net.minecraft.src.buildcraft.logisticspipes.MessageManager;
import net.minecraft.src.buildcraft.transport.PipeLogicDiamond;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.IPacketHandler;
import net.minecraft.src.krapht.ItemIdentifier;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] bytes) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
		try
		{
			NetServerHandler net = (NetServerHandler)network.getNetHandler();
			int packetID = data.read();
			switch (packetID) {

			case NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE:
				PacketCoordinates packet = new PacketCoordinates();
				packet.readData(data);
				onCraftingPipeNextSatellite(net.getPlayerEntity(), packet);
				break;
				
			case NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE:
				PacketCoordinates packetA = new PacketCoordinates();
				packetA.readData(data);
				onCraftingPipePrevSatellite(net.getPlayerEntity(), packetA);
				break;
			case NetworkConstants.CRAFTING_PIPE_IMPORT:
				PacketCoordinates packetB = new PacketCoordinates();
				packetB.readData(data);
				onCraftingPipeImport(net.getPlayerEntity(), packetB);
				break;
			case NetworkConstants.SATELLITE_PIPE_NEXT:
				PacketCoordinates packetC = new PacketCoordinates();
				packetC.readData(data);
				onSatellitePipeNext(net.getPlayerEntity(), packetC);
				break;
			case NetworkConstants.SATELLITE_PIPE_PREV:
				PacketCoordinates packetD = new PacketCoordinates();
				packetD.readData(data);
				onSatellitePipePrev(net.getPlayerEntity(), packetD);
				break;
			case NetworkConstants.CHASSI_GUI_PACKET_ID:
				PacketPipeInteger packetE = new PacketPipeInteger();
				packetE.readData(data);
				onModuleGuiOpen(net.getPlayerEntity(), packetE);
				break;
			case NetworkConstants.GUI_BACK_PACKET:
				PacketPipeInteger packetF = new PacketPipeInteger();
				packetF.readData(data);
				onGuiBackOpen(net.getPlayerEntity(), packetF);
				break;
			case NetworkConstants.REQUEST_SUBMIT:
				PacketRequestSubmit packetG = new PacketRequestSubmit();
				packetG.readData(data);
				onRequestSubmit(net.getPlayerEntity(), packetG);
				break;
			case NetworkConstants.ORDERER_REFRESH_REQUEST:
				PacketPipeInteger packetH = new PacketPipeInteger();
				packetH.readData(data);
				onRefreshRequest(net.getPlayerEntity(), packetH);
				break;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	private void onCraftingPipeNextSatellite(EntityPlayerMP player, PacketCoordinates packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof LogicCrafting))
			return;
		
		((LogicCrafting) pipe.pipe.logic).setNextSatellite(player);
	}

	private void onCraftingPipePrevSatellite(EntityPlayerMP player, PacketCoordinates packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof LogicCrafting))
			return;
		
		((LogicCrafting) pipe.pipe.logic).setPrevSatellite(player);
	}

	private void onCraftingPipeImport(EntityPlayerMP player, PacketCoordinates packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof LogicCrafting))
			return;
		
		((LogicCrafting) pipe.pipe.logic).importFromCraftingTable(player);
	}

	private void onSatellitePipeNext(EntityPlayerMP player, PacketCoordinates packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof LogicSatellite))
			return;
		
		((LogicSatellite) pipe.pipe.logic).setNextId(player);
	}

	private void onSatellitePipePrev(EntityPlayerMP player, PacketCoordinates packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof LogicSatellite))
			return;
		
		((LogicSatellite) pipe.pipe.logic).setPrevId(player);
	}

	private void onModuleGuiOpen(EntityPlayerMP player, PacketPipeInteger packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe instanceof PipeLogisticsChassi))
			return;
		
		PipeLogisticsChassi cassiPipe = (PipeLogisticsChassi) pipe.pipe;
		
		player.openGui(mod_LogisticsPipes.instance, cassiPipe.getLogisticsModule().getSubModule(packet.integer).getGuiHandlerID()  + (100 * (packet.integer + 1)), player.worldObj, packet.posX, packet.posY, packet.posZ);
	}

	private void onGuiBackOpen(EntityPlayerMP player,PacketPipeInteger packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe instanceof PipeLogisticsChassi))
			return;
		
		player.openGui(mod_LogisticsPipes.instance, packet.integer, player.worldObj, packet.posX, packet.posY, packet.posZ);
	}
	
	private void onRequestSubmit(EntityPlayerMP player, PacketRequestSubmit packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe instanceof PipeItemsRequestLogistics))
			return;
		
		OrdererRequests.request(player, packet, (PipeItemsRequestLogistics)pipe.pipe);
	}

	private void onRefreshRequest(EntityPlayerMP player, PacketPipeInteger packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe instanceof CoreRoutedPipe))
			return;
		
		OrdererRequests.DisplayOptions option;
		switch(packet.integer) {
		case 0:
			option = OrdererRequests.DisplayOptions.Both;
			break;
		case 1:
			option = OrdererRequests.DisplayOptions.SupplyOnly;
			break;
		case 2:
			option = OrdererRequests.DisplayOptions.CraftOnly;
			break;
		default: 
			option = OrdererRequests.DisplayOptions.Both;
			break;
		}
		
		OrdererRequests.refresh(player, (CoreRoutedPipe)pipe.pipe, option);
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

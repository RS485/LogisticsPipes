/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.logic;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketInventoryChange;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;

public class LogicCrafting extends BaseLogicCrafting {

	/* ** NETWORK FUNCTIONS ** */

	public void setNextSatellite(EntityPlayerMP player) {
		super.setNextSatellite();

		// Using existing BuildCraft packet system
		final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
		CoreProxy.sendToPlayer(player, packet);
	}

	public void setPrevSatellite(EntityPlayerMP player) {
		super.setPrevSatellite();

		// Using existing BuildCraft packet system
		final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
		CoreProxy.sendToPlayer(player, packet);
	}

	public void importFromCraftingTable(EntityPlayerMP player) {
		super.importFromCraftingTable();

		// Send inventory as packet
		// Using existing BuildCraft packet system
		final PacketInventoryChange packet = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, xCoord, yCoord, zCoord, _dummyInventory);
		CoreProxy.sendToPlayer(player, packet);
	}
}

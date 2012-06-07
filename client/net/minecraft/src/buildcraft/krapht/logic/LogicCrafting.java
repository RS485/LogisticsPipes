/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.logic;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketCoordinates;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.krapht.SimpleInventory;

public class LogicCrafting extends BaseLogicCrafting {

	/* ** NETWORK FUNCTIONS ** */

	@Override
	public void setNextSatellite() {
		super.setNextSatellite();

		if (APIProxy.isRemote()) {
			// Using existing BuildCraft packet system
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE, xCoord, yCoord, zCoord);
			CoreProxy.sendToServer(packet.getPacket());
		}
	}

	@Override
	public void setPrevSatellite() {
		super.setPrevSatellite();

		if (APIProxy.isRemote()) {
			// Using existing BuildCraft packet system
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE, xCoord, yCoord, zCoord);
			CoreProxy.sendToServer(packet.getPacket());
		}
	}

	// This is called by the packet PacketCraftingPipeSatelliteId
	public void setSatelliteId(int satelliteId) {
		this.satelliteId = satelliteId;
	}

	@Override
	public void importFromCraftingTable() {
		super.importFromCraftingTable();

		// Send packet asking for import
		if (APIProxy.isRemote()) {
			// Using existing BuildCraft packet system
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_IMPORT, xCoord, yCoord, zCoord);
			CoreProxy.sendToServer(packet.getPacket());
		}
	}

	public void setDummyInventorySlot(int slot, ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}

	/* ** GUI ** */

	public SimpleInventory get_dummyInventory() {
		return _dummyInventory;
	}

	/* ** NON NETWORKING ** */

	public void paintPathToSatellite() {
		final IRouter satelliteRouter = getSatelliteRouter();
		if (satelliteRouter == null) {
			return;
		}

		getRouter().displayRouteTo(satelliteRouter);
	}
}

/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.logic;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;

public class LogicSatellite extends BaseLogicSatellite {

	/* ** NETWORK FUNCTIONS ** */

	public void setNextId(EntityPlayerMP player) {
		super.setNextId();

		// Using existing BuildCraft packet system
		final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
		CoreProxy.sendToPlayer(player, packet);
	}

	public void setPrevId(EntityPlayerMP player) {
		super.setPrevId();

		// Using existing BuildCraft packet system
		final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
		CoreProxy.sendToPlayer(player, packet);
	}

	@Override
	public void onWrenchClicked(EntityPlayer player) {
		// Send the satellite id when opening gui
		// Using existing BuildCraft packet system
		final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
		CoreProxy.sendToPlayer(player, packet);
		player.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_SatelitePipe_ID, worldObj, xCoord, yCoord, zCoord);
	}
}

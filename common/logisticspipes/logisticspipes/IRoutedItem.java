/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.order.IDistanceTracker;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * This interface describes the actions that must be available on an item that
 * is considered routed
 */
public interface IRoutedItem {

	public enum TransportMode {
		Unknown,
		Default,
		Passive,
		Active
	}

	public int getDestination();

	public UUID getDestinationUUID();

	public void setDestination(int destination);

	public void clearDestination();

	public void setTransportMode(TransportMode transportMode);

	public TransportMode getTransportMode();

	public void setAdditionalTargetInformation(IAdditionalTargetInformation info);

	public IAdditionalTargetInformation getAdditionalTargetInformation();

	public void setDoNotBuffer(boolean doNotBuffer);

	public boolean getDoNotBuffer();

	public int getBufferCounter();

	public void setBufferCounter(int counter);

	public void setArrived(boolean flag);

	public boolean getArrived();

	public void addToJamList(IRouter router);

	public List<Integer> getJamList();

	public void checkIDFromUUID();

	ItemIdentifierStack getItemIdentifierStack();

	public void readFromNBT(NBTTagCompound data);

	public void writeToNBT(NBTTagCompound tagentityitem);

	public void setDistanceTracker(IDistanceTracker tracker);

	public IDistanceTracker getDistanceTracker();

	public ItemRoutingInformation getInfo();

	void split(int itemsToTake, ForgeDirection orientation);
}

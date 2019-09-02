/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.order.IDistanceTracker;
import logisticspipes.utils.item.ItemIdentifierStack;

/**
 * This interface describes the actions that must be available on an item that
 * is considered routed
 */
public interface IRoutedItem {

	enum TransportMode {
		Unknown,
		Default,
		Passive,
		Active
	}

	int getDestination();

	UUID getDestinationUUID();

	void setDestination(int destination);

	void clearDestination();

	void setTransportMode(TransportMode transportMode);

	TransportMode getTransportMode();

	void setAdditionalTargetInformation(IAdditionalTargetInformation info);

	IAdditionalTargetInformation getAdditionalTargetInformation();

	void setDoNotBuffer(boolean doNotBuffer);

	boolean getDoNotBuffer();

	int getBufferCounter();

	void setBufferCounter(int counter);

	void setArrived(boolean flag);

	boolean getArrived();

	void addToJamList(IRouter router);

	List<Integer> getJamList();

	void checkIDFromUUID();

	ItemIdentifierStack getItemIdentifierStack();

	void readFromNBT(NBTTagCompound data);

	void writeToNBT(NBTTagCompound tagentityitem);

	void setDistanceTracker(IDistanceTracker tracker);

	IDistanceTracker getDistanceTracker();

	ItemRoutingInformation getInfo();

	void split(int itemsToTake, EnumFacing orientation);
}

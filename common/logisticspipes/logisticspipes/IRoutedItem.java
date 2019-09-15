/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.routing.Router;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.order.IDistanceTracker;

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

	void addToJamList(Router router);

	List<Integer> getJamList();

	void checkIDFromUUID();

	ItemStack getStack();

	void readFromNBT(CompoundTag data);

	void writeToNBT(CompoundTag tagentityitem);

	void setDistanceTracker(IDistanceTracker tracker);

	IDistanceTracker getDistanceTracker();

	ItemRoutingInformation getInfo();

	void split(int itemsToTake, Direction orientation);
}

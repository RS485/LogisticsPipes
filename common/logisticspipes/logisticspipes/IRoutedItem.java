/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import buildcraft.transport.TravelingItem;

import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * This interface describes the actions that must be available on an item that is considered routed
 *
 */
public interface IRoutedItem {
	
	public class DelayComparator implements Comparator<IRoutedItem> {

		@Override
		public int compare(IRoutedItem o1, IRoutedItem o2) {
			return (int)(o2.getTimeOut()-o1.getTimeOut()); // cast will never overflow because the delta is in 1/20ths of a second.
		}
	
	}
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
	
	public void setDoNotBuffer(boolean doNotBuffer);
	public boolean getDoNotBuffer();

	public int getBufferCounter();
	public void setBufferCounter(int counter);

	public ItemStack getItemStack();
	public void setItemStack(ItemStack item);
	
	public TravelingItem getTravelingItem();
	public TravelingItem getNewTravelingItem();
	
	public void setArrived(boolean flag);
	public boolean getArrived();
	
	public void split(int itemsToTake, ForgeDirection orientation);
	public void SetPosition(double x, double y, double z);
	
	public void addToJamList(IRouter router);
	public List<Integer> getJamList();
	
	public IRoutedItem getCopy();
	public void checkIDFromUUID();
	ItemIdentifierStack getIDStack();

	// how many ticks until this times out
	public long getTickToTimeOut();
	// the world tick in which getTickToTimeOut returns 0.
	public long getTimeOut();

//FIXME: not sure when/if this will be called correctly
	void remove();

	public NBTTagCompound getNBTData();
	public void loadFromNBT(NBTTagCompound data);
}

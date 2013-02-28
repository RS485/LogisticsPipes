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
import java.util.concurrent.Delayed;

import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.EntityPassiveItem;

/**
 * This interface describes the actions that must be available on an item that is considered routed
 * @author Krapht
 *
 */
public interface IRoutedItem extends Delayed{
	
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
	
	public void addRelayPoints(List<IRelayItem> relays);
	public void itemRelayed();
	public boolean isItemRelayed();
	public void replaceRelayID(int newId);
	
//	public boolean isPassive();
//	public void setPassive(boolean isPassive);
//	public boolean isDefault();
//	public void setDefault(boolean isDefault);
	
	public void setTransportMode(TransportMode transportMode);
	public TransportMode getTransportMode();
	
	public void setDoNotBuffer(boolean doNotBuffer);
	public boolean getDoNotBuffer();

	public int getBufferCounter();
	public void setBufferCounter(int counter);

	public ItemStack getItemStack();
	public void setItemStack(ItemStack item);
	
	//public void setSpeedBoost(float multiplier);
	//public float getSpeedBoost();
	
	public EntityPassiveItem getEntityPassiveItem();
	public IPipedItem getNewEntityPassiveItem();
	
	public void setArrived(boolean flag);
	public boolean getArrived();
	
	public void split(World worldObj, int itemsToTake, ForgeDirection orientation);
	public void SetPosition(double x, double y, double z);
	
	public void addToJamList(IRouter router);
	public List<Integer> getJamList();
	
	public IRoutedItem getCopy();
	public void checkIDFromUUID();
	ItemIdentifierStack getIDStack();
}

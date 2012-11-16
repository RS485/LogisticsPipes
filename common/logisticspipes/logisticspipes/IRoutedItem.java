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

import logisticspipes.routing.IRouter;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.EntityPassiveItem;

/**
 * This interface describes the actions that must be available on an item that is considered routed
 * @author Krapht
 *
 */
public interface IRoutedItem {
	
	public enum TransportMode {
		Unknown,
		Default,
		Passive,
		Active
	}
	
	public UUID getDestination();
	public void setDestination(UUID destination);
	public void changeDestination(UUID destination);
	public UUID getSource();
	public void setSource(UUID source);
	
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
	
	//public void setSpeedBoost(float multiplier);
	//public float getSpeedBoost();
	
	public EntityPassiveItem getEntityPassiveItem();
	public IRoutedItem getNewUnRoutedItem();
	public IPipedItem getNewEntityPassiveItem();
	
	public void setArrived(boolean flag);
	public boolean getArrived();
	
	public IRoutedItem split(World worldObj, int itemsToTake, ForgeDirection orientation);
	public void SetPosition(double x, double y, double z);
	
	public boolean isReRoute();
	public void setReRoute(boolean flag);
	
	public void addToJamList(IRouter router);
	public List<UUID> getJamList();
	
	public boolean isUnRouted();
}

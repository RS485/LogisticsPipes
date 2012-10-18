/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import java.util.UUID;

import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import buildcraft.api.core.Orientations;
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
	
	public ItemStack getItemStack();
	
	//public void setSpeedBoost(float multiplier);
	//public float getSpeedBoost();
	
	public EntityPassiveItem getEntityPassiveItem();
	public EntityPassiveItem getNewEntityPassiveItem();
	
	@Deprecated
	public void setArrived();
	
	public IRoutedItem split(World worldObj, int itemsToTake, Orientations orientation);
	public void SetPosition(double x, double y, double z);
		
}

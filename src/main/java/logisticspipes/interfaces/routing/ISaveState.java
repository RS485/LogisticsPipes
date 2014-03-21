/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.interfaces.routing;

import net.minecraft.nbt.NBTTagCompound;

public interface ISaveState {
	/**
	 * Called to read every information for the given class from the NBTTagCompount
	 * @param nbttagcompound to read from
	 */
	public void readFromNBT(NBTTagCompound nbttagcompound);
	
	/**
	 * Called to save all information of the given class into an NBTTagCompount
	 * @param nbttagcompound to save the information in
	 */
	public void writeToNBT(NBTTagCompound nbttagcompound);
}

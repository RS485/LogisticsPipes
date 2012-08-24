/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht;

import net.minecraft.src.NBTTagCompound;

public interface ISaveState {
	/**
	 * Called to read every information for the given class from the NBTTagCompount
	 * @param nbttagcompound to read from
	 * @param prefix before every key to seperate information
	 */
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix);
	
	/**
	 * Called to save all information of the given class into an NBTTagCompount
	 * @param nbttagcompound to save the information in
	 * @param prefix before every key to seperate information
	 */
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix);
}

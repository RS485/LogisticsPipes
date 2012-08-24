/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.routing;

import java.util.UUID;

import net.minecraft.src.World;


public interface IRouterManager {

	public IRouter getOrCreateRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord);
	public IRouter getRouter(UUID id);
	public boolean isRouter(UUID id);
}

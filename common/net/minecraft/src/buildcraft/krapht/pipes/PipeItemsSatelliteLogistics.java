/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;

public class PipeItemsSatelliteLogistics extends RoutedPipe implements IRequestItems{

	public PipeItemsSatelliteLogistics(int itemID) {
		super(new LogicSatellite(), itemID);
	}

	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_SATELLITE_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		// TODO Auto-generated method stub
		return null;
	}
}

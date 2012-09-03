/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.main.RoutedPipe;

public class PipeItemsSatelliteLogistics extends RoutedPipe implements IRequestItems{

	public PipeItemsSatelliteLogistics(int itemID) {
		super(new BaseLogicSatellite(), itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_SATELLITE_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
}

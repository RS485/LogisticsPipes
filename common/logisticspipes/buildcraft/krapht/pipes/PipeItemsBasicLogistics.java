/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht.pipes;

import logisticspipes.mod_LogisticsPipes;
import logisticspipes.buildcraft.krapht.RoutedPipe;
import logisticspipes.buildcraft.krapht.logic.TemporaryLogic;
import logisticspipes.buildcraft.logisticspipes.modules.ILogisticsModule;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleItemSink;

public class PipeItemsBasicLogistics extends RoutedPipe {
	
	private ModuleItemSink itemSinkModule;

	public PipeItemsBasicLogistics(int itemID) {
		super(new TemporaryLogic(), itemID);
		itemSinkModule = new ModuleItemSink();
	}

	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return itemSinkModule;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
}

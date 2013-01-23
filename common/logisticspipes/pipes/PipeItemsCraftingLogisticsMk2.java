/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeTransportLogistics;

public class PipeItemsCraftingLogisticsMk2 extends PipeItemsCraftingLogistics{
	
	public PipeItemsCraftingLogisticsMk2(int itemID) {
		super(itemID);
	}

	public PipeItemsCraftingLogisticsMk2(PipeTransportLogistics transport, int itemID) {
		super(transport, itemID);
	}

	@Override
	protected int neededEnergy() {
		return 15;
	}

	@Override
	protected int itemsToExtract() {
		return 64;
	}
	
	@Override
	protected int stacksToExtract() {
		return 1;
	}
	
	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CRAFTERMK2_TEXTURE;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}
}

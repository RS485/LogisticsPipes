/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import logisticspipes.modules.ModuleCrafterMK2;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

public class PipeItemsCraftingLogisticsMk2 extends PipeItemsCraftingLogistics {
	
	public PipeItemsCraftingLogisticsMk2(int itemID) {
		super(itemID);
		craftingModule=new ModuleCrafterMK2(this);
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

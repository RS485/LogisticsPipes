/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import logisticspipes.modules.ModuleCrafterMK2;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

import net.minecraft.item.Item;

public class PipeItemsCraftingLogisticsMk2 extends PipeItemsCraftingLogistics {

	public PipeItemsCraftingLogisticsMk2(Item item) {
		super(item);
		craftingModule = new ModuleCrafterMK2(this);
		craftingModule.registerPosition(ModulePositionType.IN_PIPE, 0);
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

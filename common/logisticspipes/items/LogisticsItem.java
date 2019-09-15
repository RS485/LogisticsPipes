/**
 * Copyright (c) Krapht, 2011
 * <p>
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.items;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import logisticspipes.utils.string.StringUtils;
import network.rs485.logisticspipes.item.ItemWithInfo;

@Deprecated
public class LogisticsItem extends ItemWithInfo {

	public LogisticsItem(Item.Settings settings) {
		super(settings);
	}

}

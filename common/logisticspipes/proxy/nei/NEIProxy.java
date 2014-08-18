package logisticspipes.proxy.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import logisticspipes.proxy.interfaces.INEIProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import codechicken.nei.api.ItemInfo;

public class NEIProxy implements INEIProxy {
	@Override
	public List<String> getInfoForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver) {
		List<ItemStack> items = ItemInfo.getIdentifierItems(world, player, objectMouseOver);
		if(items.isEmpty()) return new ArrayList<String>(0);
		Collections.sort(items, new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack stack0, ItemStack stack1) {
				return stack1.getItemDamage() - stack0.getItemDamage();
			}
		});
		return ItemInfo.getText(items.get(0), world, player, objectMouseOver);
	}

	@Override
	public ItemStack getItemForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver) {
		List<ItemStack> items = ItemInfo.getIdentifierItems(world, player, objectMouseOver);
		if(items.isEmpty()) return null;
		Collections.sort(items, new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack stack0, ItemStack stack1) {
				return stack1.getItemDamage() - stack0.getItemDamage();
			}
		});
		return items.get(0);
	}
}

package logisticspipes.items;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDisk extends LogisticsItem {

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			if (stack.getTagCompound().hasKey("name")) {
				String name = "\u00a78" + stack.getTagCompound().getString("name");
				tooltip.add(name);
			}
		}
	}
}

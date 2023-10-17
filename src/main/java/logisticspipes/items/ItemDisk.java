package logisticspipes.items;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemDisk extends LogisticsItem {

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (!stack.isEmpty() && stack.hasTagCompound()) {
			final NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
			if (tag.hasKey("name")) {
				String name = "\u00a78" + tag.getString("name");
				tooltip.add(name);
			}
		}
	}
}

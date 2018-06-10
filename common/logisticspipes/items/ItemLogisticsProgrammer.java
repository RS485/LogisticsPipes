package logisticspipes.items;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import logisticspipes.utils.string.StringUtils;

public class ItemLogisticsProgrammer extends LogisticsItem {

	public static final String RECIPE_TARGET = "LogisticsRecipeTarget";

	public ItemLogisticsProgrammer() {
		super();
		setNoRepair();
		setContainerItem(this);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(!stack.isEmpty()) {
			if(stack.hasTagCompound()) {
				NBTTagCompound nbt = stack.getTagCompound();
				String target = nbt.getString(RECIPE_TARGET);
				if (!target.isEmpty()) {
					Item target_item = REGISTRY.getObject(new ResourceLocation(target));
					if(target_item instanceof ItemModule) {
						tooltip.add(StringUtils.translate("tooltip.programmerForModule"));
						tooltip.add(StringUtils.translate(target_item.getUnlocalizedName()));
					} else if(target_item instanceof ItemLogisticsPipe) {
						tooltip.add(StringUtils.translate("tooltip.programmerForPipe"));
						tooltip.add(StringUtils.translate(target_item.getUnlocalizedName()));
					} else {
						tooltip.add(StringUtils.translate("tooltip.programmerForUnknown"));
					}
				}
			} else {
				tooltip.add(StringUtils.translate("tooltip.programmerForUnknown"));
			}
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
}

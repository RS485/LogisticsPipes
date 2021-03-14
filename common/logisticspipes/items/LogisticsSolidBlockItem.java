package logisticspipes.items;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.BlockDummy;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.interfaces.ILogisticsItem;

public class LogisticsSolidBlockItem extends ItemBlock implements ILogisticsItem {

	@Getter
	private final LogisticsSolidBlock.Type type;

	public LogisticsSolidBlockItem(LogisticsSolidBlock block) {
		super(block);
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
		type = block.getType();
		BlockDummy.updateItemMap.put(type.getMeta(), this);
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack itemstack) {
		return I18n.translateToLocal(getUnlocalizedName(itemstack) + ".name");
	}

}

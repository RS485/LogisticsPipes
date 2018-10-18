package logisticspipes.items;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.BlockDummy;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.interfaces.ILogisticsItem;
import logisticspipes.utils.string.StringUtils;
import lombok.Getter;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class LogisticsSolidBlockItem extends ItemBlock implements ILogisticsItem {

	@Getter
	private final LogisticsSolidBlock.Type type;

	public LogisticsSolidBlockItem(LogisticsSolidBlock block) {
		super(block);
		setHasSubtypes(true);
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
		type = block.getType();
		BlockDummy.updateItemMap.put(type.getMeta(), this);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		return StringUtils.translate(getUnlocalizedName(itemstack) + ".name");
	}

}

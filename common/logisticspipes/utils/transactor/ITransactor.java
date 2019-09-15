package logisticspipes.utils.transactor;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public interface ITransactor {

	ItemStack add(ItemStack stack, Direction orientation, boolean doAdd);
}

package logisticspipes.utils.transactor;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface ITransactor {

	/**
	 * Adds stack to the transactor
	 * @param stack to add.
	 * @param orientation side where transaction is being performed
	 * @param doAdd whether to commit or simulate transaction
	 * @return added stack.
	 */
	@Nonnull
	ItemStack add(@Nonnull ItemStack stack, EnumFacing orientation, boolean doAdd);
}

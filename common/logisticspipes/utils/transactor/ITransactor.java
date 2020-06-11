package logisticspipes.utils.transactor;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface ITransactor {

	@Nonnull
	ItemStack add(@Nonnull ItemStack stack, EnumFacing orientation, boolean doAdd);
}

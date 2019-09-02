package logisticspipes.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import logisticspipes.api.ILPPipeConfigTool;
import logisticspipes.api.ILPPipeTile;

public class ItemPipeManager extends LogisticsItem implements ILPPipeConfigTool {

	public ItemPipeManager() {
		super();
	}

	@Override
	public boolean canWrench(EntityPlayer player, ItemStack wrench, ILPPipeTile pipe) {
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, ItemStack wrench, ILPPipeTile pipe) {

	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}
}

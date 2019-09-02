package logisticspipes.items;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.guis.LogisticsPlayerSettingsGuiProvider;
import logisticspipes.proxy.MainProxy;

public class ItemPipeController extends LogisticsItem {

	public ItemPipeController() {
		super();
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand handIn) {
		ItemStack stack = player.getHeldItem(handIn);
		if (MainProxy.isClient(world)) {
			return new ActionResult<>(EnumActionResult.PASS, stack);
		}
		useItem(player, world);
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (MainProxy.isClient(world)) {
			return EnumActionResult.PASS;
		}
		useItem(player, world);
		return EnumActionResult.SUCCESS;
	}

	private void useItem(EntityPlayer player, World world) {
		NewGuiHandler.getGui(LogisticsPlayerSettingsGuiProvider.class).open(player);
	}
}

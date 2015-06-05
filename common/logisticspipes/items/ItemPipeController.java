package logisticspipes.items;

import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.guis.LogisticsPlayerSettingsGuiProvider;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemPipeController extends LogisticsItem {

	public ItemPipeController() {
		super();
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (MainProxy.isClient(world)) {
			return stack;
		}
		useItem(player, world);
		return stack.copy();
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return false;
	}

	private void useItem(EntityPlayer player, World world) {
		NewGuiHandler.getGui(LogisticsPlayerSettingsGuiProvider.class).open(player);
	}
}

package logisticspipes.items;

import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.guis.item.ItemMangerGui;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LogisticsNetworkManager extends LogisticsItem {

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		if(par3EntityPlayer.isSneaking()) {
			if(MainProxy.isServer(par2World)) {
				NewGuiHandler.getGui(ItemMangerGui.class).open(par3EntityPlayer);
			}
			return par1ItemStack.copy();
		}
		return super.onItemRightClick(par1ItemStack, par2World, par3EntityPlayer);
	}
}

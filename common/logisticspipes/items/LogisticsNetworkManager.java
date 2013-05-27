package logisticspipes.items;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LogisticsNetworkManager extends LogisticsItem {

	public LogisticsNetworkManager(int i) {
		super(i);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		if(par3EntityPlayer.isSneaking()) {
			if(MainProxy.isServer(par2World)) {
				par3EntityPlayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Item_Manager, par2World, 0, 0, 0);
			}
			return par1ItemStack.copy();
		}
		return super.onItemRightClick(par1ItemStack, par2World, par3EntityPlayer);
	}
}

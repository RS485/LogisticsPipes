package logisticspipes.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ToolWrenchHandler {

	public boolean isWrenchEquipped(EntityPlayer entityplayer) {
		ItemStack stack = entityplayer.inventory.mainInventory.get(entityplayer.inventory.currentItem);
		if(stack.isEmpty()) return false;
		if(SimpleServiceLocator.thermalExpansionProxy.isToolHammer(stack.getItem())) return true;
		return false;
	}

	@Deprecated
	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z) {
		return canWrench(entityplayer, new BlockPos(x, y, z));
	}

	public boolean canWrench(EntityPlayer entityplayer, BlockPos pos) {
		ItemStack stack = entityplayer.inventory.mainInventory.get(entityplayer.inventory.currentItem);
		if(stack.isEmpty()) return false;

		if(SimpleServiceLocator.thermalExpansionProxy.isToolHammer(stack.getItem())) return SimpleServiceLocator.thermalExpansionProxy.canHammer(stack, entityplayer, pos);
		return false;
	}

	@Deprecated
	public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z) {
		wrenchUsed(entityplayer, new BlockPos(x, y, z));
	}

	public void wrenchUsed(EntityPlayer entityplayer, BlockPos pos) {
		ItemStack stack = entityplayer.inventory.mainInventory.get(entityplayer.inventory.currentItem);
		if(stack.isEmpty()) return;
		if(SimpleServiceLocator.thermalExpansionProxy.isToolHammer(stack.getItem())) SimpleServiceLocator.thermalExpansionProxy.toolUsed(stack, entityplayer, pos);
	}

	public boolean isWrench(Item item) {
		if(SimpleServiceLocator.thermalExpansionProxy.isToolHammer(item)) return true;
		return false;
	}
}

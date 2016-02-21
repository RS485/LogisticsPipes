package logisticspipes.proxy.buildcraft.subproxies;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;

public interface IBCPipePart {

	boolean canConnectRedstone();

	int isPoweringTo(EnumFacing side);

	int isIndirectlyPoweringTo(int l);

	Object getClientGui(InventoryPlayer inventory, int side);

	Container getGateContainer(InventoryPlayer inventory, int side);

	void addItemDrops(ArrayList<ItemStack> result);

	Object getOriginal();

}

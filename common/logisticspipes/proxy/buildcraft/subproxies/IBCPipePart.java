package logisticspipes.proxy.buildcraft.subproxies;

import java.util.ArrayList;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public interface IBCPipePart {

	boolean canConnectRedstone();

	int isPoweringTo(int l);

	int isIndirectlyPoweringTo(int l);

	Object getClientGui(InventoryPlayer inventory, int side);

	Container getGateContainer(InventoryPlayer inventory, int side);

	void addItemDrops(ArrayList<ItemStack> result);

	Object getOriginal();

}

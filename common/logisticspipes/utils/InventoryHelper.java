package logisticspipes.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemAttributes;

import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.transactor.TransactorSimple;

public class InventoryHelper {

	// BC getTransactorFor using our getInventory
	public static ITransactor getTransactorFor(World world, BlockPos pos, Direction dir) {
		ITransactor t = InventoryUtilFactory.INSTANCE.getUtil(world, pos, dir, false, false, 0, 0);
		if (t != null) return t;

		FixedItemInv inv = ItemAttributes.FIXED_INV.get(world, pos, SearchOptions.inDirection(dir));
		if (inv.getSlotCount() > 0) return new TransactorSimple(inv);

		return null;
	}

}

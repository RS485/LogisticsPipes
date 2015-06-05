package logisticspipes.proxy.bs;

import logisticspipes.proxy.interfaces.IBetterStorageProxy;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.mcft.copy.betterstorage.api.crate.ICrateStorage;

public class BetterStorageProxy implements IBetterStorageProxy {

	@Override
	public boolean isBetterStorageCrate(TileEntity tile) {
		return tile instanceof ICrateStorage;
	}

	@Override
	public ICrateStorageProxy getCrateStorageProxy(TileEntity tile) {
		final ICrateStorage crate = (ICrateStorage) tile;
		return new ICrateStorageProxy() {

			@Override
			public Iterable<ItemStack> getContents() {
				return crate.getContents();
			}

			@Override
			public int getUniqueItems() {
				return crate.getUniqueItems();
			}

			@Override
			public int getItemCount(ItemStack stack) {
				return crate.getItemCount(stack);
			}

			@Override
			public ItemStack extractItems(ItemStack stack, int count) {
				return crate.extractItems(stack, count);
			}

			@Override
			public int getSpaceForItem(ItemStack stack) {
				return crate.getSpaceForItem(stack);
			}

			@Override
			public ItemStack insertItems(ItemStack stack) {
				return crate.insertItems(stack);
			}
		};
	}
}

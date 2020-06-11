package logisticspipes.proxy.bs;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.mcft.copy.betterstorage.api.crate.ICrateStorage;

import logisticspipes.proxy.interfaces.IBetterStorageProxy;

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
			public int getItemCount(@Nonnull ItemStack stack) {
				return crate.getItemCount(stack);
			}

			@Override
			public @Nonnull ItemStack extractItems(@Nonnull ItemStack stack, int count) {
				return crate.extractItems(stack, count);
			}

			@Override
			public int getSpaceForItem(@Nonnull ItemStack stack) {
				return crate.getSpaceForItem(stack);
			}

			@Override
			public @Nonnull ItemStack insertItems(@Nonnull ItemStack stack) {
				return crate.insertItems(stack);
			}
		};
	}
}

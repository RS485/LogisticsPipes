package logisticspipes.proxy.specialinventoryhandler;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.VersionRange;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.api.storage.ISmartGroup;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.IVoidable;

import logisticspipes.utils.item.ItemIdentifier;

public class StorageDrawersInventoryHandler extends SpecialInventoryHandler {

	private final IDrawerGroup _drawer;
	private final ISmartGroup _smartGroup;
	private final boolean _hideOnePerStack;
	private final boolean _hideOnePerType;

	private StorageDrawersInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_drawer = (IDrawerGroup) tile;
		_smartGroup = (_drawer instanceof ISmartGroup) ? (ISmartGroup) _drawer : null;
		_hideOnePerStack = hideOnePerStack;
		_hideOnePerType = hideOne;
	}

	public StorageDrawersInventoryHandler() {
		_drawer = null;
		_smartGroup = null;
		_hideOnePerStack = false;
		_hideOnePerType = false;
	}

	@Override
	public boolean init() {
		List<ModContainer> modList = Loader.instance().getModList();
		for (ModContainer mod : modList) {
			if (mod.getModId().equals("StorageDrawers")) {
				try {
					VersionRange validVersions = VersionRange.createFromVersionSpec("[1.7.8,)");
					ArtifactVersion version = new DefaultArtifactVersion(mod.getVersion());
					return validVersions.containsVersion(version);
				} catch (InvalidVersionSpecificationException e) {
					return false;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isType(TileEntity tile, EnumFacing dir) {
		return tile instanceof IDrawerGroup;
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new StorageDrawersInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		int count = 0;
		boolean first = true;

		if (_smartGroup != null) {
			ItemStack protoStack = itemIdent.makeNormalStack(1);
			for (int slot : _smartGroup.enumerateDrawersForExtraction(protoStack, true)) {
				IDrawer drawer = _drawer.getDrawer(slot);
				if (drawer.isEmpty() || !ItemIdentifier.get(drawer.getStoredItemPrototype()).equals(itemIdent)) {
					continue;
				}

				count += drawer.getStoredItemCount() - ((_hideOnePerStack || (_hideOnePerType && first)) ? 1 : 0);
				first = false;
			}

			return count;
		}

		for (int i = 0; i < _drawer.getDrawerCount(); i++) {
			if (!_drawer.isDrawerEnabled(i)) {
				continue;
			}

			IDrawer drawer = _drawer.getDrawer(i);
			if (drawer == null) {
				continue;
			}

			if (!drawer.isEmpty() && ItemIdentifier.get(drawer.getStoredItemPrototype()).equals(itemIdent)) {
				count += drawer.getStoredItemCount() - ((_hideOnePerStack || (_hideOnePerType && first)) ? 1 : 0);
				first = false;
			}
		}

		return count;
	}

	@Override
	@Nonnull
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		ItemStack stack = ItemStack.EMPTY;

		if (_smartGroup != null) {
			ItemStack protoStack = itemIdent.makeNormalStack(1);
			for (int slot : _smartGroup.enumerateDrawersForExtraction(protoStack, true)) {
				IDrawer drawer = _drawer.getDrawer(slot);
				if (drawer.isEmpty() || !ItemIdentifier.get(drawer.getStoredItemPrototype()).equals(itemIdent)) {
					continue;
				}

				if (stack.isEmpty()) {
					stack = drawer.getStoredItemCopy();
					stack.setCount(0);
				}

				int avail = Math.min(count, drawer.getStoredItemCount());
				drawer.setStoredItemCount(drawer.getStoredItemCount() - avail);

				stack.grow(avail);
				count -= avail;

				if (count <= 0) {
					break;
				}
			}

			return stack;
		}

		for (int i = 0; i < _drawer.getDrawerCount(); i++) {
			if (!_drawer.isDrawerEnabled(i)) {
				continue;
			}

			IDrawer drawer = _drawer.getDrawer(i);
			if (drawer == null || drawer.isEmpty()) {
				continue;
			}

			if (ItemIdentifier.get(drawer.getStoredItemPrototype()).equals(itemIdent)) {
				if (stack.isEmpty()) {
					stack = drawer.getStoredItemCopy();
					stack.setCount(0);
				}

				int avail = Math.min(count, drawer.getStoredItemCount());
				drawer.setStoredItemCount(drawer.getStoredItemCount() - avail);

				stack.grow(avail);
				count -= avail;

				if (count <= 0) {
					break;
				}
			}
		}

		return stack;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<>();
		for (int i = 0; i < _drawer.getDrawerCount(); i++) {
			if (!_drawer.isDrawerEnabled(i)) {
				continue;
			}

			IDrawer drawer = _drawer.getDrawer(i);
			if (drawer != null && !drawer.isEmpty()) {
				result.add(ItemIdentifier.get(drawer.getStoredItemPrototype()));
			}
		}
		return result;
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> result = new HashMap<>();
		for (int i = 0; i < _drawer.getDrawerCount(); i++) {
			if (!_drawer.isDrawerEnabled(i)) {
				continue;
			}

			IDrawer drawer = _drawer.getDrawer(i);
			if (drawer != null && !drawer.isEmpty()) {
				int count = drawer.getStoredItemCount();
				if (count > 0) {
					ItemIdentifier ident = ItemIdentifier.get(drawer.getStoredItemPrototype());
					if (result.containsKey(ident)) {
						result.put(ident, result.get(ident) + count);
					} else {
						result.put(ident, count);
					}
				}
			}
		}
		return result;
	}

	@Override
	@Nonnull
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		return getMultipleItems(itemIdent, 1);
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		if (_smartGroup != null) {
			ItemStack stack = itemIdent.makeNormalStack(1);
			BitSet set = new BitSet();
			for (int slot : _smartGroup.enumerateDrawersForInsertion(stack, true)) {
				set.set(slot);
			}
			for (int slot : _smartGroup.enumerateDrawersForExtraction(stack, true)) {
				set.set(slot);
			}
			int slot = -1;
			while ((slot = set.nextSetBit(slot + 1)) != -1) {
				IDrawer drawer = _drawer.getDrawer(slot);
				if (!drawer.isEmpty() && ItemIdentifier.get(drawer.getStoredItemPrototype()).getUndamaged().equals(itemIdent)) {
					return true;
				}
			}
			return false;
		}

		for (int i = 0; i < _drawer.getDrawerCount(); i++) {
			if (!_drawer.isDrawerEnabled(i)) {
				continue;
			}

			IDrawer drawer = _drawer.getDrawer(i);
			if (drawer != null && !drawer.isEmpty()) {
				if (ItemIdentifier.get(drawer.getStoredItemPrototype()).getUndamaged().equals(itemIdent)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int roomForItem(@Nonnull ItemStack stack) {
		int room = 0;

		if (_smartGroup != null) {
			BitSet set = new BitSet();
			for (int slot : _smartGroup.enumerateDrawersForInsertion(stack, false)) {
				set.set(slot);
			}
			for (int slot : _smartGroup.enumerateDrawersForExtraction(stack, false)) {
				set.set(slot);
			}
			int slot = -1;
			while ((slot = set.nextSetBit(slot + 1)) != -1 && stack.getCount() < room) {
				IDrawer drawer = _drawer.getDrawer(slot);
				if (!drawer.isEmpty()) {
					if (drawer instanceof IVoidable && ((IVoidable) drawer).isVoid()) {
						room += drawer.getMaxCapacity();
					} else {
						room += drawer.getRemainingCapacity();
					}
				} else {
					room += drawer.getMaxCapacity(stack);
				}
			}

			return room;
		}

		for (int i = 0; i < _drawer.getDrawerCount() && stack.getCount() < room; i++) {
			if (!_drawer.isDrawerEnabled(i)) {
				continue;
			}

			IDrawer drawer = _drawer.getDrawer(i);
			if (drawer == null) {
				continue;
			}

			if (drawer.canItemBeStored(stack)) {
				if (drawer.isEmpty()) {
					room += drawer.getMaxCapacity(stack);
				} else {
					if (drawer instanceof IVoidable && ((IVoidable) drawer).isVoid()) {
						room += drawer.getMaxCapacity();
					} else {
						room += drawer.getRemainingCapacity();
					}
				}
			}
		}

		return room;
	}

	@Override
	@Nonnull
	public ItemStack add(@Nonnull ItemStack stack, EnumFacing from, boolean doAdd) {
		ItemStack st = stack.copy();
		st.setCount(0);

		if (_smartGroup != null) {
			BitSet set = new BitSet();
			for (int slot : _smartGroup.enumerateDrawersForInsertion(stack, false)) {
				set.set(slot);
			}
			for (int slot : _smartGroup.enumerateDrawersForExtraction(stack, false)) {
				set.set(slot);
			}
			int slot = -1;
			while ((slot = set.nextSetBit(slot + 1)) != -1) {
				IDrawer drawer = _drawer.getDrawer(slot);
				int avail;
				if (!drawer.isEmpty()) {
					avail = Math.min(stack.getCount(), drawer.getRemainingCapacity());
					if (doAdd) {
						drawer.setStoredItemCount(drawer.getStoredItemCount() + avail);
					}
				} else {
					avail = Math.min(stack.getCount(), drawer.getMaxCapacity(stack));
					if (doAdd) {
						drawer.setStoredItem(stack, avail);
					}
				}

				if (drawer instanceof IVoidable && ((IVoidable) drawer).isVoid()) {
					return stack;
				}

				stack.shrink(avail);
				st.grow(avail);

				if (stack.getCount() <= 0) {
					break;
				}
			}

			return st;
		}

		for (int i = 0; i < _drawer.getDrawerCount(); i++) {
			if (!_drawer.isDrawerEnabled(i)) {
				continue;
			}

			IDrawer drawer = _drawer.getDrawer(i);
			if (drawer == null) {
				continue;
			}

			if (drawer.canItemBeStored(stack)) {
				int avail;
				if (drawer.isEmpty()) {
					avail = Math.min(stack.getCount(), drawer.getMaxCapacity(stack));
					if (doAdd) {
						drawer.setStoredItem(stack.copy(), avail);
					}
				} else {
					avail = Math.min(stack.getCount(), drawer.getRemainingCapacity());
					if (doAdd) {
						drawer.setStoredItemCount(drawer.getStoredItemCount() + avail);
					}
				}

				if (drawer instanceof IVoidable && ((IVoidable) drawer).isVoid()) {
					return stack;
				}

				stack.shrink(avail);
				st.grow(avail);

				if (stack.getCount() <= 0) {
					break;
				}
			}
		}

		return st;
	}

	@Override
	public int getSizeInventory() {
		return _drawer.getDrawerCount();
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int i) {
		if (!_drawer.isDrawerEnabled(i)) {
			return ItemStack.EMPTY;
		}

		return _drawer.getDrawer(i) != null ? _drawer.getDrawer(i).getStoredItemCopy() : ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int i, int j) {
		if (!_drawer.isDrawerEnabled(i)) {
			return ItemStack.EMPTY;
		}

		IDrawer drawer = _drawer.getDrawer(i);
		if (drawer == null || drawer.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = drawer.getStoredItemCopy();
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		int avail = Math.min(j, drawer.getStoredItemCount());
		drawer.setStoredItemCount(drawer.getStoredItemCount() - avail);
		stack.setCount(avail);

		return stack;
	}
}

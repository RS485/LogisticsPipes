package logisticspipes.blocks.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.container.Slot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;

import net.fabricmc.fabric.api.util.NbtType;

import logisticspipes.LPBlocks;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.entity.FakePlayerLP;
import logisticspipes.interfaces.IGuiOpenController;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.guis.block.AutoCraftingGui;
import logisticspipes.network.packets.block.CraftingSetType;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.CraftingUtil;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.PlayerIdentifier;
import network.rs485.logisticspipes.config.LPConfiguration;
import network.rs485.logisticspipes.ext.InventoryKt;
import network.rs485.logisticspipes.init.Blocks;
import network.rs485.logisticspipes.inventory.InventoryWithStackSize;
import network.rs485.logisticspipes.routing.request.Resource;
import network.rs485.logisticspipes.util.ItemVariant;

public class LogisticsCraftingTableTileEntity extends LogisticsSolidTileEntity implements Inventory, IGuiTileEntity, InventoryListener, IGuiOpenController {

	public BasicInventory inv = new BasicInventory(18);
	public InventoryWithStackSize matrix = new InventoryWithStackSize(18, 1);
	public InventoryWithStackSize resultInv = new InventoryWithStackSize(18, 1);

	private CraftingResultInventory vanillaResult = new CraftingResultInventory();

	public ItemVariant targetType = null;

	// just use CraftingRequirement to store flags; field "stack" is ignored
	public Resource.Dict[] fuzzyFlags = new Resource.Dict[9];
	public Resource.Dict outputFuzzyFlags = new Resource.Dict(ItemStack.EMPTY, null);
	private Recipe<CraftingInventory> cache;
	private UUID placedBy = null;

	private PlayerCollectionList guiWatcher = new PlayerCollectionList();

	public LogisticsCraftingTableTileEntity(BlockEntityType<? extends LogisticsCraftingTableTileEntity> type) {
		super(type);
		matrix.addListener(this);
		for (int i = 0; i < 9; i++) {
			fuzzyFlags[i] = new Resource.Dict(ItemStack.EMPTY, null);
		}
	}

	public void cacheRecipe() {
		ItemVariant oldTargetType = targetType;
		cache = null;
		resultInv.removeInvStack(0);
		AutoCraftingInventory craftInv = new AutoCraftingInventory(placedBy);
		for (int i = 0; i < 9; i++) {
			craftInv.setInvStack(i, matrix.getInvStack(i));
		}
		List<Recipe<CraftingInventory>> list = new ArrayList<>();
		for (Recipe<CraftingInventory> r : CraftingUtil.getRecipeList()) {
			if (r.matches(craftInv, getWorld())) {
				list.add(r);
			}
		}
		if (list.size() == 1) {
			cache = list.get(0);
			resultInv.setInvStack(0, cache.craft(craftInv));
			targetType = null;
		} else if (list.size() > 1) {
			if (targetType != null) {
				for (Recipe<CraftingInventory> recipe : list) {
					craftInv = new AutoCraftingInventory(placedBy);
					for (int i = 0; i < 9; i++) {
						craftInv.setInvStack(i, matrix.getInvStack(i));
					}
					ItemStack result = recipe.craft(craftInv);
					if (!result.isEmpty() && targetType.equals(ItemVariant.fromStack(result))) {
						resultInv.setInvStack(0, result);
						cache = recipe;
						break;
					}
				}
			}
			if (cache == null) {
				for (Recipe<CraftingInventory> r : list) {
					ItemStack result = r.craft(craftInv);
					if (!result.isEmpty()) {
						cache = r;
						resultInv.setInvStack(0, result);
						targetType = ItemVariant.fromStack(result);
						break;
					}
				}
			}
		} else {
			targetType = null;
		}
		outputFuzzyFlags = new Resource.Dict(resultInv.getInvStack(0), null);
		if (((targetType == null && oldTargetType != null) || (targetType != null && !targetType.equals(oldTargetType))) && !guiWatcher.isEmpty() && !getWorld().isClient) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(CraftingSetType.class).setTargetType(targetType).setTilePos(this), guiWatcher);
		}
	}

	public void cycleRecipe(boolean down) {
		cacheRecipe();
		if (targetType == null) return;

		cache = null;
		AutoCraftingInventory craftInv = new AutoCraftingInventory(placedBy);

		for (int i = 0; i < 9; i++) {
			craftInv.setInvStack(i, matrix.getInvStack(i));
		}

		List<Recipe<CraftingInventory>> list = new ArrayList<>();
		for (Recipe<CraftingInventory> r : CraftingUtil.getRecipeList()) {
			if (r.matches(craftInv, getWorld())) {
				list.add(r);
			}
		}

		if (list.size() > 1) {
			boolean found = false;
			Recipe<CraftingInventory> prev = null;
			for (Recipe<CraftingInventory> recipe : list) {
				if (found) {
					cache = recipe;
					break;
				}
				craftInv = new AutoCraftingInventory(placedBy);
				for (int i = 0; i < 9; i++) {
					craftInv.setInvStack(i, matrix.getInvStack(i));
				}
				if (targetType != null && targetType.equals(ItemVariant.fromStack(recipe.craft(craftInv)))) {
					if (down) {
						found = true;
					} else {
						if (prev == null) {
							cache = list.get(list.size() - 1);
						} else {
							cache = prev;
						}
						break;
					}
				}
				prev = recipe;
			}

			if (cache == null) {
				cache = list.get(0);
			}

			craftInv = new AutoCraftingInventory(placedBy);
			for (int i = 0; i < 9; i++) {
				craftInv.setInvStack(i, matrix.getInvStack(i));
			}

			targetType = ItemVariant.fromStack(cache.craft(craftInv));
		}

		if (!guiWatcher.isEmpty() && !getWorld().isClient) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(CraftingSetType.class).setTargetType(targetType).setTilePos(this), guiWatcher);
		}

		cacheRecipe();
	}

	private boolean testFuzzy(ItemVariant item, ItemStack item2, int slot) {
		fuzzyFlags[slot] = fuzzyFlags[slot].withStack(item.makeStack());
		return fuzzyFlags[slot].matches(item2, false);
	}

	public ItemStack getOutput(Resource wanted, IRoutedPowerProvider power) {
		boolean isFuzzy = isFuzzy();
		if (cache == null) {
			cacheRecipe();
			if (cache == null) {
				return null;
			}
		}
		int[] toUse = new int[9];
		int[] used = new int[inv.getInvSize()];
		outer:
		for (int i = 0; i < 9; i++) {
			ItemStack stack = matrix.getInvStack(i);
			if (stack == null) {
				toUse[i] = -1;
				continue;
			}
			ItemVariant ident = ItemVariant.fromStack(stack);
			for (int j = 0; j < inv.getInvSize(); j++) {
				stack = inv.getInvStack(j);
				if (stack == null) {
					continue;
				}
				if (isFuzzy ? (testFuzzy(ident, stack, i)) : ident.equalsForCrafting(ItemVariant.fromStack(stack))) {
					if (stack.getCount() > used[j]) {
						used[j]++;
						toUse[i] = j;
						continue outer;
					}
				}
			}
			// Not enough material
			return null;
		}
		AutoCraftingInventory crafter = new AutoCraftingInventory(placedBy);
		for (int i = 0; i < 9; i++) {
			int j = toUse[i];
			if (j != -1) {
				crafter.setInvStack(i, inv.getInvStack(j));
			}
		}
		Recipe<CraftingInventory> recipe = cache;
		outputFuzzyFlags = outputFuzzyFlags.withStack(resultInv.getInvStack(0));
		if (!recipe.matches(crafter, getWorld())) {
			if (isFuzzy && outputFuzzyFlags.getBitSet().nextSetBit(0) != -1) {
				recipe = null;
				for (Recipe<CraftingInventory> r : CraftingUtil.getRecipeList()) {
					if (r.matches(crafter, getWorld()) && outputFuzzyFlags.matches(r.getOutput(), false)) {
						recipe = r;
						break;
					}
				}
				if (recipe == null) {
					return null;
				}
			} else {
				return null; // Fix MystCraft
			}
		}
		ItemStack result = recipe.craft(crafter);
		if (result.isEmpty()) {
			return null;
		}
		if (isFuzzy && outputFuzzyFlags.getBitSet().nextSetBit(0) != -1) {
			if (!outputFuzzyFlags.matches(result, false)) {
				return null;
			}
			if (!outputFuzzyFlags.matches(wanted.getAsItem(), false)) {
				return null;
			}
		} else {
			if (resultInv.getInvStack(0).getItem() != result.getItem()) {
				return null;
			}
			if (!wanted.matches(resultInv.getInvStack(0), true)) {
				return null;
			}
		}
		if (!power.useEnergy(LPConfiguration.INSTANCE.getLogisticsCraftingTablePowerUsage())) {
			return null;
		}
		crafter = new AutoCraftingInventory(placedBy);
		for (int i = 0; i < 9; i++) {
			int j = toUse[i];
			if (j != -1) {
				crafter.setInvStack(i, inv.takeInvStack(j, 1));
			}
		}
		result = recipe.craft(crafter);
		ServerWorld world;
		if (this.world instanceof ServerWorld) {
			world = (ServerWorld) this.world;
		} else {
			return result;
		}
		FakePlayerLP fake = FakePlayerLP.getInstance(world);
		result = result.copy();
		Slot craftingSlot = new Slot(fake, crafter, resultInv, 0, 0, 0) {

			@Override
			protected void onCrafting(@Nonnull ItemStack stack) {
				IInventory tmp = this.inventory;
				vanillaResult.setRecipeUsed(cache);
				this.inventory = vanillaResult;
				super.onCrafting(stack);
				this.inventory = tmp;
			}
		};
		result = craftingSlot.onTake(fake, result);
		for (int i = 0; i < 9; i++) {
			ItemStack left = crafter.removeInvStack(i);
			if (!left.isEmpty()) {
				left.setCount(inv.addCompressed(left, false));
				if (left.getCount() > 0) {
					ItemIdentifierInventory.dropItems(world, left, getPos());
				}
			}
		}
		for (int i = 0; i < fake.inventory.getInvSize(); i++) {
			ItemStack left = fake.inventory.removeInvStack(i);
			if (!left.isEmpty()) {
				left.setCount(inv.addCompressed(left, false));
				if (left.getCount() > 0) {
					ItemIdentifierInventory.dropItems(world, left, getPos());
				}
			}
		}
		return result;
	}

	@Override
	public void onBlockBreak() {
		ItemScatterer.spawn(getWorld(), getPos(), inv);
	}

	@Override
	public void onInvChange(Inventory inventory) {
		if (inventory == matrix) {
			cacheRecipe();
		}
	}

	public void handleNEIRecipePacket(ItemStack[] content) {
		for (int i = 0; i < 9; i++) {
			matrix.setInvStack(i, content[i]);
		}
		cacheRecipe();
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		InventoryKt.fromTag(inv, tag.getList("inv", NbtType.COMPOUND));
		InventoryKt.fromTag(matrix, tag.getList("matrix", NbtType.COMPOUND));

		placedBy = tag.getUuid("owner");

		if (tag.containsKey("fuzzy_flags", NbtType.LIST)) {
			ListTag list = tag.getList("fuzzy_flags", NbtType.COMPOUND);
			for (int i = 0; i < 9; i++) {
				fuzzyFlags[i] = Resource.Dict.Companion.fromTag(list.getCompoundTag(i));
			}
		}

		if (tag.containsKey("output_fuzzy_flags", NbtType.COMPOUND)) {
			outputFuzzyFlags = Resource.Dict.Companion.fromTag(tag.getCompound("output_fuzzy_flags"));
		}

		if (tag.containsKey("output")) {
			targetType = ItemVariant.fromTag(tag.getCompound("output"));
		}

		cacheRecipe();
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		tag.put("inv", InventoryKt.toTag(inv));
		tag.put("matrix", InventoryKt.toTag(matrix));

		tag.putUuid("owner", placedBy);

		ListTag list = new ListTag();
		for (Resource.Dict fuzzyFlag : fuzzyFlags) {
			list.add(fuzzyFlag.toTag());
		}
		tag.put("fuzzy_flags", list);

		tag.put("output_fuzzy_flags", outputFuzzyFlags.toTag());

		if (targetType != null) {
			tag.put("output", targetType.toTag(new CompoundTag()));
		}

		return tag;
	}



	@Override
	public int getInvSize() {
		return inv.getInvSize();
	}

	@Override
	public boolean isInvEmpty() {
		return inv.isInvEmpty();
	}

	@Override
	public ItemStack getInvStack(int slot) {
		return inv.getInvStack(slot);
	}

	@Override
	public ItemStack takeInvStack(int slot, int count) {
		return inv.takeInvStack(slot, count);
	}

	@Override
	public ItemStack removeInvStack(int slot) {
		return inv.removeInvStack(slot);
	}

	@Override
	public void setInvStack(int slot, ItemStack stack) {
		inv.setInvStack(slot, stack);
	}

	@Override
	public boolean canPlayerUseInv(PlayerEntity var1) {
		return true;
	}

	@Override
	public void onInvOpen(PlayerEntity playerEntity_1) {

	}

	@Override
	public void onInvClose(PlayerEntity playerEntity_1) {

	}

	@Override
	public boolean isValidInvStack(int slot, ItemStack itemstack) {
		if (slot < 9 && slot >= 0) {
			ItemStack stack = matrix.getInvStack(slot);
			if (stack != null && !itemstack.isEmpty()) {
				if (isFuzzy() && fuzzyFlags[slot].getBitSet().nextSetBit(0) != -1) {
					fuzzyFlags[slot] = fuzzyFlags[slot].withStack(stack);
					return fuzzyFlags[slot].matches(itemstack, false);
				}
				return stack.getItem() == itemstack.getItem();
			}
		}
		return true;
	}

	@Override
	public int countInInv(Item item_1) {
		return 0;
	}

	@Override
	public boolean containsAnyInInv(Set<Item> set_1) {
		return false;
	}

	@Override
	public void clear() {

	}

	public void placedBy(LivingEntity entity) {
		if (entity instanceof PlayerEntity) {
			placedBy = entity.getUuid();
		}
	}

	public boolean isFuzzy() {
		return getCachedState().getBlock() == Blocks.INSTANCE.getFuzzyCraftingTable();
	}

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(AutoCraftingGui.class).setCraftingTable(this);
	}

	@Override
	public void guiOpenedByPlayer(PlayerEntity player) {
		guiWatcher.add(player);
	}

	@Override
	public void guiClosedByPlayer(PlayerEntity player) {
		guiWatcher.remove(player);
	}



	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload worldEvent) {
		if (fake.world == worldEvent.getWorld())
			fake = null;
	}
}

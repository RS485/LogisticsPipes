package logisticspipes.blocks.crafting;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import logisticspipes.LPBlocks;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.guis.block.AutoCraftingGui;
import logisticspipes.network.packets.block.CraftingSetType;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.resources.IResource;
import logisticspipes.utils.CraftingUtil;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.PlayerIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.property.BitSetProperty;
import network.rs485.logisticspipes.property.IBitSet;
import network.rs485.logisticspipes.util.FuzzyUtil;
import network.rs485.logisticspipes.util.items.ItemStackLoader;

public class LogisticsCraftingTableTileEntity extends LogisticsSolidTileEntity
		implements IInventory, IGuiTileEntity, ISimpleInventoryEventHandler, IGuiOpenControler {

	public final BitSetProperty fuzzyFlags = new BitSetProperty(new BitSet(4 * (9 + 1)), "fuzzyBitSet");
	public ItemIdentifierInventory inv = new ItemIdentifierInventory(18, "Crafting Resources", 64);
	public ItemIdentifierInventory matrix = new ItemIdentifierInventory(9, "Crafting Matrix", 1);
	public ItemIdentifierInventory resultInv = new ItemIdentifierInventory(1, "Crafting Result", 1);
	public ItemIdentifier targetType = null;

	private InventoryCraftResult vanillaResult = new InventoryCraftResult();
	private IRecipe cache;
	private EntityPlayerMP fake;
	private PlayerIdentifier placedBy = null;

	private InvWrapper invWrapper = new InvWrapper(this);

	private PlayerCollectionList guiWatcher = new PlayerCollectionList();

	public LogisticsCraftingTableTileEntity() {
		matrix.addListener(this);
	}

	public void cacheRecipe() {
		ItemIdentifier oldTargetType = targetType;
		cache = null;
		resultInv.clearInventorySlotContents(0);
		AutoCraftingInventory craftInv = new AutoCraftingInventory(placedBy);
		for (int i = 0; i < 9; i++) {
			craftInv.setInventorySlotContents(i, matrix.getStackInSlot(i));
		}
		List<IRecipe> list = new ArrayList<>();
		for (IRecipe r : CraftingUtil.getRecipeList()) {
			if (r.matches(craftInv, getWorld())) {
				list.add(r);
			}
		}
		if (list.size() == 1) {
			cache = list.get(0);
			resultInv.setInventorySlotContents(0, cache.getCraftingResult(craftInv));
			targetType = null;
		} else if (list.size() > 1) {
			if (targetType != null) {
				for (IRecipe recipe : list) {
					craftInv = new AutoCraftingInventory(placedBy);
					for (int i = 0; i < 9; i++) {
						craftInv.setInventorySlotContents(i, matrix.getStackInSlot(i));
					}
					ItemStack result = recipe.getCraftingResult(craftInv);
					if (!result.isEmpty() && targetType.equals(ItemIdentifier.get(result))) {
						resultInv.setInventorySlotContents(0, result);
						cache = recipe;
						break;
					}
				}
			}
			if (cache == null) {
				for (IRecipe r : list) {
					ItemStack result = r.getCraftingResult(craftInv);
					if (!result.isEmpty()) {
						cache = r;
						resultInv.setInventorySlotContents(0, result);
						targetType = ItemIdentifier.get(result);
						break;
					}
				}
			}
		} else {
			targetType = null;
		}
		if (((targetType == null && oldTargetType != null) || (targetType != null && !targetType.equals(oldTargetType)))
				&& !guiWatcher.isEmpty() && MainProxy.isServer(getWorld())) {
			MainProxy.sendToPlayerList(
					PacketHandler.getPacket(CraftingSetType.class).setTargetType(targetType).setTilePos(this),
					guiWatcher);
		}
	}

	public void cycleRecipe(boolean down) {
		cacheRecipe();
		if (targetType == null) return;

		cache = null;
		AutoCraftingInventory craftInv = new AutoCraftingInventory(placedBy);

		for (int i = 0; i < 9; i++) {
			craftInv.setInventorySlotContents(i, matrix.getStackInSlot(i));
		}

		List<IRecipe> list = new ArrayList<>();
		for (IRecipe r : CraftingUtil.getRecipeList()) {
			if (r.matches(craftInv, getWorld())) {
				list.add(r);
			}
		}

		if (list.size() > 1) {
			boolean found = false;
			IRecipe prev = null;
			for (IRecipe recipe : list) {
				if (found) {
					cache = recipe;
					break;
				}
				craftInv = new AutoCraftingInventory(placedBy);
				for (int i = 0; i < 9; i++) {
					craftInv.setInventorySlotContents(i, matrix.getStackInSlot(i));
				}
				if (targetType != null && targetType.equals(ItemIdentifier.get(recipe.getCraftingResult(craftInv)))) {
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
				craftInv.setInventorySlotContents(i, matrix.getStackInSlot(i));
			}

			targetType = ItemIdentifier.get(cache.getCraftingResult(craftInv));
		}

		if (!guiWatcher.isEmpty() && MainProxy.isServer(getWorld())) {
			MainProxy.sendToPlayerList(
					PacketHandler.getPacket(CraftingSetType.class).setTargetType(targetType).setTilePos(this),
					guiWatcher);
		}

		cacheRecipe();
	}

	public IBitSet outputFuzzy() {
		final int startIdx = 4 * 9; // after the 9th slot
		return fuzzyFlags.get(startIdx, startIdx + 3);
	}

	public IBitSet inputFuzzy(int slot) {
		final int startIdx = 4 * slot;
		return fuzzyFlags.get(startIdx, startIdx + 3);
	}

	@Nonnull
	public ItemStack getOutput(IResource wanted, IRoutedPowerProvider power) {
		boolean isFuzzy = isFuzzy();
		if (cache == null) {
			cacheRecipe();
			if (cache == null) {
				return ItemStack.EMPTY;
			}
		}
		int[] toUse = new int[9];
		int[] used = new int[inv.getSizeInventory()];
		outer:
		for (int i = 0; i < 9; i++) {
			ItemIdentifierStack item = matrix.getIDStackInSlot(i);
			if (item == null) {
				toUse[i] = -1;
				continue;
			}
			ItemIdentifier ident = item.getItem();
			for (int j = 0; j < inv.getSizeInventory(); j++) {
				item = inv.getIDStackInSlot(j);
				if (item == null) {
					continue;
				}

				final boolean doItemsEqual = isFuzzy ?
						(FuzzyUtil.INSTANCE
								.fuzzyMatches(FuzzyUtil.INSTANCE.getter(inputFuzzy(i)), ident, item.getItem())) :
						ident.equalsForCrafting(item.getItem());

				if (doItemsEqual && item.getStackSize() > used[j]) {
					used[j]++;
					toUse[i] = j;
					continue outer;
				}
			}
			//Not enough material
			return ItemStack.EMPTY;
		}
		AutoCraftingInventory crafter = new AutoCraftingInventory(placedBy);
		for (int i = 0; i < 9; i++) {
			int j = toUse[i];
			if (j != -1) {
				crafter.setInventorySlotContents(i, inv.getStackInSlot(j));
			}
		}
		IRecipe recipe = cache;
		final ItemIdentifierStack outStack = Objects.requireNonNull(resultInv.getIDStackInSlot(0));
		if (!recipe.matches(crafter, getWorld())) {
			if (isFuzzy && outputFuzzy().nextSetBit(0) != -1) {
				recipe = null;
				for (IRecipe r : CraftingUtil.getRecipeList()) {

					if (r.matches(crafter, getWorld()) && FuzzyUtil.INSTANCE
							.fuzzyMatches(FuzzyUtil.INSTANCE.getter(outputFuzzy()), outStack.getItem(),
									ItemIdentifier.get(r.getRecipeOutput()))) {
						recipe = r;
						break;
					}
				}
				if (recipe == null) {
					return ItemStack.EMPTY;
				}
			} else {
				return ItemStack.EMPTY; //Fix MystCraft
			}
		}
		ItemStack result = recipe.getCraftingResult(crafter);
		if (result.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (isFuzzy && outputFuzzy().nextSetBit(0) != -1) {
			if (!FuzzyUtil.INSTANCE.fuzzyMatches(FuzzyUtil.INSTANCE.getter(outputFuzzy()), outStack.getItem(),
					ItemIdentifier.get(result))) {
				return ItemStack.EMPTY;
			}
			if (!FuzzyUtil.INSTANCE.fuzzyMatches(FuzzyUtil.INSTANCE.getter(outputFuzzy()), wanted.getAsItem(),
					ItemIdentifier.get(result))) {
				return ItemStack.EMPTY;
			}
		} else {
			if (!outStack.getItem().equalsWithoutNBT(ItemIdentifier.get(result))) {
				return ItemStack.EMPTY;
			}
			if (!wanted.matches(outStack.getItem(), IResource.MatchSettings.WITHOUT_NBT)) {
				return ItemStack.EMPTY;
			}
		}
		if (!power.useEnergy(Configs.LOGISTICS_CRAFTING_TABLE_POWER_USAGE)) {
			return ItemStack.EMPTY;
		}
		crafter = new AutoCraftingInventory(placedBy);
		for (int i = 0; i < 9; i++) {
			int j = toUse[i];
			if (j != -1) {
				crafter.setInventorySlotContents(i, inv.decrStackSize(j, 1));
			}
		}
		result = recipe.getCraftingResult(crafter);
		if (fake == null) {
			fake = MainProxy.getFakePlayer(this.world);
		}
		result = result.copy();
		SlotCrafting craftingSlot = new SlotCrafting(fake, crafter, resultInv, 0, 0, 0) {

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
			ItemStack left = crafter.getStackInSlot(i);
			crafter.setInventorySlotContents(i, ItemStack.EMPTY);
			if (!left.isEmpty()) {
				left.setCount(inv.addCompressed(left, false));
				if (left.getCount() > 0) {
					ItemIdentifierInventory.dropItems(world, left, getPos());
				}
			}
		}
		for (int i = 0; i < fake.inventory.getSizeInventory(); i++) {
			ItemStack left = fake.inventory.getStackInSlot(i);
			fake.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
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
		inv.dropContents(world, getPos());
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		if (inventory == matrix) {
			cacheRecipe();
		}
	}

	public void handleNEIRecipePacket(NonNullList<ItemStack> content) {
		if (matrix.getSizeInventory() != content.size())
			throw new IllegalStateException("Different sizes of matrix and inventory from packet");
		for (int i = 0; i < content.size(); i++) {
			matrix.setInventorySlotContents(i, content.get(i));
		}
		cacheRecipe();
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		inv.readFromNBT(par1nbtTagCompound, "inv");
		matrix.readFromNBT(par1nbtTagCompound, "matrix");
		if (par1nbtTagCompound.hasKey("placedBy")) {
			String name = par1nbtTagCompound.getString("placedBy");
			placedBy = PlayerIdentifier.convertFromUsername(name);
		} else {
			placedBy = PlayerIdentifier.readFromNBT(par1nbtTagCompound, "placedBy");
		}
		fuzzyFlags.readFromNBT(par1nbtTagCompound);
		// FIXME: remove after 1.12
		if (par1nbtTagCompound.hasKey("fuzzyFlags")) {
			NBTTagList lst = par1nbtTagCompound.getTagList("fuzzyFlags", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < 9; i++) {
				FuzzyUtil.INSTANCE.readFromNBT(inputFuzzy(i), lst.getCompoundTagAt(i));
			}
		}
		// FIXME: remove after 1.12
		if (par1nbtTagCompound.hasKey("outputFuzzyFlags")) {
			FuzzyUtil.INSTANCE.readFromNBT(outputFuzzy(), par1nbtTagCompound.getCompoundTag("outputFuzzyFlags"));
		}
		if (par1nbtTagCompound.hasKey("targetType")) {
			targetType = ItemIdentifier
					.get(ItemStackLoader.loadAndFixItemStackFromNBT(par1nbtTagCompound.getCompoundTag("targetType")));
		}
		cacheRecipe();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound par1nbtTagCompound) {
		par1nbtTagCompound = super.writeToNBT(par1nbtTagCompound);
		inv.writeToNBT(par1nbtTagCompound, "inv");
		matrix.writeToNBT(par1nbtTagCompound, "matrix");
		if (placedBy != null) {
			placedBy.writeToNBT(par1nbtTagCompound, "placedBy");
		}
		fuzzyFlags.writeToNBT(par1nbtTagCompound);
		if (targetType != null) {
			NBTTagCompound type = new NBTTagCompound();
			targetType.makeNormalStack(1).writeToNBT(type);
			par1nbtTagCompound.setTag("targetType", type);
		} else {
			par1nbtTagCompound.removeTag("targetType");
		}
		return par1nbtTagCompound;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) invWrapper;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public boolean isEmpty() {
		return inv.isEmpty();
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int i) {
		return inv.getStackInSlot(i);
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int i, int j) {
		return inv.decrStackSize(i, j);
	}

	@Override
	@Nonnull
	public ItemStack removeStackFromSlot(int i) {
		return inv.removeStackFromSlot(i);
	}

	@Override
	public void setInventorySlotContents(int i, @Nonnull ItemStack itemstack) {
		inv.setInventorySlotContents(i, itemstack);
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openInventory(@Nonnull EntityPlayer player) {
	}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
		if (i < 9 && i >= 0) {
			ItemIdentifierStack stack = matrix.getIDStackInSlot(i);
			if (stack != null && !itemstack.isEmpty()) {
				if (isFuzzy() && inputFuzzy(i).nextSetBit(0) != -1) {
					return FuzzyUtil.INSTANCE.fuzzyMatches(FuzzyUtil.INSTANCE.getter(inputFuzzy(i)),
							stack.getItem(),
							ItemIdentifier.get(itemstack));
				}
				return stack.getItem().equalsWithoutNBT(ItemIdentifier.get(itemstack));
			}
		}
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	public void placedBy(EntityLivingBase par5EntityLivingBase) {
		if (par5EntityLivingBase instanceof EntityPlayer) {
			placedBy = PlayerIdentifier.get((EntityPlayer) par5EntityLivingBase);
		}
	}

	public boolean isFuzzy() {
		return world.getBlockState(pos).getBlock() == LPBlocks.crafterFuzzy;
	}

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(AutoCraftingGui.class).setCraftingTable(this);
	}

	@Override
	public void guiOpenedByPlayer(EntityPlayer player) {
		guiWatcher.add(player);
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		guiWatcher.remove(player);
	}

	@Nonnull
	@Override
	public String getName() {
		return "LogisticsCraftingTable";
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Nullable
	@Override
	public ITextComponent getDisplayName() {
		return null;
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload worldEvent) {
		if (fake.world == worldEvent.getWorld()) fake = null;
	}
}

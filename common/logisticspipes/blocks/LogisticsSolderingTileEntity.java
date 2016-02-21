package logisticspipes.blocks;

import logisticspipes.LPConstants;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.ICraftingResultHandler;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.guis.block.SolderingStationGui;
import logisticspipes.network.packets.block.SolderingStationHeat;
import logisticspipes.network.packets.block.SolderingStationInventory;
import logisticspipes.network.packets.block.SolderingStationProgress;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.recipes.SolderingStationRecipes.SolderingStationRecipe;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.UtilEnumFacing;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;

import java.util.LinkedList;
import java.util.List;

public class LogisticsSolderingTileEntity extends LogisticsSolidTileEntity implements IGuiTileEntity, ISidedInventory, IGuiOpenControler {

	private ItemIdentifierInventory inv = new ItemIdentifierInventory(12, "Soldering Inventory", 64);
	public int heat = 0;
	public int progress = 0;
	public boolean hasWork = false;

	private PlayerCollectionList listener = new PlayerCollectionList();

	public LogisticsSolderingTileEntity() {}

	public boolean checkSlot(ItemStack stack, int slotNumber) {
		if (getRecipeForTaget() == null || getRecipeForTaget().length <= slotNumber) {
			return true;
		}
		ItemStack allowed = getRecipeForTaget()[slotNumber];
		if (allowed == null) {
			return stack == null;
		}
		return stack.getItem() == allowed.getItem() && stack.getItemDamage() == allowed.getItemDamage();
	}

	public boolean areStacksEmpty() {
		for (int i = 0; i < 9; i++) {
			if (inv.getStackInSlot(i) != null) {
				return false;
			}
		}
		return true;
	}

	public ItemStack[] getRecipeForTaget() {
		return getRecipeForTaget(inv.getStackInSlot(11));
	}

	public ItemStack[] getRecipeForTaget(ItemStack target) {
		if (target == null) {
			return null;
		}
		for (SolderingStationRecipe recipe : SolderingStationRecipes.getRecipes()) {
			if (target.getItem() == recipe.result.getItem() && target.getItemDamage() == recipe.result.getItemDamage()) {
				return recipe.source;
			}
		}
		return null;
	}

	public ItemStack getTargetForTaget() {
		return getTargetForTaget(inv.getStackInSlot(11));
	}

	public ItemStack getTargetForTaget(ItemStack target) {
		if (target == null) {
			return null;
		}
		for (SolderingStationRecipe recipe : SolderingStationRecipes.getRecipes()) {
			if (target.getItem() == recipe.result.getItem() && target.getItemDamage() == recipe.result.getItemDamage()) {
				return recipe.result;
			}
		}
		return null;
	}

	public List<ItemIdentifierStack> getRecipeForTagetAsItemIdentifierStackList() {
		LinkedList<ItemIdentifierStack> list = new LinkedList<ItemIdentifierStack>();
		ItemStack[] array = getRecipeForTaget();
		if (array != null) {
			for (ItemStack stack : array) {
				if (stack != null) {
					list.addLast(ItemIdentifier.get(stack).makeStack(1));
				} else {
					list.addLast(null);
				}
			}
		}
		return list;
	}

	private boolean itemEquals(ItemStack var1, ItemStack var2) {
		return var1.getItem() == var2.getItem() && var1.getItemDamage() == var2.getItemDamage();
	}

	public ItemStack getTagetForRecipe(boolean remove) {
		for (SolderingStationRecipe recipe : SolderingStationRecipes.getRecipes()) {
			boolean match = true;
			boolean removeThis = false;
			for (int i = 0; i < 9; i++) {
				ItemStack recipestack = recipe.source[i];
				ItemStack inputStack = inv.getStackInSlot(i);
				if (recipestack == null) {
					if (inputStack != null) {
						match = false;
					}
					continue;
				} else if (inputStack == null) {
					match = false;
					continue;
				} else {
					if (!itemEquals(recipestack, inputStack)) {
						match = false;
					} else {
						if (remove && ((getTagetForRecipe(false) != null && itemEquals(getTagetForRecipe(false), recipe.result)) || removeThis)) {
							inputStack.stackSize -= 1;
							if (inputStack.stackSize <= 0) {
								inputStack = null;
							}
							inv.setInventorySlotContents(i, inputStack);
							removeThis = true;
						}
					}
				}
			}
			if (match) {
				return recipe.result.copy();
			}
		}
		return null;
	}

	public ICraftingResultHandler getHandlerForRecipe() {
		for (SolderingStationRecipe recipe : SolderingStationRecipes.getRecipes()) {
			boolean match = true;
			for (int i = 0; i < 9; i++) {
				ItemStack recipestack = recipe.source[i];
				ItemStack inputStack = inv.getStackInSlot(i);
				if (recipestack == null) {
					if (inputStack != null) {
						match = false;
					}
					continue;
				} else if (inputStack == null) {
					match = false;
					continue;
				} else {
					if (!itemEquals(recipestack, inputStack)) {
						match = false;
					}
				}
			}
			if (match) {
				return recipe.handler;
			}
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		inv.readFromNBT(nbt, "");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		inv.writeToNBT(nbt, "");
	}

	private boolean hasWork() {
		return getTagetForRecipe(false) != null && inv.getStackInSlot(9) != null;
	}

	private void updateHeat() {
		MainProxy.sendPacketToAllWatchingChunk(pos.getX(), pos.getZ(), MainProxy.getDimensionForWorld(getWorld()), PacketHandler.getPacket(SolderingStationHeat.class).setInteger(heat).setPosX(pos.getX()).setPosY(pos.getY()).setPosZ(pos.getZ()));
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SolderingStationHeat.class).setInteger(heat).setPosX(pos.getX()).setPosY(pos.getY()).setPosZ(pos.getZ()), listener);
	}

	private void updateProgress() {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SolderingStationProgress.class).setInteger(progress).setPosX(pos.getX()).setPosY(pos.getY()).setPosZ(pos.getZ()), listener);
	}

	private void updateInventory() {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SolderingStationInventory.class).setInventory(this).setPosX(pos.getX()).setPosY(pos.getY()).setPosZ(pos.getZ()), listener);
	}

	@Override
	public void update() {
		super.update();
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		hasWork = hasWork();
		if (hasWork && heat < 100) {
			boolean usedEnergy = false;
			if (Configs.LOGISTICS_POWER_USAGE_DISABLED) {
				if (heat < 100) {
					heat += 5;
				}
				if (heat > 100) {
					heat = 100;
				}
				usedEnergy = true;
			} else {
				for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
					LPPosition pos = new LPPosition(this);
					pos.moveForward(dir);
					TileEntity tile = pos.getTileEntity(getWorld());
					if (!(tile instanceof LogisticsTileGenericPipe)) {
						continue;
					}
					LogisticsTileGenericPipe tPipe = (LogisticsTileGenericPipe) tile;
					if (!(tPipe.pipe instanceof CoreRoutedPipe)) {
						continue;
					}
					CoreRoutedPipe pipe = (CoreRoutedPipe) tPipe.pipe;
					if (pipe.useEnergy(50)) {
						heat += 5;
						if (heat > 100) {
							heat = 100;
						}
						updateHeat();
						usedEnergy = true;
						break;
					}
				}
			}
			if (!usedEnergy && getWorld().getTotalWorldTime() % 5 == 0) {
				heat--;
				if (heat < 0) {
					heat = 0;
				}
				updateHeat();
			}
		} else if (!hasWork && heat > 0) {
			heat--;
			updateHeat();
		}
		if (hasWork && heat >= 100) {
			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				LPPosition pos = new LPPosition(this);
				pos.moveForward(dir);
				TileEntity tile = pos.getTileEntity(getWorld());
				if (!(tile instanceof LogisticsTileGenericPipe)) {
					continue;
				}
				LogisticsTileGenericPipe tPipe = (LogisticsTileGenericPipe) tile;
				if (!(tPipe.pipe instanceof CoreRoutedPipe)) {
					continue;
				}
				CoreRoutedPipe pipe = (CoreRoutedPipe) tPipe.pipe;
				if (pipe.useEnergy(30)) {
					progress += 3;
				} else if (pipe.useEnergy(20)) {
					progress += 2;
				} else if (pipe.useEnergy(10)) {
					progress += 1;
				}
				if (progress >= 100) {
					if (tryCraft()) {
						progress = 0;
					} else {
						progress -= 50;
					}
				}
				updateProgress();
			}
		} else if (!hasWork && progress != 0) {
			progress = 0;
			updateProgress();
		}
	}

	private boolean tryCraft() {
		ItemIdentifierStack content = inv.getIDStackInSlot(10);
		ICraftingResultHandler handler = getHandlerForRecipe();
		ItemStack toAdd = getTagetForRecipe(false);
		if (handler != null) {
			handler.handleCrafting(toAdd);
		}
		if (content != null) {
			if (!content.getItem().equals(toAdd)) {
				return false;
			}
			if (content.getStackSize() + toAdd.stackSize > content.getItem().getMaxStackSize()) {
				return false;
			}
			toAdd.stackSize += content.getStackSize();
		}

		//dummy
		getTagetForRecipe(true);

		inv.setInventorySlotContents(10, toAdd);

		inv.decrStackSize(9, 1);

		inv.markDirty();
		super.markDirty();
		updateInventory();

		return true;
	}

	@Override
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return inv.getStackInSlot(var1);
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		return inv.decrStackSize(var1, var2);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return inv.getStackInSlotOnClosing(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		inv.setInventorySlotContents(var1, var2);
	}

	@Override
	public String getInventoryName() {
		return inv.getInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return inv.isUseableByPlayer(var1);
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}



	@Override
	public void guiOpenedByPlayer(EntityPlayer player) {
		listener.add(player);
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		listener.remove(player);
	}

	public void onBlockBreak() {
		inv.dropContents(getWorld(), getPos());
	}

	@Override
	public int getFrontTexture() {
		if (heat > 0) {
			return 3;
		} else {
			return 8;
		}
	}


	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
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

	@Override
	public void addInfoToCrashReport (CrashReportCategory par1CrashReportCategory) {
		super.addInfoToCrashReport(par1CrashReportCategory);
		par1CrashReportCategory.addCrashSection("LP-Version", LPConstants.VERSION);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	}

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(SolderingStationGui.class);
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3) {
		return var1 < 10;
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3) {
		return var1 == 10;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public IChatComponent getDisplayName() {
		return null;
	}
}

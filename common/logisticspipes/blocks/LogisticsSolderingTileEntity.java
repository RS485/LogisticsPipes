package logisticspipes.blocks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.ICraftingResultHandler;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.recipes.SolderingStationRecipes.SolderingStationRecipe;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import cpw.mods.fml.common.network.Player;

public class LogisticsSolderingTileEntity extends TileEntity implements IPowerReceptor, ISpecialInventory, IGuiOpenControler, IRotationProvider {
	
	private IPowerProvider provider;
	private SimpleInventory inv = new SimpleInventory(12, "Soldering Inventory", 64);
	public int heat = 0;
	public int progress = 0;
	public boolean hasWork = false;
	public int rotation = 0;
	private boolean init = false;
	
	private List<EntityPlayer> listener = new ArrayList<EntityPlayer>();

	public LogisticsSolderingTileEntity() {
		provider = PowerFramework.currentFramework.createPowerProvider();
		provider.configure(10, 10, 100, 10, 100);
	}

	public Container createContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player, this, this);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				final int slotNumber = i * 3 + j;
				dummy.addRestrictedSlot(slotNumber, this, 44 + (j * 18),
						17 + (i * 18), new ISlotCheck() {
							@Override
							public boolean isStackAllowed(ItemStack itemStack) {
								return checkSlot(itemStack, slotNumber);
							}
						});
			}
		}
		dummy.addRestrictedSlot(9, this, 107, 17, Item.ingotIron.itemID);
		dummy.addRestrictedSlot(10, this, 141, 47, -1);
		dummy.addRestrictedSlot(11, this, 9, 9, new ISlotCheck() {
			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				return getRecipeForTaget(itemStack) != null && areStacksEmpty();
			}
		});
		dummy.addNormalSlotsForPlayerInventory(8, 84);
		return dummy;
	}

	private boolean checkSlot(ItemStack stack, int slotNumber) {
		if(getRecipeForTaget() == null || getRecipeForTaget().length <= slotNumber) {
			return true;
		}
		ItemStack allowed = getRecipeForTaget()[slotNumber];
		if(allowed == null) {
			return stack == null;
		}
		return stack.itemID == allowed.itemID && stack.getItemDamage() == allowed.getItemDamage();
	}
	
	public boolean areStacksEmpty() {
		for(int i=0; i<9;i++) {
			if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).itemID != 0) {
				return false;
			}
		}
		return true;
	}
	
	public ItemStack[] getRecipeForTaget() {
		return getRecipeForTaget(inv.getStackInSlot(11));
	}
	
	public ItemStack[] getRecipeForTaget(ItemStack target) {
		if(target == null) return null;
		for(SolderingStationRecipe recipe:SolderingStationRecipes.getRecipes()) {
			if(target.itemID == recipe.result.itemID && target.getItemDamage() == recipe.result.getItemDamage()) {
				return recipe.source;
			}
		}
		return null;
	}
	
	public ItemStack getTargetForTaget() {
		return getTargetForTaget(inv.getStackInSlot(11));
	}
	
	public ItemStack getTargetForTaget(ItemStack target) {
		if(target == null) return null;
		for(SolderingStationRecipe recipe:SolderingStationRecipes.getRecipes()) {
			if(target.itemID == recipe.result.itemID && target.getItemDamage() == recipe.result.getItemDamage()) {
				return recipe.result;
			}
		}
		return null;
	}

	public List<ItemIdentifierStack> getRecipeForTagetAsItemIdentifierStackList() {
		LinkedList<ItemIdentifierStack> list = new LinkedList<ItemIdentifierStack>();
		ItemStack[] array = getRecipeForTaget();
		if(array != null) {
			for(ItemStack stack:array) {
				if(stack != null) {
					list.addLast(ItemIdentifier.get(stack).makeStack(1));
				} else {
					list.addLast(null);
				}
			}
		}
		return list;
	}
	
	private boolean itemEquals(ItemStack var1, ItemStack var2) {
		return var1.itemID == var2.itemID && var1.getItemDamage() == var2.getItemDamage();
	}
	
	public ItemStack getTagetForRecipe(boolean remove) {
		for(SolderingStationRecipe recipe:SolderingStationRecipes.getRecipes()) {
			boolean match = true;
			boolean removeThis = false;
			for(int i=0;i<9;i++) {
				ItemStack recipestack = recipe.source[i];
				ItemStack inputStack = inv.getStackInSlot(i);
				if(recipestack == null) {
					if(inputStack != null) {
						match = false;
					}
					continue;
				} else if(inputStack == null) {
					match = false;
					continue;
				} else {
					if(!itemEquals(recipestack,inputStack)) {
						match = false;
					} else {
						if(remove && ((getTagetForRecipe(false) != null && itemEquals(getTagetForRecipe(false),recipe.result)) || removeThis)) {
							inputStack.stackSize -= 1;
							if(inputStack.stackSize <= 0) {
								inputStack = null;
							}
							inv.setInventorySlotContents(i, inputStack);
							removeThis = true;
						}
					}
				}
			}
			if(match) {
				return recipe.result.copy();
			}
		}
		return null;
	}
	public ICraftingResultHandler getHandlerForRecipe() {
		for(SolderingStationRecipe recipe:SolderingStationRecipes.getRecipes()) {
			boolean match = true;
			for(int i=0;i<9;i++) {
				ItemStack recipestack = recipe.source[i];
				ItemStack inputStack = inv.getStackInSlot(i);
				if(recipestack == null) {
					if(inputStack != null) {
						match = false;
					}
					continue;
				} else if(inputStack == null) {
					match = false;
					continue;
				} else {
					if(!itemEquals(recipestack,inputStack)) {
						match = false;
					}
				}
			}
			if(match) {
				return recipe.handler;
			}
		}
		return null;
	}
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		inv.readFromNBT(nbt, "");
		rotation = nbt.getInteger("rotation");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		inv.writeToNBT(nbt, "");
		nbt.setInteger("rotation", rotation);
	}

	private boolean hasWork() {
		return getTagetForRecipe(false) != null && inv.getStackInSlot(9) != null;
	}
	
	private void updateHeat() {
		MainProxy.sendPacketToAllAround(xCoord, yCoord, zCoord, 64, MainProxy.getDimensionForWorld(worldObj), new PacketPipeInteger(NetworkConstants.SOLDERING_UPDATE_HEAT, xCoord, yCoord, zCoord, this.heat).getPacket());
		for(EntityPlayer player:listener) {
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SOLDERING_UPDATE_HEAT, xCoord, yCoord, zCoord, this.heat).getPacket(), (Player)player);
		}
	}

	private void updateProgress() {
		for(EntityPlayer player:listener) {
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SOLDERING_UPDATE_PROGRESS, xCoord, yCoord, zCoord, this.progress).getPacket(), (Player)player);
		}
	}
	
	private void updateInventory() {
		for(EntityPlayer player:listener) {
			MainProxy.sendPacketToPlayer(new PacketInventoryChange(NetworkConstants.SOLDERING_UPDATE_INVENTORY, xCoord, yCoord, zCoord, this).getPacket(), (Player)player);
		}
	}
	
	@Override
	public void updateEntity() {
		if(MainProxy.isClient(worldObj)) {
			if(!init) {
				MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.ROTATION_REQUEST, xCoord, yCoord, zCoord).getPacket());
				init = true;
			}
			return;
		}
		hasWork = hasWork();
		if(hasWork && heat < 100) {
			if(provider.useEnergy(1, 100, false) >= 1) {
				heat += provider.useEnergy(1, 100, true);
				if(heat > 100) {
					heat = 100;
				}
				updateHeat();
			} else {
				if(worldObj.getWorldTime() % 5 == 0) {
					heat--;
					if(heat < 0) {
						heat = 0;
					}
					updateHeat();
				}
			}
		} else if(!hasWork && heat > 0) {
			heat--;
			updateHeat();
		}
		if(hasWork && heat >= 100) {
			progress += provider.useEnergy(1, 3, true);
			if(progress >= 100) {
				if(tryCraft()) {
					progress = 0;
				} else {
					progress -= 50;
				}
			}
			updateProgress();
		} else if(!hasWork && progress != 0) {
			progress = 0;
			updateProgress();
		}
	}

	private boolean tryCraft() {
		ItemStack content = inv.getStackInSlot(10);
		ICraftingResultHandler handler = getHandlerForRecipe();
		ItemStack toAdd = getTagetForRecipe(false);
		if(handler != null) {
			handler.handleCrafting(toAdd);
		}
		if(content != null) {
			if(!content.isItemEqual(toAdd) || !ItemStack.areItemStackTagsEqual(content, toAdd)) {
				return false;
			}
			if(content.stackSize + toAdd.stackSize > content.getMaxStackSize()) {
				return false;
			}
			toAdd.stackSize += content.stackSize;
		}

		//dummy
		content = getTagetForRecipe(true);

		inv.setInventorySlotContents(10, toAdd);

		inv.getStackInSlot(9).stackSize -= 1;
		if(inv.getStackInSlot(9).stackSize <= 0) {
			inv.setInventorySlotContents(9, null);
		}

		inv.onInventoryChanged();
		super.onInventoryChanged();
		updateInventory();

		return true;
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		this.provider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return provider;
	}

	@Override
	public void doWork() {
		
	}

	@Override
	public int powerRequest(ForgeDirection from) {
		if (hasWork()) {
			return provider.getMaxEnergyReceived();
		} else {
			return 0;
		}
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
	public ItemStack getStackInSlotOnClosing(int var1) {
		return inv.getStackInSlotOnClosing(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		inv.setInventorySlotContents(var1, var2);
	}

	@Override
	public String getInvName() {
		return inv.getInvName();
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
	public void openChest() {
		inv.openChest();
	}

	@Override
	public void closeChest() {
		inv.closeChest();
	}

	@Override
	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection from) {
		if(stack == null) return 0;
		if(stack.getItem() == null) return 0;
		if (stack.getItem() == Item.ingotIron) {
			ItemStack iron = inv.getStackInSlot(9);
			if (iron == null) {
				iron = new ItemStack(Item.ingotIron, 0, 0);
				inv.setInventorySlotContents(9, iron);
			}
			int freespace = 64 - iron.stackSize;
			int toAdd = Math.min(stack.stackSize, freespace);
			if (doAdd) {
				iron.stackSize += toAdd;
				inv.onInventoryChanged();
				super.onInventoryChanged();
			}
			if (iron.stackSize == 0) {
				inv.setInventorySlotContents(9, null);
			}
			return toAdd;
		}
		ItemStack[] recipe = getRecipeForTaget();
		if(recipe == null) return 0;
		
		int availableslots = 0;
		int itemsinslots = 0;
		int i=0;
		for(ItemStack itemstack:recipe) {
			if(itemstack == null) {
				i++;
				continue;
			}
			if(stack.itemID == itemstack.itemID && stack.getItemDamage() == itemstack.getItemDamage()) {
				availableslots++;
				ItemStack slot = inv.getStackInSlot(i);
				if(slot != null) {
					itemsinslots += slot.stackSize;
				}
			}
			i++;
		}
		int toadd = Math.min(availableslots * 64 - itemsinslots, stack.stackSize);
		if(!doAdd) {
			return toadd;
		}
		if(toadd <= 0) {
			return 0;
		}
		itemsinslots += toadd;
		int itemsperslot = itemsinslots / availableslots;
		int itemsextra = itemsinslots - (itemsperslot * availableslots);
		i = 0;
		for(ItemStack itemstack:recipe) {
			if(itemstack == null) {
				i++;
				continue;
			}
			if(stack.itemID == itemstack.itemID && stack.getItemDamage() == itemstack.getItemDamage()) {
				if(itemsperslot == 0 && itemsextra == 0) {
					inv.setInventorySlotContents(i, null);
				} else {
					ItemStack slot = inv.getStackInSlot(i);
					if(slot == null) {
						slot = stack.copy();
					}
					slot.stackSize = itemsperslot;
					if(itemsextra > 0) {
						slot.stackSize++;
						itemsextra--;
					}
					inv.setInventorySlotContents(i, slot);
				}
			}
			i++;
		}
		inv.onInventoryChanged();
		super.onInventoryChanged();
		return toadd;
	}

	@Override
	public ItemStack[] extractItem(boolean doRemove, ForgeDirection from, int maxItemCount) {
		ItemStack[] tmp = new ItemStack[] { inv.getStackInSlot(10) };
		if (doRemove) {
			inv.setInventorySlotContents(10, null);
			inv.onInventoryChanged();
			super.onInventoryChanged();
		}
		return tmp;
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
		inv.dropContents(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public int getRotation() {
		return rotation;
	}

	@Override
	public int getFrontTexture() {
		if(heat > 0) {
			return 3;
		} else {
			return 17;
		}
	}

	@Override
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	@Override
	public boolean isInvNameLocalized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return false;
	}
}

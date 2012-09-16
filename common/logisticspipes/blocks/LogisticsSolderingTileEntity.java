package logisticspipes.blocks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.ICraftingResultHandler;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.recipes.SolderingStationRecipes.SolderingStationRecipe;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import buildcraft.api.core.Orientations;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class LogisticsSolderingTileEntity extends TileEntity implements IPowerReceptor, ISpecialInventory , IGuiOpenControler{
	
	private IPowerProvider provider;
	private SimpleInventory inv = new SimpleInventory(12, "Soldering Inventory", 64);
	public int heat = 0;
	public int progress = 0;
	
	private List<EntityPlayer> listener = new ArrayList<EntityPlayer>();

	public LogisticsSolderingTileEntity() {
		provider = PowerFramework.currentFramework.createPowerProvider();
		provider.configure(10, 10, 10, 10, 10);
	}

	public Container createContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player, this, this);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				final int slotNumber = i * 3 + j;
				dummy.addRestrictedSlot(slotNumber, this, 30 + (j * 18),
						17 + (i * 18), new ISlotCheck() {
							@Override
							public boolean isStackAllowed(ItemStack itemStack) {
								return checkSlot(itemStack, slotNumber);
							}
						});
			}
		}
		dummy.addRestrictedSlot(9, this, 93, 17, Item.ingotIron.shiftedIndex);
		dummy.addRestrictedSlot(10, this, 127, 47, -1);
		dummy.addRestrictedSlot(11, this, 149, 11, new ISlotCheck() {
			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				return getRecipeForTaget(itemStack) != null;
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
		for(EntityPlayer player:listener) {
			PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SOLDERING_UPDATE_HEAT, xCoord, yCoord, zCoord, this.heat).getPacket(), (Player)player);
		}
	}

	private void updateProgress() {
		for(EntityPlayer player:listener) {
			PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SOLDERING_UPDATE_PROGRESS, xCoord, yCoord, zCoord, this.progress).getPacket(), (Player)player);
		}
	}
	
	private void updateInventory() {
		for(EntityPlayer player:listener) {
			PacketDispatcher.sendPacketToPlayer(new PacketInventoryChange(NetworkConstants.SOLDERING_UPDATE_INVENTORY, xCoord, yCoord, zCoord, this).getPacket(), (Player)player);
		}
	}
	
	@Override
	public void updateEntity() {
		if(hasWork() && heat < 100) {
			if(provider.useEnergy(1, 3, false) >= 1) {
				heat += provider.useEnergy(1, 3, true);
				heat--;
				if(heat > 100) {
					heat = 100;
				}
				updateHeat();
			} else {
				heat--;
				if(heat < 0) {
					heat = 0;
				}
				updateHeat();
			}
		} else if(!hasWork() && heat > 0) {
			heat--;
			updateHeat();
		}
		if(hasWork() && heat >= 100) {
			progress += provider.useEnergy(1, 3, true);
			if(progress >= 100) {
				ItemStack content = inv.getStackInSlot(10);
				if(content == null) {
					ICraftingResultHandler handler = getHandlerForRecipe();
					content = getTagetForRecipe(true);
					if(handler != null) {
						handler.handleCrafting(content);
					}
					inv.setInventorySlotContents(10, content);
					inv.getStackInSlot(9).stackSize -= 1;
					if(inv.getStackInSlot(9).stackSize <= 0) {
						inv.setInventorySlotContents(9, null);
					}
					
					updateInventory();
					progress = 0;
				} else {
					progress -= 50;
				}
			}
			updateProgress();
		} else if(!hasWork() && progress != 0) {
			progress = 0;
			updateProgress();
		}
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
	public int powerRequest() {
		if (hasWork()) {
			return 3;
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
	public int addItem(ItemStack stack, boolean doAdd, Orientations from) {
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
			}
			if (iron.stackSize == 0) {
				inv.setInventorySlotContents(9, null);
			}
			return toAdd;
		}
		//TODO
		return 0;
	}

	@Override
	public ItemStack[] extractItem(boolean doRemove, Orientations from,
			int maxItemCount) {
		ItemStack[] tmp = new ItemStack[] { inv.getStackInSlot(10) };
		if (doRemove) {
			inv.setInventorySlotContents(10, null);
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
}

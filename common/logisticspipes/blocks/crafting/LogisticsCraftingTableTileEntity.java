package logisticspipes.blocks.crafting;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.config.Configs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.CraftingUtil;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class LogisticsCraftingTableTileEntity extends TileEntity implements ISimpleInventoryEventHandler, IInventory {
	
	public SimpleInventory inv = new SimpleInventory(18, "Crafting Resources", 64);
	public SimpleInventory matrix = new SimpleInventory(9, "Crafting Matrix", 1);
	public SimpleInventory resultInv = new SimpleInventory(1, "Crafting Result", 1);
	private IRecipe cache;
	private EntityPlayer fake;
	
	public LogisticsCraftingTableTileEntity() {
		matrix.addListener(this);
	}
	
	public void cacheRecipe() {
		cache = null;
		resultInv.clearInventorySlotContents(0);
		AutoCraftingInventory craftInv = new AutoCraftingInventory();
		for(int i=0; i<9;i++) {
			craftInv.setInventorySlotContents(i, matrix.getStackInSlot(i));
		}
		for(IRecipe r : CraftingUtil.getRecipeList()) {
			if(r.matches(craftInv, getWorldObj())) {
				cache = r;
				resultInv.setInventorySlotContents(0, r.getCraftingResult(craftInv));
			}
		}
	}

	public ItemStack getOutput(ItemIdentifier wanted, IRoutedPowerProvider power) {
		if(cache == null) {
			cacheRecipe();
			if(cache == null) return null;
		}
		int[] toUse = new int[9];
		int[] used = new int[inv.getSizeInventory()];
outer:
		for(int i=0;i<9;i++) {
			ItemIdentifierStack item = matrix.getIDStackInSlot(i);
			if(item == null) {
				toUse[i] = -1;
				continue;
			}
			ItemIdentifier ident = item.getItem();
			for(int j=0;j<inv.getSizeInventory();j++) {
				item = inv.getIDStackInSlot(j);
				if(item == null) continue;
				if(ident.equalsForCrafting(item.getItem())) {
					if(item.stackSize > used[j]) {
						used[j]++;
						toUse[i] = j;
						continue outer;
					}
				}
			}
			//Not enough material
			return null;
		}
		AutoCraftingInventory crafter = new AutoCraftingInventory();
		for(int i=0;i<9;i++) {
			int j = toUse[i];
			if(j != -1) crafter.setInventorySlotContents(i, inv.getStackInSlot(j));
		}
		ItemStack result = cache.getCraftingResult(crafter);
		if(result == null) return null;
		if(!resultInv.getIDStackInSlot(0).getItem().equalsWithoutNBT(ItemIdentifier.get(result))) return null;
		if(!wanted.equalsWithoutNBT(resultInv.getIDStackInSlot(0).getItem())) return null;
		if(!power.useEnergy(Configs.LOGISTICS_CRAFTING_TABLE_POWER_USAGE)) return null;
		crafter = new AutoCraftingInventory();
		for(int i=0;i<9;i++) {
			int j = toUse[i];
			if(j != -1) crafter.setInventorySlotContents(i, inv.decrStackSize(j, 1));
		}
		result = cache.getCraftingResult(crafter);
		if(fake == null) {
			fake = MainProxy.getFakePlayer(this);
		}
		result = result.copy();
		SlotCrafting craftingSlot = new SlotCrafting(fake, crafter, resultInv, 0, 0, 0);
		craftingSlot.onPickupFromSlot(fake, result);
		for(int i=0;i<9;i++) {
			ItemStack left = crafter.getStackInSlot(i);
			crafter.setInventorySlotContents(i, null);
			if(left != null) inv.addCompressed(left);
		}
		for(int i=0;i<fake.inventory.getSizeInventory();i++) {
			ItemStack left = fake.inventory.getStackInSlot(i);
			fake.inventory.setInventorySlotContents(i, null);
			if(left != null) inv.addCompressed(left);
		}
		return result;
	}

	public void onBlockBreak() {
		inv.dropContents(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		if(inventory == matrix) {
			cacheRecipe();
		}
	}

	public void handleNEIRecipePacket(ItemStack[] content) {
		for(int i=0;i<9;i++) {
			matrix.setInventorySlotContents(i, content[i]);
		}
		cacheRecipe();
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		inv.readFromNBT(par1nbtTagCompound, "inv");
		matrix.readFromNBT(par1nbtTagCompound, "matrix");
		cacheRecipe();
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		inv.writeToNBT(par1nbtTagCompound, "inv");
		matrix.writeToNBT(par1nbtTagCompound, "matrix");
	}

	@Override
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inv.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return inv.decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return inv.getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv.setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInvName() {
		return "LogisticsCraftingTable";
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if(i < 9 && i >= 0) {
			ItemIdentifierStack stack = matrix.getIDStackInSlot(i);
			if(stack != null && itemstack != null) {
				return stack.getItem().equalsWithoutNBT(ItemIdentifier.get(itemstack));
			}
		}
		return true;
	}
}

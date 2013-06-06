package logisticspipes.blocks.crafting;

import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;

import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class LogisticsCraftingTableTileEntity extends TileEntity implements ISimpleInventoryEventHandler {
	
	public SimpleInventory inv = new SimpleInventory(18, "Crafting Resources", 64);
	public SimpleInventory matrix = new SimpleInventory(10, "Crafting Matrix", 1);
	private IRecipe cache;
	
	public LogisticsCraftingTableTileEntity() {
		matrix.addListener(this);
	}
	
	@SuppressWarnings("unchecked")
	public void cacheRecipe() {
		cache = null;
		matrix.setInventorySlotContents(9, null);
		AutoCraftingInventory craftInv = new AutoCraftingInventory();
		for(int i=0; i<9;i++) {
			craftInv.stackList[i] = matrix.getStackInSlot(i);
		}
		for(IRecipe r : (List<IRecipe>)CraftingManager.getInstance().getRecipeList()) {
			if(r.matches(craftInv, worldObj)) {
				cache = r;
				matrix.setInventorySlotContents(9, r.getCraftingResult(craftInv));
			}
		}
	}

	public ItemStack getOutput() {
		if(cache == null) {
			cacheRecipe();
			if(cache == null) return null;
		}
		int[] toUse = new int[9];
		int[] used = new int[inv.getSizeInventory()];
outer:
		for(int i=0;i<9;i++) {
			ItemStack item = matrix.getStackInSlot(i);
			if(item == null) continue;
			ItemIdentifier ident = ItemIdentifier.get(item);
			for(int j=0;j<inv.getSizeInventory();j++) {
				item = inv.getStackInSlot(j);
				if(item == null) continue;
				if(ident.equalsForCrafting(ItemIdentifier.get(item))) {
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
			crafter.stackList[i] = inv.getStackInSlot(j);
		}
		ItemStack result = cache.getCraftingResult(crafter);
		if(result == null) return null;
		if(!ItemIdentifier.get(matrix.getStackInSlot(9)).equalsWithoutNBT(ItemIdentifier.get(result))) return null;
		crafter = new AutoCraftingInventory();
		for(int i=0;i<9;i++) {
			int j = toUse[i];
			crafter.stackList[i] = inv.decrStackSize(j, 1);
		}
		result = cache.getCraftingResult(crafter);
		//TODO FakePlayer
		//GameRegistry.onItemCrafted(null, result, crafter);
		return result;
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

	public void debug() {
		inv.addCompressed(getOutput());
	}
}

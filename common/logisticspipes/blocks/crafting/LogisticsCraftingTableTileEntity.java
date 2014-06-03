package logisticspipes.blocks.crafting;

import cpw.mods.fml.common.network.Player;
import logisticspipes.Configs;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.block.CraftingTableFuzzyFlagsModifyPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.CraftingRequirement;
import logisticspipes.utils.CraftingUtil;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class LogisticsCraftingTableTileEntity extends TileEntity implements ISimpleInventoryEventHandler, IInventory {
	
	public ItemIdentifierInventory inv = new ItemIdentifierInventory(18, "Crafting Resources", 64);
	public ItemIdentifierInventory matrix = new ItemIdentifierInventory(9, "Crafting Matrix", 1);
	public ItemIdentifierInventory resultInv = new ItemIdentifierInventory(1, "Crafting Result", 1);
	//just use CraftingRequirement to store flags; field "stack" is ignored
	public CraftingRequirement[] fuzzyFlags = new CraftingRequirement[9];
	private IRecipe cache;
	private EntityPlayer fake;
	private String placedBy = "";
	
	public LogisticsCraftingTableTileEntity() {
		matrix.addListener(this);
		for(int i = 0; i < 9; i++)
			fuzzyFlags[i] = new CraftingRequirement();
	}
	
	public void cacheRecipe() {
		cache = null;
		resultInv.clearInventorySlotContents(0);
		AutoCraftingInventory craftInv = new AutoCraftingInventory(placedBy);
		for(int i=0; i<9;i++) {
			craftInv.setInventorySlotContents(i, matrix.getStackInSlot(i));
		}
		for(IRecipe r : CraftingUtil.getRecipeList()) {
			if(r.matches(craftInv, getWorldObj())) {
				cache = r;
				resultInv.setInventorySlotContents(0, r.getCraftingResult(craftInv));
				break;
			}
		}
	}
	
	private boolean testFuzzy(ItemIdentifier item, ItemIdentifierStack item2, int slot) {
		fuzzyFlags[slot].stack = item.makeStack(1);
		return fuzzyFlags[slot].testItem(item2);
	}

	public ItemStack getOutput(ItemIdentifier wanted, IRoutedPowerProvider power) {
		boolean isFuzzy = this.isFuzzy();
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
				if(isFuzzy ? (testFuzzy(ident, item, i)) : ident.equalsForCrafting(item.getItem())) {
					if(item.getStackSize() > used[j]) {
						used[j]++;
						toUse[i] = j;
						continue outer;
					}
				}
			}
			//Not enough material
			return null;
		}
		AutoCraftingInventory crafter = new AutoCraftingInventory(placedBy);
		for(int i=0;i<9;i++) {
			int j = toUse[i];
			if(j != -1) crafter.setInventorySlotContents(i, inv.getStackInSlot(j));
		}
		if(!cache.matches(crafter, getWorldObj())) return null; //Fix MystCraft
		ItemStack result = cache.getCraftingResult(crafter);
		if(result == null) return null;
		if(!resultInv.getIDStackInSlot(0).getItem().equalsWithoutNBT(ItemIdentifier.get(result))) return null;
		if(!wanted.equalsWithoutNBT(resultInv.getIDStackInSlot(0).getItem())) return null;
		if(!power.useEnergy(Configs.LOGISTICS_CRAFTING_TABLE_POWER_USAGE)) return null;
		crafter = new AutoCraftingInventory(placedBy);
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
			if(left != null) {
				left.stackSize = inv.addCompressed(left, false);
				if(left.stackSize > 0) {
					ItemIdentifierInventory.dropItems(worldObj, left, xCoord, yCoord, zCoord);
				}
			}
		}
		for(int i=0;i<fake.inventory.getSizeInventory();i++) {
			ItemStack left = fake.inventory.getStackInSlot(i);
			fake.inventory.setInventorySlotContents(i, null);
			if(left != null) {
				left.stackSize = inv.addCompressed(left, false);
				if(left.stackSize > 0) {
					ItemIdentifierInventory.dropItems(worldObj, left, xCoord, yCoord, zCoord);
				}
			}
		}
		return result;
	}

	public void onBlockBreak() {
		inv.dropContents(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
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
		placedBy = par1nbtTagCompound.getString("placedBy");
		if(par1nbtTagCompound.hasKey("fuzzyFlags")) {
			NBTTagList lst = par1nbtTagCompound.getTagList("fuzzyFlags");
			for(int i = 0; i < 9; i++) {
				NBTTagCompound comp = (NBTTagCompound) lst.tagAt(i);
				fuzzyFlags[i].ignore_dmg = comp.getBoolean("ignore_dmg");
				fuzzyFlags[i].ignore_nbt = comp.getBoolean("ignore_nbt");
				fuzzyFlags[i].use_od = comp.getBoolean("use_od");
				fuzzyFlags[i].use_category = comp.getBoolean("use_category");
			}
		}
		cacheRecipe();
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		inv.writeToNBT(par1nbtTagCompound, "inv");
		matrix.writeToNBT(par1nbtTagCompound, "matrix");
		par1nbtTagCompound.setString("placedBy", placedBy);
		NBTTagList lst = new NBTTagList();
		for(int i = 0; i < 9; i++) {
			NBTTagCompound comp = new NBTTagCompound();
			comp.setBoolean("ignore_dmg", fuzzyFlags[i].ignore_dmg);
			comp.setBoolean("ignore_nbt", fuzzyFlags[i].ignore_nbt);
			comp.setBoolean("use_od", fuzzyFlags[i].use_od);
			comp.setBoolean("use_category", fuzzyFlags[i].use_category);
			lst.appendTag(comp);
		}
		par1nbtTagCompound.setTag("fuzzyFlags", lst);
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
	public boolean canUpdate() {
		return false;
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

	public void placedBy(EntityLivingBase par5EntityLivingBase) {
		if(par5EntityLivingBase instanceof EntityPlayer) {
			placedBy = par5EntityLivingBase.getEntityName();
		}
	}
	
	public boolean isFuzzy() {
		return worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == LogisticsSolidBlock.LOGISTICS_FUZZYCRAFTING_TABLE;
	}

	public void handleFuzzyFlagsChange(int integer, int integer2, EntityPlayer pl) {
		if(integer < 0 || integer >= 9)
			return;
		if(MainProxy.isClient(this.getWorldObj())) {
			if(pl == null) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingTableFuzzyFlagsModifyPacket.class)
						.setInteger2(integer2)
						.setInteger(integer)
						.setTilePos(this)
					);
			} else {
				this.fuzzyFlags[integer].use_od = (integer2 & 1) != 0;
				this.fuzzyFlags[integer].ignore_dmg = (integer2 & 2) != 0;
				this.fuzzyFlags[integer].ignore_nbt = (integer2 & 4) != 0;
				this.fuzzyFlags[integer].use_category = (integer2 & 8) != 0;
			}
		} else {
			if(integer2 == 0) this.fuzzyFlags[integer].use_od = !this.fuzzyFlags[integer].use_od;
			if(integer2 == 1) this.fuzzyFlags[integer].ignore_dmg = !this.fuzzyFlags[integer].ignore_dmg;
			if(integer2 == 2) this.fuzzyFlags[integer].ignore_nbt = !this.fuzzyFlags[integer].ignore_nbt;
			if(integer2 == 3) this.fuzzyFlags[integer].use_category = !this.fuzzyFlags[integer].use_category;
			ModernPacket pak = PacketHandler.getPacket(CraftingTableFuzzyFlagsModifyPacket.class)
					.setInteger2((fuzzyFlags[integer].use_od ? 1 : 0)
							| (fuzzyFlags[integer].ignore_dmg ? 2 : 0)
							| (fuzzyFlags[integer].ignore_nbt ? 4 : 0)
							| (fuzzyFlags[integer].use_category ? 8 : 0))
					.setInteger(integer)
					.setTilePos(this);
			if(pl != null)
				MainProxy.sendPacketToPlayer(pak, (Player)pl);
			MainProxy.sendPacketToAllWatchingChunk(xCoord, zCoord, MainProxy.getDimensionForWorld(worldObj), pak);
		}
	}
}

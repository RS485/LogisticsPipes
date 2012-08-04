package net.minecraft.src.buildcraft.logisticspipes.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NBTTagString;
import net.minecraft.src.buildcraft.krapht.LogisticsItem;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;

public abstract class ItemModuleProxy extends LogisticsItem {

	public ItemModuleProxy(int i) {
		super(i);
	}

	@Override
	public int getIconFromDamage(int i) {

		if (i >= 500){
			return 5 * 16 + (i - 500);
		}
		
		if (i >= 200){
			return 4 * 16 + (i - 200);
		}
		
		if (i >= 100){
			return 3 * 16 + (i - 100);
		}
			
		return 2 * 16 + i;
	}
	
	public boolean func_46056_k() {
		return true;
	}
	
	public void addInformation(ItemStack itemStack, List list) {
		if(itemStack.hasTagCompound()) {
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				NBTTagCompound nbt = itemStack.getTagCompound();
				if(nbt.hasKey("informationList")) {
					NBTTagList nbttaglist = nbt.getTagList("informationList");
					for(int i=0;i<nbttaglist.tagCount();i++) {
						NBTBase nbttag = nbttaglist.tagAt(i);
						String data = ((NBTTagString)nbttag).data;
						if(data.equals("<inventory>") && i + 1 < nbttaglist.tagCount()) {
							nbttag = nbttaglist.tagAt(i + 1);
							data = ((NBTTagString)nbttag).data;
							if(data.startsWith("<that>")) {
								String prefix = data.substring(6);
								NBTTagCompound module = nbt.getCompoundTag("moduleInformation");
								SimpleInventory inv = new SimpleInventory(module.getTagList(prefix + "items").tagCount(), "InformationTempInventory", Integer.MAX_VALUE);
								inv.readFromNBT(module, prefix);
								for(int pos=0;pos < inv.getSizeInventory();pos++) {
									ItemStack stack = inv.getStackInSlot(pos);
									if(stack != null) {
										if(stack.stackSize > 1) {
											list.add("  " + stack.stackSize+"x " + ItemIdentifier.get(stack).getFriendlyName());	
										} else {
											list.add("  " + ItemIdentifier.get(stack).getFriendlyName());
										}
									}
								}
							}
							i++;
						} else {
							list.add(data);
						}
					}
				}
			}
			/*if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				NBTTagCompound nbt = itemStack.getTagCompound();
				list.add(nbt.toString());
				for(Object obj:nbt.getTags().toArray()) {
					list.add(obj.toString());				
				}
			}*/
		}
	}
	
	public abstract String getModuleDisplayName(ItemStack itemstack);
	
	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return getModuleDisplayName(itemstack);
	}
	
}

package logisticspipes.items;

import java.util.List;

import logisticspipes.textures.Textures;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NBTTagString;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.SpriteHelper;

public abstract class ItemModuleProxy extends LogisticsNBTTagCompundItem {

	public ItemModuleProxy(int i) {
		super(i);
	}

	public abstract int getModuleIconFromDamage(int damage);

	@Override
	public String getTextureFile() {
		return Textures.LOGISTICSITEMS_TEXTURE_FILE;
	}
	
	@Override
	public int getIconFromDamage(int i) {
		return getModuleIconFromDamage(i);
	}
	
	public abstract String getTextureMap();
	
	public void loadModules() {
		SpriteHelper.registerSpriteMapForFile(Textures.LOGISTICSITEMS_TEXTURE_FILE, getTextureMap());
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
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
		}
	}
	
	public abstract String getModuleDisplayName(ItemStack itemstack);
	
	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return getModuleDisplayName(itemstack);
	}
	
}

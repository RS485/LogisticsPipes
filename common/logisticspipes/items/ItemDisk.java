package logisticspipes.items;

import java.util.List;

import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.textures.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;


public class ItemDisk extends LogisticsItem {

	public ItemDisk(int i) {
		super(i);
	}
	
	@Override
	public int getItemStackLimit()
    {
        return 1;
    }

	@Override
	public CreativeTabs getCreativeTab()
    {
        return CreativeTabs.tabRedstone;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
		if(itemStack.hasTagCompound()) {
			if(itemStack.getTagCompound().hasKey("name")) {
				String name = "\u00a78" + itemStack.getTagCompound().getString("name");
				list.add(name);
			}
		}
	}
}

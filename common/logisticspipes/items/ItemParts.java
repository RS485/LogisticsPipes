package logisticspipes.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemParts extends LogisticsItem {

	public ItemParts() {
		super();
		setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {
		for (int i = 0; i < 4; i++) {
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation("logisticspipes:" + getUnlocalizedName().replace("item.", "") + "/" + i, "inventory"));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		switch (par1ItemStack.getItemDamage()) {
			case 0: //bow
				return "item.HUDbow".toLowerCase();
			case 1: //glass
				return "item.HUDglass".toLowerCase();
			case 2: //nose bridge
				return "item.HUDnosebridge".toLowerCase();
			case 3:
				return "item.NanoHopper".toLowerCase();
		}
		return super.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			items.add(new ItemStack(this, 1, 0));
			items.add(new ItemStack(this, 1, 1));
			items.add(new ItemStack(this, 1, 2));
			items.add(new ItemStack(this, 1, 3));
		}
	}

}

package logisticspipes.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemParts extends LogisticsItem {
	private IIcon[] _icons;
	public ItemParts() {
		this.setHasSubtypes(true);
	}
	@Override
	public void registerIcons(IIconRegister iconreg)
	{
		_icons=new IIcon[4];
		for(int i=0;i<4;i++)
		{
			_icons[i]=iconreg.registerIcon("logisticspipes:"+getUnlocalizedName().replace("item.", "")+"/"+i);
		}
	}
    @Override
	public IIcon getIconFromDamage(int par1) {
    		return _icons[par1%4];
    }

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
    	switch(par1ItemStack.getItemDamage()) {
    	case 0: //bow
    		return "item.HUDbow";
    	case 1: //glass
    		return "item.HUDglass";
    	case 2: //nose bridge
    		return "item.HUDnosebridge";
    	case 3: 
    		return "item.NanoHopper";
    	}
		return super.getUnlocalizedName(par1ItemStack);
	}

	@Override
	public CreativeTabs getCreativeTab() {
        return CreativeTabs.tabRedstone;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(this, 1, 0));
		par3List.add(new ItemStack(this, 1, 1));
		par3List.add(new ItemStack(this, 1, 2));
		par3List.add(new ItemStack(this, 1, 3));
    }
	
}

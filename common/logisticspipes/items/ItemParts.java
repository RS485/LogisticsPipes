package logisticspipes.items;

import java.util.List;

import logisticspipes.textures.Textures;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemParts extends LogisticsItem {
	private static Icon[] _icons;
	public ItemParts(int par1) {
		super(par1);
		this.setHasSubtypes(true);
	}
	@Override
	public void registerIcons(IconRegister iconreg)
	{
		_icons=new Icon[4];
		for(int i=0;i<4;i++)
		{
			_icons[i]=iconreg.registerIcon("logisticspipes:"+getUnlocalizedName().replace("item.", "")+"/"+i);
		}
	}
    @Override
	public Icon getIconFromDamage(int par1) {
    		return _icons[par1%3];
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
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(this, 1, 0));
		par3List.add(new ItemStack(this, 1, 1));
		par3List.add(new ItemStack(this, 1, 2));
		par3List.add(new ItemStack(this, 1, 3));
    }
	
}

package logisticspipes.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemPipeParts extends LogisticsItem
{
	private IIcon[] _icons; 
	
	public ItemPipeParts()
	{
		this.setHasSubtypes(true);
	}
	
	@Override
	public void registerIcons(IIconRegister iconreg)
	{
		_icons=new IIcon[16];
		for(int i=0;i<16;i++)
		{
			_icons[i]=iconreg.registerIcon("logisticspipes:"+getUnlocalizedName().replace("item.", "")+"/"+i);
		}
	}
	
	@Override
	public IIcon getIconFromDamage(int i) {
    		return _icons[i%16];
    }
	
	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
    	switch(par1ItemStack.getItemDamage()) {
    	case 0:
    		return "item.dustSilicium";
    	case 1:
    		return "item.boardKernel";
    	case 2:
    		return "item.boardInterface";
    	case 3: 
    		return "item.coreRouting";
    	case 4:
    		return "item.coreCrafter";
    	case 5:
    		return "item.coreSecurity";
    	case 6:
    		return "item.coreStatistic";
    	case 7:
    		return "item.interfaceRouting";
    	case 8:
    		return "item.interfaceCrafter";
    	case 9:
    		return "item.interfaceSecurity";
    	case 10:
    		return "item.interfaceStatistic";
    	case 11:
    		return "item.interfaceUI";
    	case 12:
    		return "item.interfaceExtraction";
    	case 13:
    		return "item.interfaceSupply";
    	case 14:
    		return "item.interfaceEnergy";
    	case 15:
    		return "item.interfaceProvider";
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
		par3List.add(new ItemStack(this, 1, 4));
		par3List.add(new ItemStack(this, 1, 5));
		par3List.add(new ItemStack(this, 1, 6));
		par3List.add(new ItemStack(this, 1, 7));
		par3List.add(new ItemStack(this, 1, 8));
		par3List.add(new ItemStack(this, 1, 9));
		par3List.add(new ItemStack(this, 1, 10));
		par3List.add(new ItemStack(this, 1, 11));
		par3List.add(new ItemStack(this, 1, 12));
		par3List.add(new ItemStack(this, 1, 13));
		par3List.add(new ItemStack(this, 1, 14));
		par3List.add(new ItemStack(this, 1, 15));
    }
}

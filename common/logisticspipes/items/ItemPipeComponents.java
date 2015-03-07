package logisticspipes.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemPipeComponents extends LogisticsItem{
	//Structural Frame of the Pipes
	public static final int ITEM_PIPESTRUCTURE = 0;
	//Servo for Item Extraction
	public static final int ITEM_MICROSERVO = 1;
	//Routing Processor to compute routing actions
	public static final int ITEM_ROUTEPROCESSOR = 2;
	//Micro Packager to un-/Package Items on arrival/department
	public static final int ITEM_MICROPACKAGER = 3;
	//Micro Encapsulator for Fluid un-/packaging on arrival/department
	public static final int ITEM_MICROCAPSULATOR = 4;
	//Logic Expander Circuit for upgrades and such
	public static final int ITEM_LOGICEXPANDER = 5;
	
	private IIcon[] _icons;
	
	public ItemPipeComponents()
	{
		this.setHasSubtypes(true);
	}
	
	@Override
	public void registerIcons(IIconRegister iconreg)
	{
		_icons = new IIcon[6];
		for(int i = 0; i<6; i++)
		{
			_icons[i]=iconreg.registerIcon("logisticspipes:"+getUnlocalizedName().replace("item.", "")+"/"+i);
		}
	}
	
	@Override
	public IIcon getIconFromDamage(int i) {return _icons[i%6];}
	
	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack)
	{
		switch(par1ItemStack.getItemDamage())
		{
		case 0: return "item.structframe";
		case 1: return "item.microservo";
		case 2: return "item.routeprocess";
		case 3: return "item.micropackager";
		case 4: return "item.microcapsulator";
		case 5: return "item.logicexpander";
		}
		return super.getUnlocalizedName(par1ItemStack);
	}
	
	@Override
	public CreativeTabs getCreativeTab() {
        return CreativeTabs.tabRedstone;
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public void getSubItems(Item par1, CreativeTabs par2, @SuppressWarnings("rawtypes") List par3)
	{
		par3.add(new ItemStack(this, 1, 0));
		par3.add(new ItemStack(this, 1, 1));
		par3.add(new ItemStack(this, 1, 2));
		par3.add(new ItemStack(this, 1, 3));
		par3.add(new ItemStack(this, 1 ,4));
		par3.add(new ItemStack(this, 1, 5));
	}
}

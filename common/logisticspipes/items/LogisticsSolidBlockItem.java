package logisticspipes.items;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsSolidBlockItem extends ItemBlock {

	public LogisticsSolidBlockItem(int par1) {
		super(par1);
		this.setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemDisplayName(ItemStack stack) {
		switch (stack.getItemDamage()) {
		case LogisticsSolidBlock.SOLDERING_STATION:
			return "Soldering Station";
		case LogisticsSolidBlock.LOGISTICS_POWER_JUNCTION:
			return "Logistics Power Junction";
		case LogisticsSolidBlock.LOGISTICS_SECURITY_STATION:
			return "Logistics Security Station";
		case LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE:
			return "Logistics Crafting Table";
		case LogisticsSolidBlock.LOGISTICS_BC_POWERPROVIDER:
			return "Logistics BC Power Provider";
		case LogisticsSolidBlock.LOGISTICS_RF_POWERPROVIDER:
			return "Logistics TE Power Provider";
		case LogisticsSolidBlock.LOGISTICS_IC2_POWERPROVIDER:
			return "Logistics IC2 Power Provider";
		}
		return super.getItemDisplayName(stack);
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return LogisticsPipes.LPCreativeTab;
	}

	@Override
	public int getMetadata(int par1) {
        return par1;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(this,1,LogisticsSolidBlock.SOLDERING_STATION));
		par3List.add(new ItemStack(this,1,LogisticsSolidBlock.LOGISTICS_POWER_JUNCTION));
		par3List.add(new ItemStack(this,1,LogisticsSolidBlock.LOGISTICS_SECURITY_STATION));
		par3List.add(new ItemStack(this,1,LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE));
		par3List.add(new ItemStack(this,1,LogisticsSolidBlock.LOGISTICS_BC_POWERPROVIDER));
		if(SimpleServiceLocator.thermalExpansionProxy.isTE()) {
			par3List.add(new ItemStack(this,1,LogisticsSolidBlock.LOGISTICS_RF_POWERPROVIDER));
		}
		if(SimpleServiceLocator.IC2Proxy.hasIC2()) {
			par3List.add(new ItemStack(this,1,LogisticsSolidBlock.LOGISTICS_IC2_POWERPROVIDER));
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {}
}

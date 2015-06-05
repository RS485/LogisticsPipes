package logisticspipes.items;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class LogisticsSolidBlockItem extends ItemBlock {

	public LogisticsSolidBlockItem(Block par1) {
		super(par1);
		setHasSubtypes(true);
		setUnlocalizedName("logisticsSolidBlock");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		switch (stack.getItemDamage()) {
			case LogisticsSolidBlock.SOLDERING_STATION:
				return "tile.solderingstation";
			case LogisticsSolidBlock.LOGISTICS_POWER_JUNCTION:
				return "tile.logisticspowerjunction";
			case LogisticsSolidBlock.LOGISTICS_SECURITY_STATION:
				return "tile.logisticssecuritystation";
			case LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE:
				return "tile.logisticscraftingtable";
			case LogisticsSolidBlock.LOGISTICS_FUZZYCRAFTING_TABLE:
				return "tile.logisticsfuzzycraftingtable";
			case LogisticsSolidBlock.LOGISTICS_STATISTICS_TABLE:
				return "tile.logisticsstatisticstable";
			case LogisticsSolidBlock.LOGISTICS_RF_POWERPROVIDER:
				return "tile.logisticstepowerprovider";
			case LogisticsSolidBlock.LOGISTICS_IC2_POWERPROVIDER:
				return "tile.logisticsic2powerprovider";
		}
		return super.getUnlocalizedName(stack);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		return StringUtils.translate(getUnlocalizedName(itemstack));
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
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.SOLDERING_STATION));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.LOGISTICS_POWER_JUNCTION));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.LOGISTICS_SECURITY_STATION));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.LOGISTICS_AUTOCRAFTING_TABLE));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.LOGISTICS_FUZZYCRAFTING_TABLE));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.LOGISTICS_STATISTICS_TABLE));
		if (SimpleServiceLocator.cofhPowerProxy.isAvailable()) {
			par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.LOGISTICS_RF_POWERPROVIDER));
		}
		if (SimpleServiceLocator.IC2Proxy.hasIC2()) {
			par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.LOGISTICS_IC2_POWERPROVIDER));
		}
	}

	@Override
	public void registerIcons(IIconRegister par1IIconRegister) {}
}

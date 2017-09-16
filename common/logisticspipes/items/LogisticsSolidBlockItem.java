package logisticspipes.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.string.StringUtils;

public class LogisticsSolidBlockItem extends ItemBlock {

	public LogisticsSolidBlockItem(Block par1) {
		super(par1);
		setHasSubtypes(true);
		setUnlocalizedName("logisticsSolidBlock");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		switch (LogisticsSolidBlock.BlockType.getForMeta(stack.getItemDamage())) {
			case SOLDERING_STATION:
				return "tile.solderingstation";
			case LOGISTICS_POWER_JUNCTION:
				return "tile.logisticspowerjunction";
			case LOGISTICS_SECURITY_STATION:
				return "tile.logisticssecuritystation";
			case LOGISTICS_AUTOCRAFTING_TABLE:
				return "tile.logisticscraftingtable";
			case LOGISTICS_FUZZYCRAFTING_TABLE:
				return "tile.logisticsfuzzycraftingtable";
			case LOGISTICS_STATISTICS_TABLE:
				return "tile.logisticsstatisticstable";
			case LOGISTICS_RF_POWERPROVIDER:
				return "tile.logisticstepowerprovider";
			case LOGISTICS_IC2_POWERPROVIDER:
				return "tile.logisticsic2powerprovider";
			case LOGISTICS_BLOCK_FRAME:
				return "tile.logisticsblankblock";
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
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_BLOCK_FRAME.getMeta()));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.SOLDERING_STATION.getMeta()));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_POWER_JUNCTION.getMeta()));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_SECURITY_STATION.getMeta()));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_AUTOCRAFTING_TABLE.getMeta()));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_FUZZYCRAFTING_TABLE.getMeta()));
		par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_STATISTICS_TABLE.getMeta()));
		if (SimpleServiceLocator.cofhPowerProxy.isAvailable()) {
			par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_RF_POWERPROVIDER.getMeta()));
		}
		if (SimpleServiceLocator.IC2Proxy.hasIC2()) {
			par3List.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_IC2_POWERPROVIDER.getMeta()));
		}
	}
}

package logisticspipes.items;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.client.model.ModelLoader;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.newpipe.LogisticsBlockModel;
import logisticspipes.utils.string.StringUtils;

public class LogisticsSolidBlockItem extends ItemBlock {

	public LogisticsSolidBlockItem(Block par1) {
		super(par1);
		setHasSubtypes(true);
		setUnlocalizedName("logisticssolidblock");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		LogisticsSolidBlock.BlockType forMeta = LogisticsSolidBlock.BlockType.getForMeta(stack.getItemDamage());
		String x = getUnlocalozedName(forMeta);
		if (x != null) return x;
		return super.getUnlocalizedName(stack);
	}

	private String getUnlocalozedName(LogisticsSolidBlock.BlockType forMeta) {
		switch (forMeta) {
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
		return null;
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
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList par3List) {
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

	public LogisticsSolidBlockItem registerModels() {
		for(LogisticsSolidBlock.BlockType block: Arrays.asList(LogisticsSolidBlock.BlockType.LOGISTICS_BLOCK_FRAME,
				LogisticsSolidBlock.BlockType.SOLDERING_STATION,
				LogisticsSolidBlock.BlockType.LOGISTICS_POWER_JUNCTION,
				LogisticsSolidBlock.BlockType.LOGISTICS_SECURITY_STATION,
				LogisticsSolidBlock.BlockType.LOGISTICS_AUTOCRAFTING_TABLE,
				LogisticsSolidBlock.BlockType.LOGISTICS_FUZZYCRAFTING_TABLE,
				LogisticsSolidBlock.BlockType.LOGISTICS_STATISTICS_TABLE,
				LogisticsSolidBlock.BlockType.LOGISTICS_RF_POWERPROVIDER,
				LogisticsSolidBlock.BlockType.LOGISTICS_IC2_POWERPROVIDER)) {
			ModelResourceLocation resourceLocation = new ModelResourceLocation("logisticspipes:" + getUnlocalozedName(block), "inventory");
			ModelLoader.setCustomModelResourceLocation(this, block.getMeta(), resourceLocation);
			LogisticsBlockModel.nameTextureIdMap.put(resourceLocation, block);
		}
		return this;
	}
}

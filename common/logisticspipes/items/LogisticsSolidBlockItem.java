package logisticspipes.items;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
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
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
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
			case LOGISTICS_BC_POWERPROVIDER:
				return "tile.logisticsbcpowerprovider";
			case LOGISTICS_PROGRAM_COMPILER:
				return "tile.logisticsprogramcompiler";
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
	public int getMetadata(int par1) {
		return par1;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_BLOCK_FRAME.getMeta()));
			items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.SOLDERING_STATION.getMeta()));
			items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_POWER_JUNCTION.getMeta()));
			items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_SECURITY_STATION.getMeta()));
			items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_AUTOCRAFTING_TABLE.getMeta()));
			items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_FUZZYCRAFTING_TABLE.getMeta()));
			items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_STATISTICS_TABLE.getMeta()));
			if (SimpleServiceLocator.powerProxy.isAvailable()) {
				items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_RF_POWERPROVIDER.getMeta()));
			}
			if (SimpleServiceLocator.IC2Proxy.hasIC2()) {
				items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_IC2_POWERPROVIDER.getMeta()));
			}
			if (SimpleServiceLocator.buildCraftProxy.isActive()) {
				items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_BC_POWERPROVIDER.getMeta()));
			}
			items.add(new ItemStack(this, 1, LogisticsSolidBlock.BlockType.LOGISTICS_PROGRAM_COMPILER.getMeta()));
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
				LogisticsSolidBlock.BlockType.LOGISTICS_IC2_POWERPROVIDER,
				LogisticsSolidBlock.BlockType.LOGISTICS_BC_POWERPROVIDER,
				LogisticsSolidBlock.BlockType.LOGISTICS_PROGRAM_COMPILER)) {
			ModelResourceLocation resourceLocation = new ModelResourceLocation("logisticspipes:" + getUnlocalozedName(block), "inventory");
			ModelLoader.setCustomModelResourceLocation(this, block.getMeta(), resourceLocation);
			LogisticsBlockModel.nameTextureIdMap.put(resourceLocation, block);
		}
		return this;
	}
}

package logisticspipes.items;

import java.util.Arrays;

import logisticspipes.interfaces.ILogisticsItem;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.newpipe.LogisticsBlockModel;
import logisticspipes.utils.string.StringUtils;
import org.apache.commons.lang3.NotImplementedException;

public class LogisticsSolidBlockItem extends ItemBlock implements ILogisticsItem {

	public LogisticsSolidBlockItem(Block block) {
		super(block);
		setHasSubtypes(true);
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		LogisticsSolidBlock.BlockType forMeta = LogisticsSolidBlock.BlockType.getForMeta(stack.getItemDamage());
		String x = getUnlocalizedName(forMeta);
		if (x != null) return x;
		return super.getUnlocalizedName(stack);
	}

	private String getUnlocalizedName(LogisticsSolidBlock.BlockType forMeta) {
		switch (forMeta) {
			case SOLDERING_STATION:
				return "tile.logisticspipes.soldering_station.name";
			case LOGISTICS_POWER_JUNCTION:
				return "tile.logisticspipes.power_junction.name";
			case LOGISTICS_SECURITY_STATION:
				return "tile.logisticspipes.security_station.name";
			case LOGISTICS_AUTOCRAFTING_TABLE:
				return "tile.logisticspipes.crafting_table.name";
			case LOGISTICS_FUZZYCRAFTING_TABLE:
				return "tile.logisticspipes.fuzzy_crafting_table.name";
			case LOGISTICS_STATISTICS_TABLE:
				return "tile.logisticspipes.statistics_table.name";
			case LOGISTICS_RF_POWERPROVIDER:
				return "tile.logisticspipes.power_provider_rf.name";
			case LOGISTICS_IC2_POWERPROVIDER:
				return "tile.logisticspipes.power_provider_eu.name";
			case LOGISTICS_BC_POWERPROVIDER:
				return "tile.logisticspipes.power_provider_mj.name";
			case LOGISTICS_PROGRAM_COMPILER:
				return "tile.logisticspipes.program_compiler.name";
			case LOGISTICS_BLOCK_FRAME:
				return "tile.logisticspipes.blank_block.name";
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

	@SideOnly(Side.CLIENT)
	public void registerModels() {
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
			ModelResourceLocation resourceLocation = new ModelResourceLocation(getRegistryName(), "inventory");
			ModelLoader.setCustomModelResourceLocation(this, block.getMeta(), resourceLocation);
			LogisticsBlockModel.nameTextureIdMap.put(resourceLocation, block);
		}
	}

	@Override
	public String getModelPath() {
		// TODO split this block into one block for each variant
		throw new NotImplementedException("not implemented");
	}
}

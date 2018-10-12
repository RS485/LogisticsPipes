/**
 * Copyright (c) Krapht, 2011
 * <p>
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.items;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsItem;
import logisticspipes.utils.string.StringUtils;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class LogisticsItem extends Item implements ILogisticsItem {

	public LogisticsItem() {
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
	}

	@SideOnly(Side.CLIENT)
	public final void registerModels() {
		int mc = getModelCount();
		for (int i = 0; i < mc; i++) {
			String modelPath = getModelPath();
			if (mc > 1) {
				String resourcePath = getRegistryName().getResourcePath();
				if (modelPath.matches(String.format(".*%s/%s", resourcePath, resourcePath))) {
					modelPath = String.format("%s/%d", modelPath.substring(0, modelPath.length() - resourcePath.length() - 1), i);
				} else {
					modelPath = String.format("%s.%d", modelPath, i);
				}
			}
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(new ResourceLocation(getRegistryName().getResourceDomain(), modelPath), "inventory"));
		}
	}

	@Override
	public String getModelPath() {
		String modelFile = getRegistryName().getResourcePath();
		String dir = getModelSubdir();
		if (!dir.isEmpty()) {
			if (modelFile.startsWith(String.format("%s_", dir))) {
				modelFile = modelFile.substring(dir.length() + 1);
			}
			return String.format("%s/%s", dir, modelFile).replaceAll("/+", "/");
		}
		return modelFile;
	}

	public String getModelSubdir() {
		return "";
	}

	public int getModelCount() {
		return 1;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (getHasSubtypes()) {
			return String.format("%s.%d", super.getUnlocalizedName(stack), stack.getMetadata());
		}
		return super.getUnlocalizedName(stack);
	}

	/**
	 * Adds all keys from the translation file in the format:
	 * item.className.tip([0-9]*) Tips start from 1 and increment. Sparse rows
	 * should be left empty (ie empty line must still have a key present) Shift
	 * shows full tooltip, without it you just get the first line.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (addShiftInfo()) {
			StringUtils.addShiftAddition(stack, tooltip);
		}
	}

	public boolean addShiftInfo() {
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		return StringUtils.translate(getUnlocalizedName(itemstack));
	}
}

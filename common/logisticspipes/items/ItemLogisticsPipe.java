/**
 * Copyright (c) Krapht, 2011
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.items;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.apache.logging.log4j.Level;

/**
 * A logistics pipe Item
 */
public class ItemLogisticsPipe extends LogisticsItem {

	@SideOnly(Side.CLIENT)
	private IIconProvider iconProvider;
	private int pipeIconIndex;
	private int newPipeIconIndex;
	private int newPipeRenderList = -1;

	public ItemLogisticsPipe() {
		super();
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		return StringUtils.translate(getUnlocalizedName(itemstack));
	}

	/**
	 * Adds all keys from the translation file in the format:
	 * item.className.tip([0-9]*) Tips start from 1 and increment. Sparse rows
	 * should be left empty (ie empty line must still have a key present) Shift
	 * shows full tooltip, without it you just get the first line.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flags) {
		StringUtils.addShiftAddition(stack, list);
	}

	@Override
	//TODO use own pipe handling
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int sideI, float par8, float par9, float par10) {
		int side = sideI;
		Block block = LogisticsPipes.LogisticsPipeBlock;

		int i = x;
		int j = y;
		int k = z;

		Block worldBlock = world.getBlock(i, j, k);

		if (worldBlock == Blocks.snow) {
			side = 1;
		} else if (worldBlock != Blocks.vine && worldBlock != Blocks.tallgrass && worldBlock != Blocks.deadbush && (worldBlock == null || !worldBlock.isReplaceable(world, i, j, k))) {
			if (side == 0) {
				j--;
			}
			if (side == 1) {
				j++;
			}
			if (side == 2) {
				k--;
			}
			if (side == 3) {
				k++;
			}
			if (side == 4) {
				i--;
			}
			if (side == 5) {
				i++;
			}
		}

		if (itemstack.stackSize == 0) {
			return false;
		}

		if (world.canPlaceEntityOnSide(block, i, j, k, false, side, entityplayer, itemstack)) {
			CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.createPipe(this);

			if (pipe == null) {
				LogisticsPipes.log.log(Level.WARN, "Pipe failed to create during placement at {0},{1},{2}", new Object[] { i, j, k });
				return true;
			}

			if (LogisticsBlockGenericPipe.placePipe(pipe, world, i, j, k, block, 0)) {
				block.onBlockPlacedBy(world, i, j, k, entityplayer, itemstack);

				itemstack.stackSize--;
			}

			return true;
		} else {
			return false;
		}
	}

	@SideOnly(Side.CLIENT)
	public void setPipesIcons(IIconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}

	public void setPipeIconIndex(int index, int newIndex) {
		pipeIconIndex = index;
		newPipeIconIndex = newIndex;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		if (iconProvider != null) { // invalid pipes won't have this set
			return iconProvider.getIcon(pipeIconIndex);
		} else {
			return null;
		}
	}

	public int getNewPipeIconIndex() {
		return newPipeIconIndex;
	}

	public int getNewPipeRenderList() {
		return newPipeRenderList;
	}

	public void setNewPipeRenderList(int list) {
		if (newPipeRenderList != -1) {
			throw new UnsupportedOperationException("Can't reset this");
		}
		newPipeRenderList = list;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		// NOOP
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}
}

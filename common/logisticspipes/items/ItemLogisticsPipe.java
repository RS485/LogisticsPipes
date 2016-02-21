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
import logisticspipes.utils.UtilWorld;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.apache.logging.log4j.Level;

/**
 * A logistics pipe Item
 */
public class ItemLogisticsPipe extends LogisticsItem {

	private int pipeIconIndex;
	private int newPipeIconIndex;
	private int newPipeRenderList = -1;
	private UtilWorld utilWorld;

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
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flags) {
		StringUtils.addShiftAddition(stack, list);
	}

	@Override
	//TODO use own pipe handling
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		int side = sideI;
		Block block = LogisticsPipes.LogisticsPipeBlock;

		int i = x;
		int j = y;
		int k = z;

		Block worldBlock = utilWorld.getBlock(pos, world);

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

		if (stack.stackSize == 0) {
			return false;
		}

		if (world.canBlockBePlaced(block, i, j, k, false, side, player, stack)) {
			CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.createPipe(this);

			if (pipe == null) {
				LogisticsPipes.log.log(Level.WARN, "Pipe failed to create during placement at {0},{1},{2}", new Object[]{i, j, k});
				return true;
			}

			if (LogisticsBlockGenericPipe.placePipe(pipe, world, i, j, k, block, 0)) {
				block.onBlockPlacedBy(world, i, j, k, player, stack);

				stack.stackSize--;
			}

			return true;
		} else {
			return false;
		}
	}

	public void setPipeIconIndex(int index, int newIndex) {
		pipeIconIndex = index;
		newPipeIconIndex = newIndex;
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
}
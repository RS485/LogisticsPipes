package logisticspipes.waila.providers;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity_BuildCraft;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataProvider;

public class SolidBlockProvider implements IWailaDataProvider {

	@Override
	public ItemStack getWailaStack(World world, EntityPlayer player,
			TileEntity entity, Block block, MovingObjectPosition mop,
			IWailaConfigHandler config) {
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, World world,
			EntityPlayer player, TileEntity entity, Block block,
			MovingObjectPosition mop, List<String> currenttip,
			IWailaConfigHandler config) {
		if (entity instanceof LogisticsPowerJunctionTileEntity_BuildCraft) {
			final LogisticsPowerJunctionTileEntity_BuildCraft junctionEntity = (LogisticsPowerJunctionTileEntity_BuildCraft) entity;
			currenttip.add(junctionEntity.getPowerLevel() + "/"
					+ junctionEntity.MAX_STORAGE + " LP");
		}
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, World world,
			EntityPlayer player, TileEntity entity, Block block,
			MovingObjectPosition mop, List<String> currenttip,
			IWailaConfigHandler config) {
		return currenttip;
	}

}

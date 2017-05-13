package logisticspipes.proxy.toolWrench;

import logisticspipes.proxy.DontLoadProxy;
import logisticspipes.proxy.interfaces.IToolWrenchProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.tools.IToolWrench;

public class ToolWrenchProxy implements IToolWrenchProxy {

	public ToolWrenchProxy() {
		try {
			IToolWrench.class.getName();
		} catch (Throwable e) {
			throw new DontLoadProxy();
		}
	}

	@Override
	public boolean isWrenchEquipped(EntityPlayer entityplayer) {
		return (entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null) && (entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof IToolWrench);
	}

	@Override
	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z) {
		if ((entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null) && (entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof IToolWrench)) {
			return ((IToolWrench) entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem()).canWrench(entityplayer, EnumHand.MAIN_HAND, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), new RayTraceResult(new Vec3d(x, y, z), EnumFacing.UP, new BlockPos(x, y, z)));
		}
		return false;
	}

	@Override
	public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z) {
		if ((entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null) && (entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof IToolWrench)) {
			((IToolWrench) entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem()).wrenchUsed(entityplayer, EnumHand.MAIN_HAND, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), new RayTraceResult(new Vec3d(x, y, z), EnumFacing.UP, new BlockPos(x, y, z)));
		}
	}

	@Override
	public boolean isWrench(Item item) {
		return item instanceof IToolWrench;
	}
}

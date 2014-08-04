package logisticspipes.proxy.toolWrench;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import logisticspipes.proxy.DontLoadProxy;
import logisticspipes.proxy.interfaces.IToolWrenchProxy;

public class ToolWrenchProxy implements IToolWrenchProxy {
	
	public ToolWrenchProxy() {
		try {
			IToolWrench.class.getName();
		} catch(Throwable e) {
			throw new DontLoadProxy();
		}
	}
	
	public boolean isWrenchEquipped(EntityPlayer entityplayer) {
		return (entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench);
	}

	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z) {
		if ((entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))
			return ((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).canWrench(entityplayer, x, y, z);
		return false;
	}

	public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z) {
		if ((entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))
			((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);
	}

	@Override
	public boolean isWrench(Item item) {
		return item instanceof IToolWrench;
	}
}

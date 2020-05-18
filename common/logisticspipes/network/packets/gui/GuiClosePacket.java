package logisticspipes.network.packets.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class GuiClosePacket extends CoordinatesPacket {

	public GuiClosePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		// always mark the GUI origin's chunk dirty - something may have changed in the GUI
		getTileAs(player.world, TileEntity.class).markDirty();
	}

	@Override
	public ModernPacket template() {
		return new GuiClosePacket(getId());
	}
}

package logisticspipes.network.guis.block;

import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.gui.GuiPowerJunction;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

public class PowerJunctionGui extends CoordinatesGuiProvider {

	public PowerJunctionGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsPowerJunctionTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsPowerJunctionTileEntity.class);
		if (tile == null) {
			return null;
		}
		return new GuiPowerJunction(player, tile);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsPowerJunctionTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsPowerJunctionTileEntity.class);
		if (tile == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player, null, tile);
		dummy.addNormalSlotsForPlayerInventory(8, 80);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new PowerJunctionGui(getId());
	}
}

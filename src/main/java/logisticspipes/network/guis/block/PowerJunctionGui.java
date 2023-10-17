package logisticspipes.network.guis.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.gui.GuiPowerJunction;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;

@StaticResolve
public class PowerJunctionGui extends CoordinatesGuiProvider {

	public PowerJunctionGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		return new GuiPowerJunction(player, getTileAs(player.world, LogisticsPowerJunctionTileEntity.class));
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player, null, getTileAs(player.world, LogisticsPowerJunctionTileEntity.class));
		dummy.addNormalSlotsForPlayerInventory(8, 80);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new PowerJunctionGui(getId());
	}
}

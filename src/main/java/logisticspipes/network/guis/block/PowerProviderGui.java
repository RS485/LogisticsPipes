package logisticspipes.network.guis.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.gui.GuiPowerProvider;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;

@StaticResolve
public class PowerProviderGui extends CoordinatesGuiProvider {

	public PowerProviderGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		return new GuiPowerProvider(player, getTileAs(player.world, LogisticsPowerProviderTileEntity.class));
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player, null, getTileAs(player.world, LogisticsPowerProviderTileEntity.class));
		dummy.addNormalSlotsForPlayerInventory(8, 80);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new PowerProviderGui(getId());
	}
}

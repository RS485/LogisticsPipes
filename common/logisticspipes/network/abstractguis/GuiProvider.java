package logisticspipes.network.abstractguis;

import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;

import lombok.Getter;

import logisticspipes.network.NewGuiHandler;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class GuiProvider {

	@Getter
	private final int id;

	public GuiProvider(int id) {
		this.id = id;
	}

	public void writeData(LPDataOutput output) {}

	public void readData(LPDataInput input) {}

	/**
	 * @return LogisticsBaseGuiScreen
	 */
	public abstract Object getClientGui(PlayerEntity player);

	public abstract Container getContainer(PlayerEntity player);

	public abstract GuiProvider template();

	public final void open(PlayerEntity player) {
		// if (player instanceof FakePlayer) return;
		NewGuiHandler.openGui(this, player);
	}
}

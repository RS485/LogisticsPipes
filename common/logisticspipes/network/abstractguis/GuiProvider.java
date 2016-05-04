package logisticspipes.network.abstractguis;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.util.FakePlayer;

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

	public void writeData(LPDataOutput output) throws IOException {}

	public void readData(LPDataInput input) throws IOException {}

	/**
	 * @return LogisticsBaseGuiScreen
	 */
	public abstract Object getClientGui(EntityPlayer player);

	public abstract Container getContainer(EntityPlayer player);

	public abstract GuiProvider template();

	public final void open(EntityPlayer player) {
		if (player instanceof FakePlayer) return;
		NewGuiHandler.openGui(this, player);
	}
}

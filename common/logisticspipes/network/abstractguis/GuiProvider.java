package logisticspipes.network.abstractguis;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.NewGuiHandler;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

@Accessors(chain=true)
public abstract class GuiProvider {
	
	@Getter
	private final int id;
	
	public GuiProvider(int id) {
		this.id = id;
	}
	
	public void writeData(LPDataOutputStream data) throws IOException {}

	public void readData(LPDataInputStream data) throws IOException {}
	
	/**
	 * @return LogisticsBaseGuiScreen
	 */
	public abstract Object getClientGui(EntityPlayer player);
	public abstract Container getContainer(EntityPlayer player);
	
	public abstract GuiProvider template();
	
	public final void open(EntityPlayer player) {
		NewGuiHandler.openGui(this, player);
	}
}

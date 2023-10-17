package logisticspipes.network.abstractguis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public abstract class PopupGuiProvider extends GuiProvider {

	public PopupGuiProvider(int id) {
		super(id);
	}

	@Override
	public final Container getContainer(EntityPlayer player) {
		return null;
	}
}

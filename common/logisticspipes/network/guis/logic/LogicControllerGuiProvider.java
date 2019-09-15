package logisticspipes.network.guis.logic;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import logisticspipes.logic.LogicController;
import logisticspipes.logic.gui.LogicLayoutGui;
import logisticspipes.logic.interfaces.ILogicControllerTile;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;

@StaticResolve
public class LogicControllerGuiProvider extends CoordinatesGuiProvider {

	public LogicControllerGuiProvider(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		BlockEntity pipe = this.getTile(player.getEntityWorld(), BlockEntity.class);
		if (pipe instanceof ILogicControllerTile) {
			return new LogicLayoutGui(((ILogicControllerTile) pipe).getLogicController(), player);
		}
		return null;
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		BlockEntity pipe = this.getTile(player.getEntityWorld(), BlockEntity.class);
		if (pipe instanceof ILogicControllerTile) {
			LogicController controller = ((ILogicControllerTile) pipe).getLogicController();
			DummyContainer dummy = new DummyContainer(player.inventory, null);
			dummy.addNormalSlotsForPlayerInventory(50, 190);
			return dummy;
		}
		return null;
	}

	@Override
	public GuiProvider template() {
		return new LogicControllerGuiProvider(getId());
	}
}

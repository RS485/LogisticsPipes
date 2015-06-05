package logisticspipes.network.guis.module.inpipe;

import java.io.IOException;

import logisticspipes.gui.modules.GuiCCBasedQuickSort;
import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CCBasedQuickSortSlot extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private int timeOut;

	public CCBasedQuickSortSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleCCBasedQuickSort module = this.getLogisticsModule(player.getEntityWorld(), ModuleCCBasedQuickSort.class);
		if (module == null) {
			return null;
		}
		module.setTimeout(timeOut);
		return new GuiCCBasedQuickSort(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleCCBasedQuickSort module = this.getLogisticsModule(player.getEntityWorld(), ModuleCCBasedQuickSort.class);
		if (module == null) {
			return null;
		}
		return new DummyContainer(player.inventory, null);
	}

	@Override
	public GuiProvider template() {
		return new CCBasedQuickSortSlot(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(timeOut);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		timeOut = data.readInt();
	}
}

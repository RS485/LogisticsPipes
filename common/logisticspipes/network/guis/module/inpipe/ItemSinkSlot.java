package logisticspipes.network.guis.module.inpipe;

import java.io.IOException;
import java.util.BitSet;

import logisticspipes.gui.modules.GuiItemSink;
import logisticspipes.modules.ModuleItemSink;
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
public class ItemSinkSlot extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private boolean isDefaultRoute;

	@Getter
	@Setter
	private boolean hasFuzzyUpgrade;

	@Getter
	@Setter
	private BitSet ignoreData;

	@Getter
	@Setter
	private BitSet ignoreNBT;

	public ItemSinkSlot(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(isDefaultRoute);
		data.writeBoolean(hasFuzzyUpgrade);
		data.writeBitSet(ignoreData);
		data.writeBitSet(ignoreNBT);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		isDefaultRoute = data.readBoolean();
		hasFuzzyUpgrade = data.readBoolean();
		ignoreData = data.readBitSet();
		ignoreNBT = data.readBitSet();
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleItemSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleItemSink.class);
		if (module == null) {
			return null;
		}
		module.setDefaultRoute(isDefaultRoute);
		module.setIgnoreData(ignoreData);
		module.setIgnoreNBT(ignoreNBT);
		return new GuiItemSink(player.inventory, module, hasFuzzyUpgrade);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleItemSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleItemSink.class);
		if (module == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player.inventory, module.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}

		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ItemSinkSlot(getId());
	}
}

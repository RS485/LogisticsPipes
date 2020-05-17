package logisticspipes.network.guis.module.inpipe;

import java.util.BitSet;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.modules.GuiItemSink;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(isDefaultRoute);
		output.writeBoolean(hasFuzzyUpgrade);
		output.writeBitSet(ignoreData);
		output.writeBitSet(ignoreNBT);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		isDefaultRoute = input.readBoolean();
		hasFuzzyUpgrade = input.readBoolean();
		ignoreData = input.readBitSet();
		ignoreNBT = input.readBitSet();
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
		return SimpleFilterInventorySlot.getContainerFromFilterModule(this, player);
	}

	@Override
	public GuiProvider template() {
		return new ItemSinkSlot(getId());
	}
}

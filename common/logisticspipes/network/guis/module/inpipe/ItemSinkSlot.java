package logisticspipes.network.guis.module.inpipe;

import java.util.BitSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.gui.widget.module.ItemSinkContainer;
import network.rs485.logisticspipes.gui.widget.module.ItemSinkGui;
import network.rs485.logisticspipes.property.PropertyLayer;
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
	private BitSet fuzzyFlags;

	public ItemSinkSlot(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(isDefaultRoute);
		output.writeBoolean(hasFuzzyUpgrade);
		output.writeBitSet(fuzzyFlags);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		isDefaultRoute = input.readBoolean();
		hasFuzzyUpgrade = input.readBoolean();
		fuzzyFlags = input.readBitSet();
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleItemSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleItemSink.class);
		if (module == null) {
			return null;
		}
		module.setDefaultRoute(isDefaultRoute);
		module.setFuzzyFlags(fuzzyFlags);

		return ItemSinkGui.create(player.inventory, module, ItemStack.EMPTY, hasFuzzyUpgrade, false);
	}

	@Override
	public ItemSinkContainer getContainer(EntityPlayer player) {
		ModuleItemSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleItemSink.class);
		if (module == null) {
			return null;
		}
		return new ItemSinkContainer(
			player.inventory,
			module.filterInventory,
			module,
			new PropertyLayer(module.getProperties()),
			hasFuzzyUpgrade,
			ItemStack.EMPTY
		);
	}

	@Override
	public GuiProvider template() {
		return new ItemSinkSlot(getId());
	}
}

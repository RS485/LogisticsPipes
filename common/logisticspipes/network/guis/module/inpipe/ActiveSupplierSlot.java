package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleActiveSupplier.PatternMode;
import logisticspipes.modules.ModuleActiveSupplier.SupplyMode;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ActiveSupplierSlot extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private boolean patternUpgarde;

	@Getter
	@Setter
	private int[] slotArray;

	@Getter
	@Setter
	private boolean isLimit;

	@Getter
	@Setter
	private int mode;

	public ActiveSupplierSlot(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(patternUpgarde);
		output.writeIntArray(slotArray);
		output.writeBoolean(isLimit);
		output.writeInt(mode);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		patternUpgarde = input.readBoolean();
		slotArray = input.readIntArray();
		isLimit = input.readBoolean();
		mode = input.readInt();
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleActiveSupplier module = this.getLogisticsModule(player.getEntityWorld(), ModuleActiveSupplier.class);
		if (module == null) {
			return null;
		}
		module.isLimited.setValue(isLimit);
		if (patternUpgarde) {
			module.patternMode.setValue(PatternMode.values()[mode]);
		} else {
			module.requestMode.setValue(SupplyMode.values()[mode]);
		}
		module.slotAssignmentPattern.replaceContent(slotArray);
		return new GuiSupplierPipe(player.inventory, module.inventory, module, patternUpgarde, slotArray);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleActiveSupplier module = this.getLogisticsModule(player.getEntityWorld(), ModuleActiveSupplier.class);
		if (module == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player.inventory, module.inventory);
		dummy.addNormalSlotsForPlayerInventory(18, 97);

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				dummy.addDummySlot(column + row * 3, 72 + column * 18, 18 + row * 18);
			}
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ActiveSupplierSlot(getId());
	}
}

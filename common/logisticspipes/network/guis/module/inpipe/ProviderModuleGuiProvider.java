package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.inventory.ProviderMode;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ProviderModuleGuiProvider extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private boolean exclude;

	@Getter
	@Setter
	private int extractorMode;

	@Getter
	@Setter
	private boolean isActive;

	@Getter
	@Setter
	private EnumFacing sneakyOrientation;

	public ProviderModuleGuiProvider(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleProvider module = this.getLogisticsModule(player.getEntityWorld(), ModuleProvider.class);
		if (module == null) {
			return null;
		}
		module.isExclusionFilter.setValue(exclude);
		module.providerMode.setValue(ProviderMode.modeFromIntSafe(extractorMode));
		module.setSneakyDirection(sneakyOrientation);
		module.isActive.setValue(isActive);
		return new logisticspipes.gui.modules.GuiProvider(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleProvider module = this.getLogisticsModule(player.getEntityWorld(), ModuleProvider.class);
		if (module == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player.inventory, module.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(18, 97);

		int xOffset = 72;
		int yOffset = 18;

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);
			}
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ProviderModuleGuiProvider(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(exclude);
		output.writeInt(extractorMode);
		output.writeBoolean(isActive);
		output.writeFacing(sneakyOrientation);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		exclude = input.readBoolean();
		extractorMode = input.readInt();
		isActive = input.readBoolean();
		sneakyOrientation = input.readFacing();
	}
}

package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.gui.widget.module.ProviderContainer;
import network.rs485.logisticspipes.gui.widget.module.ProviderGui;
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
		return new ProviderGui(player.inventory, module, ItemStack.EMPTY);
	}

	@Override
	public ProviderContainer getContainer(EntityPlayer player) {
		ModuleProvider module = this.getLogisticsModule(player.getEntityWorld(), ModuleProvider.class);
		if (module == null) {
			return null;
		}
		return new ProviderContainer(player.inventory, module, ItemStack.EMPTY);
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

package logisticspipes.network.abstractguis;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.utils.DummyWorldProvider;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ModuleInHandGuiProvider extends GuiProvider {

	public ModuleInHandGuiProvider(int id) {
		super(id);
	}

	@Getter
	@Setter
	private int invSlot;

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeInt(invSlot);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		invSlot = input.readInt();
	}

	public final LogisticsModule getLogisticsModule(EntityPlayer player) {
		ItemStack item = player.inventory.mainInventory[invSlot];
		if (item == null) {
			return null;
		}
		LogisticsModule module = LogisticsPipes.ModuleItem.getModuleForItem(item, null, new DummyWorldProvider(player.getEntityWorld()), null);
		module.registerPosition(ModulePositionType.IN_HAND, invSlot);
		ItemModuleInformationManager.readInformation(item, module);
		return module;
	}
}

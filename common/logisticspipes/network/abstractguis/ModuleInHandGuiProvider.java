package logisticspipes.network.abstractguis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.utils.DummyWorldProvider;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ModuleInHandGuiProvider extends GuiProvider {

	private int invSlot;

	public ModuleInHandGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(invSlot);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		invSlot = input.readInt();
	}

	public final LogisticsModule getLogisticsModule(EntityPlayer player) {
		ItemStack item = player.inventory.mainInventory.get(invSlot);
		if (item.isEmpty()) {
			return null;
		}
		LogisticsModule module = ((ItemModule) item.getItem()).getModuleForItem(item, null, new DummyWorldProvider(player.getEntityWorld()), null);
		module.registerPosition(ModulePositionType.IN_HAND, invSlot);
		ItemModuleInformationManager.readInformation(item, module);
		return module;
	}

	public int getInvSlot() {
		return this.invSlot;
	}

	public ModuleInHandGuiProvider setInvSlot(int invSlot) {
		this.invSlot = invSlot;
		return this;
	}
}

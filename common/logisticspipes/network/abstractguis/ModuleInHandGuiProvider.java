package logisticspipes.network.abstractguis;

import java.io.IOException;

import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.utils.DummyWorldProvider;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class ModuleInHandGuiProvider extends GuiProvider {

	public ModuleInHandGuiProvider(int id) {
		super(id);
	}

	@Getter
	@Setter
	private int invSlot;

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(invSlot);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		invSlot = data.readInt();
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

package logisticspipes.network.guis.module.inhand;

import java.io.IOException;

import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CraftingModuleInHand extends ModuleInHandGuiProvider {

	@Getter
	@Setter
	private int[] amount;

	@Getter
	@Setter
	private boolean cleanupExclude;

	public CraftingModuleInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof ModuleCrafter)) {
			return null;
		}
		return new GuiCraftingPipe(player, ((ModuleCrafter) module).getDummyInventory(), ((ModuleCrafter) module), false, 0, amount, false, false, 0, cleanupExclude);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof ModuleCrafter)) {
			return null;
		}
		MainProxy.sendPacketToPlayer(((ModuleCrafter)dummy.getModule()).getCPipePacket(), player);
		dummy.setInventory(((ModuleCrafter) dummy.getModule()).getDummyInventory());
		dummy.addNormalSlotsForPlayerInventory(18, 97);
		//Input slots
		for (int l = 0; l < 9; l++) {
			dummy.addFuzzyDummySlot(l, 18 + l * 18, 18, ((ModuleCrafter) dummy.getModule()).fuzzyCraftingFlagArray[l]);
		}

		//Output slot
		dummy.addFuzzyDummySlot(9, 90, 64, ((ModuleCrafter) dummy.getModule()).outputFuzzyFlags);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new CraftingModuleInHand(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeIntegerArray(amount);
		data.writeBoolean(cleanupExclude);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		amount = data.readIntegerArray();
		cleanupExclude = data.readBoolean();
	}
}

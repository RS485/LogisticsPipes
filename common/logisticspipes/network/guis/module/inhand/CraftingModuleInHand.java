package logisticspipes.network.guis.module.inhand;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import logisticspipes.utils.StaticResolve;

@StaticResolve
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
		return new GuiCraftingPipe(player, ((ModuleCrafter) module).getDummyInventory(), ((ModuleCrafter) module), false, 0, amount, false, false, 0,
				cleanupExclude);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof ModuleCrafter)) {
			return null;
		}
		MainProxy.sendPacketToPlayer(((ModuleCrafter) dummy.getModule()).getCPipePacket(), player);
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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeIntArray(amount);
		output.writeBoolean(cleanupExclude);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		amount = input.readIntArray();
		cleanupExclude = input.readBoolean();
	}
}

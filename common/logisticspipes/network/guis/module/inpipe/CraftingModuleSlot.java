package logisticspipes.network.guis.module.inpipe;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class CraftingModuleSlot extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private boolean isAdvancedSat;

	@Getter
	@Setter
	private int liquidCrafter;

	@Getter
	@Setter
	private int[] amount;

	@Getter
	@Setter
	private boolean hasByproductExtractor;

	@Getter
	@Setter
	private boolean isFuzzy;

	@Getter
	@Setter
	private int cleanupSize;

	@Getter
	@Setter
	private boolean cleanupExclude;

	public CraftingModuleSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player.getEntityWorld(), ModuleCrafter.class);
		if (module == null) {
			return null;
		}
		return new GuiCraftingPipe(player, module.getDummyInventory(), module, isAdvancedSat, liquidCrafter, amount, hasByproductExtractor, isFuzzy,
				cleanupSize, cleanupExclude);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player.getEntityWorld(), ModuleCrafter.class);
		if (module == null) {
			return null;
		}
		MainProxy.sendPacketToPlayer(module.getCPipePacket(), player);
		DummyContainer dummy = new DummyContainer(player.inventory, module.getDummyInventory());
		dummy.addNormalSlotsForPlayerInventory(18, 97);
		//Input slots
		for (int l = 0; l < 9; l++) {
			dummy.addFuzzyDummySlot(l, 18 + l * 18, 18, module.fuzzyCraftingFlagArray[l]);
		}

		//Output slot
		dummy.addFuzzyDummySlot(9, 90, 64, module.outputFuzzyFlags);

		for (int i = 0; i < liquidCrafter; i++) {
			int liquidLeft = -(i * 40) - 40;
			dummy.addFluidSlot(i, module.getFluidInventory(), liquidLeft + 13, 42);
		}

		if (hasByproductExtractor) {
			dummy.addDummySlot(10, 197, 104);
		}

		for (int Y = 0; Y < cleanupSize; Y++) {
			for (int X = 0; X < 3; X++) {
				dummy.addDummySlot(Y * 3 + X, module.getCleanupInventory(), X * 18 - 57, Y * 18 + 13);
			}
		}

		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new CraftingModuleSlot(getId());
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeBoolean(isAdvancedSat);
		output.writeInt(liquidCrafter);
		output.writeIntArray(amount);
		output.writeBoolean(hasByproductExtractor);
		output.writeBoolean(isFuzzy);
		output.writeInt(cleanupSize);
		output.writeBoolean(cleanupExclude);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		isAdvancedSat = input.readBoolean();
		liquidCrafter = input.readInt();
		amount = input.readIntArray();
		hasByproductExtractor = input.readBoolean();
		isFuzzy = input.readBoolean();
		cleanupSize = input.readInt();
		cleanupExclude = input.readBoolean();
	}
}

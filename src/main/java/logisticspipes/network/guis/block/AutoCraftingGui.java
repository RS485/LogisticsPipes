package logisticspipes.network.guis.block;

import java.util.BitSet;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.gui.GuiLogisticsCraftingTable;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class AutoCraftingGui extends CoordinatesGuiProvider {

	boolean isFuzzy;
	BitSet fuzzyFlags = new BitSet(9 * 4);
	ItemIdentifier targetType;

	public AutoCraftingGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsCraftingTableTileEntity tile = getTileAs(player.world, LogisticsCraftingTableTileEntity.class);
		if (tile.isFuzzy()) {
			tile.fuzzyFlags.replaceWith(fuzzyFlags);
		}
		tile.targetType = targetType;
		return new GuiLogisticsCraftingTable(player, tile);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsCraftingTableTileEntity tile = getTileAs(player.world, LogisticsCraftingTableTileEntity.class);
		DummyContainer dummy = new DummyContainer(player, tile.matrix, tile);

		for (int X = 0; X < 3; X++) {
			for (int Y = 0; Y < 3; Y++) {
				dummy.addFuzzyDummySlot(Y * 3 + X, 35 + X * 18, 10 + Y * 18, tile.inputFuzzy(Y * 3 + X));
			}
		}
		dummy.addFuzzyUnmodifiableSlot(0, tile.resultInv, 125, 28, tile.outputFuzzy());
		for (int Y = 0; Y < 2; Y++) {
			for (int X = 0; X < 9; X++) {
				dummy.addNormalSlot(Y * 9 + X, tile.inv, 8 + X * 18, 80 + Y * 18);
			}
		}
		dummy.addNormalSlotsForPlayerInventory(8, 135);
		return dummy;
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeItemIdentifier(targetType);
		output.writeBoolean(isFuzzy);
		if (isFuzzy) {
			output.writeBitSet(fuzzyFlags);
		}
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		targetType = input.readItemIdentifier();
		if (input.readBoolean()) {
			fuzzyFlags = input.readBitSet();
		}
	}

	public AutoCraftingGui setCraftingTable(LogisticsCraftingTableTileEntity tile) {
		setTilePos(tile);
		if (tile.isFuzzy()) {
			isFuzzy = true;
			fuzzyFlags = tile.fuzzyFlags.copyValue(0, (9 * 4) - 1);
		}
		targetType = tile.targetType;
		return this;
	}

	@Override
	public GuiProvider template() {
		return new AutoCraftingGui(getId());
	}
}

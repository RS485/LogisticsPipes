package logisticspipes.network.guis.block;

import java.io.IOException;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.gui.GuiLogisticsCraftingTable;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.entity.player.EntityPlayer;

public class AutoCraftingGui extends CoordinatesGuiProvider {

	boolean isFuzzy;
	boolean[] ignore_dmg = new boolean[9];
	boolean[] ignore_nbt = new boolean[9];
	boolean[] use_od = new boolean[9];
	boolean[] use_category = new boolean[9];
	ItemIdentifier targetType;

	public AutoCraftingGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsCraftingTableTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsCraftingTableTileEntity.class);
		if (tile == null) {
			return null;
		}
		if (tile.isFuzzy()) {
			for (int i = 0; i < 9; i++) {
				tile.fuzzyFlags[i].ignore_dmg = ignore_dmg[i];
				tile.fuzzyFlags[i].ignore_nbt = ignore_nbt[i];
				tile.fuzzyFlags[i].use_od = use_od[i];
				tile.fuzzyFlags[i].use_category = use_category[i];
			}
		}
		tile.targetType = targetType;
		return new GuiLogisticsCraftingTable(player, tile);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsCraftingTableTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsCraftingTableTileEntity.class);
		if (tile == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player, tile.matrix, tile);

		for (int X = 0; X < 3; X++) {
			for (int Y = 0; Y < 3; Y++) {
				dummy.addFuzzyDummySlot(Y * 3 + X, 35 + X * 18, 10 + Y * 18, tile.fuzzyFlags[Y * 3 + X]);
			}
		}
		dummy.addFuzzyUnmodifiableSlot(0, tile.resultInv, 125, 28, tile.outputFuzzyFlags);
		for (int Y = 0; Y < 2; Y++) {
			for (int X = 0; X < 9; X++) {
				dummy.addNormalSlot(Y * 9 + X, tile.inv, 8 + X * 18, 80 + Y * 18);
			}
		}
		dummy.addNormalSlotsForPlayerInventory(8, 135);
		return dummy;
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeItemIdentifier(targetType);
		data.writeBoolean(isFuzzy);
		if (isFuzzy) {
			for (int i = 0; i < 9; i++) {
				data.writeBoolean(ignore_dmg[i]);
				data.writeBoolean(ignore_nbt[i]);
				data.writeBoolean(use_od[i]);
				data.writeBoolean(use_category[i]);
			}
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		targetType = data.readItemIdentifier();
		if (data.readBoolean()) {
			for (int i = 0; i < 9; i++) {
				ignore_dmg[i] = data.readBoolean();
				ignore_nbt[i] = data.readBoolean();
				use_od[i] = data.readBoolean();
				use_category[i] = data.readBoolean();
			}
		}
	}

	public AutoCraftingGui setCraftingTable(LogisticsCraftingTableTileEntity tile) {
		setTilePos(tile);
		if (tile.isFuzzy()) {
			isFuzzy = true;
			for (int i = 0; i < 9; i++) {
				ignore_dmg[i] = tile.fuzzyFlags[i].ignore_dmg;
				ignore_nbt[i] = tile.fuzzyFlags[i].ignore_nbt;
				use_od[i] = tile.fuzzyFlags[i].use_od;
				use_category[i] = tile.fuzzyFlags[i].use_category;
			}
		}
		targetType = tile.targetType;
		return this;
	}

	@Override
	public GuiProvider template() {
		return new AutoCraftingGui(getId());
	}
}

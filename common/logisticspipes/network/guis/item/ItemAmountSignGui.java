package logisticspipes.network.guis.item;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.ItemAmountSignCreationGui;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.ItemAmountPipeSign;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ItemAmountSignGui extends CoordinatesGuiProvider {

	@Getter
	@Setter
	private EnumFacing dir;

	public ItemAmountSignGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getTileAs(player.world, LogisticsTileGenericPipe.class);
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		return new ItemAmountSignCreationGui(player, (CoreRoutedPipe) pipe.pipe, dir);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getTileAs(player.world, LogisticsTileGenericPipe.class);
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		ItemAmountPipeSign sign = ((ItemAmountPipeSign) ((CoreRoutedPipe) pipe.pipe).getPipeSign(dir));
		Objects.requireNonNull(sign);
		DummyContainer dummy = new DummyContainer(player.inventory, sign.itemTypeInv);
		dummy.addDummySlot(0, 0, 0);
		dummy.addNormalSlotsForPlayerInventory(0, 0);
		return dummy;
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeFacing(dir);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		dir = input.readFacing();
	}

	@Override
	public GuiProvider template() {
		return new ItemAmountSignGui(getId());
	}
}

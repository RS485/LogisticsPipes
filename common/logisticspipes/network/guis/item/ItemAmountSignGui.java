package logisticspipes.network.guis.item;

import java.io.IOException;

import logisticspipes.gui.ItemAmountSignCreationGui;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.ItemAmountPipeSign;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ItemAmountSignGui extends CoordinatesGuiProvider {

	@Getter
	@Setter
	private ForgeDirection dir;

	public ItemAmountSignGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		return new ItemAmountSignCreationGui(player, (CoreRoutedPipe) pipe.pipe, dir);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		ItemAmountPipeSign sign = ((ItemAmountPipeSign) ((CoreRoutedPipe) pipe.pipe).getPipeSign(dir));
		DummyContainer dummy = new DummyContainer(player.inventory, sign.itemTypeInv);
		dummy.addDummySlot(0, 0, 0);
		dummy.addNormalSlotsForPlayerInventory(0, 0);
		return dummy;
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeForgeDirection(dir);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		dir = data.readForgeDirection();
	}

	@Override
	public GuiProvider template() {
		return new ItemAmountSignGui(getId());
	}
}

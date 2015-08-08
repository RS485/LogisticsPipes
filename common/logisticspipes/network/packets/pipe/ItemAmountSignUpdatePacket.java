package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.IPipeSign;
import logisticspipes.pipes.signs.ItemAmountPipeSign;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ItemAmountSignUpdatePacket extends Integer2CoordinatesPacket {

	@Getter
	@Setter
	private ItemIdentifierStack stack = null;

	public ItemAmountSignUpdatePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if (pipe == null || !pipe.isInitialized()) {
			return;
		}

		IPipeSign sign = ((CoreRoutedPipe) pipe.pipe).getPipeSign(ForgeDirection.getOrientation(getInteger()));
		if (sign == null) {
			return;
		}
		((ItemAmountPipeSign) sign).amount = getInteger2();
		((ItemAmountPipeSign) sign).itemTypeInv.setInventorySlotContents(0, stack);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		if (data.readBoolean()) {
			stack = data.readItemIdentifierStack();
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		if (stack == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			data.writeItemIdentifierStack(stack);
		}
	}

	@Override
	public ModernPacket template() {
		return new ItemAmountSignUpdatePacket(getId());
	}
}

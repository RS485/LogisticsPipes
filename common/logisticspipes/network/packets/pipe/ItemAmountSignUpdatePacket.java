package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Direction;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.signs.PipeSign;
import logisticspipes.pipes.signs.ItemAmountPipeSign;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemStack;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ItemAmountSignUpdatePacket extends Integer2CoordinatesPacket {

	@Getter
	@Setter
	private ItemStack stack = null;

	public ItemAmountSignUpdatePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if (pipe == null || !pipe.isInitialized()) {
			return;
		}

		PipeSign sign = ((CoreRoutedPipe) pipe.pipe).getPipeSign(Direction.getFront(getInteger()));
		if (sign == null) {
			return;
		}
		((ItemAmountPipeSign) sign).amount = getInteger2();
		((ItemAmountPipeSign) sign).itemTypeInv.setInventorySlotContents(0, stack);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		if (input.readBoolean()) {
			stack = input.readItemStack();
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		if (stack == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(true);
			output.writeItemStack(stack);
		}
	}

	@Override
	public ModernPacket template() {
		return new ItemAmountSignUpdatePacket(getId());
	}
}

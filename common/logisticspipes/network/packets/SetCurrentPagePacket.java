package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SetCurrentPagePacket extends ModernPacket {

	@Getter
	@Setter
	private NBTTagCompound nbt;

	@Getter
	@Setter
	private int slot;

	public SetCurrentPagePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ItemStack book = player.inventory.getStackInSlot(slot);
		book.setTagCompound(nbt);;
	}

	@Override
	public void readData(LPDataInput input) {
		nbt = input.readNBTTagCompound();
		slot = input.readInt();
		super.readData(input);
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeNBTTagCompound(nbt);
		output.writeInt(slot);
		super.writeData(output);
	}

	@Override
	public ModernPacket template() {
		return new SetCurrentPagePacket(getId());
	}
}

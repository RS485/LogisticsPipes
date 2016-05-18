package logisticspipes.network.packets.upgrade;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.SlotPacket;
import logisticspipes.pipes.upgrades.SneakyUpgradeConfig;
import logisticspipes.utils.gui.UpgradeSlot;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class SneakyUpgradeSidePacket extends SlotPacket {

	@Setter
	@Getter
	private ForgeDirection side;

	public SneakyUpgradeSidePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		UpgradeSlot slot = getSlot(player, UpgradeSlot.class);
		ItemStack stack = slot.getStack();
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setString(SneakyUpgradeConfig.SIDE_KEY, SneakyUpgradeConfig.Sides.getNameForDirection(side));
		slot.putStack(stack);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeForgeDirection(side);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		side = input.readForgeDirection();
	}

	@Override
	public ModernPacket template() {
		return new SneakyUpgradeSidePacket(getId());
	}
}

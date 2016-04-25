package logisticspipes.network.packets.upgrade;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.*;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.upgrades.ConnectionUpgradeConfig;
import logisticspipes.utils.gui.UpgradeSlot;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;

@Accessors(chain = true)
public class ToogleDisconnectionUpgradeSidePacket extends SlotPacket {

	@Getter
	@Setter
	private ForgeDirection side;

	public ToogleDisconnectionUpgradeSidePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		UpgradeSlot slot = getSlot(player, UpgradeSlot.class);
		ItemStack stack = slot.getStack();

		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}

		NBTTagCompound nbt = stack.getTagCompound();
		String sideName = ConnectionUpgradeConfig.Sides.getNameForDirection(side);
		nbt.setBoolean(sideName, !nbt.getBoolean(sideName));

		stack.setTagCompound(nbt);

		slot.putStack(stack);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeForgeDirection(side);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		side = data.readForgeDirection();
	}

	@Override
	public ModernPacket template() {
		return new ToogleDisconnectionUpgradeSidePacket(getId());
	}
}

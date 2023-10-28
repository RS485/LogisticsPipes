package logisticspipes.network.packets.pipe;

import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.property.PropertyHolder;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class PipePropertiesUpdate extends CoordinatesPacket {

	@Nonnull
	public NBTTagCompound tag = new NBTTagCompound();

	public PipePropertiesUpdate(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeNBTTagCompound(tag);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		tag = Objects.requireNonNull(input.readNBTTagCompound(), "read null NBT in PipePropertiesUpdate");
	}

	@Override
	public ModernPacket template() {
		return new PipePropertiesUpdate(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (!(tile.pipe instanceof PropertyHolder)) {
			return;
		}

		// sync updated properties
		tile.pipe.readFromNBT(tag);

		MainProxy.runOnServer(player.world, () -> () -> {
			// resync client; always
			MainProxy.sendPacketToPlayer(fromPropertyHolder((PropertyHolder) tile.pipe).setPacketPos(this), player);
		});
	}

	@Nonnull
	public static PipePropertiesUpdate fromPropertyHolder(PropertyHolder holder) {
		final PipePropertiesUpdate packet = PacketHandler.getPacket(PipePropertiesUpdate.class);
		PropertyHolder.writeToNBT(packet.tag, holder);
		return packet;
	}

}

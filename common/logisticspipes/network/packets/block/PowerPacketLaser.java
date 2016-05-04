package logisticspipes.network.packets.block;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class PowerPacketLaser extends CoordinatesPacket {

	public PowerPacketLaser(int id) {
		super(id);
	}

	@Getter
	@Setter
	private ForgeDirection dir;

	@Getter
	@Setter
	private int color;

	@Getter
	@Setter
	private boolean reverse;

	@Getter
	@Setter
	private boolean renderBall;

	@Getter
	@Setter
	private float length;

	@Getter
	@Setter
	private boolean remove = false;

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		length = input.readFloat();
		dir = input.readForgeDirection();
		color = input.readInt();
		reverse = input.readBoolean();
		renderBall = input.readBoolean();
		remove = input.readBoolean();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.getEntityWorld());
		if (remove) {
			tile.removeLaser(dir, getColor(), isRenderBall());
		} else {
			tile.addLaser(dir, getLength(), getColor(), isReverse(), isRenderBall());
		}
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeFloat(length);
		output.writeForgeDirection(dir);
		output.writeInt(color);
		output.writeBoolean(reverse);
		output.writeBoolean(renderBall);
		output.writeBoolean(remove);
	}

	@Override
	public ModernPacket template() {
		return new PowerPacketLaser(getId());
	}
}

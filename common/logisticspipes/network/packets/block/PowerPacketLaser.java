package logisticspipes.network.packets.block;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		length = data.readFloat();
		dir = data.readForgeDirection();
		color = data.readInt();
		reverse = data.readBoolean();
		renderBall = data.readBoolean();
		remove = data.readBoolean();
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeFloat(length);
		data.writeForgeDirection(dir);
		data.writeInt(color);
		data.writeBoolean(reverse);
		data.writeBoolean(renderBall);
		data.writeBoolean(remove);
	}

	@Override
	public ModernPacket template() {
		return new PowerPacketLaser(getId());
	}
}

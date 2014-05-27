package logisticspipes.network.packets.block;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.tuples.LPPosition;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

@Accessors(chain=true)
public class PowerPacketLaser extends ModernPacket {
	
	public PowerPacketLaser(int id) {
		super(id);
	}

	@Getter
	@Setter
	private LPPosition pos;
	
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
		length = data.readFloat();
		pos = data.readLPPosition();
		dir = data.readForgeDirection();
		color = data.readInt();
		reverse = data.readBoolean();
		renderBall = data.readBoolean();
		remove = data.readBoolean();
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity tile = pos.getTileEntity(MainProxy.getClientMainWorld());
		if(tile instanceof LogisticsTileGenericPipe) {
			if(remove) {
				((LogisticsTileGenericPipe)tile).removeLaser(dir, getColor(), isRenderBall());
			} else {
				((LogisticsTileGenericPipe)tile).addLaser(dir, getLength(), getColor(), isReverse(), isRenderBall());
			}
		}
	}
	
	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeFloat(length);
		data.writeLPPosition(pos);
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

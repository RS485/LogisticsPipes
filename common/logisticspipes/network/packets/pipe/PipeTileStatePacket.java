package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.interfaces.IClientState;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

public class PipeTileStatePacket extends CoordinatesPacket {

	@Setter
	private IClientState renderState;

	@Setter
	private IClientState coreState;

	@Setter
	private IClientState bcPluggableState;

	@Setter
	private IClientState pipe;

	@Getter
	private byte[] bytesRenderState;

	@Getter
	private byte[] bytesCoreState;

	@Getter
	private byte[] bytesBCPluggableState;

	@Getter
	private byte[] bytesPipe;

	public PipeTileStatePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if (pipe == null) {
			return;
		}
		try {
			pipe.renderState.readData(new LPDataInputStream(bytesRenderState));
			pipe.coreState.readData(new LPDataInputStream(bytesCoreState));
			pipe.bcPlugableState.readData(new LPDataInputStream(bytesBCPluggableState));
			pipe.afterStateUpdated();
			pipe.pipe.readData(new LPDataInputStream(bytesPipe));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ModernPacket template() {
		return new PipeTileStatePacket(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		LPDataOutputStream out = new LPDataOutputStream();
		renderState.writeData(out);
		byte[] bytes = out.toByteArray();
		data.writeInt(bytes.length);
		data.write(bytes);

		out = new LPDataOutputStream();
		coreState.writeData(out);
		bytes = out.toByteArray();
		data.writeInt(bytes.length);
		data.write(bytes);

		out = new LPDataOutputStream();
		bcPluggableState.writeData(out);
		bytes = out.toByteArray();
		data.writeInt(bytes.length);
		data.write(bytes);

		out = new LPDataOutputStream();
		pipe.writeData(out);
		bytes = out.toByteArray();
		data.writeInt(bytes.length);
		data.write(bytes);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		bytesRenderState = new byte[data.readInt()];
		data.read(bytesRenderState);
		bytesCoreState = new byte[data.readInt()];
		data.read(bytesCoreState);
		bytesBCPluggableState = new byte[data.readInt()];
		data.read(bytesBCPluggableState);
		bytesPipe = new byte[data.readInt()];
		data.read(bytesPipe);
	}
}

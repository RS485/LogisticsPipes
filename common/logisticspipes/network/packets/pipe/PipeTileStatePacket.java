package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.interfaces.IClientState;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.subproxies.IBCCoreState;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;

public class PipeTileStatePacket extends CoordinatesPacket {

	@Setter
	private IClientState renderState;

	@Setter
	private IClientState coreState;

	@Setter
	private IBCCoreState bcCoreState;

	@Setter
	private IClientState pipe;

	@Getter
	private byte[] bytesRenderState;
	
	@Getter
	private byte[] bytesCoreState;
	
	@Getter
	private byte[] bytesBCCoreState;
	
	@Getter
	private byte[] bytesPipe;
	
	public PipeTileStatePacket(int id) {
		super(id);
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.getEntityWorld());
		if(tile == null) return;
		try {
			tile.renderState.readData(new LPDataInputStream(bytesRenderState));
			tile.coreState.readData(new LPDataInputStream(bytesCoreState));
			tile.bcCoreState.readData(new LPDataInputStream(bytesBCCoreState));
			tile.afterStateUpdated();
			tile.pipe.readData(new LPDataInputStream(bytesPipe));
		} catch(IOException e) {
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
		bcCoreState.writeData(out);
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
		bytesBCCoreState = new byte[data.readInt()];
		data.read(bytesBCCoreState);
		bytesPipe = new byte[data.readInt()];
		data.read(bytesPipe);
	}
}

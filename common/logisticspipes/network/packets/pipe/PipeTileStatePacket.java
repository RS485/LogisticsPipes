package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.IClientState;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataIOWrapper;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
		LPDataIOWrapper.provideData(bytesRenderState, pipe.renderState::readData);
		LPDataIOWrapper.provideData(bytesCoreState, pipe.coreState::readData);
		LPDataIOWrapper.provideData(bytesBCPluggableState, pipe.bcPlugableState::readData);
		pipe.afterStateUpdated();
		LPDataIOWrapper.provideData(bytesPipe, pipe.pipe::readData);
	}

	@Override
	public ModernPacket template() {
		return new PipeTileStatePacket(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);

		IClientState[] clientStates = new IClientState[] { renderState, coreState, bcPluggableState, pipe };
		byte[][] clientStateBuffers = new byte[][] { bytesRenderState, bytesCoreState, bytesBCPluggableState, bytesPipe };
		for (int i = 0; i < clientStates.length; i++) {
			clientStateBuffers[i] = LPDataIOWrapper.collectData(clientStates[i]::writeData);
			output.writeByteArray(clientStateBuffers[i]);
		}
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);

		bytesRenderState = input.readByteArray();
		bytesCoreState = input.readByteArray();
		bytesBCPluggableState = input.readByteArray();
		bytesPipe = input.readByteArray();
	}
}

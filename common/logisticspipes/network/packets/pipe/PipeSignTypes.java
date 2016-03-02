package logisticspipes.network.packets.pipe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PipeSignTypes extends CoordinatesPacket {

	public PipeSignTypes(int id) {
		super(id);
	}

	@Getter
	@Setter
	private List<Integer> types;

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (pipe == null || !pipe.isInitialized()) {
			return;
		}
		((CoreRoutedPipe) pipe.pipe).handleSignPacket(types);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeList(types, DataOutputStream::writeInt);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		types = data.readList(DataInputStream::readInt);
	}

	@Override
	public ModernPacket template() {
		return new PipeSignTypes(getId());
	}
}

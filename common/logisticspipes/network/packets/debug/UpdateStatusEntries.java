package logisticspipes.network.packets.debug;

import java.io.IOException;
import java.util.List;

import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.debug.LogWindow;
import logisticspipes.pipes.basic.debug.StatusEntry;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class UpdateStatusEntries extends ModernPacket {

	@Getter
	@Setter
	public int windowID;

	@Getter
	@Setter
	public List<StatusEntry> status;

	public UpdateStatusEntries(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		windowID = data.readInt();
		status = data.readList(new IReadListObject<StatusEntry>() {

			@Override
			public StatusEntry readObject(LPDataInputStream data) throws IOException {
				StatusEntry status = new StatusEntry();
				status.name = data.readUTF();
				if (data.readBoolean()) {
					status.subEntry = data.readList(this);
				}
				return status;
			}
		});
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogWindow.getWindow(windowID).updateStatus(status);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(windowID);
		data.writeList(status, new IWriteListObject<StatusEntry>() {

			@Override
			public void writeObject(LPDataOutputStream data, StatusEntry entry) throws IOException {
				data.writeUTF(entry.name);
				data.writeBoolean(entry.subEntry != null);
				if (entry.subEntry != null) {
					data.writeList(entry.subEntry, this);
				}
			}
		});
	}

	@Override
	public ModernPacket template() {
		return new UpdateStatusEntries(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

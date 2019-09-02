package logisticspipes.network.packets.debug;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.debug.LogWindow;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
	public void readData(LPDataInput input) {
		windowID = input.readInt();
		status = input.readArrayList(new IReadListObject<StatusEntry>() {

			@Override
			public StatusEntry readObject(LPDataInput input) {
				StatusEntry status = new StatusEntry();
				status.name = input.readUTF();
				if (input.readBoolean()) {
					status.subEntry = input.readArrayList(this);
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
	public void writeData(LPDataOutput output) {
		output.writeInt(windowID);
		output.writeCollection(status, new IWriteListObject<StatusEntry>() {

			@Override
			public void writeObject(LPDataOutput output, StatusEntry entry) {
				output.writeUTF(entry.name);
				output.writeBoolean(entry.subEntry != null);
				if (entry.subEntry != null) {
					output.writeCollection(entry.subEntry, this);
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

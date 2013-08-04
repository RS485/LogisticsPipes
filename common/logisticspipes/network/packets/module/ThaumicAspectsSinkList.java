package logisticspipes.network.packets.module;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.NBTCoordinatesPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.gui.DummyModuleContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class ThaumicAspectsSinkList extends NBTCoordinatesPacket {

	@Getter
	@Setter
	private int slot;
	
	public ThaumicAspectsSinkList(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ThaumicAspectsSinkList(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(slot < 0) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleThaumicAspectSink) {
					ModuleThaumicAspectSink module = (ModuleThaumicAspectSink) dummy.getModule();
					module.readFromNBT(getTag());
				}
			}
			return;
		}
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) return;
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot()) instanceof ModuleThaumicAspectSink) {
			((ModuleThaumicAspectSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot())).readFromNBT(getTag());
			((ModuleThaumicAspectSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot())).aspectListChanged();
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(slot);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		slot = data.readInt();
	}
}


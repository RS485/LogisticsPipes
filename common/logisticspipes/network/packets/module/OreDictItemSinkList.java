package logisticspipes.network.packets.module;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.NBTCoordinatesPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyModuleContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class OreDictItemSinkList extends NBTCoordinatesPacket {

	@Getter
	@Setter
	private int slot;

	public OreDictItemSinkList(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new OreDictItemSinkList(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(MainProxy.isClient(player.worldObj)) {
			final TileGenericPipe pipe = this.getPipe(player.worldObj);
			if(pipe == null) {
				return;
			}
			if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot()) instanceof ModuleOreDictItemSink) {
				((ModuleOreDictItemSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot())).readFromNBT(getTag());
			}
		} else {
			if(getSlot() < 0) {
				if(player.openContainer instanceof DummyModuleContainer) {
					DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
					if(dummy.getModule() instanceof ModuleOreDictItemSink) {
						((ModuleOreDictItemSink)dummy.getModule()).readFromNBT(getTag());
						return;
					}
				}
			}
			final TileGenericPipe pipe = this.getPipe(player.worldObj);
			if(pipe == null) {
				return;
			}
			if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot()) instanceof ModuleOreDictItemSink) {
				((ModuleOreDictItemSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot())).readFromNBT(getTag());
				((ModuleOreDictItemSink)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot())).OreListChanged();
			}
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

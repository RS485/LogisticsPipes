package logisticspipes.network.packets.module;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class ModuleInventory extends InventoryCoordinatesPacket {

	@Getter
	@Setter
	private int slot;
	
	public ModuleInventory(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ModuleInventory(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot()) instanceof IModuleInventoryReceive) {
			IModuleInventoryReceive module = (IModuleInventoryReceive) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot());
			module.handleInvContent(getIdentList());
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(getSlot());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		setSlot(data.readInt());
	}
}


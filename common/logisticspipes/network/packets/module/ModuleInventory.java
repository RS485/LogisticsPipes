package logisticspipes.network.packets.module;

import java.io.IOException;

import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

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
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot()) instanceof IModuleInventoryReceive) {
			IModuleInventoryReceive module = (IModuleInventoryReceive) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getSlot());
			module.handleInvContent(getIdentList());
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(getSlot());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		setSlot(data.readInt());
	}
}


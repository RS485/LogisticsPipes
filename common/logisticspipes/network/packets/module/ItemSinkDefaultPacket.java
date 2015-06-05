package logisticspipes.network.packets.module;

import java.io.IOException;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ItemSinkDefaultPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private boolean isDefault;

	public ItemSinkDefaultPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(isDefault);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		isDefault = data.readBoolean();
	}

	@Override
	public ModernPacket template() {
		return new ItemSinkDefaultPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleItemSink module = this.getLogisticsModule(player, ModuleItemSink.class);
		if (module == null) {
			return;
		}
		module.setDefaultRoute(isDefault);
	}
}

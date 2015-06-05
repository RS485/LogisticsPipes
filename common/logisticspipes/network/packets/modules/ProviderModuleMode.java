package logisticspipes.network.packets.modules;

import java.io.IOException;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ProviderModuleMode extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int mode;

	public ProviderModuleMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderModuleMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final ModuleProvider module = this.getLogisticsModule(player, ModuleProvider.class);
		if (module == null) {
			return;
		}
		module.setExtractionMode(mode);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(mode);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		mode = data.readInt();
	}
}

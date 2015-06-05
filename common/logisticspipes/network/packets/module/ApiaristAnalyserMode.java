package logisticspipes.network.packets.module;

import java.io.IOException;

import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ApiaristAnalyserMode extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int mode;

	public ApiaristAnalyserMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ApiaristAnalyserMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleApiaristAnalyser module = this.getLogisticsModule(player, ModuleApiaristAnalyser.class);
		if (module == null) {
			return;
		}
		module.setExtractMode(mode);
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

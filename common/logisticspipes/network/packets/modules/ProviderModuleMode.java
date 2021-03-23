package logisticspipes.network.packets.modules;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.inventory.ProviderMode;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
		module.providerMode.setValue(ProviderMode.modeFromIntSafe(mode));
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(mode);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		mode = input.readInt();
	}
}

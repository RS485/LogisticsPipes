package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ItemSinkDefaultPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private boolean isDefault;

	public ItemSinkDefaultPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(isDefault);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		isDefault = input.readBoolean();
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

package logisticspipes.network.packets.pipe;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.IntegerModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

/**
 * Created by davboecki on 20/06/2019.
 * All rights reserved.
 */
@StaticResolve
public class CraftingPipeSetSatellitePacket extends IntegerModuleCoordinatesPacket {

	@Getter
	@Setter
	private UUID pipeID;

	public CraftingPipeSetSatellitePacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		pipeID = input.readUUID();
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeUUID(pipeID);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		if (getInteger() == 0) {
			module.setSatelliteUUID(getPipeID());
		} else if (getInteger() >= 10 && getInteger() < 20) {
			module.setAdvancedSatelliteUUID(getInteger() - 10, getPipeID());
		} else if (getInteger() == 100) {
			module.setFluidSatelliteUUID(getPipeID());
		} else if (getInteger() >= 110 && getInteger() <= 120) {
			module.setAdvancedFluidSatelliteUUID(getInteger() - 110, getPipeID());
		}
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeSetSatellitePacket(getId());
	}
}

package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.items.ItemUpgrade;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class CraftingPipeUpdatePacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int[] amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];

	@Getter
	@Setter
	private int[] liquidSatelliteIdArray = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];

	@Getter
	@Setter
	private int liquidSatelliteId = 0;

	@Getter
	@Setter
	private int satelliteId = 0;

	@Getter
	@Setter
	private int[] advancedSatelliteIdArray = new int[9];

	@Getter
	@Setter
	private int priority = 0;

	public CraftingPipeUpdatePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.handleCraftingUpdatePacket(this);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeIntArray(amount);
		output.writeIntArray(liquidSatelliteIdArray);
		output.writeInt(liquidSatelliteId);
		output.writeInt(satelliteId);
		output.writeIntArray(advancedSatelliteIdArray);
		output.writeInt(priority);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		amount = input.readIntArray();
		liquidSatelliteIdArray = input.readIntArray();
		liquidSatelliteId = input.readInt();
		satelliteId = input.readInt();
		advancedSatelliteIdArray = input.readIntArray();
		priority = input.readInt();
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeUpdatePacket(getId());
	}
}

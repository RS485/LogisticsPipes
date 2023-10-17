package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.items.ItemUpgrade;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class CraftingPipeUpdatePacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int[] amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];

	@Getter
	@Setter
	private String[] liquidSatelliteNameArray = new String[ItemUpgrade.MAX_LIQUID_CRAFTER];

	@Getter
	@Setter
	private String liquidSatelliteName = "";

	@Getter
	@Setter
	private String satelliteName = "";

	@Getter
	@Setter
	private String[] advancedSatelliteNameArray = new String[9];

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
		output.writeUTFArray(liquidSatelliteNameArray);
		output.writeUTF(liquidSatelliteName);
		output.writeUTF(satelliteName);
		output.writeUTFArray(advancedSatelliteNameArray);
		output.writeInt(priority);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		amount = input.readIntArray();
		liquidSatelliteNameArray = input.readUTFArray();
		liquidSatelliteName = input.readUTF();
		satelliteName = input.readUTF();
		advancedSatelliteNameArray = input.readUTFArray();
		priority = input.readInt();
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipeUpdatePacket(getId());
	}
}

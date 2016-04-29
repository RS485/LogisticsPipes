package logisticspipes.network.packets.block;

import java.io.IOException;

import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.experimental.Accessors;

public class PowerProviderLevel extends CoordinatesPacket {

	private Double aDouble;

	public PowerProviderLevel(int id) {
		super(id);
	}

	public double getDouble() {
		return aDouble;
	}

	public PowerProviderLevel setDouble(double d) {
		aDouble = d;
		return this;
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		setDouble(data.readDouble());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeDouble(getDouble());
	}

	@Override
	public ModernPacket template() {
		return new PowerProviderLevel(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsPowerProviderTileEntity tile = this.getTile(player.worldObj, LogisticsPowerProviderTileEntity.class);
		if (tile != null) {
			tile.handlePowerPacket(getDouble());
		}
	}
}

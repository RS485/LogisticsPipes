package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain = true)
public class GuiReopenPacket extends CoordinatesPacket {
	
	@Getter
	@Setter
	private int guiID;
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(getGuiID());
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		guiID = data.readInt();
	}
	
	public GuiReopenPacket(int id) {
		super(id);
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		player.openGui(LogisticsPipes.instance, getGuiID(), player.worldObj, getPosX(), getPosY(), getPosZ());
	}
	
	@Override
	public ModernPacket template() {
		return new GuiReopenPacket(getId());
	}
	
}


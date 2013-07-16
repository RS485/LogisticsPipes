package logisticspipes.network.packets.debuggui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.ticks.DebugGuiTickHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

@Accessors(chain = true)
public class DebugExpandPart extends ModernPacket {
	
	public DebugExpandPart(int id) {
		super(id);
	}
	
	@Getter
	@Setter
	public Integer[] tree;
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		int size = data.readInt();
		tree = new Integer[size];
		for(int i = 0; i < size; i++) {
			tree[i] = data.readInt();
		}
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		try {
			DebugGuiTickHandler.instance().expandGuiAt(tree, (Player) player);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(tree.length);
		for(int i = 0; i < tree.length; i++) {
			data.writeInt(tree[i]);
		}
	}
	
	@Override
	public ModernPacket template() {
		return new DebugExpandPart(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}


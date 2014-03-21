package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.Player;

@Accessors(chain=true)
public class PlayerListRequest extends ModernPacket {

	public PlayerListRequest(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new PlayerListRequest(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		List<String> list = new LinkedList<String>();
		File root = DimensionManager.getCurrentSaveRootDirectory();
		if(root == null) return;
		if(!root.exists()) return;
		File players = new File(root, "players");
		if(!players.exists()) return;
		for(String names:players.list()) {
			if(names.endsWith(".dat") && new File(players, names).isFile()) {
				list.add(names.substring(0, names.length() - 4));
			}
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PlayerList.class).setStringList(list), (Player) player);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {}

	@Override
	public void writeData(DataOutputStream data) throws IOException {}
}


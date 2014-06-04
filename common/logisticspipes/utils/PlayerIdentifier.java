package logisticspipes.utils;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

@Accessors(chain=true)
public class PlayerIdentifier {
	private static Map<String, PlayerIdentifier> idBased = new HashMap<String, PlayerIdentifier>();
	private static Map<String, PlayerIdentifier> nameBased = new HashMap<String, PlayerIdentifier>();
	
	private PlayerIdentifier(String username, String id) {
		this.username = username;
		this.id = id;
	}
	
	@Setter
	public String username;
	public final String id;
	
	public static PlayerIdentifier get(EntityPlayer player) {
		return get(player.getGameProfile().getName(), player.getGameProfile().getId());
	}
	
	public static PlayerIdentifier get(String username, String id) {
		if(idBased.containsKey(id)) {
			return idBased.get(id).setUsername(username);
		}
		if(nameBased.containsKey(username)) {
			return nameBased.get(username);
		}
		if(id != null) {
			PlayerIdentifier ident = new PlayerIdentifier(username, id);
			idBased.put(id, ident);
			return ident;
		}
		if(username == null) throw new NullPointerException();
		PlayerIdentifier ident = new PlayerIdentifier(username, id);
		nameBased.put(username, ident);
		return ident;
	}
	
	public void writeToNBT(NBTTagCompound nbt, String prefix) {
		if(id != null) {
			nbt.setString(prefix + "_id", id);
		}
		nbt.setString(prefix + "_name", username);
	}
	
	public static PlayerIdentifier readFromNBT(NBTTagCompound nbt, String prefix) {
		String id = null;
		if(nbt.hasKey(prefix + "_id")) {
			id = nbt.getString(prefix + "_id");
		}
		String username = nbt.getString(prefix + "_name");
		return get(username, id);
	}
	
	public static PlayerIdentifier convertFromUsername(String name) {
		return get(name, null);
	}

	public String getAsString() {
		return id != null ? id : username;
	}
}

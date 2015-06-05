package logisticspipes.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PlayerIdentifier {

	private static Map<UUID, PlayerIdentifier> idBased = new HashMap<UUID, PlayerIdentifier>();
	private static Map<String, PlayerIdentifier> nameBased = new HashMap<String, PlayerIdentifier>();

	private PlayerIdentifier(String username, UUID id) {
		this.username = username;
		this.id = id;
	}

	@Getter
	private String username;
	@Getter
	private UUID id;

	public static PlayerIdentifier get(EntityPlayer player) {
		return PlayerIdentifier.get(player.getGameProfile().getName(), player.getGameProfile().getId());
	}

	public static PlayerIdentifier get(String username, UUID id) {
		if (PlayerIdentifier.idBased.containsKey(id)) {
			return PlayerIdentifier.idBased.get(id).setUsername(username);
		}
		if (PlayerIdentifier.nameBased.containsKey(username)) {
			return PlayerIdentifier.nameBased.get(username);
		}
		if (id != null) {
			PlayerIdentifier ident = new PlayerIdentifier(username, id);
			PlayerIdentifier.idBased.put(id, ident);
			return ident;
		}
		if (username == null) {
			throw new NullPointerException();
		}
		PlayerIdentifier ident = new PlayerIdentifier(username, id);
		PlayerIdentifier.nameBased.put(username, ident);
		return ident;
	}

	public void writeToNBT(NBTTagCompound nbt, String prefix) {
		if (id != null) {
			nbt.setString(prefix + "_id", id.toString());
		}
		nbt.setString(prefix + "_name", username);
	}

	public static PlayerIdentifier readFromNBT(NBTTagCompound nbt, String prefix) {
		UUID id = null;
		if (nbt.hasKey(prefix + "_id")) {
			String tmp = nbt.getString(prefix + "_id");
			try {
				id = UUID.fromString(tmp);
			} catch (Exception e) {}
		}
		String username = nbt.getString(prefix + "_name");
		return PlayerIdentifier.get(username, id);
	}

	public static PlayerIdentifier convertFromUsername(String name) {
		return PlayerIdentifier.get(name, null);
	}

	public String getAsString() {
		return id != null ? id.toString() : username;
	}

	public PlayerIdentifier setUsername(String string) {
		if (username == null || username.isEmpty()) {
			username = string;
		}
		return this;
	}

	public PlayerIdentifier setID(UUID uuid) {
		if (id == null) {
			id = uuid;
			PlayerIdentifier.idBased.put(id, this);
		}
		return this;
	}
}

package logisticspipes.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PlayerIdentifier {

	private static Map<UUID, PlayerIdentifier> idBased = new HashMap<>();
	private static Map<String, PlayerIdentifier> nameBased = new HashMap<>();

	private PlayerIdentifier(String username, UUID id) {
		this.username = username;
		this.id = id;
	}

	private String username;
	private UUID id;

	public UUID getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

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
			} catch (Exception ignored) {}
		}
		String username = nbt.getString(prefix + "_name");
		return PlayerIdentifier.get(username, id);
	}

	public static PlayerIdentifier convertFromUsername(String name) {
		return PlayerIdentifier.get(name, null);
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

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlayerIdentifier) {
			return id.equals(((PlayerIdentifier) obj).id);
		} else {
			return false;
		}

	}
}

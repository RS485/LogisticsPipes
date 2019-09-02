package logisticspipes.routing.channels;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import logisticspipes.utils.PlayerIdentifier;

@Data
@AllArgsConstructor
public class ChannelInformation {

	public enum AccessRights {
		PRIVATE,
		SECURED,
		PUBLIC
	}

	private String name;
	@NonNull
	private final UUID channelIdentifier;
	@NonNull
	private PlayerIdentifier owner;
	@NonNull
	private AccessRights rights;
	private UUID responsibleSecurityID;

	public ChannelInformation(NBTTagCompound nbt) {
		name = nbt.getString("name");
		channelIdentifier = UUID.fromString(nbt.getString("channelIdentifier"));
		owner = PlayerIdentifier.readFromNBT(nbt, "owner");
		rights = AccessRights.values()[nbt.getInteger("rights")];
		if (nbt.hasKey("responsibleSecurityID")) {
			responsibleSecurityID = UUID.fromString(nbt.getString("responsibleSecurityID"));
		} else {
			responsibleSecurityID = null;
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("name", name);
		compound.setString("channelIdentifier", channelIdentifier.toString());
		owner.writeToNBT(compound, "owner");
		compound.setInteger("rights", rights.ordinal());
		if (responsibleSecurityID != null) {
			compound.setString("responsibleSecurityID", responsibleSecurityID.toString());
		}

		return compound;
	}
}

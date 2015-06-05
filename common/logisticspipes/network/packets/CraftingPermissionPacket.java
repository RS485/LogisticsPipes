package logisticspipes.network.packets;

import java.io.IOException;
import java.util.EnumSet;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.recipes.CraftingDependency;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CraftingPermissionPacket extends ModernPacket {

	@Getter
	@Setter
	EnumSet<CraftingDependency> enumSet;

	public CraftingPermissionPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		enumSet = EnumSet.noneOf(CraftingDependency.class);
		for (CraftingDependency type : CraftingDependency.values()) {
			if (data.readBoolean()) {
				enumSet.add(type);
			}
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		SimpleServiceLocator.craftingPermissionManager.clientSidePermission = enumSet;
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		for (CraftingDependency type : CraftingDependency.values()) {
			data.writeBoolean(enumSet.contains(type));
		}
	}

	@Override
	public ModernPacket template() {
		return new CraftingPermissionPacket(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

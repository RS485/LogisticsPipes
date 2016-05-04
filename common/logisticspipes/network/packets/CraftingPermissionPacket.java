package logisticspipes.network.packets;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.recipes.CraftingDependency;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class CraftingPermissionPacket extends ModernPacket {

	@Getter
	@Setter
	EnumSet<CraftingDependency> enumSet;

	public CraftingPermissionPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		enumSet = EnumSet.noneOf(CraftingDependency.class);
		for (CraftingDependency type : CraftingDependency.values()) {
			if (input.readBoolean()) {
				enumSet.add(type);
			}
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		SimpleServiceLocator.craftingPermissionManager.clientSidePermission = enumSet;
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		for (CraftingDependency type : CraftingDependency.values()) {
			output.writeBoolean(enumSet.contains(type));
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

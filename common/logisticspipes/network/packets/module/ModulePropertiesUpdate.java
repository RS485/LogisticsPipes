package logisticspipes.network.packets.module;

import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.property.PropertyHolder;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ModulePropertiesUpdate extends ModuleCoordinatesPacket {

	@Nonnull
	public NBTTagCompound tag = new NBTTagCompound();

	public ModulePropertiesUpdate(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeNBTTagCompound(tag);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		tag = Objects.requireNonNull(input.readNBTTagCompound(), "read null NBT in ModulePropertiesUpdate");
	}

	@Override
	public ModernPacket template() {
		return new ModulePropertiesUpdate(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsModule module = this.getLogisticsModule(player, LogisticsModule.class);
		if (module == null) {
			return;
		}

		// sync updated properties
		module.readFromNBT(tag);

		if (!getType().isInWorld() && player.openContainer instanceof ContainerPlayer) {
			// FIXME: saveInformation & markDirty on module property change? should be called only once
			// sync slot in player inventory and mark player inventory dirty
			ItemModuleInformationManager.saveInformation(player.inventory.mainInventory.get(getPositionInt()), module);
			player.inventory.markDirty();
		}

		MainProxy.runOnServer(player.world, () -> () -> {
			// resync client; always
			MainProxy.sendPacketToPlayer(fromPropertyHolder(module).setModulePos(module), player);
		});
	}

	@Nonnull
	public static ModuleCoordinatesPacket fromPropertyHolder(PropertyHolder holder) {
		final ModulePropertiesUpdate packet = PacketHandler.getPacket(ModulePropertiesUpdate.class);
		holder.writeToNBT(packet.tag);
		return packet;
	}

}

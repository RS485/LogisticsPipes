package logisticspipes.network.packets.module;

import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.module.PropertyModule;
import network.rs485.logisticspipes.property.PropertyLayer;
import network.rs485.logisticspipes.property.UtilKt;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class PropertyModuleUpdate extends ModuleCoordinatesPacket {

	@Nonnull
	public NBTTagCompound tag = new NBTTagCompound();

	public PropertyModuleUpdate(int id) {
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
		tag = Objects.requireNonNull(input.readNBTTagCompound(), "read null NBT in PropertyModuleUpdate");
	}

	@Override
	public ModernPacket template() {
		return new PropertyModuleUpdate(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final ModuleProvider module = this.getLogisticsModule(player, ModuleProvider.class);
		if (module == null) {
			return;
		}

		// sync updated properties
		PropertyModule.DefaultImpls.readFromNBT(module, tag);

		if (!getType().isInWorld() && player.openContainer instanceof ContainerPlayer) {
			// FIXME: saveInformation & markDirty on module property change? should be called only once
			// sync slot in player inventory and mark player inventory dirty
			ItemModuleInformationManager.saveInformation(player.inventory.mainInventory.get(getPositionInt()), module);
			player.inventory.markDirty();
		}

		MainProxy.runOnServer(player.world, () -> () -> {
			// resync client
			MainProxy.sendPacketToPlayer(fromModule(module).setModulePos(module), player);
		});
	}

	@Nonnull
	public static ModuleCoordinatesPacket fromModule(PropertyModule module) {
		final PropertyModuleUpdate packet = PacketHandler.getPacket(PropertyModuleUpdate.class);
		PropertyModule.DefaultImpls.writeToNBT(module, packet.tag);
		return packet;
	}

	@Nonnull
	public static PropertyModuleUpdate fromLayer(PropertyLayer propertyLayer) {
		final PropertyModuleUpdate packet = PacketHandler.getPacket(PropertyModuleUpdate.class);
		UtilKt.writeToNBT(propertyLayer.changedProperties(), packet.tag);
		return packet;
	}

}

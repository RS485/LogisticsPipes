package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.LPConstants;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyModuleContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class ModuleCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private ModulePositionType type;

	@Getter
	@Setter
	private int positionInt;

	public ModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(type != null);
		if (type != null) {
			data.writeEnum(type);
			data.writeInt(positionInt);
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		if (data.readBoolean()) {
			type = data.readEnum(ModulePositionType.class);
			positionInt = data.readInt();
		}
	}

	public ModuleCoordinatesPacket setModulePos(LogisticsModule module) {
		type = module.getSlot();
		positionInt = module.getPositionInt();
		setPosX(module.getX());
		setPosY(module.getY());
		setPosZ(module.getZ());
		return this;
	}

	public ModuleCoordinatesPacket setPacketPos(ModuleCoordinatesPacket packet) {
		type = packet.type;
		positionInt = packet.positionInt;
		super.setPacketPos(packet);
		return this;
	}

	private boolean moduleBased = false;

	@SuppressWarnings("unchecked")
	public <T> T getLogisticsModule(EntityPlayer player, Class<T> clazz) {
		LogisticsModule module = null;
		if (type == ModulePositionType.IN_PIPE) {
			moduleBased = true;
			LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
			moduleBased = false;
			if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
				targetNotFound("Couldn't find " + clazz.getName() + ", pipe didn't exsist");
				return null;
			}
			module = ((CoreRoutedPipe) pipe.pipe).getLogisticsModule();
		} else if (type == ModulePositionType.IN_HAND) {
			if (MainProxy.isServer(player.getEntityWorld())) {
				if (player.openContainer instanceof DummyModuleContainer) {
					DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
					module = dummy.getModule();
				} else {
					targetNotFound("Couldn't find " + clazz.getName() + ", container wasn't a DummyModule Container");
					return null;
				}
			} else {
				module = MainProxy.proxy.getModuleFromGui();
				if (module == null) {
					targetNotFound("Couldn't find " + clazz.getName() + ", GUI didn't provide the module");
					return null;
				}
			}
		} else {
			moduleBased = true;
			LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
			moduleBased = false;
			if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
				targetNotFound("Couldn't find " + clazz.getName() + ", pipe didn't exsist");
				return null;
			} else if (!pipe.isInitialized()) {
				return null;
			}
			if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
				targetNotFound("Couldn't find " + clazz.getName() + ", pipe wasn't a chassi pipe");
				return null;
			}
			module = ((PipeLogisticsChassi) pipe.pipe).getLogisticsModule().getSubModule(positionInt);
		}
		if (module != null) {
			if (!(clazz.isAssignableFrom(module.getClass()))) {
				targetNotFound("Couldn't find " + clazz.getName() + ", found " + module.getClass());
				return null;
			}
		} else {
			targetNotFound("Couldn't find " + clazz.getName());
		}
		return (T) module;
	}

	@Override
	public <T> T getTile(World world, Class<T> clazz) {
		if (LPConstants.DEBUG && !moduleBased && type != null) {
			new Exception("ModulePacket was asked for a pipe").printStackTrace();
		}
		return super.getTile(world, clazz);
	}
}

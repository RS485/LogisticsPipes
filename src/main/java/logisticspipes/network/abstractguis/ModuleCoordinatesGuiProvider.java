package logisticspipes.network.abstractguis;

import net.minecraft.world.World;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ModuleCoordinatesGuiProvider extends CoordinatesGuiProvider {

	private ModulePositionType slot;
	private int positionInt;

	public ModuleCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeEnum(slot);
		output.writeInt(positionInt);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		slot = input.readEnum(ModulePositionType.class);
		positionInt = input.readInt();
	}

	@SuppressWarnings("unchecked")
	public <T> T getLogisticsModule(World world, Class<T> clazz) {
		LogisticsTileGenericPipe pipe = getTileAs(world, LogisticsTileGenericPipe.class);
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			if (LogisticsPipes.isDEBUG() && pipe.isInitialized()) {
				LogisticsPipes.log.fatal(toString());
				new RuntimeException("Couldn't find " + clazz.getName() + ", pipe didn't exsist").printStackTrace();
			}
			return null;
		}
		LogisticsModule module;
		if (slot == ModulePositionType.IN_PIPE) {
			module = ((CoreRoutedPipe) pipe.pipe).getLogisticsModule();
		} else if (slot == ModulePositionType.IN_HAND) {
			throw new UnsupportedOperationException("NO IN_HAND FOR THIS PACKET TYPE");
		} else {
			if (!(pipe.pipe instanceof PipeLogisticsChassis)) {
				if (LogisticsPipes.isDEBUG()) {
					LogisticsPipes.log.fatal(toString());
					new RuntimeException("Couldn't find " + clazz.getName() + ", pipe wasn't a chassi pipe").printStackTrace();
				}
				return null;
			}
			module = ((PipeLogisticsChassis) pipe.pipe).getSubModule(positionInt);
		}
		if (module != null) {
			if (!(clazz.isAssignableFrom(module.getClass()))) {
				if (LogisticsPipes.isDEBUG()) {
					LogisticsPipes.log.fatal(toString());
					new RuntimeException("Couldn't find " + clazz.getName() + ", found " + module.getClass()).printStackTrace();
				}
				return null;
			}
		} else {
			if (LogisticsPipes.isDEBUG()) {
				LogisticsPipes.log.fatal(toString());
				new RuntimeException("Couldn't find " + clazz.getName()).printStackTrace();
			}
		}
		return (T) module;
	}

	public ModulePositionType getSlot() {
		return this.slot;
	}

	public ModuleCoordinatesGuiProvider setSlot(ModulePositionType slot) {
		this.slot = slot;
		return this;
	}

	public int getPositionInt() {
		return this.positionInt;
	}

	public ModuleCoordinatesGuiProvider setPositionInt(int positionInt) {
		this.positionInt = positionInt;
		return this;
	}
}

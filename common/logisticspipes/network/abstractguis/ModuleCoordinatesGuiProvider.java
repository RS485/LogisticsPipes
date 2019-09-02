package logisticspipes.network.abstractguis;

import net.minecraft.world.World;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ModuleCoordinatesGuiProvider extends CoordinatesGuiProvider {

	@Getter
	@Setter
	private ModulePositionType slot;
	@Getter
	@Setter
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
		LogisticsTileGenericPipe pipe = getPipe(world);
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			if (LPConstants.DEBUG && (pipe == null || pipe.isInitialized())) {
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
			if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
				if (LPConstants.DEBUG) {
					LogisticsPipes.log.fatal(toString());
					new RuntimeException("Couldn't find " + clazz.getName() + ", pipe wasn't a chassi pipe").printStackTrace();
				}
				return null;
			}
			module = ((PipeLogisticsChassi) pipe.pipe).getLogisticsModule().getSubModule(positionInt);
		}
		if (module != null) {
			if (!(clazz.isAssignableFrom(module.getClass()))) {
				if (LPConstants.DEBUG) {
					LogisticsPipes.log.fatal(toString());
					new RuntimeException("Couldn't find " + clazz.getName() + ", found " + module.getClass()).printStackTrace();
				}
				return null;
			}
		} else {
			if (LPConstants.DEBUG) {
				LogisticsPipes.log.fatal(toString());
				new RuntimeException("Couldn't find " + clazz.getName()).printStackTrace();
			}
		}
		return (T) module;
	}
}

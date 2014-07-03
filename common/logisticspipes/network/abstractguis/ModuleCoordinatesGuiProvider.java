package logisticspipes.network.abstractguis;

import java.io.IOException;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.World;

@Accessors(chain=true)
public abstract class ModuleCoordinatesGuiProvider extends CoordinatesGuiProvider {
	
	public ModuleCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Getter
	@Setter
	private ModulePositionType slot;

	@Getter
	@Setter
	private int positionInt;
	
	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeEnum(slot);
		data.writeInt(positionInt);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		slot = data.readEnum(ModulePositionType.class);
		positionInt = data.readInt();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getLogisticsModule(World world, Class<T> clazz) {
		LogisticsTileGenericPipe pipe = this.getPipe(world);
		if(pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			if(LogisticsPipes.DEBUG) {
				LogisticsPipes.log.fatal(this.toString());
				new RuntimeException("Couldn't find " + clazz.getName() + ", pipe didn't exsist").printStackTrace();
			}
			return null;
		}
		LogisticsModule module = null;
		if(this.slot == ModulePositionType.IN_PIPE) {
			module = ((CoreRoutedPipe)pipe.pipe).getLogisticsModule();
		} else if (this.slot == ModulePositionType.IN_HAND) {
			throw new UnsupportedOperationException("NO IN_HAND FOR THIS PACKET TYPE");
		} else {
			if(!(pipe.pipe instanceof PipeLogisticsChassi)) {
				if(LogisticsPipes.DEBUG) {
					LogisticsPipes.log.fatal(this.toString());
					new RuntimeException("Couldn't find " + clazz.getName() + ", pipe wasn't a chassi pipe").printStackTrace();
				}
				return null;
			}
			module = ((PipeLogisticsChassi)pipe.pipe).getLogisticsModule().getSubModule(positionInt);
		}
		if(module != null) {
			if(!(clazz.isAssignableFrom(module.getClass()))) {
				if(LogisticsPipes.DEBUG) {
					LogisticsPipes.log.fatal(this.toString());
					new RuntimeException("Couldn't find " + clazz.getName() + ", found " + module.getClass()).printStackTrace();
				}
				return null;
			}
		} else {
			if(LogisticsPipes.DEBUG) {
				LogisticsPipes.log.fatal(this.toString());
				new RuntimeException("Couldn't find " + clazz.getName()).printStackTrace();
			}
		}
		return (T) module;
	}
}

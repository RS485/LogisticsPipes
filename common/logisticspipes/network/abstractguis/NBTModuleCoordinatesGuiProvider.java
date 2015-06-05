package logisticspipes.network.abstractguis;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class NBTModuleCoordinatesGuiProvider extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private NBTTagCompound nbt;

	public NBTModuleCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeNBTTagCompound(nbt);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		nbt = data.readNBTTagCompound();
	}
}

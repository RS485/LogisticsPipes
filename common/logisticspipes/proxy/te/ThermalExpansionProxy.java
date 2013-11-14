package logisticspipes.proxy.te;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import net.minecraft.tileentity.TileEntity;
import thermalexpansion.block.tesseract.TileTesseract;
import cofh.api.transport.IEnderAttuned;

public class ThermalExpansionProxy implements IThermalExpansionProxy {

	@Override
	public boolean isTesseract(TileEntity tile) {
		return tile instanceof TileTesseract;
	}

	@Override
	public List<TileEntity> getConnectedTesseracts(TileEntity tile) {
		List<IEnderAttuned> interfaces = ((TileTesseract)tile).getValidItemOutputs();
	    List<TileEntity> validOutputs = new LinkedList<TileEntity>();
	    for (IEnderAttuned object: interfaces) {
	    	if(object instanceof TileEntity) {
	    		validOutputs.add((TileEntity) object);
	    	}
	    }
	    return validOutputs;
	}

	@Override
	public boolean isTE() {
		return true;
	}
}

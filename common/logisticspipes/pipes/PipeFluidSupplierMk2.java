package logisticspipes.pipes;

import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.logic.LogicFluidSupplierMk2;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.FluidIdentifier;

public class PipeFluidSupplierMk2 extends FluidRoutedPipe implements IRequestFluid {

	private boolean _lastRequestFailed = false;

	public PipeFluidSupplierMk2(int ItemID) {
		super(new LogicFluidSupplierMk2(), ItemID);
		((LogicFluidSupplierMk2) logic)._power = this;
	}

	@Override
	public void sendFailed(FluidIdentifier value1, Integer value2) {
		if(logic instanceof IRequireReliableFluidTransport) {
			((IRequireReliableFluidTransport)logic).liquidLost(value1, value2);
		}
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return true;
	}

	@Override
	public boolean canInsertToTanks() {
		return true;
	}

	/* TRIGGER INTERFACE */
	public boolean isRequestFailed(){
		return _lastRequestFailed;
	}

	public void setRequestFailed(boolean value){
		_lastRequestFailed = value;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUIDSUPPLIER_MK2_TEXTURE;
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}
}

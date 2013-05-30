package logisticspipes.pipes;

import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.interfaces.routing.IRequireReliableLiquidTransport;
import logisticspipes.logic.LogicLiquidSupplierMk2;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.LiquidIdentifier;

public class PipeLiquidSupplierMk2 extends LiquidRoutedPipe implements IRequestLiquid {

	private boolean _lastRequestFailed = false;

	public PipeLiquidSupplierMk2(int ItemID) {
		super(new LogicLiquidSupplierMk2(), ItemID);
		((LogicLiquidSupplierMk2) logic)._power = this;
	}

	@Override
	public void sendFailed(LiquidIdentifier value1, Integer value2) {
		if(logic instanceof IRequireReliableLiquidTransport) {
			((IRequireReliableLiquidTransport)logic).itemLost(value1, value2);
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

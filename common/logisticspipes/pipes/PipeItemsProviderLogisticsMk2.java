package logisticspipes.pipes;

import logisticspipes.config.Textures;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;

public class PipeItemsProviderLogisticsMk2 extends PipeItemsProviderLogistics {

	public PipeItemsProviderLogisticsMk2(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		if(SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
			return Textures.LOGISTICSPIPE_PROVIDERMK2_TEXTURE;
		} else {
			return Textures.LOGISTICSPIPE_PROVIDERMK2_TEXTURE_DIS;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (!_orderManager.hasOrders() || worldObj.getWorldTime() % 6 != 0) return;
		for(int i = 0; i < 16; i++) {
			if(_orderManager.hasOrders()) {
				if(!useEnergy(2)) return;
				Pair<ItemIdentifierStack,IRequestItems> order = _orderManager.getNextRequest();
				int sent = sendItem(order.getValue1().getItem(), order.getValue1().stackSize, order.getValue2().getRouter().getId());
				if (sent > 0){
					_orderManager.sendSuccessfull(sent);
				}
				else {
					_orderManager.sendFailed();
				}
				if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
					break;
				}
			}
		}
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}
}

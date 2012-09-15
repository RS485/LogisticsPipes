package logisticspipes.pipes;

import logisticspipes.config.Textures;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;

public class PipeItemsProviderLogisticsMk2 extends PipeItemsProviderLogistics {

	public PipeItemsProviderLogisticsMk2(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_PROVIDERMK2_TEXTURE;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (!_orderManager.hasOrders() || worldObj.getWorldTime() % 6 != 0) return;
		for(int i = 0; i < 16; i++) {
			if(_orderManager.hasOrders()) {
				Pair<ItemIdentifierStack,IRequestItems> order = _orderManager.getNextRequest();
				int sent = sendItem(order.getValue1().getItem(), order.getValue1().stackSize, order.getValue2().getRouter().getId());
				if (sent > 0){
					_orderManager.sendSuccessfull(sent);
				}
				else {
					_orderManager.sendFailed();
				}
			}
		}
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}
}

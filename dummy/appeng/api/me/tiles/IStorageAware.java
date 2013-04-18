package appeng.api.me.tiles;

import appeng.api.IItemList;

public interface IStorageAware {
	
	void onNetworkInventoryChange( IItemList iss );
	
}

package appeng.api.me.tiles;

import java.util.List;

import appeng.api.me.util.IMEInventoryHandler;

public interface ICellContainer {
	
	List<IMEInventoryHandler> getCellArray();
	
	int getPriority();
	
}

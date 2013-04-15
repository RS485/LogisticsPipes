package appeng.api.me.tiles;

import appeng.api.me.util.IMEInventoryHandler;

/**
 * Both useless and incredibly useful, maybe...
 */
public interface ICellProvider
{
	public int usePowerForAddition( int items, int multiplier );
	
    public IMEInventoryHandler provideCell();
    
}

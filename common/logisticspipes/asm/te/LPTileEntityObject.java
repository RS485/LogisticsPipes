package logisticspipes.asm.te;

import java.util.ArrayList;
import java.util.List;

public class LPTileEntityObject {

	public List<ITileEntityChangeListener> changeListeners = new ArrayList<ITileEntityChangeListener>();
	public long initialised = 0;
	
}

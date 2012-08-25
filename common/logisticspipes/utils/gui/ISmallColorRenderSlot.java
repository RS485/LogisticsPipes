package logisticspipes.utils.gui;

public abstract class ISmallColorRenderSlot implements IRenderSlot {
	public abstract int getColor();
	
	public abstract boolean drawColor();

	public int getSize() {
		return 8;
	}
}

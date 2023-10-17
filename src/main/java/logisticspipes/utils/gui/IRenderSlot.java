package logisticspipes.utils.gui;

public interface IRenderSlot {

	void mouseClicked(int button);

	boolean drawSlotBackground();

	int getXPos();

	int getYPos();

	String getToolTipText();

	boolean displayToolTip();

	int getSize();
}

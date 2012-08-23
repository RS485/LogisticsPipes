package net.minecraft.src.krapht.gui;

public interface IRenderSlot {
	public void mouseClicked(int button);

	public boolean drawSlotBackground();

	public int getXPos();

	public int getYPos();
	
	public String getToolTipText();
	
	public boolean displayToolTip();
	
	public abstract int getSize();
}

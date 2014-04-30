package logisticspipes.utils.gui.extention;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class GuiExtention {
	@Getter
	@Setter(value=AccessLevel.PACKAGE)
	private boolean extending;
	private int currentW = getMinimumWidth();
	private int currentH = getMinimumHeight();
	@Getter
	private int currentXPos=0;
	@Getter
	private int currentYPos=0;
	
	@Getter
	@Setter(value=AccessLevel.PACKAGE)
	private boolean disabled;

	public abstract int getFinalWidth();
	public abstract int getFinalHeight();
	public abstract void renderForground(int left, int top);

	public final void update(int xPos, int yPos) {
		currentXPos = xPos;
		currentYPos = yPos;
		if(extending) {
			if(currentH < getFinalHeight()) {
				currentH += 4;
			} else {
				currentH = getFinalHeight();
			}
			if(currentW < getFinalWidth()) {
				currentW += 2;
			} else {
				currentW = getFinalWidth();
			}
		} else {
			if(currentH > getMinimumHeight()) {
				currentH -= 4;
			} else {
				currentH = getMinimumHeight();
			}
			if(currentW > getMinimumWidth()) {
				currentW -= 2;
			} else {
				currentW = getMinimumWidth();
			}
		}
	}

	public int getMinimumWidth() {
		return 23;
	}
	
	public int getMinimumHeight() {
		return 26;
	}
	
	public int getCurrentWidth() {
		return currentW;
	}

	public int getCurrentHeight() {
		return currentH;
	}
	
	public boolean isFullyExtended() {
		return currentW == getFinalWidth() && currentH == getFinalHeight();
	}
	
	public void handleMouseOverAt(int xPos, int yPos) {}
}

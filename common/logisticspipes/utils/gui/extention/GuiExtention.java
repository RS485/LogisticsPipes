package logisticspipes.utils.gui.extention;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class GuiExtention {

	@Getter
	@Setter(value = AccessLevel.PACKAGE)
	private boolean extending;
	private int currentW = getMinimumWidth();
	private int currentH = getMinimumHeight();
	@Getter
	private int currentXPos = 0;
	@Getter
	private int currentYPos = 0;
	private int targetYPos = 0;
	private boolean init = true;
	private long lastTime;
	private List<Integer> slotList = new ArrayList<>();
	private List<Integer> buttonList = new ArrayList<>();

	public abstract int getFinalWidth();

	public abstract int getFinalHeight();

	public abstract void renderForground(int left, int top);

	public final void update(int xPos, int yPos) {
		double d = 0;
		if(lastTime > 0) {
			long time = System.currentTimeMillis();
			d = (time - lastTime) * 1.0 / 5;
			if (d < 1) {
				d = 0;
			} else {
				lastTime = time;
			}
			d = Math.min(d, 20.0);
		} else {
			lastTime = System.currentTimeMillis();
		}
		currentXPos = xPos;
		if (yPos > currentYPos + 1 * d && !init) {
			currentYPos += 2 * d;
		} else if (yPos < currentYPos - 1 * d && !init) {
			currentYPos -= 2 * d;
		} else {
			currentYPos = yPos;
		}
		targetYPos = yPos;
		init = false;
		if (extending) {
			if (currentH < getFinalHeight()) {
				currentH += 4 * d;
			} else {
				currentH = getFinalHeight();
			}
			if (currentW < getFinalWidth()) {
				currentW += 2 * d;
			} else {
				currentW = getFinalWidth();
			}
		} else {
			if (currentH > getMinimumHeight()) {
				currentH -= 4 * d;
			} else {
				currentH = getMinimumHeight();
			}
			if (currentW > getMinimumWidth()) {
				currentW -= 2 * d;
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
		return currentW == getFinalWidth() && currentH == getFinalHeight() && targetYPos == currentYPos;
	}

	public boolean isFullyRetracted() {
		return currentW == getMinimumWidth() && currentH == getMinimumHeight() && targetYPos == currentYPos;
	}

	public void handleMouseOverAt(int xPos, int yPos) {}

	public void registerSlot(int id) {
		slotList.add(id);
	}

	public boolean renderSlot(int id) {
		return slotList.contains(id);
	}

	public boolean renderSelectSlot(int id) {
		return slotList.contains(id);
	}

	public void registerButton(int id) {
		buttonList.add(id);
	}

	public boolean renderButton(int id) {
		return buttonList.contains(id);
	}
}

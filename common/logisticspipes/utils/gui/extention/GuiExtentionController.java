package logisticspipes.utils.gui.extention;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;

import lombok.Setter;

import logisticspipes.utils.gui.GuiGraphics;

public class GuiExtentionController {

	public enum GuiSide {
		LEFT,
		RIGHT
	}

	private final List<GuiExtention> extentions = new ArrayList<>();
	private final List<GuiExtention> extentionsToRemove = new ArrayList<>();
	@Setter
	private int maxBottom;
	private GuiExtention currentlyExtended = null;
	private Map<Slot, Integer> slotMap = new HashMap<>();
	private Map<GuiButton, Integer> buttonMap = new HashMap<>();

	private final GuiSide side;

	public GuiExtentionController(GuiSide side) {
		this.side = side;
	}

	public void render(int xPos, int yPos) {
		yPos += 4;
		if (currentlyExtended == null) {
			for (GuiExtention extention : extentions) {
				extention.setExtending(false);
				int left;
				int right;
				if (side == GuiSide.LEFT) {
					left = xPos - extention.getCurrentWidth();
					right = xPos + 15;
				} else {
					left = xPos - 15;
					right = xPos + extention.getCurrentWidth();
				}
				int bottom = yPos + extention.getCurrentHeight();
				extention.update(left, yPos);
				GuiGraphics.drawGuiBackGround(Minecraft.getMinecraft(), left, yPos, right, bottom, 0, true, true, side != GuiSide.RIGHT, true, side != GuiSide.LEFT);
				extention.renderForground(left + (side == GuiSide.RIGHT ? 20 : 0), yPos);
				yPos = bottom;
			}
		} else {
			if (currentlyExtended.isExtending()) {
				int left;
				int right;
				if (side == GuiSide.LEFT) {
					left = xPos - currentlyExtended.getCurrentWidth();
					right = xPos + 15;
				} else {
					left = xPos - 15;
					right = xPos + currentlyExtended.getCurrentWidth();
				}
				int bottom = currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight();
				currentlyExtended.update(left, yPos);
				GuiGraphics.drawGuiBackGround(Minecraft.getMinecraft(), left, currentlyExtended.getCurrentYPos(), right, bottom, 0, true, true, side != GuiSide.RIGHT, true, side != GuiSide.LEFT);
				currentlyExtended.renderForground(left + (side == GuiSide.RIGHT ? 20 : 0), currentlyExtended.getCurrentYPos());
			} else {
				for (GuiExtention extention : extentions) {
					if (extention == currentlyExtended) {
						break;
					}
					extention.setExtending(false);
					int bottom = yPos + extention.getCurrentHeight();
					yPos = bottom;
				}
				int left;
				int right;
				if (side == GuiSide.LEFT) {
					left = xPos - currentlyExtended.getCurrentWidth();
					right = xPos + 15;
				} else {
					left = xPos - 15;
					right = xPos + currentlyExtended.getCurrentWidth();
				}
				int bottom = currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight();
				currentlyExtended.update(left, yPos);
				GuiGraphics.drawGuiBackGround(Minecraft.getMinecraft(), left, currentlyExtended.getCurrentYPos(), right, bottom, 0, true, true, side != GuiSide.RIGHT, true, side != GuiSide.LEFT);
				currentlyExtended.renderForground(left + (side == GuiSide.RIGHT ? 20 : 0), currentlyExtended.getCurrentYPos());
				if (currentlyExtended.isFullyRetracted()) {
					currentlyExtended = null;
				}
			}
		}
		if (currentlyExtended != null && extentionsToRemove.contains(currentlyExtended)) {
			currentlyExtended = null;
		}
		extentions.removeAll(extentionsToRemove);
		extentionsToRemove.clear();
	}

	public void addExtention(GuiExtention extention) {
		extentions.add(extention);
	}

	public void removeExtention(GuiExtention extention) {
		extentionsToRemove.add(extention);
	}

	public void mouseClicked(int x, int y, int k) {
		if (currentlyExtended == null) {
			extentions.stream()
					.filter(extention -> x > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight())
					.forEach(extention -> {
						currentlyExtended = extention;
						currentlyExtended.setExtending(true);
					});
		} else {
			if (x > currentlyExtended.getCurrentXPos() && x < currentlyExtended.getCurrentXPos() + currentlyExtended.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y > currentlyExtended.getCurrentYPos() && y < currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight()) {
				currentlyExtended.setExtending(false);
			}
		}
	}

	public void mouseOver(int i, int j) {
		int x = i;
		int y = j;
		if (currentlyExtended == null) {
			for (GuiExtention extention : extentions) {
				if (x > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight()) {
					extention.handleMouseOverAt(x, y);
					return;
				}
			}
		} else {
			if (x > currentlyExtended.getCurrentXPos() && x < currentlyExtended.getCurrentXPos() + currentlyExtended.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y > currentlyExtended.getCurrentYPos() && y < currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight()) {
				currentlyExtended.handleMouseOverAt(x, y);
				return;
			}
		}
	}

	public int registerControlledSlot(Slot slot) {
		int size = slotMap.size();
		slotMap.put(slot, size);
		return size;
	}

	public boolean renderSlot(Slot slot) {
		if (!slotMap.containsKey(slot)) {
			return true;
		}
		if (currentlyExtended == null) {
			return false;
		}
		if (!currentlyExtended.isFullyExtended()) {
			return false;
		}
		int id = slotMap.get(slot);
		return currentlyExtended.renderSlot(id);
	}

	public boolean renderSelectSlot(Slot slot) {
		if (!slotMap.containsKey(slot)) {
			return true;
		}
		if (currentlyExtended == null) {
			return false;
		}
		if (!currentlyExtended.isFullyExtended()) {
			return false;
		}
		int id = slotMap.get(slot);
		return currentlyExtended.renderSelectSlot(id);
	}

	public int registerControlledButton(GuiButton button) {
		int size = buttonMap.size();
		buttonMap.put(button, size);
		return size;
	}

	public boolean renderButtonControlled(GuiButton button) {
		return buttonMap.containsKey(button);
	}

	public boolean renderButton(GuiButton button) {
		if (!buttonMap.containsKey(button)) {
			return true;
		}
		if (currentlyExtended == null) {
			return false;
		}
		if (!currentlyExtended.isFullyExtended()) {
			return false;
		}
		int id = buttonMap.get(button);
		return currentlyExtended.renderButton(id);
	}

	public void clear() {
		extentions.clear();
		extentionsToRemove.clear();
		currentlyExtended = null;
	}

	public void retract() {
		if (currentlyExtended != null) {
			currentlyExtended.setExtending(false);
		}
	}

	public List<Rectangle> getGuiExtraAreas() {
		List<Rectangle> list = new ArrayList<>();
		if (currentlyExtended == null) {
			for (GuiExtention extention : extentions) {
				list.add(new Rectangle(extention.getCurrentXPos(), extention.getCurrentYPos(), extention.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0), extention.getCurrentHeight()));
			}
		} else {
			list.add(new Rectangle(currentlyExtended.getCurrentXPos(), currentlyExtended.getCurrentYPos(), currentlyExtended.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0), currentlyExtended.getCurrentHeight()));
		}
		return list;
	}

	public boolean isOverPanel(int x, int y, int w, int h) {
		if (currentlyExtended == null) {
			for (GuiExtention extention : extentions) {
				if (x + w > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y + h > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight()) {
					return true;
				}
			}
		} else {
			if (x + w > currentlyExtended.getCurrentXPos() && x < currentlyExtended.getCurrentXPos() + currentlyExtended.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y + h > currentlyExtended.getCurrentYPos() && y < currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight()) {
				return true;
			}
		}
		return false;
	}
}

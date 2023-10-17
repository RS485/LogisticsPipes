package logisticspipes.utils.gui.extension;

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

public class GuiExtensionController {

	public enum GuiSide {
		LEFT,
		RIGHT
	}

	private final List<GuiExtension> extensions = new ArrayList<>();
	private final List<GuiExtension> extensionsToRemove = new ArrayList<>();
	@Setter
	private int maxBottom;
	private GuiExtension currentlyExtended = null;
	private Map<Slot, Integer> slotMap = new HashMap<>();
	private Map<GuiButton, Integer> buttonMap = new HashMap<>();

	private final GuiSide side;

	public GuiExtensionController(GuiSide side) {
		this.side = side;
	}

	public void render(int xPos, int yPos) {
		yPos += 4;
		if (currentlyExtended == null) {
			for (GuiExtension extension : extensions) {
				extension.setExtending(false);
				int left;
				int right;
				if (side == GuiSide.LEFT) {
					left = xPos - extension.getCurrentWidth();
					right = xPos + 15;
				} else {
					left = xPos - 15;
					right = xPos + extension.getCurrentWidth();
				}
				int bottom = yPos + extension.getCurrentHeight();
				extension.update(left, yPos);
				GuiGraphics.drawGuiBackGround(Minecraft.getMinecraft(), left, yPos, right, bottom, 0, true, true, side != GuiSide.RIGHT, true, side != GuiSide.LEFT);
				extension.renderForeground(left + (side == GuiSide.RIGHT ? 20 : 0), yPos);
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
				currentlyExtended.renderForeground(left + (side == GuiSide.RIGHT ? 20 : 0), currentlyExtended.getCurrentYPos());
			} else {
				for (GuiExtension extension : extensions) {
					if (extension == currentlyExtended) {
						break;
					}
					extension.setExtending(false);
					int bottom = yPos + extension.getCurrentHeight();
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
				currentlyExtended.renderForeground(left + (side == GuiSide.RIGHT ? 20 : 0), currentlyExtended.getCurrentYPos());
				if (currentlyExtended.isFullyRetracted()) {
					currentlyExtended = null;
				}
			}
		}
		if (currentlyExtended != null && extensionsToRemove.contains(currentlyExtended)) {
			currentlyExtended = null;
		}
		extensions.removeAll(extensionsToRemove);
		extensionsToRemove.clear();
	}

	public void addExtension(GuiExtension extension) {
		extensions.add(extension);
	}

	public void removeExtension(GuiExtension extension) {
		extensionsToRemove.add(extension);
	}

	public void mouseClicked(int x, int y, int k) {
		if (currentlyExtended == null) {
			extensions.stream()
					.filter(extension -> x > extension.getCurrentXPos() && x < extension.getCurrentXPos() + extension.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y > extension.getCurrentYPos() && y < extension.getCurrentYPos() + extension.getCurrentHeight())
					.forEach(extension -> {
						currentlyExtended = extension;
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
			for (GuiExtension extension : extensions) {
				if (x > extension.getCurrentXPos() && x < extension.getCurrentXPos() + extension.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y > extension.getCurrentYPos() && y < extension.getCurrentYPos() + extension.getCurrentHeight()) {
					extension.handleMouseOverAt(x, y);
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
		extensions.clear();
		extensionsToRemove.clear();
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
			for (GuiExtension extension : extensions) {
				list.add(new Rectangle(extension.getCurrentXPos(), extension.getCurrentYPos(), extension.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0), extension.getCurrentHeight()));
			}
		} else {
			list.add(new Rectangle(currentlyExtended.getCurrentXPos(), currentlyExtended.getCurrentYPos(), currentlyExtended.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0), currentlyExtended.getCurrentHeight()));
		}
		return list;
	}

	public boolean isOverPanel(int x, int y, int w, int h) {
		if (currentlyExtended == null) {
			for (GuiExtension extension : extensions) {
				if (x + w > extension.getCurrentXPos() && x < extension.getCurrentXPos() + extension.getCurrentWidth() + (side == GuiSide.RIGHT ? 15 : 0) && y + h > extension.getCurrentYPos() && y < extension.getCurrentYPos() + extension.getCurrentHeight()) {
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

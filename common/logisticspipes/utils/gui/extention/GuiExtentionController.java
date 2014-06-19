package logisticspipes.utils.gui.extention;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.utils.gui.BasicGuiHelper;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;

public class GuiExtentionController {

	private final List<GuiExtention> extentions = new ArrayList<GuiExtention>();
	private final List<GuiExtention> extentionsToRemove = new ArrayList<GuiExtention>();
	@Setter
	private int maxBottom;
	private GuiExtention currentlyExtended = null;
	private Map<Slot, Integer> slotMap = new HashMap<Slot, Integer>();
	private Map<GuiButton, Integer> buttonMap = new HashMap<GuiButton, Integer>();
	
	public void render(int xPos, int yPos) {
		yPos += 4;
		if(currentlyExtended == null) {
			for(GuiExtention extention:extentions) {
				extention.setExtending(false);
				int left = xPos - extention.getCurrentWidth();
				int bottom = yPos + extention.getCurrentHeight();
				extention.update(left, yPos);
				BasicGuiHelper.drawGuiBackGround(Minecraft.getMinecraft(), left, yPos, xPos + 15, bottom, 0, true, true, true, true, false);
				extention.renderForground(left, yPos);
				yPos = bottom;
			}
		} else {
			if(currentlyExtended.isExtending()) {
				int left = xPos - currentlyExtended.getCurrentWidth();
				int bottom = currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight();
				currentlyExtended.update(left, yPos);
				BasicGuiHelper.drawGuiBackGround(Minecraft.getMinecraft(), left, currentlyExtended.getCurrentYPos(), xPos + 15, bottom, 0, true, true, true, true, false);
				currentlyExtended.renderForground(left, currentlyExtended.getCurrentYPos());
			} else {
				for(GuiExtention extention:extentions) {
					if(extention == currentlyExtended) break;
					extention.setExtending(false);
					int bottom = yPos + extention.getCurrentHeight();
					yPos = bottom;
				}
				int left = xPos - currentlyExtended.getCurrentWidth();
				int bottom = currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight();
				currentlyExtended.update(left, yPos);
				BasicGuiHelper.drawGuiBackGround(Minecraft.getMinecraft(), left, currentlyExtended.getCurrentYPos(), xPos + 15, bottom, 0, true, true, true, true, false);
				currentlyExtended.renderForground(left, currentlyExtended.getCurrentYPos());
				if(currentlyExtended.isFullyRetracted()) {
					currentlyExtended = null;
				}
			}
		}
		if(currentlyExtended != null && extentionsToRemove.contains(currentlyExtended)) {
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
		if(currentlyExtended == null) {
			for(GuiExtention extention:extentions) {
				if(x > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() && y > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight()) {
					currentlyExtended = extention;
					currentlyExtended.setExtending(true);
				}
			}
		} else {
			if(x > currentlyExtended.getCurrentXPos() && x < currentlyExtended.getCurrentXPos() + currentlyExtended.getCurrentWidth() && y > currentlyExtended.getCurrentYPos() && y < currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight()) {
				currentlyExtended.setExtending(false);
			}
		}
	}

	public void mouseOver(int i, int j) {
		int x = i;
		int y = j;
		if(currentlyExtended == null) {
			for(GuiExtention extention:extentions) {
				if(x > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() && y > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight()) {
					extention.handleMouseOverAt(x, y);
					return;
				}
			}
		} else {
			if(x > currentlyExtended.getCurrentXPos() && x < currentlyExtended.getCurrentXPos() + currentlyExtended.getCurrentWidth() && y > currentlyExtended.getCurrentYPos() && y < currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight()) {
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
		if(!slotMap.containsKey(slot)) return true;
		if(currentlyExtended == null) return false;
		if(!currentlyExtended.isFullyExtended()) return false;
		int id = slotMap.get(slot);
		return currentlyExtended.renderSlot(id);
	}

	public boolean renderSelectSlot(Slot slot) {
		if(!slotMap.containsKey(slot)) return true;
		if(currentlyExtended == null) return false;
		if(!currentlyExtended.isFullyExtended()) return false;
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
		if(!buttonMap.containsKey(button)) return true;
		if(currentlyExtended == null) return false;
		if(!currentlyExtended.isFullyExtended()) return false;
		int id = buttonMap.get(button);
		return currentlyExtended.renderButton(id);
	}

	public void clear() {
		extentions.clear();
		extentionsToRemove.clear();
		currentlyExtended = null;
	}

	public void retract() {
		if(currentlyExtended != null) {
			currentlyExtended.setExtending(false);
		}
	}
}

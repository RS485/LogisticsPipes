package logisticspipes.utils.gui.extention;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.utils.gui.BasicGuiHelper;
import lombok.Setter;
import net.minecraft.client.Minecraft;

public class GuiExtentionController {
	public enum Side{LEFT, RIGHT};

	private final List<GuiExtention> extentions = new ArrayList<GuiExtention>();
	private final List<GuiExtention> extentionsToRemove = new ArrayList<GuiExtention>();
	private final Side side;
	@Setter
	private int maxBottom;
	private GuiExtention currentlyExtended = null;
	
	public GuiExtentionController(Side side) {
		this.side = side;
	}
	
	public void render(int xPos, int yPos) {
		yPos += 4;
		boolean first = true;
		for(GuiExtention extention:extentions) {
			if(side == Side.LEFT) {
				int left = xPos - extention.getCurrentWidth();
				int bottom = yPos + extention.getCurrentHeight();
				if(bottom > this.maxBottom - 20 && !first) {
					yPos = bottom;
					extention.setDisabled(true);
					continue;
				}
				extention.setDisabled(false);
				extention.update(left, yPos);
				BasicGuiHelper.drawGuiBackGround(Minecraft.getMinecraft(), left, yPos, xPos + 15, bottom, 0, true, true, true, true, false);
				extention.renderForground(left, yPos);
				yPos = bottom;
			} else {
				//TODO when needed
			}
			first = false;
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
		for(GuiExtention extention:extentions) {
			if(x > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() && y > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight() && !extention.isDisabled()) {
				if(extention.isExtending()) {
					extention.setExtending(false);
					currentlyExtended = null;
				} else {
					if(currentlyExtended != null) {
						currentlyExtended.setExtending(false);
					}
					currentlyExtended = extention;
					currentlyExtended.setExtending(true);
				}
			}
		}
	}

	public void mouseOver(int i, int j) {
		int x = i;
		int y = j;
		for(GuiExtention extention:extentions) {
			if(x > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() && y > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight() && !extention.isDisabled()) {
				extention.handleMouseOverAt(x, y);
				return;
			}
		}
	}
}

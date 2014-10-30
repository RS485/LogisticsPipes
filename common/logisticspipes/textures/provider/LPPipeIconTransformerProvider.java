package logisticspipes.textures.provider;

import java.util.ArrayList;

import net.minecraft.util.IIcon;
import codechicken.lib.render.uv.IconTransformation;

public class LPPipeIconTransformerProvider {
	public ArrayList<IconTransformation> icons = new ArrayList<IconTransformation>();
	
	public IconTransformation getIcon(int iconIndex) {
		return icons.get(iconIndex);
	}
	
	public void setIcon(int index, IIcon icon) {
		while(icons.size() < index + 1) {
			icons.add(null);
		}
		if(icons.get(index) != null) {
			icons.get(index).icon = icon;
		} else {
			icons.set(index, new IconTransformation(icon));
		}
	}
}

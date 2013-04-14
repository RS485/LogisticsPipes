/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gates;

import logisticspipes.textures.Textures;
import buildcraft.core.triggers.BCAction;

public class ActionDisableLogistics extends BCAction{

	public ActionDisableLogistics(int id) {
		super(id);
	}
	
	@Override
	public String getDescription() {
		return "Disable Pipe";
	}
	
	@Override
	public int getIconIndex() {
		return 1 * 16 + 0 ;
	}
	
}

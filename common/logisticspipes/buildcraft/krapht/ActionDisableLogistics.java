/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht;

import logisticspipes.mod_LogisticsPipes;
import buildcraft.api.gates.Action;

public class ActionDisableLogistics extends Action{

	public ActionDisableLogistics(int id) {
		super(id);
	}
	
	@Override
	public String getDescription() {
		return "Disable Pipe";
	}
	
	@Override
	public int getIndexInTexture() {
		// TODO Auto-generated method stub
		return 1 * 16 + 0 ;
	}
	
	@Override
	public String getTexture() {
		// TODO Auto-generated method stub
		return mod_LogisticsPipes.LOGISTICSACTIONTRIGGERS_TEXTURE_FILE;
	}
}

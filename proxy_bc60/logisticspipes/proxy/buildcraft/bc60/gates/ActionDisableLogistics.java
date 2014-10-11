/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft.bc60.gates;

import logisticspipes.textures.provider.LPActionTriggerIconProvider;

public class ActionDisableLogistics extends LPAction{

	public ActionDisableLogistics() {
		super("LogisticsPipes:action.pipeDisable");
	}
	
	@Override
	public boolean hasParameter() {
		return false;
	}
	
	@Override
	public String getDescription() {
		return "Disable Pipe";
	}
	
	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.actionDisablePipeIconIndex;
	}

	
}

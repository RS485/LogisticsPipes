/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gates;

import logisticspipes.textures.Textures;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.core.IIconProvider;

public class ActionDisableLogistics extends LPAction{

	public ActionDisableLogistics(int id) {
		super(id);
	}
	
	@Override
	public boolean hasParameter() {
		return false;
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public String getDescription() {
		return "Disable Pipe";
	}
	
	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.actionDisablePipeIconIndex;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return Textures.LPactionIconProvider;
	}
	
}

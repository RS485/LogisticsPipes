/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Icon;
import logisticspipes.textures.Textures;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.IAction;
import buildcraft.core.triggers.BCAction;

public class ActionDisableLogistics implements IAction{

	int id;
	public ActionDisableLogistics(int id) {
		this.id = id;
	}
	
	@Override
	public String getDescription() {
		return "Disable Pipe";
	}

	@Override
	public Icon getTexture() {
		// TODO Auto-generated method stub
		return Textures.LOGISTICSACTIONTRIGGERS_DISABLED;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasParameter() {
		// TODO Auto-generated method stub
		return false;
	}
}

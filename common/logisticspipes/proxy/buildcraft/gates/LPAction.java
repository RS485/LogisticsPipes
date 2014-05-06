/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft.gates;


import logisticspipes.textures.Textures;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public abstract  class LPAction implements IAction {

	protected final int legacyId;
	protected final String uniqueTag;

	public LPAction(int legacyId, String uniqueTag) {
		this.legacyId = legacyId;
		this.uniqueTag = uniqueTag;
		ActionManager.registerAction(this);
	}

	@Override
	public int getLegacyId() {
		return this.legacyId;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		// TODO Auto-generated method stub
		Textures.LPactionIconProvider.registerIcons(iconRegister);
		
	}

	@Override
	public String getUniqueTag() {
		return this.uniqueTag;
	}

	public int getIconIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return Textures.LPactionIconProvider.getIcon(getIconIndex());
	}
	
}

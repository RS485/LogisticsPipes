/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.core.utils.Localization;
import net.minecraft.src.buildcraft.krapht.BuildCraftProxy3;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.ModTextureProxy;

public class mod_LogisticsPipes extends ModTextureProxy {
	
	public mod_LogisticsPipes() {
		SimpleServiceLocator.setBuildCraftProxy(new BuildCraftProxy3());
		instance = this;
	}
	
	public static mod_LogisticsPipes instance;
	
	//Log Requests
	public static boolean DisplayRequests;

	
	@Override
	public void modsLoaded() {
		Localization.addLocalization("/lang/logisticspipes/", "en_US");
		
		super.modsLoaded();
	}
}
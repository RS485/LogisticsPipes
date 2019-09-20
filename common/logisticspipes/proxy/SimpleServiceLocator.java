/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy;

import java.util.LinkedList;

import logisticspipes.proxy.interfaces.CraftingRecipeProvider;

public final class SimpleServiceLocator {

	private SimpleServiceLocator() {}

	public static final LinkedList<CraftingRecipeProvider> craftingRecipeProviders = new LinkedList<>();

}

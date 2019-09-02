/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logisticspipes;

import net.minecraft.util.EnumFacing;

/**
 * This class is responsible for handling items arriving at its destination
 *
 * @author Krapht
 */
public abstract class TransportLayer {

	public abstract boolean stillWantItem(IRoutedItem item);

	public abstract EnumFacing itemArrived(IRoutedItem item, EnumFacing denyed);

	public void handleItem(IRoutedItem item) {}

}

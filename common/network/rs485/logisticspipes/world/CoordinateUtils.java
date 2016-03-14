/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the MIT license:
 *
 * Copyright (c) 2015  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular MIT license in your project, replace this copyright notice (this line and any lines below and NOT the copyright line above) with the lines from the original MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this file and associated documentation files (the "Source Code"), to deal in the Source Code without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Source Code, and to permit persons to whom the Source Code is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Source Code, which also can be distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package network.rs485.logisticspipes.world;

import net.minecraft.util.EnumFacing;

public final class CoordinateUtils {

	private CoordinateUtils() {}

	public static DoubleCoordinates add(DoubleCoordinates coords, DoubleCoordinates toAdd) {
		coords.setXCoord(coords.getXCoord() + toAdd.getXCoord());
		coords.setYCoord(coords.getYCoord() + toAdd.getYCoord());
		coords.setZCoord(coords.getZCoord() + toAdd.getZCoord());
		return coords;
	}

	public static IntegerCoordinates add(IntegerCoordinates coords, IntegerCoordinates toAdd) {
		coords.setXCoord(coords.getXCoord() + toAdd.getXCoord());
		coords.setYCoord(coords.getYCoord() + toAdd.getYCoord());
		coords.setZCoord(coords.getZCoord() + toAdd.getZCoord());
		return coords;
	}

	public static DoubleCoordinates sum(DoubleCoordinates first, DoubleCoordinates second) {
		DoubleCoordinates ret = new DoubleCoordinates();
		ret.setXCoord(first.getXCoord() + second.getXCoord());
		ret.setYCoord(first.getYCoord() + second.getYCoord());
		ret.setZCoord(first.getZCoord() + second.getZCoord());
		return ret;
	}

	public static IntegerCoordinates sum(IntegerCoordinates first, IntegerCoordinates second) {
		IntegerCoordinates ret = new IntegerCoordinates();
		ret.setXCoord(first.getXCoord() + second.getXCoord());
		ret.setYCoord(first.getYCoord() + second.getYCoord());
		ret.setZCoord(first.getZCoord() + second.getZCoord());
		return ret;
	}

	public static DoubleCoordinates add(DoubleCoordinates coords, EnumFacing direction) {
		return CoordinateUtils.add(coords, direction, 1);
	}

	public static IntegerCoordinates add(IntegerCoordinates coords, EnumFacing direction) {
		return CoordinateUtils.add(coords, direction, 1);
	}

	public static DoubleCoordinates add(DoubleCoordinates coords, EnumFacing direction, double times) {
		coords.setXCoord(coords.getXCoord() + direction.getDirectionVec().getX() * times);
		coords.setYCoord(coords.getYCoord() + direction.getDirectionVec().getY() * times);
		coords.setZCoord(coords.getZCoord() + direction.getDirectionVec().getZ() * times);
		return coords;
	}

	public static IntegerCoordinates add(IntegerCoordinates coords, EnumFacing direction, int times) {
		coords.setXCoord(coords.getXCoord() + direction.getDirectionVec().getX() * times);
		coords.setYCoord(coords.getYCoord() + direction.getDirectionVec().getY() * times);
		coords.setZCoord(coords.getZCoord() + direction.getDirectionVec().getZ() * times);
		return coords;
	}

	public static DoubleCoordinates sum(DoubleCoordinates coords, EnumFacing direction) {
		return CoordinateUtils.sum(coords, direction, 1);
	}

	public static IntegerCoordinates sum(IntegerCoordinates coords, EnumFacing direction) {
		return CoordinateUtils.sum(coords, direction, 1);
	}

	public static DoubleCoordinates sum(DoubleCoordinates coords, EnumFacing direction, double times) {
		DoubleCoordinates ret = new DoubleCoordinates(coords);
		return CoordinateUtils.add(ret, direction, times);
	}

	public static IntegerCoordinates sum(IntegerCoordinates coords, EnumFacing direction, int times) {
		IntegerCoordinates ret = new IntegerCoordinates(coords);
		return CoordinateUtils.add(ret, direction, times);
	}
}

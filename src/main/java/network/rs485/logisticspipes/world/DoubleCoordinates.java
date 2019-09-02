/*
 * Copyright (c) 2016  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2016  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import lombok.Data;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.IPositionRotateble;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.util.LPSerializable;

@Data
public class DoubleCoordinates implements IPositionRotateble, ICoordinates, LPSerializable {

	private double xCoord;
	private double yCoord;
	private double zCoord;

	public DoubleCoordinates() {
		setXCoord(0.0);
		setYCoord(0.0);
		setZCoord(0.0);
	}

	public DoubleCoordinates(double xCoord, double yCoord, double zCoord) {
		setXCoord(xCoord);
		setYCoord(yCoord);
		setZCoord(zCoord);
	}

	public DoubleCoordinates(LPDataInput input) {
		read(input);
	}

	public DoubleCoordinates(ICoordinates coords) {
		this(coords.getXDouble(), coords.getYDouble(), coords.getZDouble());
	}

	public DoubleCoordinates(TileEntity tile) {
		this(tile.getPos());
	}

	public DoubleCoordinates(CoreUnroutedPipe pipe) {
		this(pipe.getX(), pipe.getY(), pipe.getZ());
	}

	public DoubleCoordinates(IPipeInformationProvider pipe) {
		this(pipe.getX(), pipe.getY(), pipe.getZ());
	}

	public DoubleCoordinates(CoordinatesPacket packet) {
		this(packet.getPosX(), packet.getPosY(), packet.getPosZ());
	}

	public DoubleCoordinates(Entity entity) {
		this(entity.posX, entity.posY, entity.posZ);
	}

	public DoubleCoordinates(BlockPos pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}

	public static DoubleCoordinates readFromNBT(String prefix, NBTTagCompound nbt) {
		if (nbt.hasKey(prefix + "xPos") && nbt.hasKey(prefix + "yPos") && nbt.hasKey(prefix + "zPos")) {
			return new DoubleCoordinates(nbt.getDouble(prefix + "xPos"), nbt.getDouble(prefix + "yPos"), nbt.getDouble(prefix + "zPos"));
		}
		return null;
	}

	@Override
	public double getXDouble() {
		return getXCoord();
	}

	@Override
	public double getYDouble() {
		return getYCoord();
	}

	@Override
	public double getZDouble() {
		return getZCoord();
	}

	@Override
	public int getXInt() {
		return (int) getXCoord();
	}

	@Override
	public int getYInt() {
		return (int) getYCoord();
	}

	@Override
	public int getZInt() {
		return (int) getZCoord();
	}

	public BlockPos getBlockPos() {
		return new BlockPos(getXCoord(), getYCoord(), getZCoord());
	}

	public TileEntity getTileEntity(IBlockAccess world) {
		return world.getTileEntity(getBlockPos());
	}

	@Override
	public String toString() {
		return "(" + getXCoord() + ", " + getYCoord() + ", " + getZCoord() + ")";
	}

	public String toIntBasedString() {
		return "(" + getXCoord() + ", " + getYCoord() + ", " + getZCoord() + ")";
	}

	public Block getBlock(IBlockAccess world) {
		IBlockState state = this.getBlockState(world);
		return state == null ? null : state.getBlock();
	}

	public IBlockState getBlockState(IBlockAccess world) {
		return world.getBlockState(getBlockPos());
	}

	public boolean blockExists(World world) {
		return !world.isAirBlock(getBlockPos());
	}

	public double distanceTo(DoubleCoordinates targetPos) {
		return Math.sqrt(Math.pow(targetPos.getXCoord() - getXCoord(), 2) + Math.pow(targetPos.getYCoord() - getYCoord(), 2) + Math
				.pow(targetPos.getZCoord() - getZCoord(), 2));
	}

	public DoubleCoordinates center() {
		DoubleCoordinates coords = new DoubleCoordinates();
		coords.setXCoord(getXInt() + 0.5);
		coords.setYCoord(getYInt() + 0.5);
		coords.setYCoord(getZInt() + 0.5);
		return this;
	}

	public void writeToNBT(String prefix, NBTTagCompound nbt) {
		nbt.setDouble(prefix + "xPos", xCoord);
		nbt.setDouble(prefix + "yPos", yCoord);
		nbt.setDouble(prefix + "zPos", zCoord);
	}

	public DoubleCoordinates add(DoubleCoordinates toAdd) {
		setXCoord(getXCoord() + toAdd.getXCoord());
		setYCoord(getYCoord() + toAdd.getYCoord());
		setZCoord(getZCoord() + toAdd.getZCoord());
		return this;
	}

	public void setBlockToAir(World world) {
		world.setBlockToAir(getBlockPos());
	}

	@Override
	public void rotateLeft() {
		double tmp = getZCoord();
		setZCoord(-getXCoord());
		setXCoord(tmp);
	}

	@Override
	public void rotateRight() {
		double tmp = getXCoord();
		setXCoord(-getZCoord());
		setZCoord(tmp);
	}

	@Override
	public void mirrorX() {
		setXCoord(-getXCoord());
	}

	@Override
	public void mirrorZ() {
		setZCoord(-getZCoord());
	}

	public double getLength() {
		return Math.sqrt(getXDouble() * getXDouble() + getYDouble() * getYDouble() + getZDouble() * getZDouble());
	}

	@Override
	public void read(LPDataInput input) {
		xCoord = input.readDouble();
		yCoord = input.readDouble();
		zCoord = input.readDouble();
	}

	@Override
	public void write(LPDataOutput output) {
		output.writeDouble(xCoord);
		output.writeDouble(yCoord);
		output.writeDouble(zCoord);
	}
}

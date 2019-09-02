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

package network.rs485.logisticspipes.proxy.mcmp.subproxy;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import lombok.SneakyThrows;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.multipart.PartInfo;

import logisticspipes.modplugins.mcmp.LPMCMPAddon;
import logisticspipes.modplugins.mcmp.LPMultipartTile;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.ticks.LPTickHandler;

public class LPTileMultipartContainer extends TileMultipartContainer.Ticking {

	private static final Field isInWorldField;
	private static final Field partsField;
	private static final Field tickingPartsField;

	static {
		try {
			isInWorldField = TileMultipartContainer.class.getDeclaredField("isInWorld");
			partsField = TileMultipartContainer.class.getDeclaredField("parts");
			tickingPartsField = TileMultipartContainer.Ticking.class.getDeclaredField("tickingParts");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		isInWorldField.setAccessible(true);
		partsField.setAccessible(true);
		tickingPartsField.setAccessible(true);
	}

	private final LogisticsTileGenericPipe pipe;
	private Map<IPartSlot, PartInfo> parts = null;
	private PartInfo partInfo = null;

	private final Set<ITickable> superTickingParts;

	@SneakyThrows({ IllegalAccessException.class })
	public LPTileMultipartContainer(LogisticsTileGenericPipe pipe) {
		super();
		this.pipe = pipe;
		isInWorldField.set(this, false);
		this.setWorld(pipe.getWorld());
		this.setPos(pipe.getBlockPos());

		superTickingParts = (Set<ITickable>) tickingPartsField.get(this);
	}

	@Override
	public World getPartWorld() {
		return this.getWorld();
	}

	@SuppressWarnings("NullableProblems") // TileEntity#getWorld should be @Nullable
	@Nullable
	@Override
	public World getWorld() {
		return pipe.hasWorld() ? pipe.getWorld() : super.getWorld();
	}

	@Override
	public void setWorld(World world) {
		super.setWorld(world);
	}

	@Override
	public BlockPos getPartPos() {
		return pipe.getBlockPos();
	}

	@Override
	public Map<IPartSlot, PartInfo> getParts() {
		checkLocalPartsFieldInit();
		return super.getParts();
	}

	@SneakyThrows({ IllegalAccessException.class })
	private void checkLocalPartsFieldInit() {
		if (parts == null) {
			parts = (Map<IPartSlot, PartInfo>) partsField.get(this);
		}
		if (partInfo == null) {
			partInfo = new PartInfo(this, EnumCenterSlot.CENTER, LPMCMPAddon.lpPipeMultipart, this.pipe.getWorld().getBlockState(this.pipe.getBlockPos()), new LPMultipartTile(pipe));
		}
		parts.put(EnumCenterSlot.CENTER, partInfo);
	}

	@Override
	protected void updateWorldState() {
		if (this.superTickingParts.isEmpty()) return; //
		//super.updateWorldState();
		LPTickHandler.getWorldInfo(getWorld()).setSkipBlockUpdateForWorld(true);
		IBlockState prevSt = this.getWorld().getBlockState(this.getPos());

		IBlockState st = this.getWorld().getBlockState(this.getPos());
		this.getWorld().markAndNotifyBlock(this.getPos(), null, prevSt, st, 1);
		this.getWorld().checkLight(this.getPos());
		LPTickHandler.getWorldInfo(getWorld()).setSkipBlockUpdateForWorld(true);
	}

	@Override
	public boolean canAddPart(IPartSlot slot, IBlockState state, IMultipartTile tile) {
		if (!pipe.isMultipartAllowedInPipe()) return false;
		return super.canAddPart(slot, state, tile);
	}

	@Override
	public void addPart(IPartSlot slot, IBlockState state, IMultipartTile tile) {
		if (!pipe.isMultipartAllowedInPipe()) return;
		super.addPart(slot, state, tile);
	}

	@Override
	protected void add(IPartSlot slot, PartInfo partInfo) {
		if (!pipe.isMultipartAllowedInPipe()) return;
		super.add(slot, partInfo);
	}
}

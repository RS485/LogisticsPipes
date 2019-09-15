/*
 * Copyright (c) 2019  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019  RS485
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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import lombok.Data;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import network.rs485.logisticspipes.connection.NeighborBlockEntity;

@Data
public class WorldCoordinatesWrapper {

	private World world;
	private BlockPos pos;

	public WorldCoordinatesWrapper(World world) {
		setWorld(world);
		setPos(BlockPos.ORIGIN);
	}

	public WorldCoordinatesWrapper(World world, BlockPos pos) {
		setWorld(world);
		setPos(pos);
	}

	public WorldCoordinatesWrapper(BlockEntity tileEntity) {
		this(tileEntity.getWorld(), tileEntity.getPos());
	}

	@Nullable
	private static BlockEntity getBlockEntity(World world, BlockPos pos) {
		return world.getBlockEntity(pos);
	}

	public void setWorld(World world) {
		if (world == null) throw new NullPointerException("World must not be null");
		this.world = world;
	}

	public void setPos(BlockPos pos) {
		if (pos == null) throw new NullPointerException("Coordinates must not be null");
		this.pos = pos;
	}

	public Stream<NeighborBlockEntity<BlockEntity>> allNeighborTileEntities() {
		return Arrays.stream(Direction.values()).map(this::getNeighbor).filter(Objects::nonNull);
	}

	public Stream<NeighborBlockEntity<BlockEntity>> connectedTileEntities() {
		BlockEntity pipe = getBlockEntity();
		if (PipeInformationManager.INSTANCE.isNotAPipe(pipe)) {
			LogisticsPipes.log.warn("The coordinates didn't hold a pipe at all", new Throwable("Stack trace"));
			return Stream.empty();
		}
		return allNeighborTileEntities().filter(adjacent -> MainProxy.checkPipesConnections(pipe, adjacent.getBlockEntity(), adjacent.getDirection()));
	}

	public Stream<NeighborBlockEntity<BlockEntity>> connectedTileEntities(ConnectionPipeType pipeType) {
		BlockEntity pipe = getBlockEntity();
		if (!PipeInformationManager.INSTANCE.isPipe(pipe, true, pipeType)) {
			if (LPConstants.DEBUG) {
				LogisticsPipes.log.warn("The coordinates didn't hold the pipe type " + pipeType, new Throwable("Stack trace"));
			}
			return Stream.empty();
		}
		return allNeighborTileEntities().filter(neighbor -> MainProxy.checkPipesConnections(pipe, neighbor.getBlockEntity(), neighbor.getDirection()));
	}

	@Nullable
	public BlockEntity getBlockEntity() {
		return WorldCoordinatesWrapper.getBlockEntity(world, coords);
	}

	@Nullable
	public NeighborBlockEntity<BlockEntity> getNeighbor(@Nonnull Direction direction) {
		BlockPos newPos = pos.offset(direction);
		BlockEntity tileEntity = WorldCoordinatesWrapper.getBlockEntity(world, newCoords);
		if (tileEntity == null) return null;
		return new NeighborBlockEntity<>(tileEntity, direction);
	}

}

/**
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;

import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.connection.NeighborBlockEntity;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class PipeItemsBasicLogistics extends CoreRoutedPipe {

	private ModuleItemSink itemSinkModule;

	public PipeItemsBasicLogistics() {
		super(new PipeTransportLogistics(true) {

			@Override
			public boolean canPipeConnect(BlockEntity tile, Direction dir) {
				if (super.canPipeConnect(tile, dir)) {
					return true;
				}
				if (tile instanceof LogisticsSecurityTileEntity) {
					Direction ori = OrientationsUtil.getOrientationOfTileWithTile(container, tile);
					if (ori == null || ori == Direction.DOWN || ori == Direction.UP) {
						return false;
					}
					return true;
				}
				if (tile instanceof LogisticsProgramCompilerTileEntity) {
					Direction ori = OrientationsUtil.getOrientationOfTileWithTile(container, tile);
					if (ori == null || ori == Direction.DOWN) {
						return false;
					}
					return true;
				}
				return false;
			}
		}, Items.AIR);
		itemSinkModule = new ModuleItemSink();
		itemSinkModule.registerHandler(this, this);
	}

	@Override
	public TextureType getNonRoutedTexture(Direction connection) {
		if (isSecurityProvider(connection)) {
			return Textures.LOGISTICSPIPE_SECURITY_TEXTURE;
		}
		return super.getNonRoutedTexture(connection);
	}

	@Override
	public boolean isLockedExit(Direction orientation) {
		if (isPowerJunction(orientation) || isSecurityProvider(orientation)) {
			return true;
		}
		return super.isLockedExit(orientation);
	}

	private boolean isPowerJunction(Direction ori) {
		BlockEntity tilePipe = container.getTile(ori);
		if (tilePipe == null || !container.canPipeConnect(tilePipe, ori)) {
			return false;
		}

		if (tilePipe instanceof LogisticsPowerJunctionTileEntity) {
			return true;
		}
		return false;
	}

	private boolean isSecurityProvider(Direction ori) {
		BlockEntity tilePipe = container.getTile(ori);
		if (tilePipe == null || !container.canPipeConnect(tilePipe, ori)) {
			return false;
		}
		if (tilePipe instanceof LogisticsSecurityTileEntity) {
			return true;
		}
		return false;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return itemSinkModule;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void setTile(BlockEntity tile) {
		super.setTile(tile);
		itemSinkModule.registerPosition(ModulePositionType.IN_PIPE, 0);
	}

	@Nullable
	@Override
	public IInventoryUtil getPointedInventory() {
		IInventoryUtil invUtil = super.getPointedInventory();
		if (invUtil == null) {
			invUtil = new WorldCoordinatesWrapper(container)
					.connectedTileEntities(ConnectionPipeType.ITEM)
					.filter(NeighborBlockEntity::isItemHandler)
					.findFirst()
					.map(NeighborBlockEntity::getUtilForItemHandler)
					.orElse(null);
		}
		return invUtil;
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		if (itemSinkModule.isDefaultRoute()) {
			return null;
		}

		Set<ItemIdentifier> l1 = new TreeSet<>();
		Collection<ItemIdentifier> current = itemSinkModule.getSpecificInterests();
		if (current != null) {
			l1.addAll(current);
		}
		return l1;
	}

	@Override
	public boolean hasGenericInterests() {
		return itemSinkModule.isDefaultRoute();
	}
}

package logisticspipes.network.guis.pipe;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerEntity;

import logisticspipes.LPItems;
import logisticspipes.gui.GuiPipeController;
import logisticspipes.interfaces.IGuiOpenController;
import logisticspipes.interfaces.SecurityStationManager;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.SneakyUpgradeConfig;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;

@StaticResolve
public class PipeController extends CoordinatesGuiProvider {

	public PipeController(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		return new GuiPipeController(player, (CoreRoutedPipe) pipe.pipe);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe tile = getPipe(player.getEntityWorld());
		if (tile == null || !(tile.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		final CoreRoutedPipe pipe = (CoreRoutedPipe) tile.pipe;
		DummyContainer dummy = new DummyContainer(player, null, pipe.getOriginalUpgradeManager().getGuiController(), new IGuiOpenController() {

			// Network Statistics
			@Override
			public void guiOpenedByPlayer(PlayerEntity player) {
				pipe.playerStartWatching(player, 0);
			}

			@Override
			public void guiClosedByPlayer(PlayerEntity player) {
				pipe.playerStopWatching(player, 0);
			}
		});
		dummy.addNormalSlotsForPlayerInventory(0, 0);
		// TAB_1 SLOTS
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addUpgradeSlot(pipeSlot, pipe.getOriginalUpgradeManager(), pipeSlot, 8 + pipeSlot * 18, 18, itemStack -> {
				if (itemStack == null) {
					return false;
				}
				if (itemStack.getItem() instanceof ItemUpgrade) {
					if (!((ItemUpgrade) itemStack.getItem()).getUpgradeForItem(itemStack, null).isAllowedForPipe(pipe)) {
						return false;
					}
				} else {
					return false;
				}
				return true;
			});
		}
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addSneakyUpgradeSlot(pipeSlot, pipe.getOriginalUpgradeManager(), pipeSlot + 9, 8 + pipeSlot * 18, 48, itemStack -> {
				if (itemStack == null) {
					return false;
				}
				if (itemStack.getItem() instanceof ItemUpgrade) {
					IPipeUpgrade upgrade = ((ItemUpgrade) itemStack.getItem()).getUpgradeForItem(itemStack, null);
					if (!(upgrade instanceof SneakyUpgradeConfig)) {
						return false;
					}
					if (!upgrade.isAllowedForPipe(pipe)) {
						return false;
					}
				} else {
					return false;
				}
				return true;
			});
		}
		// TAB_2 SLOTS
		dummy.addStaticRestrictedSlot(0, pipe.getOriginalUpgradeManager().getSecInv(), 8 + 8 * 18, 18, itemStack -> {
			if (itemStack == null) {
				return false;
			}
			if (itemStack.getItem() != LPItems.itemCard) {
				return false;
			}
			if (itemStack.getDamage() != LogisticsItemCard.SEC_CARD) {
				return false;
			}
			return SecurityStationManager.getInstance().isAuthorized(UUID.fromString(itemStack.getTag().getString("UUID")));
		}, 1);
		dummy.addRestrictedSlot(0, tile.logicController.diskInv, 14, 36, LPItems.disk);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new PipeController(getId());
	}
}

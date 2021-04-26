package logisticspipes.network.guis.pipe;

import java.util.Objects;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.LPItems;
import logisticspipes.gui.GuiPipeController;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.SneakyUpgradeConfig;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;

@StaticResolve
public class PipeController extends CoordinatesGuiProvider {

	public PipeController(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getTileAs(player.world, LogisticsTileGenericPipe.class);
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		return new GuiPipeController(player, (CoreRoutedPipe) pipe.pipe);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe tile = getTileAs(player.world, LogisticsTileGenericPipe.class);
		if (!(tile.pipe instanceof CoreRoutedPipe)) {
			return null;
		}
		final CoreRoutedPipe pipe = (CoreRoutedPipe) tile.pipe;
		DummyContainer dummy = new DummyContainer(player, null, pipe.getOriginalUpgradeManager().getGuiController(), new IGuiOpenControler() {

			//Network Statistics
			@Override
			public void guiOpenedByPlayer(EntityPlayer player) {
				pipe.playerStartWatching(player, 0);
			}

			@Override
			public void guiClosedByPlayer(EntityPlayer player) {
				pipe.playerStopWatching(player, 0);
			}
		});
		dummy.addNormalSlotsForPlayerInventory(0, 0);
		// TAB_1 SLOTS
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addUpgradeSlot(pipeSlot, pipe.getOriginalUpgradeManager(), pipeSlot, 8 + pipeSlot * 18, 18, itemStack ->
					!itemStack.isEmpty() && itemStack.getItem() instanceof ItemUpgrade && ((ItemUpgrade) itemStack.getItem()).getUpgradeForItem(itemStack, null).isAllowedForPipe(pipe));
		}
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addSneakyUpgradeSlot(pipeSlot, pipe.getOriginalUpgradeManager(), pipeSlot + 9, 8 + pipeSlot * 18, 48, itemStack -> {
				if (itemStack.isEmpty()) {
					return false;
				}
				if (itemStack.getItem() instanceof ItemUpgrade) {
					IPipeUpgrade upgrade = ((ItemUpgrade) itemStack.getItem()).getUpgradeForItem(itemStack, null);
					return upgrade instanceof SneakyUpgradeConfig && upgrade.isAllowedForPipe(pipe);
				} else {
					return false;
				}
			});
		}
		// TAB_2 SLOTS
		dummy.addStaticRestrictedSlot(0, pipe.getOriginalUpgradeManager().secInv, 8 + 8 * 18, 18, itemStack -> {
			if (itemStack.isEmpty()) {
				return false;
			}
			if (itemStack.getItem() != LPItems.itemCard) {
				return false;
			}
			if (itemStack.getItemDamage() != LogisticsItemCard.SEC_CARD) {
				return false;
			}
			final NBTTagCompound tag = Objects.requireNonNull(itemStack.getTagCompound());
			return SimpleServiceLocator.securityStationManager.isAuthorized(UUID.fromString(tag.getString("UUID")));
		}, 1);
		dummy.addRestrictedSlot(0, tile.logicController.diskInv, 14, 36, LPItems.disk);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new PipeController(getId());
	}
}

package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.network.guis.module.inhand.SneakyModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inpipe.SneakyModuleInSlotGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyModuleContainer;

@StaticResolve
public class AdvancedExtractorSneakyGuiPacket extends ModuleCoordinatesPacket {

	public AdvancedExtractorSneakyGuiPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new AdvancedExtractorSneakyGuiPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (getType() == ModulePositionType.IN_HAND) {
			if (player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if (dummy.getModule() instanceof ModuleAdvancedExtractor) {
					player.closeScreen();
					NewGuiHandler.getGui(SneakyModuleInHandGuiProvider.class).setInvSlot(getPositionInt()).open(player);
				}
			}
			return;
		}
		final LogisticsTileGenericPipe pipe = this.getPipe(player.world);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;
		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		if (piperouted.getLogisticsModule().getSubModule(getPositionInt()) instanceof ModuleAdvancedExtractor) {
			final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor) piperouted.getLogisticsModule().getSubModule(getPositionInt());
			NewGuiHandler.getGui(SneakyModuleInSlotGuiProvider.class).setSneakyOrientation(module.getSneakyDirection()).setSlot(getType()).setPositionInt(getPositionInt()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()).open(player);
			return;
		}
	}
}

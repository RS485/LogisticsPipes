package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.network.guis.module.inhand.SneakyModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inpipe.SneakyModuleInSlotGuiProvider;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyModuleContainer;
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor;

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
				if (dummy.getModule() instanceof AsyncAdvancedExtractor) {
					player.closeScreen();
					NewGuiHandler.getGui(SneakyModuleInHandGuiProvider.class).setInvSlot(getPositionInt()).open(player);
				}
			}
			return;
		}

		PipeLogisticsChassis pipe = getTileOrPipe(player.world, PipeLogisticsChassis.class);
		LogisticsModule subModule = pipe.getSubModule(getPositionInt());
		if (subModule instanceof AsyncAdvancedExtractor) {
			NewGuiHandler.getGui(SneakyModuleInSlotGuiProvider.class)
					.setSneakyOrientation(((AsyncAdvancedExtractor) subModule).getSneakyDirection())
					.setSlot(getType())
					.setPositionInt(getPositionInt())
					.setPosX(getPosX())
					.setPosY(getPosY())
					.setPosZ(getPosZ())
					.open(player);
		}
	}
}

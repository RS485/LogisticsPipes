package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.module.inhand.ExtractorModuleInHand;
import logisticspipes.network.guis.module.inpipe.ExtractorModuleSlot;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.entity.player.EntityPlayer;

public class AdvancedExtractorSneakyGuiPacket extends IntegerCoordinatesPacket {

	public AdvancedExtractorSneakyGuiPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new AdvancedExtractorSneakyGuiPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final int slot = getInteger();
		if(slot < 0) {
			if(player.openContainer instanceof DummyModuleContainer) {
				DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
				if(dummy.getModule() instanceof ModuleAdvancedExtractor) {
					player.closeScreen();
					//player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * getInteger()), player.worldObj, getPosX(), getPosY(), getPosZ());
					NewGuiHandler.getGui(ExtractorModuleInHand.class).setInvSlot(getPosZ()).open(player);
				}
			}
			return;
		}
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;
		if(piperouted.getLogisticsModule() == null) {
			return;
		}
		if(piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
			final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor) piperouted.getLogisticsModule().getSubModule(slot - 1);
			NewGuiHandler.getGui(ExtractorModuleSlot.class).setSneakyOrientation(module.getSneakyDirection()).setSlot(slot - 1).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()).open(player);
			//player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * getInteger()), player.worldObj, getPosX(), getPosY(), getPosZ());
			//MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(slot - 1).setInteger(module.getSneakyDirection().ordinal()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
			return;
		}
	}
}


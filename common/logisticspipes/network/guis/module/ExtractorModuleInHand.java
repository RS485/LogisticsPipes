package logisticspipes.network.guis.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import net.minecraft.entity.player.EntityPlayer;

public class ExtractorModuleInHand extends ModuleInHandGuiProvider {
	
	public ExtractorModuleInHand(int id) {
		super(id);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public LogisticsBaseGuiScreen getClientGui(EntityPlayer player) {
		LogisticsModule module = this.getLogisticsModule(player);
		if(!(module instanceof ISneakyDirectionReceiver)) return null;
		return new GuiExtractor(player.inventory, null, (ISneakyDirectionReceiver) module, -2);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if(!(((DummyModuleContainer)dummy).getModule() instanceof ISneakyDirectionReceiver)) return null;
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ExtractorModuleInHand(getId());
	}
}

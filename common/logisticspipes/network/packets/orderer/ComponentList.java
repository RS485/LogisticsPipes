package logisticspipes.network.packets.orderer;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.config.Configs;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.request.resources.Resource;
import logisticspipes.request.resources.Resource.ColorCode;
import logisticspipes.request.resources.ResourceNetwork;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.config.LPConfiguration;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ComponentList extends ModernPacket {

	@Getter
	@Setter
	private Collection<Resource> used = new ArrayList<>();

	@Getter
	@Setter
	private Collection<Resource> missing = new ArrayList<>();

	public ComponentList(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ComponentList(getId());
	}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		if (LPConfiguration.INSTANCE.getDisplayRequestPopup() && FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen)
					.handleSimulateAnswer(used, missing, (GuiOrderer) FMLClientHandler.instance().getClient().currentScreen, player);
		} else if (LPConfiguration.INSTANCE.getDisplayRequestPopup() && FMLClientHandler.instance().getClient().currentScreen instanceof GuiRequestTable) {
			((GuiRequestTable) FMLClientHandler.instance().getClient().currentScreen)
					.handleSimulateAnswer(used, missing, (GuiRequestTable) FMLClientHandler.instance().getClient().currentScreen, player);
		} else {
			for (Resource item : used) {
				player.sendMessage(new TextComponentString("Component: " + item.getDisplayText(ColorCode.SUCCESS)));
			}
			for (Resource item : missing) {
				player.sendMessage(new TextComponentString("Missing: " + item.getDisplayText(ColorCode.MISSING)));
			}
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeCollection(used);
		output.writeCollection(missing);
	}

	@Override
	public void readData(LPDataInput input) {
		used = input.readArrayList(ResourceNetwork::readResource);
		missing = input.readArrayList(ResourceNetwork::readResource);
	}
}

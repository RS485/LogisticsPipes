package logisticspipes.commands.commands;

import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.RequestUpdateNamesPacket;
import logisticspipes.proxy.MainProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.network.Player;

public class TransferNamesCommand implements ICommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"transfernames", "tn"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof Player && LogisticsPipesCommand.isOP(sender);
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"Sends all item names form the client", "to the server to update the Language Database"};
	}
	
	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromText("Requesting Transfer"));
    	MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RequestUpdateNamesPacket.class), (Player)sender);
    	MainProxy.proxy.sendNameUpdateRequest((Player)sender);
	}
}

package logisticspipes.commands.commands;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.ActivatNBTDebug;
import logisticspipes.proxy.MainProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.network.Player;

public class NBTDebugCommand implements ICommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"nbtdebug"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof Player;
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"Enables the Hotkey to show an debug gui", "for the howered item. (Only if NEI is installed)"};
	}
	
	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromText("Trying to Enable NBTDebug"));
    	MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ActivatNBTDebug.class), (Player)sender);
	}
}

package logisticspipes.commands.commands.debug;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.debug.PipeDebugLogAskForTarget;
import logisticspipes.network.packets.pipe.PipeDebugAskForTarget;
import logisticspipes.proxy.MainProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.network.Player;

public class PipeCommand implements ICommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"pipe"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"Set the pipe into debug mode"};
	}
	
	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		if(args.length != 1) {
			sender.sendChatToPlayer(ChatMessageComponent.createFromText("Wrong amount of arguments"));
			return;
		}
		if(args[0].equalsIgnoreCase("help")) {
			sender.sendChatToPlayer(ChatMessageComponent.createFromText("client, server, both or console"));
		} else if(args[0].equalsIgnoreCase("both")) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeDebugAskForTarget.class).setServer(true), (Player) sender);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeDebugAskForTarget.class).setServer(false), (Player) sender);
			sender.sendChatToPlayer(ChatMessageComponent.createFromText("Asking for Target."));
		} else if(args[0].equalsIgnoreCase("console") || args[0].equalsIgnoreCase("c")) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeDebugLogAskForTarget.class), (Player) sender);
			sender.sendChatToPlayer(ChatMessageComponent.createFromText("Asking for Target."));
		} else {
			boolean isClient = args[0].equalsIgnoreCase("client");
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeDebugAskForTarget.class).setServer(!isClient), (Player) sender);
			sender.sendChatToPlayer(ChatMessageComponent.createFromText("Asking for Target."));
		}
	}
}

package logisticspipes.commands.commands.debug;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.debug.PipeDebugLogAskForTarget;
import logisticspipes.network.packets.pipe.PipeDebugAskForTarget;
import logisticspipes.proxy.MainProxy;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class PipeCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "pipe" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Set the pipe into debug mode" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		if (args.length != 1) {
			sender.addChatMessage(new ChatComponentText("Wrong amount of arguments"));
			return;
		}
		if (args[0].equalsIgnoreCase("help")) {
			sender.addChatMessage(new ChatComponentText("client, server, both or console"));
		} else if (args[0].equalsIgnoreCase("both")) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeDebugAskForTarget.class).setServer(true), (EntityPlayer) sender);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeDebugAskForTarget.class).setServer(false), (EntityPlayer) sender);
			sender.addChatMessage(new ChatComponentText("Asking for Target."));
		} else if (args[0].equalsIgnoreCase("console") || args[0].equalsIgnoreCase("c")) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeDebugLogAskForTarget.class), (EntityPlayer) sender);
			sender.addChatMessage(new ChatComponentText("Asking for Target."));
		} else {
			boolean isClient = args[0].equalsIgnoreCase("client");
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeDebugAskForTarget.class).setServer(!isClient), (EntityPlayer) sender);
			sender.addChatMessage(new ChatComponentText("Asking for Target."));
		}
	}
}

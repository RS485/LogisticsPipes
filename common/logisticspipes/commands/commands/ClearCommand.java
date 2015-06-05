package logisticspipes.commands.commands;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.utils.string.ChatColor;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class ClearCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "clear" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Clears the chat window from every content", ChatColor.GRAY + "add '" + ChatColor.YELLOW + "all" + ChatColor.GRAY + "' to also clear the send messages" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		if (args.length <= 0 || !args[0].equalsIgnoreCase("all")) {
			sender.addChatMessage(new ChatComponentText("%LPSTORESENDMESSAGE%"));
			sender.addChatMessage(new ChatComponentText("%LPCLEARCHAT%"));
			sender.addChatMessage(new ChatComponentText("%LPRESTORESENDMESSAGE%"));
		} else {
			sender.addChatMessage(new ChatComponentText("%LPCLEARCHAT%"));
		}
	}
}

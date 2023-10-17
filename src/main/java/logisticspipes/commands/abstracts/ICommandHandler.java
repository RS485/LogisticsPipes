package logisticspipes.commands.abstracts;

import net.minecraft.command.ICommandSender;

public interface ICommandHandler {

	String[] getNames();

	boolean isCommandUsableBy(ICommandSender sender);

	String[] getDescription();

	void executeCommand(ICommandSender sender, String[] args);
}

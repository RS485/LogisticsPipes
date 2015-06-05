package logisticspipes.commands.abstracts;

import net.minecraft.command.ICommandSender;

public interface ICommandHandler {

	public String[] getNames();

	public boolean isCommandUsableBy(ICommandSender sender);

	public String[] getDescription();

	public void executeCommand(ICommandSender sender, String[] args);
}

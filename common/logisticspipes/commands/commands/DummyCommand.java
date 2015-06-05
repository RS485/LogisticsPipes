package logisticspipes.commands.commands;

import logisticspipes.commands.abstracts.ICommandHandler;

import net.minecraft.command.ICommandSender;

public class DummyCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "dummy" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "#This Command doesn't do anything" };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {}
}

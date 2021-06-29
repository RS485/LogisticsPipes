package logisticspipes.commands;

import net.minecraft.command.ICommandSender;

import logisticspipes.LogisticsPipes;
import logisticspipes.commands.abstracts.SubCommandHandler;
import logisticspipes.commands.commands.BypassCommand;
import logisticspipes.commands.commands.ChangelogCommand;
import logisticspipes.commands.commands.ClearCommand;
import logisticspipes.commands.commands.DebugCommand;
import logisticspipes.commands.commands.DummyCommand;
import logisticspipes.commands.commands.DumpCommand;
import logisticspipes.commands.commands.NBTDebugCommand;
import logisticspipes.commands.commands.NameLookupCommand;
import logisticspipes.commands.commands.RoutingThreadCommand;
import logisticspipes.commands.commands.TestCommand;
import logisticspipes.commands.commands.TransferNamesCommand;
import logisticspipes.commands.commands.VersionCommand;
import logisticspipes.commands.commands.WrapperCommand;

public class MainCommandHandler extends SubCommandHandler {

	public MainCommandHandler() {
		registerSubCommand(new DummyCommand());
		registerSubCommand(new VersionCommand());
		registerSubCommand(new ChangelogCommand());
		registerSubCommand(new NBTDebugCommand());
		registerSubCommand(new RoutingThreadCommand());
		registerSubCommand(new TransferNamesCommand());
		registerSubCommand(new NameLookupCommand());
		registerSubCommand(new DumpCommand());
		registerSubCommand(new BypassCommand());
		registerSubCommand(new DebugCommand());
		registerSubCommand(new WrapperCommand());
		if (LogisticsPipes.isTesting()) {
			registerSubCommand(new TestCommand());
		}
		registerSubCommand(new ClearCommand());
	}

	@Override
	public String[] getNames() {
		return new String[] { "logisticspipes", "lp", "logipipes" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "The main LP command" };
	}
}

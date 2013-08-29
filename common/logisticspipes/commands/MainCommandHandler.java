package logisticspipes.commands;

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
import logisticspipes.commands.commands.TransferNamesCommand;
import logisticspipes.commands.commands.VersionCommand;
import logisticspipes.commands.commands.WatchCommand;
import net.minecraft.command.ICommandSender;

public class MainCommandHandler extends SubCommandHandler {
	
	@Override
	public String[] getNames() {
		return new String[]{"logisticspipes", "lp", "logipipes"};
	}
	
	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}
	
	@Override
	public String[] getDescription() {
		return new String[]{"The main LP command"};
	}
	
	@Override
	public void registerSubCommands() {
		this.registerSubCommands(new DummyCommand());
		this.registerSubCommands(new VersionCommand());
		this.registerSubCommands(new ChangelogCommand());
		this.registerSubCommands(new NBTDebugCommand());
		this.registerSubCommands(new RoutingThreadCommand());
		this.registerSubCommands(new TransferNamesCommand());
		this.registerSubCommands(new NameLookupCommand());
		this.registerSubCommands(new DumpCommand());
		this.registerSubCommands(new BypassCommand());
		this.registerSubCommands(new WatchCommand());
		this.registerSubCommands(new DebugCommand());
		this.registerSubCommands(new ClearCommand());
	}
}

package logisticspipes.commands.abstracts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;

import logisticspipes.commands.chathelper.MorePageDisplay;
import logisticspipes.commands.exception.CommandNotFoundException;
import logisticspipes.commands.exception.DublicatedCommandException;
import logisticspipes.commands.exception.MissingArgumentException;
import logisticspipes.commands.exception.PermissionDeniedException;
import logisticspipes.utils.string.ChatColor;

public abstract class SubCommandHandler implements ICommandHandler {

	public SubCommandHandler() {
		registerSubCommands();
	}

	private final List<ICommandHandler> subCommands = new ArrayList<>();

	public abstract void registerSubCommands();

	protected final void registerSubCommand(ICommandHandler newHandler) {
		List<String> newCommands = Arrays.asList(newHandler.getNames());
		for (ICommandHandler handler : subCommands) {
			for (String command : handler.getNames()) {
				if (newCommands.contains(command)) {
					throw new DublicatedCommandException();
				}
			}
		}
		subCommands.add(newHandler);
	}

	public final void displayHelp(ICommandSender sender) {
		MorePageDisplay display = new MorePageDisplay(new String[] { "|< Help - " + getNames()[0] + " - Page: %/$ >|" }, sender);
		for (ICommandHandler command : subCommands) {
			if (!command.getDescription()[0].startsWith("#")) {
				boolean first = true;
				String prefix = (command instanceof SubCommandHandler ? ChatColor.BLUE : ChatColor.YELLOW) + command.getNames()[0] + ChatColor.RESET + ": ";
				for (int d = 0; d < command.getDescription().length; d++) {
					display.append(prefix + command.getDescription()[d], !first);
					prefix = "    ";
					first = false;
				}
				if (command instanceof SubCommandHandler) {
					display.append("      " + ChatColor.GRAY + "- add " + ChatColor.YELLOW + "help" + ChatColor.GRAY + " to see the subcommands", true);
				}
				first = true;
				if (command.getNames().length > 1) {
					for (int i = 1; i < command.getNames().length; i++) {
						display.append((first ? "  alias: - " : "         - ") + ChatColor.GOLD + command.getNames()[i] + "", true);
						first = false;
					}
				}
				display.append("", true);
			}
		}
		display.display(sender);
	}

	@Override
	public final void executeCommand(ICommandSender sender, String[] args) {
		if (args.length < 1) {
			throw new MissingArgumentException();
		}
		String command = args[0];
		if (command.equalsIgnoreCase("help")) {
			displayHelp(sender);
			return;
		} else {
			for (ICommandHandler handler : subCommands) {
				if (Arrays.asList(handler.getNames()).contains(command)) {
					if (!handler.isCommandUsableBy(sender)) {
						throw new PermissionDeniedException();
					}
					String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
					handler.executeCommand(sender, newArgs);
					return;
				}
			}
			throw new CommandNotFoundException();
		}
	}
}

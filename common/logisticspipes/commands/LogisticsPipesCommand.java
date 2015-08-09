package logisticspipes.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import logisticspipes.LPConstants;
import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.commands.exception.CommandNotFoundException;
import logisticspipes.commands.exception.LPCommandException;
import logisticspipes.commands.exception.PermissionDeniedException;
import logisticspipes.proxy.MainProxy;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

import cpw.mods.fml.common.FMLCommonHandler;

public class LogisticsPipesCommand extends CommandBase {

	private final ICommandHandler mainCommand;

	public LogisticsPipesCommand() {
		mainCommand = new MainCommandHandler();
	}

	@Override
	public String getCommandName() {
		return "logisticspipes";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
		return true;
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return "/" + getCommandName() + " help";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList(new String[] { "lp", "logipipes" });
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) {
		if (arguments.length <= 0) {
			throw new WrongUsageException("Type '" + getCommandUsage(sender) + "' for help.");
		}
		try {
			boolean managed = false;
			if (LPConstants.DEBUG) {
				//Check for unlisted Debug commands
			}
			if (!managed) {
				mainCommand.executeCommand(sender, arguments);
			}
		} catch (LPCommandException e) {
			if (e instanceof PermissionDeniedException) {
				throw new CommandException("You are not allowed to execute that command now.");
			} else if (e instanceof CommandNotFoundException) {
				throw new CommandException("The command was not found");
			} else {
				throw new WrongUsageException(getCommandUsage(sender));
			}
		}
	}

	public static boolean isOP(ICommandSender sender) {
		return Arrays.asList(FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152603_m().func_152685_a())
				.contains(sender.getCommandSenderName().toLowerCase(Locale.US)) || (MainProxy.proxy.checkSinglePlayerOwner(sender.getCommandSenderName()));
	}
}

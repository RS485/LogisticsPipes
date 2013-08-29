package logisticspipes.commands;

import java.util.Arrays;
import java.util.List;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.commands.exception.CommandNotFoundException;
import logisticspipes.commands.exception.LPCommandException;
import logisticspipes.commands.exception.PermissionDenyedException;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
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
	public String getCommandUsage(ICommandSender var1) {
		return "/" + getCommandName() + " help";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList(new String[]{"lp", "logipipes"});
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) {
		if (arguments.length <= 0) {
			throw new WrongUsageException("Type '" + this.getCommandUsage(sender) + "' for help.");
		}
		try {
			mainCommand.executeCommand(sender, arguments);
		} catch(LPCommandException e) {
			if(e instanceof PermissionDenyedException) {
				throw new CommandException("You are not allowed to execute that command now.");
			} else if(e instanceof CommandNotFoundException) {
				throw new CommandException("The command was not found");
			} else {
				throw new WrongUsageException(this.getCommandUsage(sender));
			}
		}
	}
	
	public static boolean isOP(ICommandSender sender) {
		return MinecraftServer.getServerConfigurationManager(FMLCommonHandler.instance().getMinecraftServerInstance()).getOps().contains(sender.getCommandSenderName().toLowerCase()) || (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() && !FMLCommonHandler.instance().getMinecraftServerInstance().isServerInOnlineMode());
	}
}

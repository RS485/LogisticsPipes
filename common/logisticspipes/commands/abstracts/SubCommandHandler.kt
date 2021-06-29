package logisticspipes.commands.abstracts;

import net.minecraft.command.ICommandSender;

import logisticspipes.commands.chathelper.MorePageDisplay;
import logisticspipes.commands.exception.CommandNotFoundException;
import logisticspipes.commands.exception.DublicatedCommandException;
import logisticspipes.commands.exception.MissingArgumentException;
import logisticspipes.commands.exception.PermissionDeniedException;
import logisticspipes.utils.string.ChatColor;

abstract class SubCommandHandler : ICommandHandler {

	private val subCommands: MutableList<ICommandHandler> = mutableListOf()

	protected fun registerSubCommand(newHandler: ICommandHandler) {
		val newCommands = newHandler.getNames().toList()
		for (handler in subCommands) {
			for (command in handler.getNames()) {
				if (command in newCommands) {
					throw DublicatedCommandException()
				}
			}
		}
		subCommands.add(newHandler)
	}

	private fun displayHelp(sender: ICommandSender) {
		val display = MorePageDisplay(arrayListOf("|< Help - " + getNames()[0] + " - Page: %/$ >|"), sender)
		for (handler in subCommands) {
			if (handler.getDescription()[0].startsWith("#")) continue

			val color = if (handler is SubCommandHandler) ChatColor.BLUE else ChatColor.YELLOW;
			var prefix = "$color${handler.getNames()[0]}${ChatColor.RESET}: "

			for (d in handler.getDescription().indices) {
				display.append(prefix + handler.getDescription()[d], d > 0)
				prefix = "    "
			}
			if (handler is SubCommandHandler) {
				display.append("$prefix${ChatColor.GRAY}- add ${ChatColor.YELLOW}help${ChatColor.GRAY} to see the subcommands", true)
			}

			if (handler.getNames().size > 1) {
				for (i in 1..handler.getNames().indices.last) {
					display.append((if (i == 1) "  alias: - " else "         - ") + "${ChatColor.GOLD}${handler.getNames()[i]}", true)
				}
			}
			display.append("", true)
		}
		display.display(sender)
	}

	final override fun executeCommand(sender: ICommandSender, args: Array<String>) {
		if (args.isEmpty()) {
			throw MissingArgumentException()
		}
		val command = args[0]
		if (command.equals("help", true)) {
			displayHelp(sender)
			return
		}
		for (handler: ICommandHandler in subCommands) {
			if (command in handler.getNames()) {
				if (!handler.isCommandUsableBy(sender)) {
					throw PermissionDeniedException()
				}
				handler.executeCommand(sender, args.copyOfRange(1, args.size))
				return
			}
		}
		throw CommandNotFoundException()
	}
}

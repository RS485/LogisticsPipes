package logisticspipes.commands.abstracts;

import net.minecraft.command.ICommandSender;

/**
 * Base interface all chat commands of LP implement
 */
interface ICommandHandler {

	/**
	 * @return the name + all possible aliases of calling the command
	 */
	fun getNames(): Array<String>

	/**
	 * @return can this command be called by the [sender], i.e. does the sender have the permission to execute
	 */
	fun isCommandUsableBy(sender: ICommandSender): Boolean

	/**
	 * @return the description of what the command does
	 */
	fun getDescription(): Array<String>

	/**
	 * The execution function to run the command as a [sender] with the provided [arguments][args]
	 */
	fun executeCommand(sender: ICommandSender, args: Array<String>)
}

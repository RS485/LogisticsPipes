package logisticspipes.commands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.LogisticsPipesPacket;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.Player;

public class LogisticsPipesCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "logisticspipes";
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return "/" + getCommandName() + " help";
	}

	@Override
	public List getCommandAliases() {
		return Arrays.asList(new String[]{"lp", "logipipes"});
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) {
		if (arguments.length <= 0) {
			throw new WrongUsageException("Type '" + this.getCommandUsage(sender) + "' for help.");
		}
		if(arguments[0].equalsIgnoreCase("version")) {
        	commandVersion(sender, arguments);
        	return;
        } else if(arguments[0].equalsIgnoreCase("NBTDEBUG")) {
        	if(!(sender instanceof Player)) {
        		throw new WrongUsageException("You can't use this command.");
        	}
        	sender.sendChatToPlayer("Trying to Enable NBTDebug");
        	MainProxy.sendPacketToPlayer(new LogisticsPipesPacket() {
				@Override public void writeData(DataOutputStream data) throws IOException {}
				@Override public void readData(DataInputStream data) throws IOException {}
				@Override public int getID() {return NetworkConstants.ACTIVATNBTDEBUG;}
			}.getPacket(), (Player)sender);
        	return;
        } else if(arguments[0].equalsIgnoreCase("help")) {
        	sender.sendChatToPlayer("Format: '"+ this.getCommandName() +" <command> <arguments>'");
        	sender.sendChatToPlayer("Available commands:");
        	sender.sendChatToPlayer("- version : Version information.");
        	return;
        }
    	throw new WrongUsageException(this.getCommandUsage(sender));
	}

	private void commandVersion(ICommandSender sender, String[] arguments) {
    	sender.sendChatToPlayer(String.format("LogisticsPipes %s for Minecraft 1.3.2.", LogisticsPipes.class.getAnnotation(Mod.class).version()));
	}
}

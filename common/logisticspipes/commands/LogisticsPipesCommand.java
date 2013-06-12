package logisticspipes.commands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.oldpackets.PacketLogisticsPipes;
import logisticspipes.proxy.MainProxy;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.ticks.Watchdog;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
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
	public List<String> getCommandAliases() {
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
        	MainProxy.sendPacketToPlayer(new PacketLogisticsPipes() {
				@Override public void writeData(DataOutputStream data) throws IOException {}
				@Override public void readData(DataInputStream data) throws IOException {}
				@Override public int getID() {return NetworkConstants.ACTIVATNBTDEBUG;}
			}.getPacket(), (Player)sender);
        	return;
        } else if(arguments[0].equalsIgnoreCase("routingthread") || arguments[0].equalsIgnoreCase("rt")) {
        	sender.sendChatToPlayer("RoutingTableUpdateThread: TODO: " + RoutingTableUpdateThread.size());
        	return;
        } else if(arguments[0].equalsIgnoreCase("transfernames") || arguments[0].equalsIgnoreCase("tn")) {
        	if(!(sender instanceof Player)) {
        		throw new WrongUsageException("You can't use this command.");
        	}
        	sender.sendChatToPlayer("Requesting Transfer");
        	MainProxy.sendPacketToPlayer(new PacketLogisticsPipes() {
				@Override public void writeData(DataOutputStream data) throws IOException {}
				@Override public void readData(DataInputStream data) throws IOException {}
				@Override public int getID() {return NetworkConstants.REQUEST_UPDATE_NAMES;}
			}.getPacket(), (Player)sender);
        	MainProxy.proxy.sendNameUpdateRequest((Player)sender);
        	return;
        } else if(arguments[0].equalsIgnoreCase("name") && arguments.length == 3) {
        	String idString = arguments[1];
        	String metaString = arguments[2];
        	int id = Integer.valueOf(idString);
        	int meta = Integer.valueOf(metaString);
        	ItemIdentifier item = ItemIdentifier.get(id, meta, null);
        	sender.sendChatToPlayer("Name: " + item.getFriendlyNameCC());
        	return;
        } else if(arguments[0].equalsIgnoreCase("help")) {
        	sender.sendChatToPlayer("Format: '"+ this.getCommandName() +" <command> <arguments>'");
        	sender.sendChatToPlayer("Available commands:");
        	sender.sendChatToPlayer("- version : Version information.");
        	sender.sendChatToPlayer("- routingthread : Display Routing thread status information.");
        	sender.sendChatToPlayer("- transfernames : Sends all item names form the client to the server to update the Language Database.");//TODO
        	return;
        } else if(LogisticsPipes.DEBUG) {
			if(arguments[0].equalsIgnoreCase("watch")) {
	        	new Watchdog(MainProxy.proxy.getSide().equals("Client"));
				LogisticsPipes.WATCHDOG = true;
	        	sender.sendChatToPlayer("Starting Watchdog");
	        	return;
	        }
        }
    	throw new WrongUsageException(this.getCommandUsage(sender));
	}

	private void commandVersion(ICommandSender sender, String[] arguments) {
    	sender.sendChatToPlayer(String.format("LogisticsPipes %s for Minecraft %s.", LogisticsPipes.class.getAnnotation(Mod.class).version(), LogisticsPipes.MCVersion));
	}
}

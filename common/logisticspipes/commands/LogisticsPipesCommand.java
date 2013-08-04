package logisticspipes.commands;

import java.util.Arrays;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.ActivatNBTDebug;
import logisticspipes.network.packets.RequestUpdateNamesPacket;
import logisticspipes.network.packets.debuggui.DebugAskForTarget;
import logisticspipes.proxy.MainProxy;
import logisticspipes.ticks.DebugGuiTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.ticks.Watchdog;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.FMLCommonHandler;
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
        } else if(arguments[0].equalsIgnoreCase("changelog")) {
        	commandChangelog(sender, arguments);
        	return;
        } else if(arguments[0].equalsIgnoreCase("NBTDEBUG")) {
        	if(!(sender instanceof Player)) {
        		throw new WrongUsageException("You can't use this command.");
        	}
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Trying to Enable NBTDebug"));
        	MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ActivatNBTDebug.class), (Player)sender);
        	return;
        } else if(arguments[0].equalsIgnoreCase("routingthread") || arguments[0].equalsIgnoreCase("rt")) {
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("RoutingTableUpdateThread: TODO: " + RoutingTableUpdateThread.size()));
        	return;
        } else if(arguments[0].equalsIgnoreCase("transfernames") || arguments[0].equalsIgnoreCase("tn")) {
        	if(!(sender instanceof Player)) {
        		throw new WrongUsageException("You can't use this command.");
        	}
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Requesting Transfer"));
        	MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RequestUpdateNamesPacket.class), (Player)sender);
        	MainProxy.proxy.sendNameUpdateRequest((Player)sender);
        	return;
        } else if(arguments[0].equalsIgnoreCase("name") && arguments.length == 3) {
        	String idString = arguments[1];
        	String metaString = arguments[2];
        	int id = Integer.valueOf(idString);
        	int meta = Integer.valueOf(metaString);
        	ItemIdentifier item = ItemIdentifier.get(id, meta, null);
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Name: " + item.getFriendlyNameCC()));
        	return;
        } else if(arguments[0].equalsIgnoreCase("help")) {
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Format: '"+ this.getCommandName() +" <command> <arguments>'"));
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Available commands:"));
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("- version : Version information."));
        	if(VersionChecker.hasNewVersion) sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("- changelog : Shows the changelog for the new version."));
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("- routingthread : Display Routing thread status information."));
        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("- transfernames : Sends all item names form the client to the server to update the Language Database."));//TODO
        	return;
        } else if(arguments[0].equalsIgnoreCase("dump")) {
			Watchdog.dump(false, false, true);
			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Dump Created"));
        	return;
        } else if(arguments[0].equalsIgnoreCase("bypass") || arguments[0].equalsIgnoreCase("bp")) {
        	if(!(sender instanceof EntityPlayer)) return;
        	if(!MinecraftServer.getServerConfigurationManager(FMLCommonHandler.instance().getMinecraftServerInstance()).getOps().contains(sender.getCommandSenderName().toLowerCase()) && !FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() && FMLCommonHandler.instance().getMinecraftServerInstance().isServerInOnlineMode()) {
        		sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("You are not allowed to use this command"));
        		return;
        }
        	if(!LogisticsSecurityTileEntity.byPassed.contains((EntityPlayer) sender)) {
        		LogisticsSecurityTileEntity.byPassed.add((EntityPlayer) sender);
        		sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Enabled"));
        	} else {
        		LogisticsSecurityTileEntity.byPassed.remove((EntityPlayer) sender);
        		sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Disabled"));
        	}
        	return;
        }
		if(LogisticsPipes.DEBUG) {
			if(arguments[0].equalsIgnoreCase("watch")) {
	        	new Watchdog(MainProxy.proxy.getSide().equals("Client"));
				LogisticsPipes.WATCHDOG = true;
	        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Starting Watchdog"));
	        	return;
	        }
		}
		if(LogisticsPipes.DEBUG || sender.getCommandSenderName().equals("davboecki") || sender.getCommandSenderName().equals("theZorro266")) {
			if(arguments[0].equalsIgnoreCase("debug")) {
	        	try {
	        		if(arguments[1].equalsIgnoreCase("me")) {
	        			DebugGuiTickHandler.instance().startWatchingOf(sender, (Player)sender);
	        			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Starting SelfDebuging"));
	        			return;
	    	       	} else if(arguments[1].equalsIgnoreCase("look") || arguments[1].equalsIgnoreCase("watch") || arguments[1].equalsIgnoreCase("target")) {
	        			if(sender instanceof EntityPlayer) {
	        				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugAskForTarget.class), (Player) sender);
	        				sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Asking for Target."));
	        			} else {
	        				sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("No player asking!"));
	        			}
	        			return;
	        		}
	        	} catch(Exception e) {
	        		e.printStackTrace();
	        	}
	        	return;
	        }
        }
    	throw new WrongUsageException(this.getCommandUsage(sender));
	}

	private void commandVersion(ICommandSender sender, String[] arguments) {
    	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(String.format("LogisticsPipes %s for Minecraft %s.", LogisticsPipes.class.getAnnotation(Mod.class).version(), LogisticsPipes.MCVersion)));
    	if(VersionChecker.hasNewVersion) {
    		sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Your LogisticsPipes version is outdated. The newest version is #" + VersionChecker.newVersion + "."));
    		sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Use \"/logisticspipes changelog\" to see a changelog."));
        }
	}

	private void commandChangelog(ICommandSender sender, String[] arguments) {
    	if(VersionChecker.hasNewVersion) {
    		sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("The newest version is #" + VersionChecker.newVersion + "."));
			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Changelog:"));
    		for(String msg:VersionChecker.changeLog) {
    			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(msg));
    		}
        }
	}
}

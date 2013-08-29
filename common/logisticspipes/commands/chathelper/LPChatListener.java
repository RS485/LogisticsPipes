package logisticspipes.commands.chathelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.OpenChatGui;
import logisticspipes.proxy.MainProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IChatListener;
import cpw.mods.fml.common.network.Player;

public class LPChatListener implements IChatListener {

	private static final Map<String,Callable<Boolean>> tasks = new HashMap<String,Callable<Boolean>>();
	private static final Map<String,MorePageDisplay> morePageDisplays = new HashMap<String,MorePageDisplay>();

	private List<String> sendChatMessages = null;
	
	@Override
	public Packet3Chat serverChat(NetHandler handler, Packet3Chat message) {
		if(tasks.containsKey(handler.getPlayer().getCommandSenderName())){
			if(message.message.startsWith("/")) {
				handler.getPlayer().sendChatToPlayer(ChatMessageComponent.func_111066_d(ChatColor.RED + "You need to answer the question, before you can use any other command"));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) handler.getPlayer());
			} else {
				if(!message.message.equalsIgnoreCase("true") && !message.message.equalsIgnoreCase("false") && !message.message.equalsIgnoreCase("on") && !message.message.equalsIgnoreCase("off") && !message.message.equalsIgnoreCase("0") && !message.message.equalsIgnoreCase("1") && !message.message.equalsIgnoreCase("no") && !message.message.equalsIgnoreCase("yes")){
					handler.getPlayer().sendChatToPlayer(ChatMessageComponent.func_111066_d(ChatColor.RED + "Not a valid answer."));
					handler.getPlayer().sendChatToPlayer(ChatMessageComponent.func_111066_d(ChatColor.AQUA + "Please enter " + ChatColor.RESET + "<" + ChatColor.GREEN + "yes" + ChatColor.RESET + "/" + ChatColor.RED + "no " + ChatColor.RESET + "| " + ChatColor.GREEN + "true" + ChatColor.RESET + "/" + ChatColor.RED + "flase " + ChatColor.RESET + "| " + ChatColor.GREEN + "on" + ChatColor.RESET + "/" + ChatColor.RED + "off " + ChatColor.RESET + "| " + ChatColor.GREEN + "1" + ChatColor.RESET + "/" + ChatColor.RED + "0" + ChatColor.RESET + ">"));
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) handler.getPlayer());
				} else {
					boolean flag = message.message.equalsIgnoreCase("true") || message.message.equalsIgnoreCase("on") || message.message.equalsIgnoreCase("1") || message.message.equalsIgnoreCase("yes");
					if(!handleAnswer(flag, handler.getPlayer())) {
						handler.getPlayer().sendChatToPlayer(ChatMessageComponent.func_111066_d(ChatColor.RED + "Error: Could not handle answer."));
					}
				}
			}
			message.message = "/lp dummy";
		} else if(morePageDisplays.containsKey(handler.getPlayer().getCommandSenderName())) {
			if(!morePageDisplays.get(handler.getPlayer().getCommandSenderName()).isTerminated()) {
				if(message.message.startsWith("/")) {
					handler.getPlayer().sendChatToPlayer(ChatMessageComponent.func_111066_d(ChatColor.RED+"Exit "+ChatColor.AQUA+"PageView"+ChatColor.RED+" first!"));
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) handler.getPlayer());
					message.message = "/lp dummy";
				} else {
					if(morePageDisplays.get(handler.getPlayer().getCommandSenderName()).handleChat(message.message, handler.getPlayer())) {
						message.message = "/lp dummy";
					}
				}
			}
		}
		return message;
	}
	
	@Override
	public Packet3Chat clientChat(NetHandler handler, Packet3Chat message) {
		if(message != null && message.message != null && message.message.equals("%LPCLEARCHAT%")) {
			clearChat();
			message.message = null;
		}
		if(message != null && message.message != null && message.message.equals("%LPSTORESENDMESSAGE%")) {
			storeSendMessages();
			message.message = null;
		}
		if(message != null && message.message != null && message.message.equals("%LPRESTORESENDMESSAGE%")) {
			restoreSendMessages();
			message.message = null;
		}
		if(message != null && message.message != null && message.message.startsWith("%LPADDTOSENDMESSAGE%")) {
			addSendMessages(message.message.substring(20));
			message.message = null;
		}
		return message;
	}

	@ClientSideOnlyMethodContent
	private void clearChat() {
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().clearChatMessages();
	}

	@SuppressWarnings("unchecked")
	@ClientSideOnlyMethodContent
	private void storeSendMessages() {
		sendChatMessages = new ArrayList<String>();
		sendChatMessages.addAll(FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().getSentMessages());
	}

	@ClientSideOnlyMethodContent
	private void restoreSendMessages() {
		if(sendChatMessages != null) {
			for(String o: sendChatMessages) {
				FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().addToSentMessages(o);
			}
			sendChatMessages = null;
		}
	}

	@ClientSideOnlyMethodContent
	private void addSendMessages(String substring) {
		if(sendChatMessages != null) {
			sendChatMessages.add(substring);
		} else {
			FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().addToSentMessages(substring);
		}
	}

	public static void register(MorePageDisplay displayInput, String name){
		if(morePageDisplays.containsKey(name) && !morePageDisplays.get(name).isTerminated()) return;
		morePageDisplays.put(name, displayInput);
	}

	public static void remove(String name) {
		morePageDisplays.remove(name);
	}
	
	public boolean handleAnswer(boolean flag, ICommandSender sender){
		if(!tasks.containsKey(sender.getCommandSenderName())) return false;
		if(flag) {
			try {
				Boolean result;
				if((result = tasks.get(sender.getCommandSenderName()).call()) != null)
				{
					if(result != null && !result) {
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(ChatColor.GREEN + "Answer handled."));
		}
		tasks.remove(sender.getCommandSenderName());
		return true;
	}
	
	public static boolean existTaskFor(String name){
		return tasks.containsKey(name);
	}

	public static void removeTask(String name){
		tasks.remove(name);
	}
	
	public static boolean addTask(Callable<Boolean> input, ICommandSender sender){
		if(tasks.containsKey(sender.getCommandSenderName())) {
			return false;
		} else {
			tasks.put(sender.getCommandSenderName(), input);
			return true;
		}
	}
}

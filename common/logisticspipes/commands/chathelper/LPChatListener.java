package logisticspipes.commands.chathelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import logisticspipes.LPConstants;
import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.OpenChatGui;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.string.ChatColor;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.ServerChatEvent;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class LPChatListener {

	private static final Map<String, Callable<Boolean>> tasks = new HashMap<String, Callable<Boolean>>();
	private static final Map<String, MorePageDisplay> morePageDisplays = new HashMap<String, MorePageDisplay>();

	private List<String> sendChatMessages = null;

	@SubscribeEvent
	public void serverChat(ServerChatEvent event) {
		EntityPlayerMP player = event.player;
		if (LPChatListener.tasks.containsKey(event.username)) {
			if (event.message.startsWith("/")) {
				player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "You need to answer the question, before you can use any other command"));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
			} else {
				if (!event.message.equalsIgnoreCase("true") && !event.message.equalsIgnoreCase("false") && !event.message.equalsIgnoreCase("on") && !event.message.equalsIgnoreCase("off") && !event.message.equalsIgnoreCase("0") && !event.message.equalsIgnoreCase("1") && !event.message.equalsIgnoreCase("no")
						&& !event.message.equalsIgnoreCase("yes")) {
					player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "Not a valid answer."));
					player.addChatComponentMessage(new ChatComponentText(ChatColor.AQUA + "Please enter " + ChatColor.RESET + "<" + ChatColor.GREEN + "yes" + ChatColor.RESET + "/" + ChatColor.RED + "no " + ChatColor.RESET + "| " + ChatColor.GREEN + "true" + ChatColor.RESET + "/" + ChatColor.RED + "flase "
							+ ChatColor.RESET + "| " + ChatColor.GREEN + "on" + ChatColor.RESET + "/" + ChatColor.RED + "off " + ChatColor.RESET + "| " + ChatColor.GREEN + "1" + ChatColor.RESET + "/" + ChatColor.RED + "0" + ChatColor.RESET + ">"));
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
				} else {
					boolean flag = event.message.equalsIgnoreCase("true") || event.message.equalsIgnoreCase("on") || event.message.equalsIgnoreCase("1") || event.message.equalsIgnoreCase("yes");
					if (!handleAnswer(flag, player)) {
						player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "Error: Could not handle answer."));
					}
				}
			}
			event.setCanceled(true);
		} else if (LPChatListener.morePageDisplays.containsKey(event.username)) {
			if (!LPChatListener.morePageDisplays.get(event.username).isTerminated()) {
				if (event.message.startsWith("/")) {
					player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "Exit " + ChatColor.AQUA + "PageView" + ChatColor.RED + " first!"));
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
					event.setCanceled(true);
				} else {
					if (LPChatListener.morePageDisplays.get(event.username).handleChat(event.message, player)) {
						event.setCanceled(true);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void clientChat(ClientChatReceivedEvent event) {
		IChatComponent message = event.message;
		if (message != null) {
			String realMessage = null;
			try {
				realMessage = message.getFormattedText();
			} catch (ClassCastException e) {
				//Ignore that
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (realMessage != null) {
				if (realMessage.equals("%LPCLEARCHAT%")) {
					clearChat();
					event.setCanceled(true);
				}
				if (realMessage.equals("%LPSTORESENDMESSAGE%")) {
					storeSendMessages();
					event.setCanceled(true);
				}
				if (realMessage.equals("%LPRESTORESENDMESSAGE%")) {
					restoreSendMessages();
					event.setCanceled(true);
				}
				if (realMessage.startsWith("%LPADDTOSENDMESSAGE%")) {
					addSendMessages(realMessage.substring(20));
					event.setCanceled(true);
				}
				if (realMessage.contains("LPDISPLAYMISSING") && LPConstants.DEBUG) {
					System.out.println("LIST:");
					for (String key : StringUtils.UNTRANSLATED_STRINGS) {
						System.out.println(key);
					}
				}
			}
		}
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
		if (sendChatMessages != null) {
			for (String o : sendChatMessages) {
				FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().addToSentMessages(o);
			}
			sendChatMessages = null;
		}
	}

	@ClientSideOnlyMethodContent
	private void addSendMessages(String substring) {
		if (sendChatMessages != null) {
			sendChatMessages.add(substring);
		} else {
			FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().addToSentMessages(substring);
		}
	}

	public static void register(MorePageDisplay displayInput, String name) {
		if (LPChatListener.morePageDisplays.containsKey(name) && !LPChatListener.morePageDisplays.get(name).isTerminated()) {
			return;
		}
		LPChatListener.morePageDisplays.put(name, displayInput);
	}

	public static void remove(String name) {
		LPChatListener.morePageDisplays.remove(name);
	}

	public boolean handleAnswer(boolean flag, ICommandSender sender) {
		if (!LPChatListener.tasks.containsKey(sender.getCommandSenderName())) {
			return false;
		}
		if (flag) {
			try {
				Boolean result;
				if ((result = LPChatListener.tasks.get(sender.getCommandSenderName()).call()) != null) {
					if (result != null && !result) {
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			sender.addChatMessage(new ChatComponentText(ChatColor.GREEN + "Answer handled."));
		}
		LPChatListener.tasks.remove(sender.getCommandSenderName());
		return true;
	}

	public static boolean existTaskFor(String name) {
		return LPChatListener.tasks.containsKey(name);
	}

	public static void removeTask(String name) {
		LPChatListener.tasks.remove(name);
	}

	public static boolean addTask(Callable<Boolean> input, ICommandSender sender) {
		if (LPChatListener.tasks.containsKey(sender.getCommandSenderName())) {
			return false;
		} else {
			LPChatListener.tasks.put(sender.getCommandSenderName(), input);
			return true;
		}
	}
}

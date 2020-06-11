package logisticspipes.commands.chathelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import logisticspipes.LogisticsPipes;
import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.OpenChatGui;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.string.ChatColor;
import logisticspipes.utils.string.StringUtils;

public class LPChatListener {

	private static final Map<String, Callable<Boolean>> tasks = new HashMap<>();
	private static final Map<String, MorePageDisplay> morePageDisplays = new HashMap<>();

	private List<String> sendChatMessages = null;

	@SubscribeEvent
	public void serverChat(ServerChatEvent event) {
		EntityPlayerMP player = event.getPlayer();
		if (LPChatListener.tasks.containsKey(event.getUsername())) {
			if (event.getMessage().startsWith("/")) {
				player.sendMessage(new TextComponentString(ChatColor.RED + "You need to answer the question, before you can use any other command"));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
			} else {
				if (!event.getMessage().equalsIgnoreCase("true") && !event.getMessage().equalsIgnoreCase("false") && !event.getMessage().equalsIgnoreCase("on") && !event.getMessage().equalsIgnoreCase("off") && !event.getMessage().equalsIgnoreCase("0") && !event.getMessage().equalsIgnoreCase("1") && !event.getMessage()
						.equalsIgnoreCase("no")
						&& !event.getMessage().equalsIgnoreCase("yes")) {
					player.sendMessage(new TextComponentString(ChatColor.RED + "Not a valid answer."));
					player.sendMessage(new TextComponentString(ChatColor.AQUA + "Please enter " + ChatColor.RESET + "<" + ChatColor.GREEN + "yes" + ChatColor.RESET + "/" + ChatColor.RED + "no " + ChatColor.RESET + "| " + ChatColor.GREEN + "true" + ChatColor.RESET + "/" + ChatColor.RED + "flase "
							+ ChatColor.RESET + "| " + ChatColor.GREEN + "on" + ChatColor.RESET + "/" + ChatColor.RED + "off " + ChatColor.RESET + "| " + ChatColor.GREEN + "1" + ChatColor.RESET + "/" + ChatColor.RED + "0" + ChatColor.RESET + ">"));
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
				} else {
					boolean flag = event.getMessage().equalsIgnoreCase("true") || event.getMessage().equalsIgnoreCase("on") || event.getMessage().equalsIgnoreCase("1") || event.getMessage().equalsIgnoreCase("yes");
					if (!handleAnswer(flag, player)) {
						player.sendMessage(new TextComponentString(ChatColor.RED + "Error: Could not handle answer."));
					}
				}
			}
			event.setCanceled(true);
		} else if (LPChatListener.morePageDisplays.containsKey(event.getUsername())) {
			if (!LPChatListener.morePageDisplays.get(event.getUsername()).isTerminated()) {
				if (event.getMessage().startsWith("/")) {
					player.sendMessage(new TextComponentString(ChatColor.RED + "Exit " + ChatColor.AQUA + "PageView" + ChatColor.RED + " first!"));
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), player);
					event.setCanceled(true);
				} else {
					if (LPChatListener.morePageDisplays.get(event.getUsername()).handleChat(event.getMessage(), player)) {
						event.setCanceled(true);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void clientChat(ClientChatReceivedEvent event) {
		ITextComponent message = event.getMessage();
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
				if (realMessage.contains("LPDISPLAYMISSING") && LogisticsPipes.isDEBUG()) {
					System.out.println("LIST:");
					StringUtils.UNTRANSLATED_STRINGS.forEach(System.out::println);
				}
			}
		}
	}

	@ClientSideOnlyMethodContent
	private void clearChat() {
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().clearChatMessages(true);
	}

	@ClientSideOnlyMethodContent
	private void storeSendMessages() {
		sendChatMessages = new ArrayList<>();
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
		if (!LPChatListener.tasks.containsKey(sender.getName())) {
			return false;
		}
		if (flag) {
			try {
				Boolean result;
				if ((result = LPChatListener.tasks.get(sender.getName()).call()) != null) {
					if (result != null && !result) {
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			sender.sendMessage(new TextComponentString(ChatColor.GREEN + "Answer handled."));
		}
		LPChatListener.tasks.remove(sender.getName());
		return true;
	}

	public static boolean existTaskFor(String name) {
		return LPChatListener.tasks.containsKey(name);
	}

	public static void removeTask(String name) {
		LPChatListener.tasks.remove(name);
	}

	public static boolean addTask(Callable<Boolean> input, ICommandSender sender) {
		if (LPChatListener.tasks.containsKey(sender.getName())) {
			return false;
		} else {
			LPChatListener.tasks.put(sender.getName(), input);
			return true;
		}
	}
}

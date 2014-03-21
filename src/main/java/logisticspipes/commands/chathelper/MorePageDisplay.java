package logisticspipes.commands.chathelper;

import java.util.ArrayList;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.OpenChatGui;
import logisticspipes.proxy.MainProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.network.Player;

public class MorePageDisplay {
	
	private ArrayList<String> header = new ArrayList<String>();
	private ArrayList<StringConnected> content = new ArrayList<StringConnected>();
	
	private final int row = 20;
	private final int colum = 55;
	
	private int currentpage = 0;
	private int currentpagecount = 0;
	private boolean terminated = false;
	
	public MorePageDisplay(ArrayList<String> header, ICommandSender name) {
		if(header.size() <= (row - 2)) {
			this.header = header;
		}
		LPChatListener.register(this, name.getCommandSenderName());
		name.getCommandSenderName();
		name.sendChatToPlayer(ChatMessageComponent.createFromText("%LPSTORESENDMESSAGE%"));
	}
	
	public MorePageDisplay(String[] header, ICommandSender name) {
		if(header.length <= (row - 2)) {
			this.header = new ArrayList<String>();
			for(int i = 0; i < header.length; i++) {
				this.header.add(header[i]);
			}
		}
		LPChatListener.register(this, name.getCommandSenderName());
		name.getCommandSenderName();
		name.sendChatToPlayer(ChatMessageComponent.createFromText("%LPSTORESENDMESSAGE%"));
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColum() {
		return colum;
	}
	
	public void append(String input) {
		append(input, false);
	}
	
	public void append(String input, boolean flag) {
		content.add(new StringConnected(input, flag));
		System.out.println("Adding: " + input);
	}
	
	public boolean isTerminated() {
		return terminated;
	}
	
	public void display(ICommandSender sender) {
		display(sender, 1);
	}
	
	public void display(ICommandSender player, int page) {
		display(player, page, false);
	}
	
	private String replaceMeta(String input, int page, int count) {
		String output = "";
		int pagecount = getPageCount(count);
		if(count == -1) pagecount = 0;
		if(count == -2) pagecount = 1;
		for(int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			switch(c) {
				case '%':
					output += String.valueOf(page);
					break;
				case '$':
					output += String.valueOf(pagecount);
					break;
				default:
					output += c;
					break;
			}
		}
		boolean LeftDone = false;
		boolean RightDone = false;
		String output2 = "";
		// Add = for <>
		for(int i = 0; i < output.length(); i++) {
			char c = output.charAt(i);
			switch(c) {
				case '<':
					if(LeftDone) break;
					LeftDone = true;
					output2 += ChatColor.AQUA;
					for(int j = 0; j < ((colum - output.length()) / 2); j++) {
						output2 += "=";
					}
					output2 += ChatColor.WHITE;
					break;
				case '>':
					if(RightDone) break;
					RightDone = true;
					output2 += ChatColor.AQUA;
					for(int j = 0; j < ((colum - output.length()) / 2); j++) {
						output2 += "=";
					}
					output2 += ChatColor.WHITE;
					break;
				case '(':
					if(LeftDone) break;
					LeftDone = true;
					for(int j = 0; j < ((colum - output.length()) / 2); j++) {
						output2 += " ";
					}
					break;
				case ')':
					if(RightDone) break;
					RightDone = true;
					for(int j = 0; j < ((colum - output.length()) / 2); j++) {
						output2 += " ";
					}
					break;
				default:
					output2 += c;
					break;
			}
		}
		return output2;
	}
	
	public boolean handleChat(String input, ICommandSender sender) {
		if(terminated) return false;
		if(input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("q") || input.equalsIgnoreCase("e")) {
			terminated = true;
			sender.sendChatToPlayer(ChatMessageComponent.createFromText("%LPCLEARCHAT%"));
			for(Object zeilenobject: header.toArray()) {
				if( !(zeilenobject instanceof String)) continue;
				sender.sendChatToPlayer(ChatMessageComponent.createFromText(replaceMeta((String) zeilenobject, 0, -1)));
			}
			clearscreen(sender, 19 - header.size());
			sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.AQUA + "Pageview: " + ChatColor.RED + "Exit."));
			sender.sendChatToPlayer(ChatMessageComponent.createFromText("%LPRESTORESENDMESSAGE%"));
		} else if(input.equalsIgnoreCase("next") || input.equalsIgnoreCase("nex") || input.equalsIgnoreCase("n")) {
			if(currentpage > (currentpagecount - 1)) {
				display(sender, 0);
			} else {
				currentpage++;
				display(sender, currentpage);
			}
			if(sender instanceof Player) MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) sender);
		} else if(input.equalsIgnoreCase("previous") || input.equalsIgnoreCase("prev") || input.equalsIgnoreCase("pre") || input.equalsIgnoreCase("p")) {
			if(currentpage < 2) {
				display(sender, currentpagecount);
			} else {
				currentpage--;
				display(sender, currentpage);
			}
		} else if(isNumber(input)) {
			if(toNumber(input) <= currentpagecount && toNumber(input) > 0) {
				display(sender, toNumber(input));
			} else {
				display(sender, currentpage, true);
				sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.AQUA + "Pageview:" + ChatColor.RED + " Not a valid number."));
			}
		} else if(input.equalsIgnoreCase("reprint")) {
			display(sender, currentpage);
		} else if(input.equalsIgnoreCase("all")) {
			display(sender, currentpage, false, true, 0);
		} else if(input.startsWith("save ")) {
			sender.sendChatToPlayer(ChatMessageComponent.createFromText("%LPADDTOSENDMESSAGE%" + input.substring(5)));
			display(sender, currentpage, true, false, 1);
			sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.AQUA + "Added '" + ChatColor.YELLOW + input.substring(5) + ChatColor.AQUA + "' to your chat history."));
			printLastLine(sender, false);
			if(sender instanceof Player) MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) sender);
		} else if(input.equals("save")) {
			display(sender, currentpage, true, false, 2);
			sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.AQUA + "Add an command after the '" + ChatColor.YELLOW + "save " + ChatColor.AQUA + "' and it will be added to your chat history."));
			printLastLine(sender, false);
			if(sender instanceof Player) MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) sender);
		} else {
			//display(sender,currentpage,true);
			printLastLine(sender, true);
			if(sender instanceof Player) MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) sender);
		}
		return true;
	}
	
	public static boolean isNumber(String input) {
		try {
			int number = Integer.parseInt(input);
			String numstring = String.valueOf(number);
			if(numstring.equalsIgnoreCase(input)) {
				return true;
			}
		} catch(Exception e) {}
		return false;
	}
	
	public static int toNumber(String input) {
		try {
			return Integer.parseInt(input);
		} catch(Exception e) {}
		return -1;
	}
	
	private void clearscreen(ICommandSender sender, int count) {
		for(int i = 0; i < count; i++) {
			sender.sendChatToPlayer(ChatMessageComponent.createFromText(""));
		}
	}
	
	public void display(ICommandSender sender, int page, boolean flag) {
		display(sender, page, flag, false, 0);
	}
	
	public void printLastLine(ICommandSender sender) {
		printLastLine(sender, false);
	}
	
	public void printLastLine(ICommandSender sender, boolean flag) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromText(
				(flag ? "! " : "") + 
				ChatColor.AQUA + "Pageview:" + 
				ChatColor.WHITE + " Enter " + 
				ChatColor.RED + "Pre" + 
				ChatColor.WHITE + "/" + 
				ChatColor.GREEN + "Next" + 
				ChatColor.WHITE + ", a " + 
				ChatColor.AQUA + "number" + 
				ChatColor.WHITE + ", " + 
				ChatColor.AQUA + "all" + 
				ChatColor.WHITE + ", " + 
				ChatColor.AQUA + "reprint" + 
				ChatColor.WHITE + ", " +
				ChatColor.AQUA + "save" + 
				ChatColor.WHITE + " or " + 
				ChatColor.RED + "exit" + 
				ChatColor.WHITE + 
				(flag ? " !" : ".")
				));
	}
	
	public int getPageCount(int count) {
		int currentPage = 0;
		int lineOnCurentPage = 0;
		for(int i = 0; i < content.size(); i++, lineOnCurentPage++) {
			int I;
			for(I = i; I < (content.size() - 1) && content.get(I + 1).connected; I++);
			int needed = I - i;
			if(lineOnCurentPage + needed > count - 1) {
				currentPage++;
				lineOnCurentPage = 0;
			}
		}
		return currentPage + 1;
	}
	
	public void display(ICommandSender sender, int page, boolean flag, boolean all, int linesub) {
		if(terminated) return;
		sender.sendChatToPlayer(ChatMessageComponent.createFromText("%LPCLEARCHAT%"));
		int count = row - header.size() - 1 - linesub;
		page = (page > 0 && !all ? page : 1);
		currentpage = page;
		int pagecount = getPageCount(count);
		currentpagecount = pagecount;
		if(all) count = -2;
		for(Object zeilenobject: header.toArray()) {
			if( !(zeilenobject instanceof String)) continue;
			sender.sendChatToPlayer(ChatMessageComponent.createFromText(replaceMeta((String) zeilenobject, page, count)));
		}
		int currentPage = 0;
		int lineOnCurentPage = 0;
		int doneLines = 0;
		for(int i = 0; i < content.size(); i++, lineOnCurentPage++) {
			if(all) {
				sender.sendChatToPlayer(ChatMessageComponent.createFromText(content.get(i).content));
			} else {
				int I;
				for(I = i; I < (content.size() - 1) && content.get(I + 1).connected; I++);
				int needed = I - i;
				if(lineOnCurentPage + needed > count - 1) {
					currentPage++;
					lineOnCurentPage = 0;
				}
				if(page == currentPage + 1) {
					sender.sendChatToPlayer(ChatMessageComponent.createFromText(content.get(i).content));
					doneLines = lineOnCurentPage;
				}
			}
		}
		if( !all) clearscreen(sender, row - 2 - header.size() - doneLines - linesub);
		if(all && count > content.size()) clearscreen(sender, count - content.size());
		if( !flag) printLastLine(sender);
		if(sender instanceof Player) MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) sender);
	}
	
	private class StringConnected {
		StringConnected(String s, boolean b) {
			content = s;
			connected = b;
		}
		
		public String content;
		public boolean connected;
	}
}

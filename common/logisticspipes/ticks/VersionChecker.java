package logisticspipes.ticks;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import net.minecraft.nbt.NBTTagCompound;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;


public class VersionChecker extends Thread {

	public static boolean hasNewVersion = false;
	public static String newVersion = "";
	public static List<String> changeLog = new ArrayList<String>(0);
	public static boolean sentIMCMessage;

	public VersionChecker() {
		this.setDaemon(true);
		this.start();
	}
	
	@Override
	@SuppressWarnings({ "resource", "rawtypes", "unchecked" })
	public void run() {
		try {
			if(LPConstants.VERSION.equals("%"+"VERSION%:%DEBUG"+"%")) return;
			if(LPConstants.VERSION.contains("-")) return;
			URL url = new URL("http://rs485.thezorro266.com/version/check.php?VERSION=" + LPConstants.VERSION);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream inputStream = (InputStream) conn.getContent();
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			String string = s.next();
			s.close();
			if(!Configs.CHECK_FOR_UPDATES) return;
			Gson gson = new Gson();
			LinkedTreeMap part = gson.fromJson(string, LinkedTreeMap.class);
			Boolean hasNew = (Boolean) part.get("new");
			if(hasNew) {
				VersionChecker.hasNewVersion = true;
				VersionChecker.newVersion = Integer.toString(Double.valueOf(part.get("build").toString()).intValue());
				LogisticsPipes.log.info("New LogisticsPipes" + (LPConstants.DEV_BUILD?"-Dev":"") + " version found: #" + Double.valueOf(part.get("build").toString()).intValue());
				LinkedTreeMap changeLog = (LinkedTreeMap) part.get("changelog");
				List<String> changeLogList = new ArrayList<String>();
				if(changeLog != null) {
					for(Object oVersion:changeLog.keySet()) {
						String build = oVersion.toString();
						changeLogList.add(new StringBuilder(build).append(": ").toString());
						List<String> sub = (List<String>) changeLog.get(build);
						for(String msg:sub) {
							if(msg.length() > 60) {
								boolean first = true;
								while(!msg.isEmpty()) {
									int splitAt = msg.substring(0, Math.min(first ? 60 : 55, msg.length())).lastIndexOf(' ');
									if(msg.length() < 60) {
										splitAt = msg.length();
									}
									if(splitAt <= 0) {
										splitAt = Math.min(first ? 60 : 55, msg.length());
									} else if(msg.length() > 60 && splitAt < 40) {
										splitAt = Math.min(first ? 60 : 55, msg.length());
									}
									changeLogList.add((first ? "" : "    ") + msg.substring(0, splitAt));
									msg = msg.substring(splitAt);
									first = false;
								}
							} else {
								changeLogList.add(msg);
							}
						}
					}
				}
				VersionChecker.changeLog = changeLogList;
				sendIMCOutdatedMessage();
			}
		} catch(MalformedURLException e) {
			if (Configs.CHECK_FOR_UPDATES){
				e.printStackTrace();
			}
		} catch(IOException e) {
			if (Configs.CHECK_FOR_UPDATES){
				e.printStackTrace();
			}
		}
	}

	/**
	 * Integration with Version Checker (http://www.minecraftforum.net/topic/2721902-/)
	 */
	public static void sendIMCOutdatedMessage() {
		if (Loader.isModLoaded("VersionChecker")) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("oldVersion", LPConstants.VERSION);
			tag.setString("newVersion", newVersion);
			tag.setString("updateUrl", "http://ci.thezorro266.com/view/Logistics%20Pipes/");
			tag.setBoolean("isDirectLink", false);

			StringBuilder stringBuilder = new StringBuilder();
			for (String changeLogLine : changeLog) {
				stringBuilder.append(changeLogLine).append("\n");
			}
			tag.setString("changeLog", stringBuilder.toString());
			FMLInterModComms.sendRuntimeMessage("LogisticsPipes", "VersionChecker", "addUpdate", tag);
			sentIMCMessage = true;
		}
	}
}

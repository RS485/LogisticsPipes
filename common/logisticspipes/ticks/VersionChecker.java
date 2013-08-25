package logisticspipes.ticks;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import logisticspipes.LogisticsPipes;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class VersionChecker extends Thread {

	public static boolean hasNewVersion = false;
	public static String newVersion = "";
	public static List<String> changeLog = new ArrayList<String>(0);

	public VersionChecker() {
		this.setDaemon(true);
		this.start();
	}
	
	@Override
	@SuppressWarnings({ "resource", "rawtypes", "unchecked" })
	public void run() {
		try {
			if(LogisticsPipes.VERSION.equals("%"+"VERSION%:%DEBUG"+"%")) return;
			if(LogisticsPipes.VERSION.contains("-")) return;
			URL url = new URL("http://rs485.thezorro266.com/version/check.php?VERSION=" + LogisticsPipes.VERSION);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream inputStream = (InputStream) conn.getContent();
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			String string = s.next();
			s.close();
			Gson gson = new Gson();
			LinkedTreeMap part = gson.fromJson(string, LinkedTreeMap.class);
			Boolean hasNew = (Boolean) part.get("new");
			if(hasNew) {
				VersionChecker.hasNewVersion = true;
				VersionChecker.newVersion = Integer.toString(Double.valueOf(part.get("build").toString()).intValue());
				LogisticsPipes.log.warning("New LogisticsPipes" + (LogisticsPipes.DEV_BUILD?"-Dev":"") + " version found: #" + Double.valueOf(part.get("build").toString()).intValue());
				LinkedTreeMap changeLog = (LinkedTreeMap) part.get("changelog");
				List<String> changeLogList = new ArrayList<String>();
				if(changeLog != null) {
					for(Object oVersion:changeLog.keySet()) {
						String build = oVersion.toString();
						changeLogList.add(new StringBuilder(build).append(": ").toString());
						List<String> sub = (List<String>) changeLog.get(build);
						for(String msg:sub) {
							changeLogList.add(msg);
						}
					}
				}
				VersionChecker.changeLog = changeLogList;
			}
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}

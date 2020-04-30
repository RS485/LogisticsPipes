package logisticspipes.ticks;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import lombok.Data;

import logisticspipes.LogisticsPipes;

public final class VersionChecker implements Callable<VersionChecker.VersionInfo> {

	public static final int COMMIT_MAX_LINE_LENGTH = 60;

	private static Future<VersionInfo> versionCheckFuture;
	private String statusString;
	private VersionInfo versionInfo = null;

	private VersionChecker() {
	}

	public static VersionChecker runVersionCheck() {
		VersionChecker obj = new VersionChecker();
		versionCheckFuture = LogisticsPipes.singleThreadExecutor.submit(obj);
		return obj;
	}

	public String getVersionCheckerStatus() {
		if (versionCheckFuture != null) {
			if (versionCheckFuture.isDone()) {
				// has to be done, before getting final string and setting to null
				statusString = internalGetVersionCheckerStatus();
				versionCheckFuture = null;
			} else {
				statusString = internalGetVersionCheckerStatus();
			}
		}
		return statusString;
	}

	public boolean isVersionCheckDone() {
		return versionInfo != null;
	}

	public VersionInfo getVersionInfo() {
		return versionInfo;
	}

	private String internalGetVersionCheckerStatus() {
		if (versionCheckFuture.isDone()) {
			try {
				versionInfo = versionCheckFuture.get();
				if (versionInfo == null) {
					if (LogisticsPipes.isDevelopmentEnvironment()) {
						return "You are running Logistics Pipes from a development environment.";
					} else {
						return "It seems you are missing the current version information on Logistics Pipes. There is no version checking available.";
					}
				} else {
					if (versionInfo.isNewVersionAvailable()) {
						return "New Logistics Pipes build found: #" + versionInfo.getNewestBuild();
					} else {
						return "You have the newest Logistics Pipes build :)";
					}
				}
			} catch (InterruptedException e) {
				return "The version check task was interrupted and there is no version information available.";
			} catch (ExecutionException e) {
				LogisticsPipes.log.warn("The version check task had an exception while getting the newest version information", e);
				return "The version check task had an exception. See the log file for more information.";
			}
		} else {
			return "The version check is not yet ready, sorry.";
		}
	}

	@Override
	public VersionInfo call() throws Exception {
		if (LogisticsPipes.UNKNOWN.equals(LogisticsPipes.getVERSION())) {
			return null;
		}

		VersionInfo versionInfo = new VersionInfo();
		URL url = new URL(String.format("http://rs485.network/version?VERSION=%s:%b", LogisticsPipes.getVERSION(), LogisticsPipes.isDEBUG()));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		InputStream inputStream = (InputStream) conn.getContent();
		String jsonString;
		try (Scanner sc = new Scanner(inputStream)) {
			sc.useDelimiter("\\A");
			jsonString = sc.next();
		}

		Gson gson = new Gson();
		LinkedTreeMap part = gson.fromJson(jsonString, LinkedTreeMap.class);

		Boolean hasNew = (Boolean) part.get("new");
		versionInfo.setNewVersionAvailable(hasNew);
		if (hasNew) {
			versionInfo.setNewestBuild(String.valueOf(part.get("build")));
			LogisticsPipes.log.info("New Logistics Pipes build found: #" + versionInfo.getNewestBuild());

			@SuppressWarnings("unchecked")
			LinkedTreeMap<String, List<String>> changelog = (LinkedTreeMap<String, List<String>>) part.get("changelog");

			List<String> changeLogList = new ArrayList<>();
			if (changelog != null) {
				for (String build : changelog.keySet()) {
					changeLogList.add(build + ": ");
					for (String commit : changelog.get(build)) {
						if (commit.length() > COMMIT_MAX_LINE_LENGTH) {
							String prefix = "    ";
							boolean first = true;
							while (!commit.isEmpty()) {
								int maxLength;
								if (first) {
									maxLength = COMMIT_MAX_LINE_LENGTH;
								} else {
									maxLength = COMMIT_MAX_LINE_LENGTH - prefix.length();
								}
								int splitAt = commit.substring(0, Math.min(maxLength, commit.length())).lastIndexOf(' ');
								if (commit.length() < COMMIT_MAX_LINE_LENGTH) {
									splitAt = commit.length();
								}
								if (splitAt <= 0) {
									splitAt = Math.min(maxLength, commit.length());
								} else if (commit.length() > COMMIT_MAX_LINE_LENGTH && splitAt < COMMIT_MAX_LINE_LENGTH - 20) {
									splitAt = Math.min(maxLength, commit.length());
								}
								changeLogList.add((first ? "" : prefix) + commit.substring(0, splitAt));
								commit = commit.substring(splitAt);
								first = false;
							}
						} else {
							changeLogList.add(commit);
						}
					}
				}
			}

			versionInfo.setChangelog(changeLogList);
			sendIMCOutdatedMessage(versionInfo);
		}
		return versionInfo;
	}

	/**
	 * Integration with Version Checker
	 * (http://www.minecraftforum.net/topic/2721902-/)
	 */
	private void sendIMCOutdatedMessage(VersionInfo versionInfo) {
		if (Loader.isModLoaded("VersionChecker")) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("oldVersion", LogisticsPipes.getVERSION());
			tag.setString("newVersion", versionInfo.getNewestBuild());
			tag.setString("updateUrl", "http://ci.rs485.network/view/Logistics%20Pipes/");
			tag.setBoolean("isDirectLink", false);

			StringBuilder stringBuilder = new StringBuilder();
			for (String changeLogLine : versionInfo.getChangelog()) {
				stringBuilder.append(changeLogLine).append("\n");
			}
			tag.setString("changeLog", stringBuilder.toString());
			FMLInterModComms.sendRuntimeMessage("LogisticsPipes", "VersionChecker", "addUpdate", tag);
			versionInfo.setImcMessageSent(true);
		}
	}

	@Data
	public static class VersionInfo {

		private boolean newVersionAvailable;
		private boolean imcMessageSent;
		private String newestBuild;
		private List<String> changelog;
	}
}

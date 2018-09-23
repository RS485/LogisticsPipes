package logisticspipes.network;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import lombok.SneakyThrows;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.PopupGuiProvider;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.network.packets.gui.GUIPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SubGuiScreen;
import network.rs485.logisticspipes.util.LPDataIOWrapper;

public class NewGuiHandler {

	public static List<GuiProvider> guilist;
	public static Map<Class<? extends GuiProvider>, GuiProvider> guimap;

	private NewGuiHandler() { }

	@SuppressWarnings("unchecked")
	// Suppressed because this cast should never fail.
	public static <T extends GuiProvider> T getGui(Class<T> clazz) {
		return (T) NewGuiHandler.guimap.get(clazz).template();
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows({ IOException.class }) // , InvocationTargetException.class, IllegalAccessException.class, InstantiationException.class
	// Suppression+sneakiness because these shouldn't ever fail, and if they do, it needs to fail.
	public static final void initialize() {
		final List<ClassInfo> classes = new ArrayList<>(ClassPath.from(NewGuiHandler.class.getClassLoader())
				.getTopLevelClassesRecursive("logisticspipes.network.guis"));

		loadGuiProvidersFromList(classes.stream().map(ClassInfo::getName).collect(Collectors.toList()));

		if (NewGuiHandler.guilist == null || NewGuiHandler.guilist.isEmpty()) {
			System.err.println(
					"LogisticsPipes could not search for its GuiProvider classes. Your mods are probably installed in a path containing spaces or special characters. Trying to use fallback solution.");
			URL location = NewGuiHandler.class.getProtectionDomain().getCodeSource().getLocation();
			String locationString = location.toString();
			if (locationString.startsWith("jar:file:/") && locationString.contains("!")) {
				locationString = locationString.substring(10, locationString.indexOf('!'));
				locationString = java.net.URLDecoder.decode(locationString, "UTF-8");
			}

			List<String> classInfoList = new ArrayList<>();

			File file = new File(locationString);
			if (file.exists() && !file.isDirectory()) {
				JarFile jar = new JarFile(file);
				Enumeration<JarEntry> entriesS = jar.entries();
				while (entriesS.hasMoreElements()) {
					JarEntry entryB = entriesS.nextElement();
					if (!entryB.isDirectory() && entryB.getName().startsWith("logisticspipes/network/guis/")) {
						String filename = entryB.getName();
						int classNameEnd = filename.length() - ".class".length();
						filename = filename.substring(0, classNameEnd).replace('/', '.');
						classInfoList.add(filename);
					}
				}
			}

			loadGuiProvidersFromList(classInfoList);

			if (NewGuiHandler.guilist.isEmpty()) {
				System.err.println("Fallback solution failed. Please try to move your minecraft folder to a different location.");
				throw new RuntimeException("Cannot load GuiProvider Classes");
			}
		}
	}

	private static void loadGuiProvidersFromList(List<String> classes) {
		classes.sort(Comparator.comparing(it -> it));

		NewGuiHandler.guilist = new ArrayList<>(classes.size());
		NewGuiHandler.guimap = new HashMap<>(classes.size());

		int currentId = 0;
		for (String c : classes) {
			try {
				final Class<?> cls = PacketHandler.class.getClassLoader().loadClass(c);
				final GuiProvider instance = (GuiProvider) cls.getConstructors()[0].newInstance(currentId);
				NewGuiHandler.guilist.add(instance);
				NewGuiHandler.guimap.put((Class<? extends GuiProvider>) cls, instance);
				currentId++;
			} catch (Throwable ignoredButPrinted) {
				ignoredButPrinted.printStackTrace();
			}
		}
	}

	public static void openGui(GuiProvider guiProvider, EntityPlayer oPlayer) {
		if (!(oPlayer instanceof EntityPlayerMP)) {
			throw new UnsupportedOperationException("Gui can only be opened on the server side");
		}
		EntityPlayerMP player = (EntityPlayerMP) oPlayer;
		Container container = guiProvider.getContainer(player);
		if (container == null) {
			if (guiProvider instanceof PopupGuiProvider) {
				GUIPacket packet = PacketHandler.getPacket(GUIPacket.class);
				packet.setGuiID(guiProvider.getId());
				packet.setWindowID(-2);
				packet.setGuiData(LPDataIOWrapper.collectData(guiProvider::writeData));
				MainProxy.sendPacketToPlayer(packet, player);
			}
			return;
		}
		player.getNextWindowId();
		player.closeContainer();
		int windowId = player.currentWindowId;

		GUIPacket packet = PacketHandler.getPacket(GUIPacket.class);
		packet.setGuiID(guiProvider.getId());
		packet.setWindowID(windowId);
		packet.setGuiData(LPDataIOWrapper.collectData(guiProvider::writeData));
		MainProxy.sendPacketToPlayer(packet, player);

		player.openContainer = container;
		player.openContainer.windowId = windowId;
		player.openContainer.addListener(player);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, player.openContainer));
	}

	@SideOnly(Side.CLIENT)
	public static void openGui(GUIPacket packet, EntityPlayer player) {
		int guiID = packet.getGuiID();
		GuiProvider provider = NewGuiHandler.guilist.get(guiID).template();
		LPDataIOWrapper.provideData(packet.getGuiData(), provider::readData);
		
		if (provider instanceof PopupGuiProvider && packet.getWindowID() == -2) {
			if (FMLClientHandler.instance().getClient().currentScreen instanceof LogisticsBaseGuiScreen) {
				LogisticsBaseGuiScreen baseGUI = (LogisticsBaseGuiScreen) FMLClientHandler.instance().getClient().currentScreen;
				SubGuiScreen newSub;
				try {
					newSub = (SubGuiScreen) provider.getClientGui(player);
				} catch (TargetNotFoundException e) {
					throw e;
				} catch (Exception e) {
					LogisticsPipes.log.error(packet.getClass().getName());
					LogisticsPipes.log.error(packet.toString());
					throw new RuntimeException(e);
				}
				if (newSub != null) {
					if (!baseGUI.hasSubGui()) {
						baseGUI.setSubGui(newSub);
					} else {
						SubGuiScreen canidate = baseGUI.getSubGui();
						while (canidate.hasSubGui()) {
							canidate = canidate.getSubGui();
						}
						canidate.setSubGui(newSub);
					}
				}
			}
		} else {
			GuiContainer screen;
			try {
				screen = (GuiContainer) provider.getClientGui(player);
			} catch (TargetNotFoundException e) {
				throw e;
			} catch (Exception e) {
				LogisticsPipes.log.error(packet.getClass().getName());
				LogisticsPipes.log.error(packet.toString());
				throw new RuntimeException(e);
			}
			screen.inventorySlots.windowId = packet.getWindowID();
			FMLCommonHandler.instance().showGuiScreen(screen);
		}
	}
}

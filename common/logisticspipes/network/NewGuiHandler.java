package logisticspipes.network;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.packets.gui.GUIPacket;
import logisticspipes.proxy.MainProxy;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import lombok.SneakyThrows;

public class NewGuiHandler {

	public static List<GuiProvider> guilist;
	public static Map<Class<? extends GuiProvider>, GuiProvider> guimap;

	@SuppressWarnings("unchecked")
	// Suppressed because this cast should never fail.
	public static <T extends GuiProvider> T getGui(Class<T> clazz) {
		return (T) NewGuiHandler.guimap.get(clazz).template();
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows({ IOException.class, InvocationTargetException.class, IllegalAccessException.class, InstantiationException.class })
	// Suppression+sneakiness because these shouldn't ever fail, and if they do, it needs to fail.
	public static final void initialize() {
		final List<ClassInfo> classes = new ArrayList<ClassInfo>(ClassPath.from(NewGuiHandler.class.getClassLoader()).getTopLevelClassesRecursive("logisticspipes.network.guis"));
		Collections.sort(classes, new Comparator<ClassInfo>() {

			@Override
			public int compare(ClassInfo o1, ClassInfo o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});

		NewGuiHandler.guilist = new ArrayList<GuiProvider>(classes.size());
		NewGuiHandler.guimap = new HashMap<Class<? extends GuiProvider>, GuiProvider>(classes.size());

		int currentid = 0;

		for (ClassInfo c : classes) {
			final Class<?> cls = c.load();
			final GuiProvider instance = (GuiProvider) cls.getConstructors()[0].newInstance(currentid);
			NewGuiHandler.guilist.add(instance);
			NewGuiHandler.guimap.put((Class<? extends GuiProvider>) cls, instance);
			currentid++;
		}
	}

	@SneakyThrows(IOException.class)
	public static void openGui(GuiProvider guiProvider, EntityPlayer oPlayer) {
		if (!(oPlayer instanceof EntityPlayerMP)) {
			throw new UnsupportedOperationException("Gui can only be opened on the server side");
		}
		EntityPlayerMP player = (EntityPlayerMP) oPlayer;
		Container container = guiProvider.getContainer(player);
		if (container == null) {
			return;
		}
		player.getNextWindowId();
		player.closeContainer();
		int windowId = player.currentWindowId;

		GUIPacket packet = PacketHandler.getPacket(GUIPacket.class);
		LPDataOutputStream data = new LPDataOutputStream();
		guiProvider.writeData(data);
		packet.setGuiID(guiProvider.getId());
		packet.setWindowID(windowId);
		packet.setGuiData(data.toByteArray());
		MainProxy.sendPacketToPlayer(packet, player);

		player.openContainer = container;
		player.openContainer.windowId = windowId;
		player.openContainer.addCraftingToCrafters(player);
	}

	@SneakyThrows(IOException.class)
	@SideOnly(Side.CLIENT)
	public static void openGui(GUIPacket packet, EntityPlayer player) {
		int guiID = packet.getGuiID();
		GuiProvider provider = NewGuiHandler.guilist.get(guiID).template();
		provider.readData(new LPDataInputStream(packet.getGuiData()));
		GuiContainer screen;
		try {
			screen = (GuiContainer) provider.getClientGui(player);
		} catch (Exception e) {
			LogisticsPipes.log.error(packet.getClass().getName());
			LogisticsPipes.log.error(packet.toString());
			throw new RuntimeException(e);
		}
		screen.inventorySlots.windowId = packet.getWindowID();
		FMLCommonHandler.instance().showGuiScreen(screen);
	}
}

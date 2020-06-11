package logisticspipes.network;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.PopupGuiProvider;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.network.packets.gui.OpenGUIPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolverUtil;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SubGuiScreen;
import network.rs485.logisticspipes.util.LPDataIOWrapper;

public class NewGuiHandler {

	public static List<GuiProvider> guilist;
	public static Map<Class<? extends GuiProvider>, GuiProvider> guimap;

	private NewGuiHandler() { }

	@SuppressWarnings("unchecked") // Suppressed because this cast should never fail.
	public static <T extends GuiProvider> T getGui(Class<T> clazz) {
		return (T) NewGuiHandler.guimap.get(clazz).template();
	}

	public static void initialize() {
		Set<Class<? extends GuiProvider>> classes = StaticResolverUtil.findClassesByType(GuiProvider.class);

		loadGuiProviders(classes);

		if (NewGuiHandler.guilist == null || NewGuiHandler.guilist.isEmpty()) {
			throw new RuntimeException("Cannot load GuiProvider Classes");
		}
	}

	private static void loadGuiProviders(Set<Class<? extends GuiProvider>> classesIn) {
		List<Class<? extends GuiProvider>> classes = classesIn.stream()
				.sorted(Comparator.comparing(Class::getCanonicalName))
				.collect(Collectors.toList());

		NewGuiHandler.guilist = new ArrayList<>(classes.size());
		NewGuiHandler.guimap = new HashMap<>(classes.size());

		int currentId = 0;
		for (Class<? extends GuiProvider> cls : classes) {
			try {
				final GuiProvider instance = (GuiProvider) cls.getConstructors()[0].newInstance(currentId);
				NewGuiHandler.guilist.add(instance);
				NewGuiHandler.guimap.put(cls, instance);
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
				OpenGUIPacket packet = PacketHandler.getPacket(OpenGUIPacket.class);
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

		OpenGUIPacket packet = PacketHandler.getPacket(OpenGUIPacket.class);
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
	public static void openGui(OpenGUIPacket packet, EntityPlayer player) {
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

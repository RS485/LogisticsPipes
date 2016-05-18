package logisticspipes.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import lombok.Getter;
import lombok.SneakyThrows;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.PlayerConfigToServerPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.PlayerIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class PlayerConfig {

	private static final Lock fileAccesLock = new ReentrantLock();

	private final PlayerIdentifier playerIdent;

	private boolean useNewRenderer = true;
	@Getter
	private boolean useFallbackRenderer = true;
	@Getter
	private int renderPipeDistance = 48;
	@Getter
	private int renderPipeContentDistance = 24;
	@Getter
	private boolean isUninitialised;

	public PlayerConfig(PlayerIdentifier ident) {
		this(false, ident);
	}

	public PlayerConfig(boolean uninitialised, PlayerIdentifier ident) {
		isUninitialised = uninitialised;
		playerIdent = ident;
	}

	public void setUseFallbackRenderer(boolean flag) {
		useFallbackRenderer = flag;
	}

	public void setRenderPipeDistance(int dist) {
		renderPipeDistance = dist;
	}

	public void setRenderPipeContentDistance(int dist) {
		renderPipeContentDistance = dist;
	}

	public void sendUpdate() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(PlayerConfigToServerPacket.class).setConfig(this));
	}

	public void writeData(LPDataOutput output) {
		output.writeBoolean(useNewRenderer);
		output.writeBoolean(useFallbackRenderer);
		output.writeInt(renderPipeDistance);
		output.writeInt(renderPipeContentDistance);
	}

	public void readData(LPDataInput input) {
		useNewRenderer = input.readBoolean();
		useFallbackRenderer = input.readBoolean();
		renderPipeDistance = input.readInt();
		renderPipeContentDistance = input.readInt();
		isUninitialised = false;
	}

	@SneakyThrows(value = { FileNotFoundException.class, IOException.class })
	public void readFromFile() {
		World world = DimensionManager.getWorld(0);
		if (world == null) {
			new UnsupportedOperationException("Dimension 0 doesn't have a world? Couldn't load LP's player config.").printStackTrace();
			return;
		}
		File worldDir = world.getSaveHandler().getWorldDirectory();
		File lpData = new File(worldDir, "logisticspipes");
		File lpNameLookup = new File(lpData, "names");
		NBTTagCompound lpUserData = null;
		lpNameLookup.mkdirs();
		if (playerIdent.getId() == null) {
			File lookup = new File(lpNameLookup, playerIdent.getUsername() + ".info");
			if (lookup.exists()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(lookup))));
				String uid = reader.readLine();
				reader.close();
				UUID uuid = UUID.fromString(uid);
				if (uuid != null) {
					playerIdent.setID(uuid);
				}
			}
		}
		if (playerIdent.getUsername() != null && !playerIdent.getUsername().isEmpty()) {
			File file = new File(lpData, playerIdent.getUsername() + ".info");
			if (file.exists()) {
				fileAccesLock.lock();
				try {
					lpUserData = CompressedStreamTools.readCompressed(new FileInputStream(file));
				} catch (IOException e) {
					//We simply can't load the old settings. Just fall back to the default once.
				} finally {
					fileAccesLock.unlock();
				}
				file.delete();
			}
		}
		if (lpUserData == null && playerIdent.getId() != null) {
			File file = new File(lpData, playerIdent.getId().toString() + ".info");
			if (file.exists()) {
				fileAccesLock.lock();
				try {
					lpUserData = CompressedStreamTools.readCompressed(new FileInputStream(file));
				} catch (IOException e) {
					//We simply can't load the old settings. Just fall back to the default once.
				} finally {
					fileAccesLock.unlock();
				}
			}
		}
		if (lpUserData == null) {
			return;
		}
		useNewRenderer = lpUserData.getBoolean("useNewRenderer");
		renderPipeDistance = lpUserData.getInteger("renderPipeDistance");
		renderPipeContentDistance = lpUserData.getInteger("renderPipeContentDistance");
		useFallbackRenderer = lpUserData.getBoolean("useFallbackRenderer");
		isUninitialised = false;
	}

	@SneakyThrows(value = { FileNotFoundException.class, IOException.class })
	public void writeToFile() {
		World world = DimensionManager.getWorld(0);
		if (world == null) {
			new UnsupportedOperationException("Dimension 0 doesn't have a world? Couldn't load LP's player config.").printStackTrace();
			return;
		}
		File worldDir = world.getSaveHandler().getWorldDirectory();
		File lpData = new File(worldDir, "logisticspipes");
		File lpNameLookup = new File(lpData, "names");
		NBTTagCompound lpUserData = new NBTTagCompound();
		lpUserData.setBoolean("useNewRenderer", useNewRenderer);
		lpUserData.setBoolean("useFallbackRenderer", useFallbackRenderer);
		lpUserData.setInteger("renderPipeDistance", renderPipeDistance);
		lpUserData.setInteger("renderPipeContentDistance", renderPipeContentDistance);
		if (playerIdent.getId() != null && playerIdent.getUsername() != null && !playerIdent.getUsername().isEmpty()) {
			File lookup = new File(lpNameLookup, playerIdent.getUsername() + ".info");
			if (lookup.exists()) {
				lookup.delete();
			}
			PrintWriter writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(lookup)));
			writer.println(playerIdent.getId().toString());
			writer.close();
		}
		if (playerIdent.getId() != null) {
			File file = new File(lpData, playerIdent.getId().toString() + ".info");
			if (file.exists()) {
				file.delete();
			}
			fileAccesLock.lock();
			try {
				CompressedStreamTools.writeCompressed(lpUserData, new FileOutputStream(file));
			} catch (IOException e) {
				//If we can't save them, so be it.
			} finally {
				fileAccesLock.unlock();
			}
			lpUserData = null;
		}
		if (lpUserData != null) {
			File file = new File(lpData, playerIdent.getUsername() + ".info");
			if (file.exists()) {
				file.delete();
			}
			fileAccesLock.lock();
			try {
				CompressedStreamTools.writeCompressed(lpUserData, new FileOutputStream(file));
			} catch (IOException e) {
				//If we can't save them, so be it.
			} finally {
				fileAccesLock.unlock();
			}
			lpUserData = null;
		}
	}

	public void applyTo(PlayerConfig playerConfig) {
		playerConfig.renderPipeContentDistance = renderPipeContentDistance;
		playerConfig.renderPipeDistance = renderPipeDistance;
		playerConfig.useNewRenderer = useNewRenderer;
		playerConfig.useFallbackRenderer = useFallbackRenderer;
		playerConfig.isUninitialised = false;
	}

	public boolean isUseNewRenderer() {
		return useNewRenderer && SimpleServiceLocator.cclProxy.isActivated();
	}

	public void setUseNewRenderer(boolean flag) {
		useNewRenderer = flag;
	}
}

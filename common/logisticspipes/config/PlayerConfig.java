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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.PlayerConfigToServerPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.PlayerIdentifier;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class PlayerConfig {
	private final PlayerIdentifier playerIdent;
	@Getter
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
		this.playerIdent = ident;
	}
	
	public void setUseNewRenderer(boolean flag) {
		this.useNewRenderer = flag;
	}
	
	public void setUseFallbackRenderer(boolean flag) {
		this.useFallbackRenderer = flag;
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
	
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeBoolean(useNewRenderer);
		data.writeBoolean(useFallbackRenderer);
		data.writeInt(renderPipeDistance);
		data.writeInt(renderPipeContentDistance);
	}
	
	public void readData(LPDataInputStream data) throws IOException {
		useNewRenderer = data.readBoolean();
		useFallbackRenderer = data.readBoolean();
		renderPipeDistance = data.readInt();
		renderPipeContentDistance = data.readInt();
		isUninitialised = false;
	}

	@SneakyThrows(value={FileNotFoundException.class, IOException.class})
	public void readFromFile() {
		World world = DimensionManager.getWorld(0);
		if(world == null) {
			new UnsupportedOperationException("Dimension 0 doesn't have a world? Couldn't load LP's player config.").printStackTrace();
			return;
		}
		File worldDir = world.getSaveHandler().getWorldDirectory();
		File lpData = new File(worldDir, "logisticspipes");
		File lpNameLookup = new File(lpData, "names");
		NBTTagCompound lpUserData = null;
		lpNameLookup.mkdirs();
		if(playerIdent.getId() == null) {
			File lookup = new File(lpNameLookup, playerIdent.getUsername() + ".info");
			if(lookup.exists()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(lookup))));
				String uid = reader.readLine();
				reader.close();
				UUID uuid = UUID.fromString(uid);
				if(uuid != null) {
					playerIdent.setID(uuid);
				}
			}
		}
		if(playerIdent.getUsername() != null && !playerIdent.getUsername().isEmpty()) {
			File file = new File(lpData, playerIdent.getUsername() + ".info");
			if(file.exists()) {
				lpUserData = CompressedStreamTools.readCompressed(new FileInputStream(file));
				file.delete();
			}
		}
		if(lpUserData == null && playerIdent.getId() != null) {
			File file = new File(lpData, playerIdent.getId().toString() + ".info");
			if(file.exists()) {
				lpUserData = CompressedStreamTools.readCompressed(new FileInputStream(file));
			}
		}
		if(lpUserData == null) return;
		useNewRenderer = lpUserData.getBoolean("useNewRenderer");
		renderPipeDistance = lpUserData.getInteger("renderPipeDistance");
		renderPipeContentDistance = lpUserData.getInteger("renderPipeContentDistance");
		useFallbackRenderer = lpUserData.getBoolean("useFallbackRenderer");
		isUninitialised = false;
	}

	@SneakyThrows(value={FileNotFoundException.class, IOException.class})
	public void writeToFile() {
		World world = DimensionManager.getWorld(0);
		if(world == null) {
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
		if(playerIdent.getId() != null && playerIdent.getUsername() != null && !playerIdent.getUsername().isEmpty()) {
			File lookup = new File(lpNameLookup, playerIdent.getUsername() + ".info");
			if(lookup.exists()) {
				lookup.delete();
			}
			PrintWriter writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(lookup)));
			writer.println(playerIdent.getId().toString());
			writer.close();
		}
		if(playerIdent.getId() != null) {
			File file = new File(lpData, playerIdent.getId().toString() + ".info");
			if(file.exists()) {
				file.delete();
			}
			CompressedStreamTools.writeCompressed(lpUserData, new FileOutputStream(file));
			lpUserData = null;
		}
		if(lpUserData != null) {
			File file = new File(lpData, playerIdent.getUsername() + ".info");
			if(file.exists()) {
				file.delete();
			}
			CompressedStreamTools.writeCompressed(lpUserData, new FileOutputStream(file));
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
}

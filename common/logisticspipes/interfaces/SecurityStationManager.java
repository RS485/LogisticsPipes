package logisticspipes.interfaces;

import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.routing.RouterManagerImpl;

public interface SecurityStationManager {

	static SecurityStationManager getInstance() {
		return RouterManagerImpl.INSTANCE;
	}

	void add(LogisticsSecurityTileEntity tile);

	LogisticsSecurityTileEntity getStation(UUID id);

	void remove(LogisticsSecurityTileEntity tile);

	void deauthorizeUUID(UUID id);

	void authorizeUUID(UUID id);

	boolean isAuthorized(UUID id);

	void sendClientAuthorizationList(MinecraftServer server);

	void sendClientAuthorizationList(PlayerEntity player);

	void setClientAuthorizationList(Set<UUID> list);

}

package logisticspipes.interfaces;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.LogisticsSecurityTileEntity;

public interface ISecurityStationManager {

	void add(LogisticsSecurityTileEntity tile);

	LogisticsSecurityTileEntity getStation(UUID id);

	void remove(LogisticsSecurityTileEntity tile);

	void deauthorizeUUID(UUID id);

	void authorizeUUID(UUID id);

	boolean isAuthorized(UUID id);

	boolean isAuthorized(String id);

	void sendClientAuthorizationList();

	void sendClientAuthorizationList(EntityPlayer player);

	void setClientAuthorizationList(List<String> list);
}

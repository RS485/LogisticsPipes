package logisticspipes.interfaces;

import java.util.List;
import java.util.UUID;

import logisticspipes.blocks.LogisticsSecurityTileEntity;

import net.minecraft.entity.player.EntityPlayer;

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

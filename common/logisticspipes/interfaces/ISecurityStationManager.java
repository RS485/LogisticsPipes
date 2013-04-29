package logisticspipes.interfaces;

import java.util.List;
import java.util.UUID;

import logisticspipes.blocks.LogisticsSecurityTileEntity;

public interface ISecurityStationManager {
	public void add(LogisticsSecurityTileEntity tile);
	public LogisticsSecurityTileEntity getStation(UUID id);
	public void remove(LogisticsSecurityTileEntity tile);
	public void deauthorizeUUID(UUID id);
	public void authorizeUUID(UUID id);
	public boolean isAuthorized(UUID id);
	public void sendClientAuthorizationList();
	public void setClientAuthorizationList(List<String> list);
}

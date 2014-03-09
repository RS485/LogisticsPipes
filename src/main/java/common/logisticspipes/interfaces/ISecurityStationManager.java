package logisticspipes.interfaces;

import java.util.List;
import java.util.UUID;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import net.minecraft.entity.player.EntityPlayer;

public interface ISecurityStationManager {
	public void add(LogisticsSecurityTileEntity tile);
	public LogisticsSecurityTileEntity getStation(UUID id);
	public void remove(LogisticsSecurityTileEntity tile);
	public void deauthorizeUUID(UUID id);
	public void authorizeUUID(UUID id);
	public boolean isAuthorized(UUID id);
	public boolean isAuthorized(String id);
	public void sendClientAuthorizationList();
	public void sendClientAuthorizationList(EntityPlayer player);
	public void setClientAuthorizationList(List<String> list);
}

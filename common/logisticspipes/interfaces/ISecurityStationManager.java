package logisticspipes.interfaces;

import java.util.UUID;

import logisticspipes.blocks.LogisticsSecurityTileEntity;

public interface ISecurityStationManager {
	public void add(LogisticsSecurityTileEntity tile);
	public LogisticsSecurityTileEntity getStation(UUID id);
	public void remove(LogisticsSecurityTileEntity tile);
}

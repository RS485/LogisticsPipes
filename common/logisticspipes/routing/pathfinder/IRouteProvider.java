package logisticspipes.routing.pathfinder;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraftforge.common.util.ForgeDirection;

public interface IRouteProvider {
	
	@Data
	@AllArgsConstructor
	public static class RouteInfo {
		private IPipeInformationProvider pipe;
		private int length;
		private ForgeDirection exitOrientation;
	}
	
	List<RouteInfo> getConnectedPipes(ForgeDirection from);
}

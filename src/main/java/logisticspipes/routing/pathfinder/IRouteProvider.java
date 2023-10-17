package logisticspipes.routing.pathfinder;

import java.util.List;

import net.minecraft.util.EnumFacing;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface IRouteProvider {

	@Data
	@AllArgsConstructor
	class RouteInfo {

		private IPipeInformationProvider pipe;
		private int length;
		private EnumFacing exitOrientation;
	}

	List<RouteInfo> getConnectedPipes(EnumFacing from);
}

package logisticspipes.routing.pathfinder;

import java.util.List;

import net.minecraft.util.math.Direction;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface IRouteProvider {

	@Data
	@AllArgsConstructor
	class RouteInfo {

		private IPipeInformationProvider pipe;
		private int length;
		private Direction exitOrientation;
	}

	List<RouteInfo> getConnectedPipes(Direction from);
}

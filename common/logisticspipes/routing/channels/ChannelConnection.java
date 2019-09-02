package logisticspipes.routing.channels;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class ChannelConnection {

	public Set<Integer> routers = new HashSet<>();
	public UUID identifier;
}

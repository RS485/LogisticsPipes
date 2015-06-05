package logisticspipes.pipes.basic.debug;

import java.util.List;

import lombok.Data;

@Data
public class StatusEntry {

	public String name;
	public List<StatusEntry> subEntry;
}

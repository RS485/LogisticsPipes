package logisticspipes.interfaces;

import java.util.List;
import javax.annotation.Nonnull;

public interface IClientInformationProvider {

	@Nonnull
	List<String> getClientInformation();

}

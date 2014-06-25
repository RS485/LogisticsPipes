package logisticspipes.proxy.cc.objects;

import java.util.Map;

import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;

@CCType(name="LP Global Access")
public class LPGlobalCCAccess {
	
	@CCCommand(description="Tryes to give more information about the givven object")
	public String identify(Object object) {
		if(object instanceof Map<?,?>) {
			StringBuilder builder = new StringBuilder("Map: ");
			if(((Map<?,?>)object).isEmpty()) return builder.append("empty").toString();
			builder.append("key [");
			if(((Map<?,?>)object).keySet().toArray()[0] != null) {
				builder.append(((Map<?,?>)object).keySet().toArray()[0].getClass());
			} else {
				builder.append("null");
			}
			builder.append("], [");
			if(((Map<?,?>)object).values().toArray()[0] != null) {
				builder.append(((Map<?,?>)object).values().toArray()[0].getClass());
			} else {
				builder.append("null");
			}
			builder.append("]");
			return builder.toString();
		}
		return object.toString() + " [" + object.getClass() + "]";
	}

	@CCCommand(description="Creates a new ItemIdentifier Builder")
	public CCItemIdentifierBuilder getItemIdentifierBuilder() {
		return new CCItemIdentifierBuilder();
	}
}

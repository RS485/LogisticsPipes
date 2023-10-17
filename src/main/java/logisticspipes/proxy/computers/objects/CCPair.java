package logisticspipes.proxy.computers.objects;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeDefinition;
import logisticspipes.utils.tuples.Pair;

public class CCPair implements ILPCCTypeDefinition {

	@Override
	public ICCTypeWrapped getTypeFor(Object input) {
		return new CCPairImplementation((Pair<?, ?>) input);
	}

	@CCType(name = "Pair")
	public static class CCPairImplementation implements ICCTypeWrapped {

		private final Pair<?, ?> pair;

		protected CCPairImplementation(Pair<?, ?> pair) {
			this.pair = pair;
		}

		@CCCommand(description = "Returns the first value")
		public Object getValue1() {
			return pair.getValue1();
		}

		@CCCommand(description = "Returns the type of the first value")
		public String getType1() {
			if (pair.getValue1() != null) {
				return pair.getValue1().getClass().toString();
			} else {
				return "null";
			}
		}

		@CCCommand(description = "Returns the second value")
		public Object getValue2() {
			return pair.getValue2();
		}

		@CCCommand(description = "Returns the type of the second value")
		public String getType2() {
			if (pair.getValue2() != null) {
				return pair.getValue2().getClass().toString();
			} else {
				return "null";
			}
		}

		@Override
		public Object getObject() {
			return pair;
		}
	}
}

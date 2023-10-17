package logisticspipes.proxy.computers.objects;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeDefinition;
import logisticspipes.proxy.computers.objects.CCPair.CCPairImplementation;
import logisticspipes.utils.tuples.Triplet;

public class CCTriplet implements ILPCCTypeDefinition {

	@Override
	public ICCTypeWrapped getTypeFor(Object input) {
		return new CCTripletImplementation((Triplet<?, ?, ?>) input);
	}

	@CCType(name = "Triplet")
	public static class CCTripletImplementation extends CCPairImplementation {

		private final Triplet<?, ?, ?> triplet;

		protected CCTripletImplementation(Triplet<?, ?, ?> triplet) {
			super(triplet);
			this.triplet = triplet;
		}

		@CCCommand(description = "Returns the third value")
		public Object getValue3() {
			return triplet.getValue3();
		}

		@CCCommand(description = "Returns the type of the third value")
		public String getType3() {
			if (triplet.getValue3() != null) {
				return triplet.getValue3().getClass().toString();
			} else {
				return "null";
			}
		}
	}
}

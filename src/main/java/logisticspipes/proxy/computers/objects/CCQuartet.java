package logisticspipes.proxy.computers.objects;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeDefinition;
import logisticspipes.proxy.computers.objects.CCTriplet.CCTripletImplementation;
import logisticspipes.utils.tuples.Quartet;

public class CCQuartet implements ILPCCTypeDefinition {

	@Override
	public ICCTypeWrapped getTypeFor(Object input) {
		return new CCQuartetImplementation((Quartet<?, ?, ?, ?>) input);
	}

	@CCType(name = "Quartet")
	public static class CCQuartetImplementation extends CCTripletImplementation {

		private final Quartet<?, ?, ?, ?> quartet;

		protected CCQuartetImplementation(Quartet<?, ?, ?, ?> quartet) {
			super(quartet);
			this.quartet = quartet;
		}

		@CCCommand(description = "Returns the forth value")
		public Object getValue4() {
			return quartet.getValue4();
		}

		@CCCommand(description = "Returns the type of the forth value")
		public String getType4() {
			if (quartet.getValue4() != null) {
				return quartet.getValue4().getClass().toString();
			} else {
				return "null";
			}
		}
	}
}

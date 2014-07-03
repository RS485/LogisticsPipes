package logisticspipes.proxy.cc.objects;

import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.proxy.cc.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.cc.interfaces.ILPCCTypeDefinition;
import logisticspipes.proxy.cc.objects.CCPair.CCPairImplementation;
import logisticspipes.utils.tuples.Triplet;

public class CCTriplet implements ILPCCTypeDefinition {
	
	@Override
	public ICCTypeWrapped getTypeFor(Object input) {
		return new CCTripletImplementation((Triplet<?,?,?>) input);
	}
	
	@CCType(name="Triplet")
	public static class CCTripletImplementation extends CCPairImplementation {
		private final Triplet<?,?,?> triplet;
		protected CCTripletImplementation(Triplet<?,?,?> triplet) {
			super(triplet);
			this.triplet = triplet;
		}
		
		@CCCommand(description="Returns the third value")
		public Object getValue3() {
			return triplet.getValue3();
		}
		
		@CCCommand(description="Returns the type of the third value")
		public String getType3() {
			if(triplet.getValue3() != null) {
				return triplet.getValue3().getClass().toString();
			} else {
				return "null";
			}
		}
	}
}

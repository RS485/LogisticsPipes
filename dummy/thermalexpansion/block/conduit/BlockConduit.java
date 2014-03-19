package thermalexpansion.block.conduit;

public class BlockConduit {
	
	public static enum ConnectionTypes {
		NONE(false), CONDUIT, ENERGY_BASIC, ENERGY_BASIC_BLOCKED(false), ENERGY_HARDENED, ENERGY_HARDENED_BLOCKED(false), ENERGY_REINFORCED, ENERGY_REINFORCED_BLOCKED(false), FLUID_NORMAL, FLUID_BLOCKED(false), FLUID_INPUT_ON, FLUID_INPUT_OFF, ITEM_NORMAL, ITEM_BLOCKED(false), ITEM_INPUT_ON, ITEM_INPUT_OFF, ITEM_STUFFED_ON, ITEM_STUFFED_OFF;
		
		private final boolean	renderConduit;
		
		private ConnectionTypes() {
			this.renderConduit = true;
		}
		
		private ConnectionTypes(boolean renderConduit) {
			this.renderConduit = renderConduit;
		}
		
		public boolean renderConduit() {
			return this.renderConduit;
		}
	}
	
}

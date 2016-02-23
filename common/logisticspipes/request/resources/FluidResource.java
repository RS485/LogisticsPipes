package logisticspipes.request.resources;

import java.io.IOException;

import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;

public class FluidResource implements IResource {

	private final FluidIdentifier liquid;
	private int amount;
	private final IRequestFluid target;

	public FluidResource(FluidIdentifier liquid, int amount, IRequestFluid target) {
		this.liquid = liquid;
		this.amount = amount;
		this.target = target;
	}

	public FluidResource(LPDataInputStream data) throws IOException {
		liquid = FluidIdentifier.get(data.readItemIdentifier());
		amount = data.readInt();
		target = null;
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeItemIdentifier(liquid.getItemIdentifier());
		data.writeInt(amount);
	}

	@Override
	public ItemIdentifier getAsItem() {
		return liquid.getItemIdentifier();
	}

	@Override
	public int getRequestedAmount() {
		return amount;
	}

	public FluidIdentifier getFluid() {
		return liquid;
	}

	public IRequestFluid getTarget() {
		return target;
	}

	@Override
	public IRouter getRouter() {
		return target.getRouter();
	}

	@Override
	public boolean matches(ItemIdentifier itemType, MatchSettings settings) {
		if (itemType.isFluidContainer()) {
			FluidIdentifier other = FluidIdentifier.get(itemType);
			return other.equals(liquid);
		}
		return false;
	}

	@Override
	public IResource clone(int multiplier) {
		return new FluidResource(liquid, amount * multiplier, target);
	}

	@Override
	public boolean mergeForDisplay(IResource resource, int withAmount) {
		if (resource instanceof FluidResource) {
			if (((FluidResource) resource).liquid.equals(liquid)) {
				amount += withAmount;
				return true;
			}
		}
		return false;
	}

	@Override
	public IResource copyForDisplayWith(int amount) {
		return new FluidResource(liquid, amount, null);
	}

	private Object ccObject;

	@Override
	public void setCCType(Object type) {
		ccObject = type;
	}

	@Override
	public Object getCCType() {
		return ccObject;
	}

	@Override
	public String getDisplayText(ColorCode code) {
		StringBuilder builder = new StringBuilder();
		if (code != ColorCode.NONE) {
			builder.append(code == ColorCode.MISSING ? ChatColor.RED : ChatColor.GREEN);
		}
		builder.append(amount);
		builder.append("mB ");
		builder.append(liquid.makeFluidStack(0).getLocalizedName());
		if (code != ColorCode.NONE) {
			builder.append(ChatColor.WHITE);
		}
		return builder.toString();
	}

	@Override
	public ItemIdentifierStack getDisplayItem() {
		return liquid.getItemIdentifier().makeStack(amount);
	}
}

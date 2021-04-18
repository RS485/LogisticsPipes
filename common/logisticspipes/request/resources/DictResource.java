package logisticspipes.request.resources;

import java.util.BitSet;
import javax.annotation.Nonnull;

import com.google.common.base.Objects;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;
import network.rs485.logisticspipes.util.FuzzyFlag;
import network.rs485.logisticspipes.util.FuzzyUtil;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class DictResource implements IResource {

	private final Object[] ccTypeHolder = new Object[1];
	private final IRequestItems requester;
	public ItemIdentifierStack stack;
	private BitSet fuzzyFlags = new BitSet(4);

	public DictResource(ItemIdentifierStack stack, IRequestItems requester) {
		this.stack = stack;
		this.requester = requester;
	}

	public DictResource(LPDataInput input) {
		stack = input.readItemIdentifierStack();
		requester = null;
		fuzzyFlags = input.readBitSet().get(0, 3);
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeItemIdentifierStack(stack);
		output.writeBitSet(fuzzyFlags);
	}

	@Override
	public ItemIdentifier getAsItem() {
		return stack.getItem();
	}

	@Override
	public int getRequestedAmount() {
		return stack.getStackSize();
	}

	@Override
	public boolean matches(IResource resource, MatchSettings settings) {
		if (resource instanceof DictResource) {
			return matches(((DictResource) resource).getItem(), IResource.MatchSettings.NORMAL)
					&& resource.matches(getItem(), IResource.MatchSettings.NORMAL)
					&& getBitSet().equals(((DictResource) resource).getBitSet());
		} else if (resource instanceof ItemResource) {
			return matches(((ItemResource) resource).getItem(), MatchSettings.NORMAL);
		}
		return false;
	}

	@Override
	public boolean matches(ItemIdentifier other, MatchSettings settings) {
		return FuzzyUtil.INSTANCE.fuzzyMatches(FuzzyUtil.INSTANCE.getter(fuzzyFlags), stack.getItem(), other);
	}

	@Override
	@Nonnull
	public IRouter getRouter() {
		return requester.getRouter();
	}

	@Override
	public IResource clone(int multiplier) {
		ItemIdentifierStack stack = new ItemIdentifierStack(this.stack);
		stack.setStackSize(stack.getStackSize() * multiplier);
		DictResource clone = new DictResource(stack, requester);
		clone.fuzzyFlags.or(fuzzyFlags);
		return clone;
	}

	public DictResource clone() {
		DictResource clone = new DictResource(new ItemIdentifierStack(this.stack), requester);
		clone.fuzzyFlags.or(fuzzyFlags);
		return clone;
	}

	public IRequestItems getTarget() {
		return requester;
	}

	public ItemIdentifier getItem() {
		return stack.getItem();
	}

	public ItemIdentifierStack getItemStack() {
		return stack;
	}

	@Override
	public boolean mergeForDisplay(IResource resource, int withAmount) {
		if (resource instanceof DictResource) {
			if (((DictResource) resource).fuzzyFlags.equals(fuzzyFlags) && ((DictResource) resource).getItem()
					.equals(getItem())) {
				stack.setStackSize(stack.getStackSize() + withAmount);
				return true;
			}
		}
		return false;
	}

	@Override
	public IResource copyForDisplayWith(int amount) {
		ItemIdentifierStack stack = new ItemIdentifierStack(this.stack);
		stack.setStackSize(amount);
		DictResource clone = new DictResource(stack, null);
		clone.fuzzyFlags.or(fuzzyFlags);
		return clone;
	}

	@Override
	public String getDisplayText(ColorCode code) {
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.GRAY);
		builder.append("{");
		if (code != ColorCode.NONE) {
			builder.append(code == ColorCode.MISSING ? ChatColor.RED : ChatColor.GREEN);
		}
		builder.append(stack.getFriendlyName());
		if (code != ColorCode.NONE) {
			builder.append(ChatColor.GRAY);
		}
		builder.append(" [");
		builder.append(useOreDict() ? ChatColor.GREEN : ChatColor.RED);
		builder.append("OreDict");
		builder.append(ChatColor.GRAY);
		builder.append(", ");
		builder.append(useOreCategory() ? ChatColor.GREEN : ChatColor.RED);
		builder.append("OreCat");
		builder.append(ChatColor.GRAY);
		builder.append(", ");
		builder.append(ignoreDamage() ? ChatColor.GREEN : ChatColor.RED);
		builder.append("IgnDmg");
		builder.append(ChatColor.GRAY);
		builder.append(", ");
		builder.append(ignoreNBT() ? ChatColor.GREEN : ChatColor.RED);
		builder.append("IgnNBT");
		builder.append(ChatColor.GRAY);
		return builder.append("]}").toString();
	}

	@Override
	public ItemIdentifierStack getDisplayItem() {
		return stack;
	}

	public DictResource loadFromBitSet(BitSet bits) {
		fuzzyFlags.clear();
		fuzzyFlags.or(bits);
		return this;
	}

	public BitSet getBitSet() {
		return fuzzyFlags.get(0, 3);
	}

	public Identifier getIdentifier() {
		return new Identifier();
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

	public boolean useOreDict() {
		return FuzzyUtil.INSTANCE.get(fuzzyFlags, FuzzyFlag.USE_ORE_DICT);
	}

	public boolean useOreCategory() {
		return FuzzyUtil.INSTANCE.get(fuzzyFlags, FuzzyFlag.USE_ORE_CATEGORY);
	}

	public boolean ignoreDamage() {
		return FuzzyUtil.INSTANCE.get(fuzzyFlags, FuzzyFlag.IGNORE_DAMAGE);
	}

	public boolean ignoreNBT() {
		return FuzzyUtil.INSTANCE.get(fuzzyFlags, FuzzyFlag.IGNORE_NBT);
	}

	public class Identifier {

		private ItemIdentifier getItem() {
			return stack.getItem();
		}

		private BitSet getBitSet() {
			return DictResource.this.getBitSet();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(getItem(), getBitSet());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Identifier) {
				Identifier id = (Identifier) obj;
				return id.getItem().equals(getItem()) && id.getBitSet().equals(getBitSet());
			}
			return false;
		}
	}

}

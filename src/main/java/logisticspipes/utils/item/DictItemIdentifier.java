package logisticspipes.utils.item;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import logisticspipes.LogisticsPipes;

public class DictItemIdentifier {

	private static ConcurrentHashMap<BitSet, DictItemIdentifier> identifiers = new ConcurrentHashMap<>();

	@Nullable
	public static DictItemIdentifier getDictItemIdentifier(@Nonnull ItemIdentifier itemIdent) {
		BitSet ids = new BitSet();
		boolean hasDict = false;
		final ItemStack stack = itemIdent.unsafeMakeNormalStack(1);
		if (stack.isEmpty()) {
			LogisticsPipes.log.error("Cannot make stack from item " + itemIdent.toString() + " -- item creates and empty stack!");
			return null;
		}
		for (int oreId : OreDictionary.getOreIDs(stack)) {
			ids.set(oreId);
			hasDict = true;
		}
		if (!hasDict) {
			return null;
		}
		DictItemIdentifier dictIdent = DictItemIdentifier.identifiers.get(ids);
		if (dictIdent == null) {
			dictIdent = new DictItemIdentifier(ids);
			DictItemIdentifier.identifiers.put(ids, dictIdent);
		}
		return dictIdent;
	}

	private final List<DictIdentifier> parts = new ArrayList<>();

	private DictItemIdentifier(BitSet set) {
		int id = -1;
		while ((id = set.nextSetBit(id + 1)) != -1) {
			parts.add(DictIdentifier.getForId(id));
		}
	}

	public boolean canMatch(DictItemIdentifier ident, boolean byName, boolean byCategory) {
		for (DictIdentifier ident1 : parts) {
			for (DictIdentifier ident2 : ident.parts) {
				if (ident1 == ident2) {
					return true;
				}
				if (byName && ident1.canNameMatch(ident2)) {
					return true;
				}
				if (byCategory && ident1.canCategoryMatch(ident2)) {
					return true;
				}
			}
		}
		return false;
	}

	public void debugDumpData(boolean isClient) {
		StringBuilder builder = new StringBuilder("DictIdentifiers: [");
		boolean first = true;
		for (DictIdentifier ident : parts) {
			if (!first) {
				builder.append(", ");
			}
			ident.debugDumpData(isClient, builder);
			first = false;
		}
		builder.append("]");
		System.out.println(builder.toString());
	}
}

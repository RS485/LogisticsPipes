package logisticspipes.utils.item;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraftforge.oredict.OreDictionary;

public class DictIdentifier {

	private static List<DictIdentifier> identifiers = new ArrayList<DictIdentifier>();

	static DictIdentifier getForId(int id) {
		if (DictIdentifier.identifiers.size() <= id) {
			ArrayList<DictIdentifier> newidentifiers = new ArrayList<DictIdentifier>(id + 1);
			while (newidentifiers.size() <= id) {
				newidentifiers.add(null);
			}
			for (int i = 0; i < DictIdentifier.identifiers.size(); i++) {
				newidentifiers.set(i, DictIdentifier.identifiers.get(i));
			}
			DictIdentifier.identifiers = newidentifiers;
		}
		DictIdentifier ident = DictIdentifier.identifiers.get(id);
		if (ident == null) {
			ident = new DictIdentifier(id);
			DictIdentifier.identifiers.set(id, ident);
		}
		return ident;
	}

	private final int id;
	private String name;
	private String category;

	private DictIdentifier(int id) {
		this.id = id;
	}

	public String getName() {
		if (name == null) {
			name = OreDictionary.getOreName(id);
		}
		return name;
	}

	public String getCategory() {
		if (category == null) {
			category = Pattern.compile("[A-Z].*").matcher(getName()).replaceFirst("");
		}
		return category;
	}

	public boolean canNameMatch(DictIdentifier ident) {
		return getName().equals(ident.getName());
	}

	public boolean canCategoryMatch(DictIdentifier ident) {
		return getCategory().equals(ident.getCategory());
	}

	public void debugDumpData(boolean isClient, StringBuilder builder) {
		builder.append(id);
		builder.append("{");
		builder.append(getName());
		builder.append("}");
	}
}

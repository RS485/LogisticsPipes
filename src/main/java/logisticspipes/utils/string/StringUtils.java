package logisticspipes.utils.string;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;

public final class StringUtils {
	public static final List<String> UNTRANSLATED_STRINGS = new ArrayList<>();

	private StringUtils() {}

	public static String getWithMaxWidth(String name, int width, FontRenderer fontRenderer) {
		boolean changed = false;
		while (fontRenderer.getStringWidth(name) > width) {
			name = name.substring(0, name.length() - 2);
			changed = true;
		}
		if (changed) {
			name += "...";
		}
		return name;
	}

	public static String getStringWithSpacesFromInteger(int source) {
		String data = Integer.toString(source);
		return StringUtils.insertThousandsSeparators(data);
	}

	public static String getStringWithSpacesFromLong(long source) {
		String data = Long.toString(source);
		return StringUtils.insertThousandsSeparators(data);
	}

	public static String insertThousandsSeparators(String source) {
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = source.length(); i > 3; i -= 3) {
			sb.insert(0, source.substring(i - 3, i));
			sb.insert(0, ' ');
		}
		sb.insert(0, source.substring(0, i));
		return sb.toString();
	}

}


package logisticspipes.utils.string;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
//import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableListIterator;
import org.lwjgl.input.Keyboard;

public final class StringUtils {

	public static final String KEY_HOLDSHIFT = "misc.holdshift";
	public static final List<String> UNTRANSLATED_STRINGS = new ArrayList<>();

	private StringUtils() {}

	public static String handleColor(String input) {
		if (input == null) {
			return "null";
		}
		StringBuilder builder = new StringBuilder();
		ImmutableList<Character> chars = Lists.charactersOf(input);
		UnmodifiableListIterator<Character> iter = chars.listIterator();
		while (iter.hasNext()) {
			Character c = iter.next();
			if (c.charValue() == '%' && iter.hasNext()) {
				Character c2 = iter.next();
				if (c2.charValue() == 'c') {
					StringBuilder handled = new StringBuilder();
					ChatColor[] values = ChatColor.values();
					List<ChatColor> colors = new ArrayList<>(values.length);
					colors.addAll(Arrays.asList(values));
					int i = 0;
					outer:
					while (iter.hasNext() && !colors.isEmpty()) {
						Character c3 = iter.next();
						handled.append(c3);
						Iterator<ChatColor> colorIter = colors.iterator();
						while (colorIter.hasNext()) {
							ChatColor color = colorIter.next();
							if (color.name().length() <= i) {
								break outer;
							}
							if (c3.charValue() != color.name().charAt(i)) {
								colorIter.remove();
							}
						}
						i++;
					}
					if (!colors.isEmpty()) {
						ChatColor color = colors.get(0);
						builder.append(color.toString());
					} else {
						builder.append(handled);
					}
				} else {
					builder.append('%');
					builder.append(c2);
				}
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	public static String translate(String key) {
		String result = StringUtils.handleColor(I18n.translateToLocal(key));
		if (result.equals(key) && !StringUtils.UNTRANSLATED_STRINGS.contains(key) && !key.contains(".tip")) {
			StringUtils.UNTRANSLATED_STRINGS.add(key);
		}
		return result;
	}

	public static void addShiftAddition(@Nonnull ItemStack stack, List<String> list) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			String baseKey = MessageFormat.format("{0}.tip", stack.getItem().getUnlocalizedName(stack));
			String key = baseKey + 1;
			String translation = StringUtils.translate(key);
			int i = 1;

			while (!translation.equals(key)) {
				list.add(translation);
				key = baseKey + ++i;
				translation = StringUtils.translate(key);
			}
		} else {
			String baseKey = MessageFormat.format("{0}.tip", stack.getItem().getUnlocalizedName(stack));
			String key = baseKey + 1;
			String translation = StringUtils.translate(key);
			if (!translation.equals(key)) {
				list.add(StringUtils.translate(StringUtils.KEY_HOLDSHIFT));
			}
		}
	}

	public static String getFormatedStackSize(long stackSize, boolean forceDisplayNumber) {
		String s;
		if (stackSize == 1 && !forceDisplayNumber) {
			s = "";
		} else if (stackSize < 1000) {
			s = stackSize + "";
		} else if (stackSize < 100000) {
			s = stackSize / 1000 + "K";
		} else if (stackSize < 10000000) {
			s = stackSize / 1000000 + "M" + (stackSize % 1000000) / 100000;
		} else if (stackSize < 100000000) {
			s = stackSize / 1000000 + "M";
		} else {
			s = stackSize / 1000000000 + "G" + (stackSize % 1000000000) / 100000000;
		}
		return s;
	}

	public static String toPercent(double value) {
		if (value > 1) {
			value = 1;
		}
		if (value < 0) {
			value = 0;
		}
		value *= 100;
		int percent = (int) value;
		return percent + "%";
	}

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

	public static String getCuttedString(String input, int maxLength, FontRenderer renderer) {
		if (renderer.getStringWidth(input) < maxLength) {
			return input;
		}
		input += "...";
		while (renderer.getStringWidth(input) > maxLength && input.length() > 5) {
			input = input.substring(0, input.length() - 4) + "...";
		}
		return input;
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

//	public static ArrayList<String> splitLines(String text, FontRenderer fontRenderer, int maxLength) {
//		text = applyFormatting(text, fontRenderer);
//		if (text.charAt(text.length() - 1) != '\n') text += '\n';
//		ArrayList<String> paragraphs = new ArrayList(Arrays.asList(text.split("\n")));
//		ArrayList<String> lines = new ArrayList<>();
//		StringBuilder currentLine = new StringBuilder();
//		for (String paragraph : paragraphs) {
//			boolean header = paragraph.indexOf("##") > -1;
//			if (header) paragraph = paragraph.replace("##", "");
//			if (paragraph != null && paragraph.length() != 0 && paragraph.charAt(paragraph.length() - 1) != '\n') paragraph += (TextFormatting.WHITE + "\n");
//			int lastSplitPoint = 0;
//			int curLength = 0;
//			char[] par = paragraph.toCharArray();
//			for (char c : par) {
//				if (c == '\n') {
//					lines.add(currentLine.toString());
//					if (header) {
//						lines.add("##" + lines.get(lines.size() - 1));
//						lines.remove(lines.size() - 2);
//					}
//					currentLine.delete(0, currentLine.length());
//					lastSplitPoint = 0;
//					curLength = fontRenderer.getStringWidth(currentLine.toString());
//				} else if (Character.isWhitespace(c)) {
//					currentLine.append(c);
//					curLength += (int) (fontRenderer.getCharWidth(c) * (header ? HEADER_SCALING : 1.0F));
//					lastSplitPoint = currentLine.length();
//				} else {
//					if ((currentLine.length() > 1 && curLength + (int) (fontRenderer.getCharWidth(c) * (header ? HEADER_SCALING : 1.0F)) > maxLength)) {
//						if (lastSplitPoint == 0) lastSplitPoint = currentLine.length();
//						lines.add(((header ? "##" : "") + currentLine.substring(0, lastSplitPoint)));
//						currentLine.delete(0, lastSplitPoint);
//						lastSplitPoint = 0;
//						curLength = fontRenderer.getStringWidth(currentLine.toString());
//					}
//					currentLine.append(c);
//					curLength += (int) (fontRenderer.getCharWidth(c) * (header ? HEADER_SCALING : 1.0F));
//				}
//			}
//		}
//		return lines;
//	}

//	public static String applyFormatting(String text, FontRenderer fontRenderer) {
//		// Multiple passes:
//		text = text.replace(" *", " " + TextFormatting.BOLD);
//		text = text.replace(" __", " " + TextFormatting.UNDERLINE);
//		text = text.replace(" _", " " + TextFormatting.ITALIC);
//		text = text.replace(" ~~", " " + TextFormatting.STRIKETHROUGH);
//		// Single pass to close
//		text = text.replace("*", TextFormatting.RESET.toString());
//		text = text.replace("__", TextFormatting.RESET.toString());
//		text = text.replace("_", TextFormatting.RESET.toString());
//		text = text.replace("~~", TextFormatting.RESET.toString());
//		return text;
//	}
}

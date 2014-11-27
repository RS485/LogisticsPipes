package logisticspipes.utils.string;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableListIterator;

public class StringUtil {
	public static String KEY_HOLDSHIFT = "misc.holdshift";
	public static List<String> untranslatedStrings = new ArrayList<String>();
	
	public static String handleColor(String input) {
		if(input == null) return "null";
		StringBuilder builder = new StringBuilder();
		ImmutableList<Character> chars = Lists.charactersOf(input);
		UnmodifiableListIterator<Character> iter = chars.listIterator();
		while(iter.hasNext()) {
			Character c = iter.next();
			if(c.charValue() == '%' && iter.hasNext()) {
				Character c2 = iter.next();
				if(c2.charValue() == 'c') {
					StringBuilder handled = new StringBuilder();
					ChatColor[] values = ChatColor.values();
					List<ChatColor> colors = new ArrayList<ChatColor>(values.length);
					colors.addAll(Arrays.asList(values));
					int i=0;
					outer:
					while(iter.hasNext() && !colors.isEmpty()) {
						Character c3 = iter.next();
						handled.append(c3);
						Iterator<ChatColor> colorIter = colors.iterator();
						while(colorIter.hasNext()) {
							ChatColor color = colorIter.next();
							if(color.name().length() <= i) {
								break outer;
							}
							if(c3.charValue() != color.name().charAt(i)) {
								colorIter.remove();
							}
						}
						i++;
					}
					if(!colors.isEmpty()) {
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
		String result = handleColor(StatCollector.translateToLocal(key));
		if(result.equals(key) && !untranslatedStrings.contains(key) && !key.contains(".tip")) {
			untranslatedStrings.add(key);
		}
		return result;
	}
	
	public static void addShiftAddition(ItemStack stack, List<String> list) {
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			String baseKey = MessageFormat.format("{0}.tip", stack.getItem().getUnlocalizedName(stack));
			String key = baseKey + 1;
			String translation = StringUtil.translate(key);
			int i = 1;

			while(!translation.equals(key)) {
				list.add(translation);
				key = baseKey + ++i;
				translation = StringUtil.translate(key);
			}
		} else {
			String baseKey = MessageFormat.format("{0}.tip", stack.getItem().getUnlocalizedName(stack));
			String key = baseKey + 1;
			String translation = StringUtil.translate(key);
			if(!translation.equals(key)) {
				list.add(translate(KEY_HOLDSHIFT));
			}
		}
	}

	public static String getFormatedStackSize(long stackSize) {
		String s;
		if (stackSize == 1) {
			s = "";
		} else if (stackSize < 1000) {
			s = stackSize + "";
		} else if (stackSize < 100000) {
			s = stackSize / 1000 + "K";
		} else if (stackSize < 1000000) {
			s = "0M" + stackSize / 100000;
		} else {
			s = stackSize / 1000000 + "M";
		}
		return s;
	}

	public static String toPercent(double value) {
		if(value > 1) value = 1;
		if(value < 0) value = 0;
		value *= 100;
		int percent = (int) value;
		return Integer.toString(percent) + "%";
	}

	public static String getWithMaxWidth(String name, int width, FontRenderer fontRenderer) {
		boolean changed = false;
		while(fontRenderer.getStringWidth(name) > width) {
			name = name.substring(0, name.length() - 2);
			changed = true;
		}
		if(changed) name += "...";
		return name;
	}
}

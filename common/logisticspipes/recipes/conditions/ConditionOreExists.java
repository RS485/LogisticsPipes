package logisticspipes.recipes.conditions;

import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;

import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class ConditionOreExists implements IConditionFactory {

	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		String str = json.get("ore").getAsString();
		boolean invert = str.startsWith("!");
		if (invert) str = str.substring(1);
		String ore = str;
		return () -> invert ^ OreDictionary.doesOreNameExist(ore);
	}

}

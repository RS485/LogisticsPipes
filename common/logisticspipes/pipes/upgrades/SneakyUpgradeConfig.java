package logisticspipes.pipes.upgrades;

import java.util.Arrays;

import logisticspipes.modules.ModuleCreativeTabBasedItemSink;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.modules.ModulePolymorphicItemSink;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.network.guis.upgrade.SneakyUpgradeConfigGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SneakyUpgradeConfig implements IConfigPipeUpgrade {

	public static final String SIDE_KEY = "LPSNEAKY-SIDE";

	@AllArgsConstructor
	public enum Sides {
		UP(ForgeDirection.UP, "LPSNEAKY-UP"),
		DOWN(ForgeDirection.DOWN, "LPSNEAKY-DOWN"),
		NORTH(ForgeDirection.NORTH, "LPSNEAKY-NORTH"),
		SOUTH(ForgeDirection.SOUTH, "LPSNEAKY-SOUTH"),
		EAST(ForgeDirection.EAST, "LPSNEAKY-EAST"),
		WEST(ForgeDirection.WEST, "LPSNEAKY-WEST");

		@Getter
		private ForgeDirection dir;
		@Getter
		private String lpName;

		public static String getNameForDirection(ForgeDirection fd) {
			return Arrays.stream(values())
					.filter(side -> side.getDir() == fd)
					.map(Sides::getLpName)
					.findFirst()
					.orElse("LPSNEAKY-UNKNWON");
		}
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public boolean isAllowedForPipe(CoreRoutedPipe pipe) {
		return true;
	}

	@Override
	public boolean isAllowedForModule(LogisticsModule module) {
		return module instanceof ModuleItemSink || module instanceof ModulePolymorphicItemSink || module instanceof ModuleModBasedItemSink
				|| module instanceof ModuleOreDictItemSink || module instanceof ModuleCreativeTabBasedItemSink;
	}

	@Override
	public String[] getAllowedPipes() {
		return new String[] { "all" };
	}

	@Override
	public String[] getAllowedModules() {
		return new String[] { "itemsink" };
	}

	@Override
	public UpgradeCoordinatesGuiProvider getGUI() {
		return NewGuiHandler.getGui(SneakyUpgradeConfigGuiProvider.class);
	}

	public ForgeDirection getSide(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbt = stack.getTagCompound();
		String sideString = nbt.getString(SIDE_KEY);

		return Arrays.stream(Sides.values())
				.filter(side -> side.getLpName().equals(sideString))
				.map(Sides::getDir)
				.findFirst()
				.orElse(ForgeDirection.UNKNOWN);
	}
}

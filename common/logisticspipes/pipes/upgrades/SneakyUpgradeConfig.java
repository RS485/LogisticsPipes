package logisticspipes.pipes.upgrades;

import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import lombok.AllArgsConstructor;
import lombok.Getter;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleCreativeTabBasedItemSink;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.modules.ModulePolymorphicItemSink;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.network.guis.upgrade.SneakyUpgradeConfigGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class SneakyUpgradeConfig implements IConfigPipeUpgrade {

	public static final String SIDE_KEY = "LPSNEAKY-SIDE";

	public static String getName() {
		return "sneaky";
	}

	@AllArgsConstructor
	public enum Sides {
		UP(EnumFacing.UP, "LPSNEAKY-UP"),
		DOWN(EnumFacing.DOWN, "LPSNEAKY-DOWN"),
		NORTH(EnumFacing.NORTH, "LPSNEAKY-NORTH"),
		SOUTH(EnumFacing.SOUTH, "LPSNEAKY-SOUTH"),
		EAST(EnumFacing.EAST, "LPSNEAKY-EAST"),
		WEST(EnumFacing.WEST, "LPSNEAKY-WEST");

		@Getter
		private EnumFacing dir;
		@Getter
		private String lpName;

		public static String getNameForDirection(EnumFacing fd) {
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

	@Nullable
	public EnumFacing getSide(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) return null;
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
		String sideString = tag.getString(SIDE_KEY);
		return Arrays.stream(Sides.values())
				.filter(side -> side.getLpName().equals(sideString))
				.map(Sides::getDir)
				.findFirst()
				.orElse(null);
	}
}

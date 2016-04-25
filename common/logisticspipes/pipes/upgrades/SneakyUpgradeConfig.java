package logisticspipes.pipes.upgrades;

import logisticspipes.modules.*;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.network.guis.upgrade.SneakyUpgradeConfigGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

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
		@Getter private String lpName;
		public static String getNameForDirection(ForgeDirection fd) {
			Optional<Sides> opt = Arrays.stream(values()).filter(side -> side.getDir() == fd).findFirst();
			if(opt.isPresent()) {
				return opt.get().getLpName();
			}
			return "LPSNEAKY-UNKNWON";
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
		return module instanceof ModuleItemSink || module instanceof ModulePolymorphicItemSink || module instanceof ModuleModBasedItemSink || module instanceof ModuleOreDictItemSink || module instanceof ModuleCreativeTabBasedItemSink;
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
		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbt = stack.getTagCompound();
		String sideString = nbt.getString(SIDE_KEY);
		Optional<Sides> opt = Arrays.stream(Sides.values()).filter(side -> side.getLpName().equals(sideString)).findFirst();
		if(opt.isPresent()) {
			return opt.get().getDir();
		}
		return ForgeDirection.UNKNOWN;
	}
}

package logisticspipes.recipes;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import lombok.Data;

@Data
public class CraftingParts {

	/**
	 * Iron Chip
	 * FPGA
	 */
	@Nonnull
	private final ItemStack chipFpga;
	/**
	 * Gold Chip
	 * Basic Microcontroller
	 */
	@Nonnull
	private final ItemStack chipBasic;
	/**
	 * Diamond Chip
	 * Advanced Microcontroller
	 */
	@Nonnull
	private final ItemStack chipAdvanced;
}

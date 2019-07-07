package logisticspipes.modplugins.jei;

import java.awt.Rectangle;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IGhostIngredientHandler;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.SetGhostItemPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.gui.DummySlot;
import logisticspipes.utils.gui.FluidSlot;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public class GhostIngredientHandler implements IGhostIngredientHandler<LogisticsBaseGuiScreen> {

	@Override
	public <I> List<Target<I>> getTargets(LogisticsBaseGuiScreen gui, I ingredient, boolean doStart) {
		if (ingredient instanceof ItemStack) {
			Stream<Target<I>> slotStream = gui.inventorySlots.inventorySlots.stream().filter(it -> it instanceof DummySlot).map(it -> (DummySlot) it)
					.map(it -> new Target<I>() {

						@Override
						public Rectangle getArea() {
							return new Rectangle(gui.getGuiLeft() + it.xPos, gui.getGuiTop() + it.yPos, 17, 17);
						}

						@Override
						public void accept(I ingredient) {
							it.putStack((ItemStack) ingredient);
							MainProxy.sendPacketToServer(
									PacketHandler.getPacket(SetGhostItemPacket.class).setInteger(it.slotNumber).setStack((ItemStack) ingredient));
						}
					});
			if (FluidIdentifier.get((ItemStack) ingredient) != null) {
				Stream<Target<I>> fluidStream = gui.inventorySlots.inventorySlots.stream().filter(it -> it instanceof FluidSlot).map(it -> (FluidSlot) it)
						.map(it -> new Target<I>() {

							@Override
							public Rectangle getArea() {
								return new Rectangle(gui.getGuiLeft() + it.xPos, gui.getGuiTop() + it.yPos, 17, 17);
							}

							@Override
							public void accept(I ingredient) {
								FluidIdentifier ident = FluidIdentifier.get((ItemStack) ingredient);
								if (ident != null) {
									ItemStack stack = ident.getItemIdentifier().unsafeMakeNormalStack(1);
									it.putStack(stack);
									MainProxy.sendPacketToServer(PacketHandler.getPacket(SetGhostItemPacket.class).setInteger(it.slotNumber).setStack(stack));
								}
							}
						});
				slotStream = Stream.concat(slotStream, fluidStream);
			}
			return slotStream.collect(Collectors.toList());
		} else if (ingredient instanceof FluidStack) {
			return  gui.inventorySlots.inventorySlots.stream().filter(it -> it instanceof FluidSlot).map(it -> (FluidSlot) it).map(it -> new Target<I>() {
				@Override
				public Rectangle getArea() {
					return new Rectangle(gui.getGuiLeft() + it.xPos, gui.getGuiTop() + it.yPos, 17, 17);
				}

				@Override
				public void accept(I ingredient) {
					FluidIdentifier ident = FluidIdentifier.get((FluidStack) ingredient);
					if (ident != null) {
						ItemStack stack = ident.getItemIdentifier().unsafeMakeNormalStack(1);
						it.putStack(stack);
						MainProxy.sendPacketToServer(PacketHandler.getPacket(SetGhostItemPacket.class).setInteger(it.slotNumber).setStack(stack));
					}
				}
			}).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	public void onComplete() {}
}

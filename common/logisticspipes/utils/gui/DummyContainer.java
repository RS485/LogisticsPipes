/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.gui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.interfaces.ISlotClick;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.ChassisModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.FuzzySlotSettingsPacket;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.MinecraftColor;
import logisticspipes.utils.ReflectionHelper;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.property.IBitSet;

public class DummyContainer extends Container {

	@SideOnly(Side.CLIENT)
	public LogisticsBaseGuiScreen guiHolderForJEI; // This is not set for every GUI. Only for the one needed by JEI.

	public List<BitSet> slotsFuzzyFlags = new ArrayList<>();
	protected IInventory _playerInventory;
	protected IInventory _dummyInventory;
	protected IGuiOpenControler[] _controler;
	boolean wasDummyLookup;
	boolean overrideMCAntiSend;
	private List<Slot> transferTop = new ArrayList<>();
	private List<Slot> transferBottom = new ArrayList<>();
	private long lastClicked;
	private long lastDragnDropLockup;

	public DummyContainer(IInventory playerInventory, IInventory dummyInventory) {
		_playerInventory = playerInventory;
		_dummyInventory = dummyInventory;
		_controler = null;
	}

	public DummyContainer(EntityPlayer player, IInventory dummyInventory, IGuiOpenControler... controler) {
		_playerInventory = player.inventory;
		_dummyInventory = dummyInventory;
		_controler = controler;
		if (MainProxy.isServer(player.world)) {
			for (IGuiOpenControler element : _controler) {
				element.guiOpenedByPlayer(player);
			}
		}
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	/***
	 * Adds all slots for the player inventory and hotbar
	 */
	public void addNormalSlotsForPlayerInventory(int xOffset, int yOffset) {
		if (_playerInventory == null) {
			return;
		}
		// Player "backpack"
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				Slot slot = new Slot(_playerInventory, column + row * 9 + 9, xOffset + column * 18, yOffset + row * 18);
				addSlotToContainer(slot);
				transferBottom.add(slot);
			}
		}

		// Player "hotbar"
		for (int i1 = 0; i1 < 9; i1++) {
			Slot slot = new Slot(_playerInventory, i1, xOffset + i1 * 18, yOffset + 58);
			addSlotToContainer(slot);
			transferBottom.add(slot);
		}
	}

	/**
	 * Add a dummy slot that will not consume players items
	 *
	 * @param slotId
	 *            The slot number in the dummy IInventory this slot should map
	 * @param xCoord
	 *            xCoord of TopLeft corner of where the slot should be rendered
	 * @param yCoord
	 *            yCoord of TopLeft corner of where the slot should be rendered
	 */
	public Slot addDummySlot(int slotId, int xCoord, int yCoord) {
		return addSlotToContainer(new DummySlot(_dummyInventory, slotId, xCoord, yCoord));
	}

	public Slot addDummySlot(int slotId, IInventory dummy, int xCoord, int yCoord) {
		return addSlotToContainer(new DummySlot(dummy, slotId, xCoord, yCoord));
	}

	public void addNormalSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		transferTop.add(addSlotToContainer(new Slot(inventory, slotId, xCoord, yCoord)));
	}

	public Slot addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, Class<? extends Item> itemClass) {
		return addSlotToContainer(new RestrictedSlot(inventory, slotId, xCoord, yCoord, itemClass));
	}

	public Slot addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, Item item) {
		return addSlotToContainer(new RestrictedSlot(inventory, slotId, xCoord, yCoord, item));
	}

	public Slot addStaticRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, Item item, int stackLimit) {
		return addSlotToContainer(new StaticRestrictedSlot(inventory, slotId, xCoord, yCoord, item, stackLimit));
	}

	public Slot addRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotCheck slotCheck) {
		return addSlotToContainer(new RestrictedSlot(inventory, slotId, xCoord, yCoord, slotCheck));
	}

	public Slot addStaticRestrictedSlot(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotCheck slotCheck, int stackLimit) {
		return addSlotToContainer(new StaticRestrictedSlot(inventory, slotId, xCoord, yCoord, slotCheck, stackLimit));
	}

	public void addModuleSlot(int slotId, IInventory inventory, int xCoord, int yCoord, PipeLogisticsChassis pipe) {
		transferTop.add(addSlotToContainer(new ModuleSlot(inventory, slotId, xCoord, yCoord, pipe)));
	}

	public Slot addFluidSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		return addSlotToContainer(new FluidSlot(inventory, slotId, xCoord, yCoord));
	}

	public Slot addColorSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		return addSlotToContainer(new ColorSlot(inventory, slotId, xCoord, yCoord));
	}

	public Slot addUnmodifiableSlot(int slotId, IInventory inventory, int xCoord, int yCoord) {
		return addSlotToContainer(new UnmodifiableSlot(inventory, slotId, xCoord, yCoord));
	}

	public Slot addCallableSlotHandler(int slotId, IInventory inventory, int xCoord, int yCoord, ISlotClick handler) {
		return addSlotToContainer(new HandelableSlot(inventory, slotId, xCoord, yCoord, handler));
	}

	public Slot addFuzzyDummySlot(int slotId, int xCoord, int yCoord, IBitSet fuzzyFlags) {
		return addSlotToContainer(new FuzzyDummySlot(_dummyInventory, slotId, xCoord, yCoord, fuzzyFlags));
	}

	public Slot addFuzzyUnmodifiableSlot(int slotId, IInventory inventory, int xCoord, int yCoord, IBitSet fuzzyFlags) {
		return addSlotToContainer(new FuzzyUnmodifiableSlot(inventory, slotId, xCoord, yCoord, fuzzyFlags));
	}

	public Slot addUpgradeSlot(int slotId, ISlotUpgradeManager manager, int upgradeSlotId, int xCoord, int yCoord, ISlotCheck slotCheck) {
		Slot slot = addSlotToContainer(new UpgradeSlot(manager, upgradeSlotId, slotId, xCoord, yCoord, slotCheck));
		transferTop.add(slot);
		return slot;
	}

	public Slot addSneakyUpgradeSlot(int slotId, UpgradeManager manager, int upgradeSlotId, int xCoord, int yCoord, ISlotCheck slotCheck) {
		Slot slot = addSlotToContainer(new SneakyUpgradeSlot(manager, upgradeSlotId, slotId, xCoord, yCoord, slotCheck));
		transferTop.add(slot);
		return slot;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int i) {
		if (transferTop.isEmpty() || transferBottom.isEmpty()) {
			return ItemStack.EMPTY;
		}
		Slot slot = inventorySlots.get(i);
		if (slot == null || slot instanceof DummySlot || slot instanceof UnmodifiableSlot || slot instanceof FluidSlot || slot instanceof ColorSlot || slot instanceof HandelableSlot || !slot.getHasStack()) {
			return ItemStack.EMPTY;
		}
		if (transferTop.contains(slot)) {
			handleShiftClickLists(slot, transferBottom, true, player);
			handleShiftClickLists(slot, transferBottom, false, player);
		} else if (transferBottom.contains(slot)) {
			handleShiftClickLists(slot, transferTop, true, player);
			handleShiftClickLists(slot, transferTop, false, player);
		}
		return ItemStack.EMPTY;
	}

	private void handleShiftClickLists(Slot from, List<Slot> toList, boolean ignoreEmpty, EntityPlayer player) {
		if (!from.getHasStack()) {
			return;
		}
		for (Slot to : toList) {
			if (handleShiftClickForSlots(from, to, ignoreEmpty, player)) {
				return;
			}
		}
	}

	private boolean handleShiftClickForSlots(Slot from, Slot to, boolean ignoreEmpty, EntityPlayer player) {
		if (!from.getHasStack()) {
			return true;
		}
		ItemStack out = from.getStack();
		if (!to.getHasStack() && !ignoreEmpty && to.isItemValid(out)) {
			boolean remove = true;
			if (out.getCount() > to.getSlotStackLimit()) {
				out = from.decrStackSize(to.getSlotStackLimit());
				remove = false;
			}
			to.putStack(from.onTake(player, out));
			if (remove) {
				from.putStack(ItemStack.EMPTY);
			}
			return true;
		}
		if (from instanceof ModuleSlot || to instanceof ModuleSlot) {
			return false;
		}
		out = from.onTake(player, out);
		if (to.getHasStack() && to.getStack().isItemEqual(out) && ItemStack.areItemStackTagsEqual(to.getStack(), from.getStack())) {
			int free = Math.min(to.getSlotStackLimit(), to.getStack().getMaxStackSize()) - to.getStack().getCount();
			if (free > 0) {
				ItemStack toInsert = from.decrStackSize(free);
				toInsert = from.onTake(player, toInsert);
				ItemStack toStack = to.getStack();
				if (!toInsert.isEmpty() && !toStack.isEmpty()) {
					toStack.grow(toInsert.getCount());
					to.putStack(toStack);
					return !from.getHasStack();
				}
			}
		}
		return false;
	}

	@Nonnull
	public ItemStack superSlotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		ItemStack itemstack = ItemStack.EMPTY;
		InventoryPlayer inventoryplayer = player.inventory;

		if (clickTypeIn == ClickType.QUICK_CRAFT) {
			int j1 = this.dragEvent;
			this.dragEvent = getDragEvent(dragType);

			if ((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent) {
				this.resetDrag();
			} else if (inventoryplayer.getItemStack().isEmpty()) {
				this.resetDrag();
			} else if (this.dragEvent == 0) {
				this.dragMode = extractDragMode(dragType);

				if (isValidDragMode(this.dragMode, player)) {
					this.dragEvent = 1;
					this.dragSlots.clear();
				} else {
					this.resetDrag();
				}
			} else if (this.dragEvent == 1) {
				Slot slot7 = this.inventorySlots.get(slotId);
				ItemStack itemstack12 = inventoryplayer.getItemStack();

				if (slot7 != null && canAddItemToSlot(slot7, itemstack12, true) && slot7.isItemValid(itemstack12) && (this.dragMode == 2
						|| itemstack12.getCount() > this.dragSlots.size()) && this.canDragIntoSlot(slot7)) {
					this.dragSlots.add(slot7);
				}
			} else if (this.dragEvent == 2) {
				if (!this.dragSlots.isEmpty()) {
					ItemStack itemstack9 = inventoryplayer.getItemStack().copy();
					int k1 = inventoryplayer.getItemStack().getCount();

					for (Slot slot8 : this.dragSlots) {
						ItemStack itemstack13 = inventoryplayer.getItemStack();

						if (slot8 != null && canAddItemToSlot(slot8, itemstack13, true) && slot8.isItemValid(itemstack13) && (this.dragMode == 2
								|| itemstack13.getCount() >= this.dragSlots.size()) && this.canDragIntoSlot(slot8)) {
							ItemStack itemstack14 = itemstack9.copy();
							int j3 = slot8.getHasStack() ? slot8.getStack().getCount() : 0;
							computeStackSize(this.dragSlots, this.dragMode, itemstack14, j3);
							int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getItemStackLimit(itemstack14));

							if (itemstack14.getCount() > k3) {
								itemstack14.setCount(k3);
							}

							k1 -= itemstack14.getCount() - j3;
							slot8.putStack(itemstack14);
						}
					}

					itemstack9.setCount(k1);
					inventoryplayer.setItemStack(itemstack9);
				}

				this.resetDrag();
			} else {
				this.resetDrag();
			}
		} else if (this.dragEvent != 0) {
			this.resetDrag();
		} else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
			if (slotId == -999) {
				if (!inventoryplayer.getItemStack().isEmpty()) {
					if (dragType == 0) {
						player.dropItem(inventoryplayer.getItemStack(), true);
						inventoryplayer.setItemStack(ItemStack.EMPTY);
					}

					if (dragType == 1) {
						player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
					}
				}
			} else if (clickTypeIn == ClickType.QUICK_MOVE) {
				if (slotId < 0) {
					return ItemStack.EMPTY;
				}

				Slot slot5 = this.inventorySlots.get(slotId);

				if (slot5 == null || !slot5.canTakeStack(player)) {
					return ItemStack.EMPTY;
				}

				for (ItemStack itemstack7 = this.transferStackInSlot(player, slotId);
					 !itemstack7.isEmpty() && ItemStack.areItemsEqual(slot5.getStack(), itemstack7); itemstack7 = this.transferStackInSlot(player, slotId)) {
					itemstack = itemstack7.copy();
				}
			} else {
				if (slotId < 0) {
					return ItemStack.EMPTY;
				}

				Slot slot6 = this.inventorySlots.get(slotId);

				if (slot6 != null) {
					ItemStack itemstack8 = slot6.getStack();
					ItemStack itemstack11 = inventoryplayer.getItemStack();

					if (!itemstack8.isEmpty()) {
						itemstack = itemstack8.copy();
					}

					if (itemstack8.isEmpty()) {
						if (!itemstack11.isEmpty() && slot6.isItemValid(itemstack11)) {
							int i3 = dragType == 0 ? itemstack11.getCount() : 1;

							if (i3 > slot6.getItemStackLimit(itemstack11)) {
								i3 = slot6.getItemStackLimit(itemstack11);
							}

							slot6.putStack(itemstack11.splitStack(i3));
						}
					} else if (slot6.canTakeStack(player)) {
						if (itemstack11.isEmpty()) {
							if (itemstack8.isEmpty()) {
								slot6.putStack(ItemStack.EMPTY);
								inventoryplayer.setItemStack(ItemStack.EMPTY);
							} else {
								int l2 = dragType == 0 ? itemstack8.getCount() : (itemstack8.getCount() + 1) / 2;
								inventoryplayer.setItemStack(slot6.decrStackSize(l2));

								if (itemstack8.isEmpty()) {
									slot6.putStack(ItemStack.EMPTY);
								}

								slot6.onTake(player, inventoryplayer.getItemStack());
							}
						} else if (slot6.isItemValid(itemstack11)) {
							if (itemstack8.getItem() == itemstack11.getItem() && itemstack8.getMetadata() == itemstack11.getMetadata() && ItemStack
									.areItemStackTagsEqual(itemstack8, itemstack11)) {
								int k2 = dragType == 0 ? itemstack11.getCount() : 1;

								if (k2 > slot6.getItemStackLimit(itemstack11) - itemstack8.getCount()) {
									k2 = slot6.getItemStackLimit(itemstack11) - itemstack8.getCount();
								}

								if (k2 > itemstack11.getMaxStackSize() - itemstack8.getCount()) {
									k2 = itemstack11.getMaxStackSize() - itemstack8.getCount();
								}

								itemstack11.shrink(k2);
								itemstack8.grow(k2);

								slot6.putStack(itemstack8); // XXX added reinserting of the modified itemStack (Fix ItemIdentifierInventory's disappearing items)
							} else if (itemstack11.getCount() <= slot6.getItemStackLimit(itemstack11)) {
								handleSwitch(slot6, itemstack8, itemstack11, player); // XXX added Slot switching handle method
								slot6.putStack(itemstack11);
								inventoryplayer.setItemStack(itemstack8);
							}
						} else if (itemstack8.getItem() == itemstack11.getItem() && itemstack11.getMaxStackSize() > 1 && (!itemstack8.getHasSubtypes()
								|| itemstack8.getMetadata() == itemstack11.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack8, itemstack11)
								&& !itemstack8.isEmpty()) {
							int j2 = itemstack8.getCount();

							if (j2 + itemstack11.getCount() <= itemstack11.getMaxStackSize()) {
								itemstack11.grow(j2);
								itemstack8 = slot6.decrStackSize(j2);

								if (itemstack8.isEmpty()) {
									slot6.putStack(ItemStack.EMPTY);
								}

								slot6.onTake(player, inventoryplayer.getItemStack());
							}
						}
					}

					slot6.onSlotChanged();
				}
			}
		} else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
			Slot slot4 = this.inventorySlots.get(slotId);
			ItemStack itemstack6 = inventoryplayer.getStackInSlot(dragType);
			ItemStack itemstack10 = slot4.getStack();

			if (!itemstack6.isEmpty() || !itemstack10.isEmpty()) {
				if (itemstack6.isEmpty()) {
					if (slot4.canTakeStack(player)) {
						inventoryplayer.setInventorySlotContents(dragType, itemstack10);
						ReflectionHelper.invokePrivateMethod(Slot.class, slot4, "onSwapCraft", "func_190900_b", new Class[] { int.class }, new Object[] { itemstack10.getCount() });
						slot4.putStack(ItemStack.EMPTY);
						slot4.onTake(player, itemstack10);
					}
				} else if (itemstack10.isEmpty()) {
					if (slot4.isItemValid(itemstack6)) {
						int l1 = slot4.getItemStackLimit(itemstack6);

						if (itemstack6.getCount() > l1) {
							slot4.putStack(itemstack6.splitStack(l1));
						} else {
							slot4.putStack(itemstack6);
							inventoryplayer.setInventorySlotContents(dragType, ItemStack.EMPTY);
						}
					}
				} else if (slot4.canTakeStack(player) && slot4.isItemValid(itemstack6)) {
					int i2 = slot4.getItemStackLimit(itemstack6);

					if (itemstack6.getCount() > i2) {
						slot4.putStack(itemstack6.splitStack(i2));
						slot4.onTake(player, itemstack10);

						if (!inventoryplayer.addItemStackToInventory(itemstack10)) {
							player.dropItem(itemstack10, true);
						}
					} else {
						slot4.putStack(itemstack6);
						inventoryplayer.setInventorySlotContents(dragType, itemstack10);
						slot4.onTake(player, itemstack10);
					}
				}
			}
		} else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack().isEmpty() && slotId >= 0) {
			Slot slot3 = this.inventorySlots.get(slotId);

			if (slot3 != null && slot3.getHasStack()) {
				ItemStack itemstack5 = slot3.getStack().copy();
				itemstack5.setCount(itemstack5.getMaxStackSize());
				inventoryplayer.setItemStack(itemstack5);
			}
		} else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack().isEmpty() && slotId >= 0) {
			Slot slot2 = this.inventorySlots.get(slotId);

			if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player)) {
				ItemStack itemstack4 = slot2.decrStackSize(dragType == 0 ? 1 : slot2.getStack().getCount());
				slot2.onTake(player, itemstack4);
				player.dropItem(itemstack4, true);
			}
		} else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
			Slot slot = this.inventorySlots.get(slotId);
			ItemStack itemstack1 = inventoryplayer.getItemStack();

			if (!itemstack1.isEmpty() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
				int i = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
				int j = dragType == 0 ? 1 : -1;

				for (int k = 0; k < 2; ++k) {
					for (int l = i; l >= 0 && l < this.inventorySlots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); l += j) {
						Slot slot1 = this.inventorySlots.get(l);

						if (slot1.getHasStack() && canAddItemToSlot(slot1, itemstack1, true) && slot1.canTakeStack(player) && this
								.canMergeSlot(itemstack1, slot1)) {
							ItemStack itemstack2 = slot1.getStack();

							if (k != 0 || itemstack2.getCount() != itemstack2.getMaxStackSize()) {
								int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.getCount(), itemstack2.getCount());
								ItemStack itemstack3 = slot1.decrStackSize(i1);
								itemstack1.grow(i1);

								if (itemstack3.isEmpty()) {
									slot1.putStack(ItemStack.EMPTY);
								}

								slot1.onTake(player, itemstack3);
							}
						}
					}
				}
			}

			this.detectAndSendChanges();
		}

		return itemstack;
	}

	private void handleSwitch(Slot slot2, @Nonnull ItemStack out, @Nonnull ItemStack in, EntityPlayer player) {
		if (slot2 instanceof ModuleSlot) {
			ChassisModule chassis = (ChassisModule) ((ModuleSlot) slot2).get_pipe().getLogisticsModule();
			int moduleIndex = ((ModuleSlot) slot2).get_moduleIndex();
			if (out.getItem() instanceof ItemModule) {
				if (chassis.hasModule(moduleIndex)) {
					ItemModuleInformationManager.saveInformation(out, chassis.getModule(moduleIndex));
					chassis.removeModule(moduleIndex);
				}
			}
		}
	}

	/**
	 * Clone/clear itemstacks for items
	 */
	@Nonnull
	@Override
	public ItemStack slotClick(int slotId, int mouseButton, @Nonnull ClickType shiftMode, @Nonnull EntityPlayer player) {
		lastClicked = System.currentTimeMillis();
		if (slotId < 0) {
			return superSlotClick(slotId, mouseButton, shiftMode, player);
		}
		Slot slot = inventorySlots.get(slotId);
		//debug dump
		if (LogisticsPipes.isDEBUG() && slot != null) {
			ItemStack stack = slot.getStack();
			if (!stack.isEmpty()) {
				ItemIdentifier.get(stack).debugDumpData(player.world.isRemote);
			}
		}
		if (slot == null) return ItemStack.EMPTY;
		if ((!(slot instanceof DummySlot) && !(slot instanceof UnmodifiableSlot) && !(slot instanceof FluidSlot) && !(slot instanceof ColorSlot) && !(slot instanceof HandelableSlot))) {
			ItemStack stack1 = superSlotClick(slotId, mouseButton, shiftMode, player);
			ItemStack stack2 = slot.getStack();
			if (!stack2.isEmpty() && stack2.getItem() instanceof ItemModule) {
				if (player instanceof EntityPlayerMP && MainProxy.isServer(player.world)) {
					((EntityPlayerMP) player).sendSlotContents(this, slotId, stack2);
				}
			}
			return stack1;
		}

		InventoryPlayer inventoryplayer = player.inventory;

		ItemStack currentlyEquippedStack = inventoryplayer.getItemStack();

		// we get a leftclick *and* a doubleclick message if there's a doubleclick with no item on the pointer, filter it out
		if (currentlyEquippedStack.isEmpty() && shiftMode == ClickType.PICKUP_ALL) {
			return currentlyEquippedStack;
		}

		if (slot instanceof HandelableSlot) {
			overrideMCAntiSend = true;
			if (currentlyEquippedStack.isEmpty()) {
				inventoryplayer.setItemStack(((HandelableSlot) slot).getProvidedStack());
				return ItemStack.EMPTY;
			}
			return currentlyEquippedStack;
		}

		if (slot instanceof UnmodifiableSlot) {
			return currentlyEquippedStack;
		}

		handleDummyClick(slot, slotId, currentlyEquippedStack, mouseButton, shiftMode, player);
		return currentlyEquippedStack;
	}

	public void handleDummyClick(Slot slot, int slotId, @Nonnull ItemStack currentlyEquippedStack, int mouseButton, ClickType shiftMode, EntityPlayer entityplayer) {
		if (slot instanceof FluidSlot) {
			if (!currentlyEquippedStack.isEmpty()) {
				FluidIdentifier ident = FluidIdentifier.get(currentlyEquippedStack);
				if (ident != null) {
					if (mouseButton == 0) {
						slot.putStack(ident.getItemIdentifier().unsafeMakeNormalStack(1));
					} else {
						slot.putStack(ItemStack.EMPTY);
					}
					return;
				}
			}
			FluidIdentifier ident = null;
			if (!slot.getStack().isEmpty()) {
				ident = FluidIdentifier.get(ItemIdentifier.get(slot.getStack()));
			}
			if (ident == null) {
				if (MainProxy.isClient(entityplayer.getEntityWorld())) {
					MainProxy.proxy.openFluidSelectGui(slotId);
				}
			}
			slot.putStack(ItemStack.EMPTY);
			return;
		}

		if (slot instanceof ColorSlot) {
			MinecraftColor equipped = MinecraftColor.getColor(currentlyEquippedStack);
			MinecraftColor color = MinecraftColor.getColor(slot.getStack());
			if (MinecraftColor.BLANK.equals(equipped)) {
				if (mouseButton == 0) {
					color = color.getNext();
				} else if (mouseButton == 1) {
					color = color.getPrev();
				} else {
					color = MinecraftColor.BLANK;
				}
				slot.putStack(color.getItemStack());
			} else {
				if (mouseButton == 1) {
					slot.putStack(MinecraftColor.BLANK.getItemStack());
				} else {
					slot.putStack(equipped.getItemStack());
				}
			}
			if (entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.world)) {
				((EntityPlayerMP) entityplayer).sendSlotContents(this, slotId, slot.getStack());
			}
			return;
		}

		if (slot instanceof DummySlot) {
			((DummySlot) slot).setRedirectCall(true);
		}

		if (mouseButton >= 1000) {
			if (mouseButton <= 1001) {
				if (slot.getHasStack()) {
					ItemStack stack = slot.getStack().copy();
					if (mouseButton == 1000) {
						stack.grow(1);
					} else if (stack.getCount() > 1) {
						stack.shrink(1);
					}
					stack.setCount(Math.min(slot.getSlotStackLimit(), Math.max(1, stack.getCount())));
					slot.putStack(stack);
				}
				if (slot instanceof DummySlot) {
					((DummySlot) slot).setRedirectCall(false);
				}
				return;
			}
		}

		if (currentlyEquippedStack.isEmpty()) {
			if (!slot.getStack().isEmpty() && mouseButton == 1) {
				ItemStack tstack = slot.getStack();
				if (shiftMode == ClickType.QUICK_MOVE) {
					tstack.setCount(Math.min(slot.getSlotStackLimit(), tstack.getCount() * 2));
				} else {
					tstack.setCount(tstack.getCount() / 2);
				}
				slot.putStack(tstack);
			} else {
				slot.putStack(ItemStack.EMPTY);
			}
			if (slot instanceof DummySlot) {
				((DummySlot) slot).setRedirectCall(false);
			}
			return;
		}

		if (!slot.getHasStack()) {
			ItemStack tstack = currentlyEquippedStack.copy();
			if (mouseButton == 1) {
				tstack.setCount(1);
			}
			if (tstack.getCount() > slot.getSlotStackLimit()) {
				tstack.setCount(slot.getSlotStackLimit());
			}
			slot.putStack(tstack);
			if (slot instanceof DummySlot) {
				((DummySlot) slot).setRedirectCall(false);
			}
			return;
		}

		ItemIdentifier currentItem = ItemIdentifier.get(currentlyEquippedStack);
		ItemIdentifier slotItem = ItemIdentifier.get(slot.getStack());
		if (currentItem.equals(slotItem)) {
			ItemStack tstack = slot.getStack();
			// Do manual shift-checking to play nice with NEI
			int counter = shiftMode == ClickType.QUICK_MOVE ? 10 : 1;
			if (mouseButton == 1) {
				if (tstack.getCount() + counter <= slot.getSlotStackLimit()) {
					tstack.grow(counter);
				} else {
					tstack.setCount(slot.getSlotStackLimit());
				}
				slot.putStack(tstack);
			} else if (mouseButton == 0) {
				tstack.shrink(counter);
				slot.putStack(tstack);
			}
			if (slot instanceof DummySlot) {
				((DummySlot) slot).setRedirectCall(false);
			}
			return;
		}

		ItemStack tstack = currentlyEquippedStack.copy();
		if (tstack.getCount() > slot.getSlotStackLimit()) {
			tstack.setCount(slot.getSlotStackLimit());
		}
		slot.putStack(tstack);
		if (slot instanceof DummySlot) {
			((DummySlot) slot).setRedirectCall(false);
		}
	}

	@Override
	public void onContainerClosed(@Nonnull EntityPlayer player) {
		if (_controler != null) {
			for (IGuiOpenControler element : _controler) {
				element.guiClosedByPlayer(player);
			}
		}
		super.onContainerClosed(player);
	}

	public void addRestrictedHotbarForPlayerInventory(int xOffset, int yOffset) {
		if (_playerInventory == null) {
			return;
		}
		// Player "hotbar"
		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new UnmodifiableSlot(_playerInventory, i1, xOffset + i1 * 18, yOffset));
		}
	}

	public void addRestrictedArmorForPlayerInventory(int xOffset, int yOffset) {
		if (_playerInventory == null) {
			return;
		}
		for (int i1 = 0; i1 < 4; i1++) {
			addSlotToContainer(new UnmodifiableSlot(_playerInventory, i1 + 36, xOffset, yOffset - i1 * 18));
		}
	}

	@Override
	public boolean canDragIntoSlot(@Nonnull Slot slot) {
		if (slot instanceof UnmodifiableSlot || slot instanceof FluidSlot || slot instanceof ColorSlot || slot instanceof HandelableSlot) {
			return false;
		}
		if (lastDragnDropLockup <= lastClicked) { // Slot was clicked after last lookup
			lastDragnDropLockup = System.currentTimeMillis();
			if (slot instanceof DummySlot) {
				wasDummyLookup = true;
				return true;
			}
			wasDummyLookup = false;
			return true;
		} else { // Still lookingUp (during drag'n'drop)
			lastDragnDropLockup = System.currentTimeMillis();
			if (slot instanceof DummySlot) {
				return wasDummyLookup;
			}
			return !wasDummyLookup;
		}
	}

	// Hacky overrides to handle client/server player inv sync with 0-slot containers
	@Override
	public Slot getSlotFromInventory(@Nonnull IInventory par1IInventory, int par2) {
		Slot s = super.getSlotFromInventory(par1IInventory, par2);
		if (s != null) {
			return s;
		}
		if (inventorySlots.isEmpty() && par1IInventory == _playerInventory) {
			s = new Slot(_playerInventory, par2, 0, 0);
			s.slotNumber = par2;
			return s;
		}
		return null;
	}

	@Override
	public void putStackInSlot(int par1, @Nonnull ItemStack par2ItemStack) {
		if (inventorySlots.isEmpty()) {
			_playerInventory.setInventorySlotContents(par1, par2ItemStack);
			_playerInventory.markDirty();
			return;
		}
		super.putStackInSlot(par1, par2ItemStack);
	}

	@Override
	public void detectAndSendChanges() {
		for (int i = 0; i < inventorySlots.size(); ++i) {
			if (inventorySlots.get(i) instanceof IFuzzySlot) {
				IFuzzySlot fuzzySlot = (IFuzzySlot) inventorySlots.get(i);
				BitSet slotFlags = fuzzySlot.getFuzzyFlags().copyValue();
				BitSet savedFlags = slotsFuzzyFlags.get(i);
				if (savedFlags == null || !savedFlags.equals(slotFlags)) {
					MainProxy.sendToPlayerList(
							PacketHandler.getPacket(FuzzySlotSettingsPacket.class)
									.setSlotNumber(fuzzySlot.getSlotId())
									.setFlags(slotFlags),
							listeners.stream().filter(o -> o instanceof EntityPlayer).map(o -> (EntityPlayer) o));
					slotsFuzzyFlags.set(i, slotFlags);
				}
			}
			ItemStack itemstack = inventorySlots.get(i).getStack();
			ItemStack itemstack1 = inventoryItemStacks.get(i);

			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
				itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
				inventoryItemStacks.set(i, itemstack1);

				for (IContainerListener crafter : listeners) {
					boolean revert = false;
					if (overrideMCAntiSend && crafter instanceof EntityPlayerMP
							&& ((EntityPlayerMP) crafter).isChangingQuantityOnly) {
						((EntityPlayerMP) crafter).isChangingQuantityOnly = false;
						revert = true;
					}
					crafter.sendSlotContents(this, i, itemstack1);
					if (revert) {
						((EntityPlayerMP) crafter).isChangingQuantityOnly = true;
					}
				}
			}
		}
		overrideMCAntiSend = false;
	}

	@Nonnull
	@Override
	protected Slot addSlotToContainer(@Nonnull Slot slotIn) {
		this.slotsFuzzyFlags.add(null);
		return super.addSlotToContainer(slotIn);
	}
}

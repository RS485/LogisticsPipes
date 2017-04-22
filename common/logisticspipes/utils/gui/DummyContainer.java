/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.gui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

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

import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.interfaces.ISlotClick;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.ChassiModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.FuzzySlotSettingsPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.resources.DictResource;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.MinecraftColor;
import logisticspipes.utils.item.ItemIdentifier;

public class DummyContainer extends Container {

	public List<BitSet> inventoryFuzzySlotsContent = new ArrayList<>();
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
		if (MainProxy.isServer()) {
			for (IGuiOpenControler element : _controler) {
				element.guiOpenedByPlayer(player);
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	/***
	 * Adds all slots for the player inventory and hotbar
	 * 
	 * @param xOffset
	 * @param yOffset
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

	public void addModuleSlot(int slotId, IInventory inventory, int xCoord, int yCoord, PipeLogisticsChassi pipe) {
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

	public Slot addFuzzyDummySlot(int slotId, int xCoord, int yCoord, DictResource dictResource) {
		return addSlotToContainer(new FuzzyDummySlot(_dummyInventory, slotId, xCoord, yCoord, dictResource));
	}

	public Slot addFuzzyUnmodifiableSlot(int slotId, IInventory inventory, int xCoord, int yCoord, DictResource dictResource) {
		return addSlotToContainer(new FuzzyUnmodifiableSlot(inventory, slotId, xCoord, yCoord, dictResource));
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

	@Override
	public ItemStack transferStackInSlot(EntityPlayer pl, int i) {
		if (transferTop.isEmpty() || transferBottom.isEmpty()) {
			return null;
		}
		Slot slot = (Slot) inventorySlots.get(i);
		if (slot == null || slot instanceof DummySlot || slot instanceof UnmodifiableSlot || slot instanceof FluidSlot || slot instanceof ColorSlot || slot instanceof HandelableSlot || !slot.getHasStack()) {
			return null;
		}
		if (transferTop.contains(slot)) {
			handleShiftClickLists(slot, transferBottom, true, pl);
			handleShiftClickLists(slot, transferBottom, false, pl);
			return null;
		} else if (transferBottom.contains(slot)) {
			handleShiftClickLists(slot, transferTop, true, pl);
			handleShiftClickLists(slot, transferTop, false, pl);
			return null;
		} else {
			return null;
		}
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
		if (!to.getHasStack() && !ignoreEmpty) {
			ItemStack out = from.getStack();
			boolean remove = true;
			if(out.stackSize > to.getSlotStackLimit()) {
				out = from.decrStackSize(to.getSlotStackLimit());
				remove = false;
			}
			from.onPickupFromSlot(player, out);
			to.putStack(out);
			if(remove) {
				from.putStack(null);
			}
			return true;
		}
		if(from instanceof ModuleSlot || to instanceof ModuleSlot) {
			return false;
		}
		ItemStack out = from.getStack();
		from.onPickupFromSlot(player, out);
		if (to.getHasStack() && to.getStack().isItemEqual(out) && ItemStack.areItemStackTagsEqual(to.getStack(), from.getStack())) {
			int free = Math.min(to.getSlotStackLimit(), to.getStack().getMaxStackSize()) - to.getStack().stackSize;
			if(free > 0) {
				ItemStack toInsert = from.decrStackSize(free);
				from.onPickupFromSlot(player, toInsert);
				ItemStack toStack = to.getStack();
				if(toInsert != null && toStack != null) {
					toStack.stackSize += toInsert.stackSize;
					to.putStack(toStack);
					return !from.getHasStack();
				}
			}
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ItemStack superSlotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		ItemStack itemstack = null;
		InventoryPlayer inventoryplayer = player.inventory;

		if (clickTypeIn == ClickType.QUICK_CRAFT) {
			int i = this.dragEvent;
			this.dragEvent = getDragEvent(dragType);

			if ((i != 1 || this.dragEvent != 2) && i != this.dragEvent) {
				this.resetDrag();
			} else if (inventoryplayer.getItemStack() == null) {
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
				Slot slot = (Slot) this.inventorySlots.get(slotId);

				if (slot != null && canAddItemToSlot(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack())
						&& inventoryplayer.getItemStack().stackSize > this.dragSlots.size() && this.canDragIntoSlot(slot)) {
					this.dragSlots.add(slot);
				}
			} else if (this.dragEvent == 2) {
				if (!this.dragSlots.isEmpty()) {
					ItemStack itemstack3 = inventoryplayer.getItemStack().copy();
					int j = inventoryplayer.getItemStack().stackSize;

					for (Slot slot1 : this.dragSlots) {
						if (slot1 != null && canAddItemToSlot(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack())
								&& inventoryplayer.getItemStack().stackSize >= this.dragSlots.size() && this.canDragIntoSlot(slot1)) {
							ItemStack itemstack1 = itemstack3.copy();
							int k = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
							computeStackSize(this.dragSlots, this.dragMode, itemstack1, k);

							if (itemstack1.stackSize > itemstack1.getMaxStackSize()) {
								itemstack1.stackSize = itemstack1.getMaxStackSize();
							}

							if (itemstack1.stackSize > slot1.getItemStackLimit(itemstack1)) {
								itemstack1.stackSize = slot1.getItemStackLimit(itemstack1);
							}

							j -= itemstack1.stackSize - k;
							slot1.putStack(itemstack1);
						}
					}

					itemstack3.stackSize = j;

					if (itemstack3.stackSize <= 0) {
						itemstack3 = null;
					}

					inventoryplayer.setItemStack(itemstack3);
				}

				this.resetDrag();
			} else {
				this.resetDrag();
			}
		} else if (this.dragEvent != 0) {
			this.resetDrag();
		} else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
			if (slotId == -999) {
				if (inventoryplayer.getItemStack() != null) {
					if (dragType == 0) {
						player.dropItem(inventoryplayer.getItemStack(), true);
						inventoryplayer.setItemStack((ItemStack) null);
					}

					if (dragType == 1) {
						player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);

						if (inventoryplayer.getItemStack().stackSize == 0) {
							inventoryplayer.setItemStack((ItemStack) null);
						}
					}
				}
			} else if (clickTypeIn == ClickType.QUICK_MOVE) {
				if (slotId < 0) {
					return null;
				}

				Slot slot6 = (Slot) this.inventorySlots.get(slotId);

				if (slot6 != null && slot6.canTakeStack(player)) {
					ItemStack itemstack8 = slot6.getStack();

					if (itemstack8 != null && itemstack8.stackSize <= 0) {
						itemstack = itemstack8.copy();
						slot6.putStack((ItemStack) null);
					}

					ItemStack itemstack11 = this.transferStackInSlot(player, slotId);

					if (itemstack11 != null) {
						Item item = itemstack11.getItem();
						itemstack = itemstack11.copy();

						if (slot6.getStack() != null && slot6.getStack().getItem() == item) {
							this.retrySlotClick(slotId, dragType, true, player);
						}
					}
				}
			} else {
				if (slotId < 0) {
					return null;
				}

				Slot slot7 = (Slot) this.inventorySlots.get(slotId);

				if (slot7 != null) {
					ItemStack itemstack9 = slot7.getStack();
					ItemStack itemstack12 = inventoryplayer.getItemStack();

					if (itemstack9 != null) {
						itemstack = itemstack9.copy();
					}

					if (itemstack9 == null) {
						if (itemstack12 != null && slot7.isItemValid(itemstack12)) {
							int l2 = dragType == 0 ? itemstack12.stackSize : 1;

							if (l2 > slot7.getItemStackLimit(itemstack12)) {
								l2 = slot7.getItemStackLimit(itemstack12);
							}

							slot7.putStack(itemstack12.splitStack(l2));

							if (itemstack12.stackSize == 0) {
								inventoryplayer.setItemStack((ItemStack) null);
							}
						}
					} else if (slot7.canTakeStack(player)) {
						if (itemstack12 == null) {
							if (itemstack9.stackSize > 0) {
								int k2 = dragType == 0 ? itemstack9.stackSize : (itemstack9.stackSize + 1) / 2;
								inventoryplayer.setItemStack(slot7.decrStackSize(k2));

								if (itemstack9.stackSize <= 0) {
									slot7.putStack((ItemStack) null);
								}

								slot7.onPickupFromSlot(player, inventoryplayer.getItemStack());
							} else {
								slot7.putStack((ItemStack) null);
								inventoryplayer.setItemStack((ItemStack) null);
							}
						} else if (slot7.isItemValid(itemstack12)) {
							if (itemstack9.getItem() == itemstack12.getItem() && itemstack9.getMetadata() == itemstack12.getMetadata() && areEqualForMerge(itemstack9, itemstack12, slot7)) {  // XXX replaced ItemStack.areItemStackTagsEqual with areEqualForMerge for slot based handling
								int j2 = dragType == 0 ? itemstack12.stackSize : 1;

								if (j2 > slot7.getItemStackLimit(itemstack12) - itemstack9.stackSize) {
									j2 = slot7.getItemStackLimit(itemstack12) - itemstack9.stackSize;
								}

								if (j2 > itemstack12.getMaxStackSize() - itemstack9.stackSize) {
									j2 = itemstack12.getMaxStackSize() - itemstack9.stackSize;
								}

								itemstack12.splitStack(j2);

								if (itemstack12.stackSize == 0) {
									inventoryplayer.setItemStack((ItemStack) null);
								}

								itemstack9.stackSize += j2;

								slot7.putStack(itemstack9); // XXX added reinserting of the modified itemStack (Fix ItemIdentifierInventory's disappearing items)

							} else if (itemstack12.stackSize <= slot7.getItemStackLimit(itemstack12)) {
								handleSwitch(slot7, itemstack9, itemstack12, player); // XXX added Slot switching handle method
								slot7.putStack(itemstack12);
								inventoryplayer.setItemStack(itemstack9);
							}
						} else if (itemstack9.getItem() == itemstack12.getItem() && itemstack12.getMaxStackSize() > 1 && (!itemstack9.getHasSubtypes() || itemstack9.getMetadata() == itemstack12.getMetadata()) && areEqualForMerge(itemstack9, itemstack12, slot7)) { // XXX replaced ItemStack.areItemStackTagsEqual with areEqualForMerge for slot based handling
							int i2 = itemstack9.stackSize;

							if (i2 > 0 && i2 + itemstack12.stackSize <= itemstack12.getMaxStackSize()) {
								itemstack12.stackSize += i2;
								itemstack9 = slot7.decrStackSize(i2);

								if (itemstack9.stackSize == 0) {
									slot7.putStack((ItemStack) null);
								}

								slot7.onPickupFromSlot(player, inventoryplayer.getItemStack());
							}
						}
					}

					slot7.onSlotChanged();
				}
			}
		} else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
			Slot slot5 = (Slot) this.inventorySlots.get(slotId);
			ItemStack itemstack7 = inventoryplayer.getStackInSlot(dragType);

			if (itemstack7 != null && itemstack7.stackSize <= 0) {
				itemstack7 = null;
				inventoryplayer.setInventorySlotContents(dragType, (ItemStack) null);
			}

			ItemStack itemstack10 = slot5.getStack();

			if (itemstack7 != null || itemstack10 != null) {
				if (itemstack7 == null) {
					if (slot5.canTakeStack(player)) {
						inventoryplayer.setInventorySlotContents(dragType, itemstack10);
						slot5.putStack((ItemStack) null);
						slot5.onPickupFromSlot(player, itemstack10);
					}
				} else if (itemstack10 == null) {
					if (slot5.isItemValid(itemstack7)) {
						int k1 = slot5.getItemStackLimit(itemstack7);

						if (itemstack7.stackSize > k1) {
							slot5.putStack(itemstack7.splitStack(k1));
						} else {
							slot5.putStack(itemstack7);
							inventoryplayer.setInventorySlotContents(dragType, (ItemStack) null);
						}
					}
				} else if (slot5.canTakeStack(player) && slot5.isItemValid(itemstack7)) {
					int l1 = slot5.getItemStackLimit(itemstack7);

					if (itemstack7.stackSize > l1) {
						slot5.putStack(itemstack7.splitStack(l1));
						slot5.onPickupFromSlot(player, itemstack10);

						if (!inventoryplayer.addItemStackToInventory(itemstack10)) {
							player.dropItem(itemstack10, true);
						}
					} else {
						slot5.putStack(itemstack7);
						inventoryplayer.setInventorySlotContents(dragType, itemstack10);
						slot5.onPickupFromSlot(player, itemstack10);
					}
				}
			}
		} else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && slotId >= 0) {
			Slot slot4 = (Slot) this.inventorySlots.get(slotId);

			if (slot4 != null && slot4.getHasStack()) {
				if (slot4.getStack().stackSize > 0) {
					ItemStack itemstack6 = slot4.getStack().copy();
					itemstack6.stackSize = itemstack6.getMaxStackSize();
					inventoryplayer.setItemStack(itemstack6);
				} else {
					slot4.putStack((ItemStack) null);
				}
			}
		} else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack() == null && slotId >= 0) {
			Slot slot3 = (Slot) this.inventorySlots.get(slotId);

			if (slot3 != null && slot3.getHasStack() && slot3.canTakeStack(player)) {
				ItemStack itemstack5 = slot3.decrStackSize(dragType == 0 ? 1 : slot3.getStack().stackSize);
				slot3.onPickupFromSlot(player, itemstack5);
				player.dropItem(itemstack5, true);
			}
		} else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
			Slot slot2 = (Slot) this.inventorySlots.get(slotId);
			ItemStack itemstack4 = inventoryplayer.getItemStack();

			if (itemstack4 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player))) {
				int i1 = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
				int j1 = dragType == 0 ? 1 : -1;

				for (int i3 = 0; i3 < 2; ++i3) {
					for (int j3 = i1; j3 >= 0 && j3 < this.inventorySlots.size() && itemstack4.stackSize < itemstack4.getMaxStackSize(); j3 += j1) {
						Slot slot8 = (Slot) this.inventorySlots.get(j3);

						if (slot8.getHasStack() && canAddItemToSlot(slot8, itemstack4, true) && slot8.canTakeStack(player) && this
								.canMergeSlot(itemstack4, slot8) && (i3 != 0 || slot8.getStack().stackSize != slot8.getStack().getMaxStackSize())) {
							int l = Math.min(itemstack4.getMaxStackSize() - itemstack4.stackSize, slot8.getStack().stackSize);
							ItemStack itemstack2 = slot8.decrStackSize(l);
							itemstack4.stackSize += l;

							if (itemstack2.stackSize <= 0) {
								slot8.putStack((ItemStack) null);
							}

							slot8.onPickupFromSlot(player, itemstack2);
						}
					}
				}
			}

			this.detectAndSendChanges();
		}

		return itemstack;
	}

	private void handleSwitch(Slot slot2, ItemStack out, ItemStack in, EntityPlayer player) {
		if(slot2 instanceof ModuleSlot) {
			ChassiModule logisticsModule = (ChassiModule) ((ModuleSlot) slot2).get_pipe().getLogisticsModule();
			int moduleIndex = ((ModuleSlot) slot2).get_moduleIndex();
			if (out.getItem() instanceof ItemModule) {
				ItemModuleInformationManager.saveInfotmation(out, logisticsModule.getSubModule(moduleIndex));
				if (logisticsModule.hasModule(moduleIndex)) {
					logisticsModule.removeModule(moduleIndex);
				}
			}
		}
	}

	private boolean areEqualForMerge(ItemStack itemstack3, ItemStack itemstack4, Slot slot) {
		if(slot instanceof ModuleSlot) {
			return false;
		}
		return ItemStack.areItemStackTagsEqual(itemstack3, itemstack4);
	}

	/**
	 * Clone/clear itemstacks for items
	 */
	@Override
	public ItemStack slotClick(int slotId, int mouseButton, ClickType shiftMode, EntityPlayer entityplayer) {
		lastClicked = System.currentTimeMillis();
		if (slotId < 0) {
			return superSlotClick(slotId, mouseButton, shiftMode, entityplayer);
		}
		Slot slot = (Slot) inventorySlots.get(slotId);
		//debug dump
		if (LPConstants.DEBUG && slot != null) {
			ItemStack stack = slot.getStack();
			if (stack != null) {
				ItemIdentifier.get(stack).debugDumpData(entityplayer.worldObj.isRemote);
			}
		}
		if (slot == null || (!(slot instanceof DummySlot) && !(slot instanceof UnmodifiableSlot) && !(slot instanceof FluidSlot) && !(slot instanceof ColorSlot) && !(slot instanceof HandelableSlot))) {
			ItemStack stack1 = superSlotClick(slotId, mouseButton, shiftMode, entityplayer);
			ItemStack stack2 = slot.getStack();
			if (stack2 != null && stack2.getItem() == LogisticsPipes.ModuleItem) {
				if (entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.worldObj)) {
					((EntityPlayerMP) entityplayer).sendSlotContents(this, slotId, stack2);
				}
			}
			return stack1;
		}

		InventoryPlayer inventoryplayer = entityplayer.inventory;

		ItemStack currentlyEquippedStack = inventoryplayer.getItemStack();

		// we get a leftclick *and* a doubleclick message if there's a doubleclick with no item on the pointer, filter it out
		if (currentlyEquippedStack == null && shiftMode == ClickType.PICKUP_ALL) {
			return currentlyEquippedStack;
		}

		if (slot instanceof HandelableSlot) {
			overrideMCAntiSend = true;
			if (currentlyEquippedStack == null) {
				inventoryplayer.setItemStack(((HandelableSlot) slot).getProvidedStack());
				return null;
			}
			return currentlyEquippedStack;
		}

		if (slot instanceof UnmodifiableSlot) {
			return currentlyEquippedStack;
		}

		handleDummyClick(slot, slotId, currentlyEquippedStack, mouseButton, shiftMode, entityplayer);
		return currentlyEquippedStack;
	}

	public void handleDummyClick(Slot slot, int slotId, ItemStack currentlyEquippedStack, int mouseButton, ClickType shiftMode, EntityPlayer entityplayer) {
		if (slot instanceof FluidSlot) {
			if (currentlyEquippedStack != null) {
				FluidStack liquidId = FluidContainerRegistry.getFluidForFilledItem(currentlyEquippedStack);
				if (liquidId != null) {
					FluidIdentifier ident = FluidIdentifier.get(liquidId);
					if (mouseButton == 0) {
						if (ident == null) {
							slot.putStack(null);
						} else {
							slot.putStack(ident.getItemIdentifier().unsafeMakeNormalStack(1));
						}
					} else {
						slot.putStack(null);
					}
					return;
				}
				FluidIdentifier ident = FluidIdentifier.get(currentlyEquippedStack);
				if (ident != null) {
					if (mouseButton == 0) {
						slot.putStack(ident.getItemIdentifier().unsafeMakeNormalStack(1));
					} else {
						slot.putStack(null);
					}
					return;
				}
			}
			FluidIdentifier ident = null;
			if (slot.getStack() != null) {
				ident = FluidIdentifier.get(ItemIdentifier.get(slot.getStack()));
			}
			if (ident == null) {
				if (MainProxy.isClient(entityplayer.getEntityWorld())) {
					MainProxy.proxy.openFluidSelectGui(slotId);
				}
			}
			slot.putStack(null);
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
			if (entityplayer instanceof EntityPlayerMP && MainProxy.isServer(entityplayer.worldObj)) {
				((EntityPlayerMP) entityplayer).sendSlotContents(this, slotId, slot.getStack());
			}
			return;
		}

		if (slot instanceof DummySlot) {
			((DummySlot) slot).setRedirectCall(true);
		}

		if (currentlyEquippedStack == null) {
			if (slot.getStack() != null && mouseButton == 1) {
				ItemStack tstack = slot.getStack();
				if (shiftMode == ClickType.QUICK_MOVE) {
					tstack.stackSize = Math.min(slot.getSlotStackLimit(), tstack.stackSize * 2);
				} else {
					tstack.stackSize /= 2;
					if (tstack.stackSize <= 0) {
						tstack = null;
					}
				}
				slot.putStack(tstack);
			} else {
				slot.putStack(null);
			}
			if (slot instanceof DummySlot) {
				((DummySlot) slot).setRedirectCall(false);
			}
			return;
		}

		if (!slot.getHasStack()) {
			ItemStack tstack = currentlyEquippedStack.copy();
			if (mouseButton == 1) {
				tstack.stackSize = 1;
			}
			if (tstack.stackSize > slot.getSlotStackLimit()) {
				tstack.stackSize = slot.getSlotStackLimit();
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
				if (tstack.stackSize + counter <= slot.getSlotStackLimit()) {
					tstack.stackSize += counter;
				} else {
					tstack.stackSize = slot.getSlotStackLimit();
				}
				slot.putStack(tstack);
			} else if (mouseButton == 0) {
				tstack.stackSize -= counter;
				if (tstack.stackSize <= 0) {
					tstack = null;
				}
				slot.putStack(tstack);
			}
			if (slot instanceof DummySlot) {
				((DummySlot) slot).setRedirectCall(false);
			}
			return;
		}

		ItemStack tstack = currentlyEquippedStack.copy();
		if (tstack.stackSize > slot.getSlotStackLimit()) {
			tstack.stackSize = slot.getSlotStackLimit();
		}
		slot.putStack(tstack);
		if (slot instanceof DummySlot) {
			((DummySlot) slot).setRedirectCall(false);
		}
		return;
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		if (_controler != null) {
			for (IGuiOpenControler element : _controler) {
				element.guiClosedByPlayer(par1EntityPlayer);
			}
		}
		super.onContainerClosed(par1EntityPlayer);
	}

	@Override
	protected void retrySlotClick(int i, int j, boolean flag, EntityPlayer entityplayer) {
		Thread.dumpStack();
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
	public boolean canDragIntoSlot(Slot slot) {
		if (slot == null || slot instanceof UnmodifiableSlot || slot instanceof FluidSlot || slot instanceof ColorSlot || slot instanceof HandelableSlot) {
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
	public Slot getSlotFromInventory(IInventory par1IInventory, int par2) {
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
	public void putStackInSlot(int par1, ItemStack par2ItemStack) {
		if (inventorySlots.isEmpty()) {
			_playerInventory.setInventorySlotContents(par1, par2ItemStack);
			_playerInventory.markDirty();
			return;
		}
		super.putStackInSlot(par1, par2ItemStack);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void detectAndSendChanges() {
		for (int i = 0; i < inventorySlots.size(); ++i) {
			if(inventorySlots.get(i) instanceof IFuzzySlot) {
				IFuzzySlot fuzzySlot = (IFuzzySlot) inventorySlots.get(i);
				BitSet set = inventoryFuzzySlotsContent.get(i);
				if(set == null) {
					set = fuzzySlot.getFuzzyFlags().getBitSet();
					MainProxy.sendToPlayerList(PacketHandler.getPacket(FuzzySlotSettingsPacket.class).setSlotNumber(fuzzySlot.getSlotId()).setFlags(set), listeners.stream().filter(o -> o instanceof EntityPlayer).map(o -> (EntityPlayer)o));
					inventoryFuzzySlotsContent.set(i, set);
				} else {
					BitSet setB = fuzzySlot.getFuzzyFlags().getBitSet();
					if(!set.equals(setB)) {
						MainProxy.sendToPlayerList(PacketHandler.getPacket(FuzzySlotSettingsPacket.class).setSlotNumber(fuzzySlot.getSlotId()).setFlags(setB), listeners.stream().filter(o -> o instanceof EntityPlayer).map(o -> (EntityPlayer)o));
						inventoryFuzzySlotsContent.set(i, setB);
					}
				}
			}
			ItemStack itemstack = ((Slot) inventorySlots.get(i)).getStack();
			ItemStack itemstack1 = (ItemStack) inventoryItemStacks.get(i);

			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
				itemstack1 = itemstack == null ? null : itemstack.copy();
				inventoryItemStacks.set(i, itemstack1);

				for (IContainerListener crafter : listeners) {
					boolean revert = false;
					if (overrideMCAntiSend && crafter instanceof EntityPlayerMP && ((EntityPlayerMP) crafter).isChangingQuantityOnly) {
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

	@Override
	protected Slot addSlotToContainer(Slot p_75146_1_) {
		this.inventoryFuzzySlotsContent.add(null);
		return super.addSlotToContainer(p_75146_1_);
	}
}

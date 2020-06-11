package logisticspipes.modplugins.nei;

import java.awt.BorderLayout;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import codechicken.nei.guihook.IContainerTooltipHandler;
import org.lwjgl.input.Keyboard;

import logisticspipes.config.Configs;
import logisticspipes.utils.item.ItemIdentifier;

public class DebugHelper implements IContainerTooltipHandler {

	private static long lastTime = 0;

	@Override
	public void handleTooltip(GuiScreen gui, int mousex, int mousey, List<String> currenttip) {}

	@Override
	public void handleItemDisplayName(GuiScreen gui, @Nonnull ItemStack itemstack, List<String> currenttip) {
		if (Configs.TOOLTIP_INFO && !itemstack.isEmpty()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_H)) {
				if (DebugHelper.lastTime + 1000 < System.currentTimeMillis()) {
					DebugHelper.lastTime = System.currentTimeMillis();
					new Thread(() -> {
						while (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_H)) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						DefaultMutableTreeNode node = new DefaultMutableTreeNode(ItemIdentifier.get(itemstack).getFriendlyName());
						node.add(new DefaultMutableTreeNode("ItemId: " + Item.getIdFromItem(itemstack.getItem())));
						node.add(new DefaultMutableTreeNode("ItemDamage: " + itemstack.getItemDamage()));
						if (itemstack.hasTagCompound()) {
							DefaultMutableTreeNode tag = new DefaultMutableTreeNode("Tag:");
							try {
								addNBTToTree(itemstack.getTagCompound(), tag);
							} catch (Exception e) {
								tag.add(new DefaultMutableTreeNode(e));
							}
							node.add(tag);
						}
						JTree tree = new JTree(node);
						JScrollPane treeView = new JScrollPane(tree);
						JFrame frame = new JFrame("Item Info");
						frame.getContentPane().add(treeView, BorderLayout.CENTER);
						frame.setLocationRelativeTo(null);
						frame.pack();
						frame.setVisible(true);
					}).start();
				}
			}
		}
	}

	private void addNBTToTree(NBTBase nbt, DefaultMutableTreeNode node) throws SecurityException, IllegalArgumentException {
		if (nbt == null) {
			return;
		}
		if (nbt instanceof NBTTagByte) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagByte");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagByte) nbt).getByte()));
			node.add(type);
		} else if (nbt instanceof NBTTagByteArray) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagByteArray");
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");
			int i = 0;
			for (byte byt : ((NBTTagByteArray) nbt).getByteArray()) {
				content.add(new DefaultMutableTreeNode("[" + i + "]: " + byt));
				i++;
			}
			node.add(content);
			node.add(type);
		} else if (nbt instanceof NBTTagDouble) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagDouble");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagDouble) nbt).getDouble()));
			node.add(type);
		} else if (nbt instanceof NBTTagFloat) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagFloat");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagFloat) nbt).getFloat()));
			node.add(type);
		} else if (nbt instanceof NBTTagInt) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagInt");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagInt) nbt).getInt()));
			node.add(type);
		} else if (nbt instanceof NBTTagIntArray) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagIntArray");
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");
			int i = 0;
			for (int byt : ((NBTTagIntArray) nbt).getIntArray()) {
				content.add(new DefaultMutableTreeNode("[" + i + "]: " + byt));
				i++;
			}
			type.add(content);
			node.add(type);
		} else if (nbt instanceof NBTTagList) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagList");
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");

			int i = 0;

			for (NBTBase object : (NBTTagList) nbt) {
				DefaultMutableTreeNode nbtNode = new DefaultMutableTreeNode("[" + i + "]");
				addNBTToTree(object, nbtNode);
				content.add(nbtNode);
				i++;
			}
			type.add(content);
			node.add(type);
		} else if (nbt instanceof NBTTagCompound) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagCompound");
			DefaultMutableTreeNode content = new DefaultMutableTreeNode("Data");

			for (String key : ((NBTTagCompound) nbt).getKeySet()) {
				NBTBase value = ((NBTTagCompound) nbt).getTag(key);
				DefaultMutableTreeNode nbtNode = new DefaultMutableTreeNode(key);
				addNBTToTree(value, nbtNode);
				content.add(nbtNode);
			}
			type.add(content);
			node.add(type);
		} else if (nbt instanceof NBTTagLong) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagLong");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagLong) nbt).getLong()));
			node.add(type);
		} else if (nbt instanceof NBTTagShort) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagShort");
			type.add(new DefaultMutableTreeNode("Data: " + ((NBTTagShort) nbt).getShort()));
			node.add(type);
		} else if (nbt instanceof NBTTagString) {
			DefaultMutableTreeNode type = new DefaultMutableTreeNode("NBTTagString");
			type.add(new DefaultMutableTreeNode("Data: '" + ((NBTTagString) nbt).getString() + "'"));
			node.add(type);
		} else {
			throw new UnsupportedOperationException("Unsupported NBTBase of type:" + nbt.getClass().getName());
		}
	}
}

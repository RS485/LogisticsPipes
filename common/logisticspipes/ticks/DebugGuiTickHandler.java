package logisticspipes.ticks;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.debuggui.DebugExpandPart;
import logisticspipes.network.packets.debuggui.DebugInfoUpdate;
import logisticspipes.network.packets.debuggui.DebugPanelOpen;
import logisticspipes.network.packets.debuggui.DebugSetVarContent;
import logisticspipes.network.packets.debuggui.DebugTargetResponse;
import logisticspipes.network.packets.debuggui.DebugTargetResponse.TargetMode;
import logisticspipes.network.packets.debuggui.DebugTypePacket;
import logisticspipes.proxy.MainProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DebugGuiTickHandler implements ITickHandler, Serializable, TreeExpansionListener, MouseListener {
	private static final long serialVersionUID = 5889863317496153769L;
	
	transient private JFrame localGui;
	transient private DefaultMutableTreeNode baseNode;
	transient private JTree tree;
	transient private JScrollPane treeView;
	transient private VarType clientType;
	
	transient private Map<Player, ServerGuiSetting> serverInfo = new HashMap<Player, ServerGuiSetting>();
	
	@Data
	public class ServerGuiSetting implements Serializable {
		private static final long serialVersionUID = 8676326523187792057L;
		VarType var;
		transient public Object base;
		//transient public HashMap<String, Object> varMap = new HashMap<String, Object>();
	}
	@Data
	public abstract class VarType implements Serializable {
		private static final long serialVersionUID = -8013762524305750292L;
		String name;
		Integer i;
		transient ExtendedDefaultMutableTreeNode node;
	}
	@Data
	@EqualsAndHashCode(callSuper=true)
	public class BasicVarType extends VarType {
		private static final long serialVersionUID = 4416098847612633006L;
		String watched;
	}
	@Data
	@EqualsAndHashCode(callSuper=true)
	public class NullVarType extends VarType {
		private static final long serialVersionUID = -3673576543767748173L;
	}
	@Data
	@ToString(exclude="parent")
	@EqualsAndHashCode(callSuper=true, exclude="parent")
	public abstract class ParentVarType extends VarType {
		private static final long serialVersionUID = -2225627128169181250L;
		ParentVarType parent;
	}
	@Data
	@EqualsAndHashCode(callSuper=true)
	public class ExtendedVarType extends ParentVarType {
		private static final long serialVersionUID = -5243734594523844526L;
		Map<Integer, FieldPart> objectType = new HashMap<Integer, FieldPart>();
		Map<Integer, MethodPart> methodType = new HashMap<Integer, MethodPart>();
		transient WeakReference<Object> watched;
		boolean extended;
		String typeName;
	}
	@Data
	@EqualsAndHashCode(callSuper=true)
	public class ArrayVarType extends ParentVarType {
		private static final long serialVersionUID = -6335674162049738144L;
		Map<Integer, VarType> objectType = new HashMap<Integer, VarType>();
		transient WeakReference<Object> watched;
	}
	@Data
	public class FieldPart implements Serializable {
		private static final long serialVersionUID = 3891140976403054537L;
		VarType type;
		transient Field field;
		String name;
	}
	@Data
	public class MethodPart implements Serializable {
		private static final long serialVersionUID = -5704715096482038636L;
		String[] param;
		transient Method method;
		String name;
		Integer i;
	}
	
	public class ExtendedDefaultMutableTreeNode extends DefaultMutableTreeNode {
		public ExtendedDefaultMutableTreeNode(String string, VarType type) {
			super(string);
			if(type instanceof ParentVarType) {
				this.type = (ParentVarType) type;				
			} else {
				this.type = null;
			}
			this.base = type;
		}
		private static final long serialVersionUID = 5318719768321332693L;
		public transient ParentVarType type;
		public transient VarType base;
		public boolean loaded;
	}
	
	public class MethodDefaultMutableTreeNode extends DefaultMutableTreeNode {
		public MethodDefaultMutableTreeNode(String string, MethodPart type) {
			super(string);
			this.type = type;
		}
		private static final long serialVersionUID = 5318719768321332693L;
		public transient final MethodPart type;
	}
	
	public void startWatchingOf(Object object, Player player) {
		if(object == null) {
			return;
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugPanelOpen.class).setName(object.getClass().getSimpleName()), player);
		ServerGuiSetting setting = new ServerGuiSetting();
		setting.var = resolveType(object, null, object.getClass().getSimpleName(), true, null);
		setting.base = object;
		serverInfo.put(player, setting);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugTypePacket.class).setToTransmit(setting.var), player);
	}
	
	private boolean isPrimitive(Class<?> clazz) {
		return clazz == Integer.class || clazz == Boolean.class || clazz == Double.class || clazz == Float.class || clazz == Long.class || clazz == UUID.class || clazz == Byte.class || clazz == String.class || clazz == ForgeDirection.class || clazz == WorldServer.class;
	}
	
	private VarType resolveType(Object toInstect, VarType prev, String name, boolean extended, ParentVarType parent) {
		if(toInstect == null) {
			NullVarType type = new NullVarType();
			type.name = name;
			return type;
		}
		Class<?> clazz = toInstect.getClass();
		if(clazz.isPrimitive() || isPrimitive(clazz)) {
			BasicVarType type = new BasicVarType();
			type.name = name;
			type.watched = toInstect.toString();
			return type;
		}
		if(clazz.isArray()) {
			ArrayVarType type = new ArrayVarType();
			type.i = 0;
			type.watched = new WeakReference<Object>(toInstect);
			type.name = name;
			type.parent = parent;
			if(prev instanceof ExtendedVarType) {
				type.i = ((ExtendedVarType)prev).i;
			}
			Object[] array = getArray(type.watched.get());
			for(int i=0;i<array.length;i++) {
				Object o = array[i];
				VarType tmp = null;
				if(prev instanceof ArrayVarType) {
					tmp = ((ArrayVarType)prev).objectType.get(i);
				}
				VarType subType = resolveType(o, tmp, i + ": ", false, type);
				subType.i = i;
				type.objectType.put(i, subType);
			}
			return type;
		}
		ExtendedVarType type = new ExtendedVarType();
		type.i = 0;
		type.watched =  new WeakReference<Object>(toInstect);
		type.name = name;
		type.extended = extended;
		type.typeName = clazz.getSimpleName();
		type.parent = parent;
		if(prev instanceof ExtendedVarType) {
			type.extended = ((ExtendedVarType)prev).extended;
			type.i = ((ExtendedVarType)prev).i;
		}	
		if(type.extended) {
			int field = 0;
			int method = 0;
			while(!clazz.equals(Object.class)) {
				Field[] fields = clazz.getDeclaredFields();
				for(int i=0;i<fields.length;i++) {
					Field f = fields[i];
					try {
						f.setAccessible(true);
						Object content = f.get(toInstect);
						VarType tmp = null;
						if(prev instanceof ExtendedVarType) {
							FieldPart part = ((ExtendedVarType)prev).objectType.get(i);
							if(part != null) {
								tmp = part.type;
							}
						}
						VarType subType = resolveType(content, tmp, f.getName(), false, type);
						FieldPart fieldPart = new FieldPart();
						fieldPart.field = f;
						fieldPart.name = f.getName();
						fieldPart.type = subType;
						subType.i = field;
						type.objectType.put(field++, fieldPart);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				Method[] methods = clazz.getDeclaredMethods();
				for(int i=0;i<methods.length;i++) {
					Method m = methods[i];
					try {
						m.setAccessible(true);
						MethodPart methodPart = new MethodPart();
						methodPart.method = m;
						methodPart.name = m.getName();
						methodPart.i = method;
						List<String> params = new ArrayList<String>();
						for(Class<?> par:m.getParameterTypes()) {
							params.add(par.getSimpleName());
						}
						methodPart.param = params.toArray(new String[]{});
						type.methodType.put(method++, methodPart);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				clazz = clazz.getSuperclass();
			}
		}
		return type;
	}
	
	private Object[] getArray(Object val) {
		if(val instanceof Object[])
			return (Object[]) val;
		int arrlength = Array.getLength(val);
		Object[] outputArray = new Object[arrlength];
		for(int i = 0; i < arrlength; ++i) {
			outputArray[i] = Array.get(val, i);
		}
		return outputArray;
	}

	private void setArray(Object val, Integer pos, Object casted) {
		if(val instanceof Object[]) {
			((Object[]) val)[pos] = casted;
		} else {
			Array.set(val, pos, casted);
		}
	}
	
	public void createNewDebugGui(String ObjectClass) {
		if(localGui != null) {
			localGui.setVisible(false);
		}
		baseNode = new DefaultMutableTreeNode(ObjectClass);
		tree = new JTree(baseNode);
		tree.addTreeExpansionListener(this);
		tree.addMouseListener(this);
		treeView = new JScrollPane(tree);
		localGui = new JFrame("Object Information");
		localGui.getContentPane().add(treeView, BorderLayout.CENTER);
		localGui.setLocationRelativeTo(null);
		localGui.pack();
		localGui.setVisible(true);
	}
	
	public void handleServerGuiSetting(VarType setting, Integer[] pos) {
		if(pos.length == 0 || pos.length == 1) {
			clientType = setting;
			for(int i = 0; i < baseNode.getChildCount(); i++) {
				baseNode.remove(i);
			}
			addParts(baseNode, setting);
			((DefaultTreeModel)tree.getModel()).reload(baseNode);
		} else {
			ExtendedDefaultMutableTreeNode node = getNodeForPathAndVarType(clientType, pos, setting);
			if(node == null) return;
			while(node.getChildCount() > 0) {
				node.remove(0);
			}
			DefaultMutableTreeNode superNode = new DefaultMutableTreeNode("Super");
			addParts(superNode, setting);
			while(superNode.getChildAt(0).getChildCount() > 0) {
				node.add((MutableTreeNode) superNode.getChildAt(0).getChildAt(0));
			}
			node.setUserObject(((DefaultMutableTreeNode)superNode.getChildAt(0)).getUserObject());
			if(node instanceof ExtendedDefaultMutableTreeNode) {
				node.loaded = true;
				if(superNode.getChildAt(0) instanceof ExtendedDefaultMutableTreeNode) {
					node.type = ((ExtendedDefaultMutableTreeNode)superNode.getChildAt(0)).type;
				}
			}
			((DefaultTreeModel)tree.getModel()).reload(node);
		}
	}
	
	private ExtendedDefaultMutableTreeNode getNodeForPathAndVarType(VarType var, Integer[] pos, VarType toRefresh) {
		VarType varPos = var;
outer:
		for(int i=1;i<pos.length;i++) {
			if(varPos instanceof ExtendedVarType) {
				ExtendedVarType eType = (ExtendedVarType) varPos;
				for(int j=0;j<eType.objectType.size();j++) {
					if(eType.objectType.get(j).type.i.equals(pos[i])) {
						if(i + 1 == pos.length && toRefresh != null) {
							varPos = eType.objectType.get(j).type;
							toRefresh.node = varPos.node;
							eType.objectType.get(j).type = toRefresh;
							
						} else {
							varPos = eType.objectType.get(j).type;	
						}
						continue outer;
					}
				}
				return null; //Nothing found
			} else {
				ArrayVarType aType = (ArrayVarType) varPos;
				for(int j=0;j<aType.objectType.size();j++) {
					if(aType.objectType.get(j).i.equals(pos[i])) {
						if(i + 1 == pos.length && toRefresh != null) {
							varPos = aType.objectType.get(j);
							toRefresh.node = varPos.node;
							aType.objectType.put(j, toRefresh);
							
						} else {
							varPos = aType.objectType.get(j);
						}
						continue outer;
					}
				}
				return null; //Nothing found
			}
		}
		return varPos.node;
	}

	private void addParts(DefaultMutableTreeNode node, VarType type) {
		try {
			if(type instanceof BasicVarType) {
				ExtendedDefaultMutableTreeNode var = new ExtendedDefaultMutableTreeNode(type.name + ": " + ((BasicVarType)type).watched, type);
				type.node = var;
				node.add(var);
			} else if(type instanceof NullVarType) {
				ExtendedDefaultMutableTreeNode var = new ExtendedDefaultMutableTreeNode(type.name + ": null", type);
				type.node = var;
				node.add(var);
			} else if(type instanceof ExtendedVarType) {
				ExtendedVarType eType = (ExtendedVarType) type;
				if(eType.extended) {
					ExtendedDefaultMutableTreeNode extendableNode = new ExtendedDefaultMutableTreeNode(eType.name + ": (" + eType.typeName + ")", eType);
					ExtendedDefaultMutableTreeNode fieldNode = new ExtendedDefaultMutableTreeNode("Fields:", null);
					ExtendedDefaultMutableTreeNode methodsNode = new ExtendedDefaultMutableTreeNode("Methods:", null);
					for(int i=0;i<eType.objectType.size();i++) {
						addParts(fieldNode, eType.objectType.get(i).type);
					}
					for(Integer i:eType.methodType.keySet()) {
						MethodPart mPart = eType.methodType.get(i);
						StringBuilder builder = new StringBuilder();
						for(String param:mPart.param) {
							if(builder.length() != 0) {
								builder.append(", ");
							}
							builder.append(param);
						}
						MethodDefaultMutableTreeNode methodNode = new MethodDefaultMutableTreeNode(mPart.name + ": (" + builder + ")", mPart);
						methodsNode.add(methodNode);
					}
					extendableNode.loaded = true;
					extendableNode.add(fieldNode);
					extendableNode.add(methodsNode);
					type.node = extendableNode;
					node.add(extendableNode);
				} else {
					ExtendedDefaultMutableTreeNode extendableNode = new ExtendedDefaultMutableTreeNode(eType.name + ": (" + eType.typeName + ")", eType);
					extendableNode.add(new DefaultMutableTreeNode("Loading..."));
					type.node = extendableNode;
					node.add(extendableNode);
				}
			} else if(type instanceof ArrayVarType) {
				ArrayVarType aType = (ArrayVarType) type;
				ExtendedDefaultMutableTreeNode extendableNode = new ExtendedDefaultMutableTreeNode(aType.name + ": ", aType);
				for(int i=0;i<aType.objectType.size();i++) {
					addParts(extendableNode, aType.objectType.get(i));
				}
				extendableNode.loaded = true;
				type.node = extendableNode;
				node.add(extendableNode);
			} else{
				throw new UnsupportedOperationException(type == null ? "null" : type.getClass().getSimpleName());
			}
		} catch(java.lang.IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void expandGuiAt(Integer[] tree, Player player) {
		ServerGuiSetting info = serverInfo.get(player);
		VarType pos = info.var;
		VarType prevPos = null;
		for(int i=1;i<tree.length;i++) {
			prevPos = pos;
			if(pos instanceof ExtendedVarType) {
				pos = ((ExtendedVarType)pos).objectType.get(tree[i]).type;
			} else if(pos instanceof ArrayVarType) {
				pos = ((ArrayVarType)pos).objectType.get(tree[i]);
			} else {
				new Exception("List unsorted for some reason. Accessing " + pos.name + ". Closing gui. (" + Arrays.toString(tree) + ")").printStackTrace();
				return;
			}
		}
		if(pos instanceof ExtendedVarType) {
			((ExtendedVarType)pos).extended = true;
			pos = resolveType(((ExtendedVarType)pos).watched.get(), pos, ((ExtendedVarType)pos).name, true, (ParentVarType) prevPos);
			if(prevPos != null) {
				if(prevPos instanceof ExtendedVarType) {
					((ExtendedVarType)prevPos).objectType.get(tree[tree.length - 1]).type = pos;
				} else if(prevPos instanceof ArrayVarType) {
					((ArrayVarType)prevPos).objectType.put(tree[tree.length - 1], pos);
				}
			} else {
				info.var = pos;
			}
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugTypePacket.class).setToTransmit(pos).setPos(tree), player);
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if(!type.contains(TickType.SERVER)) return;
		for(Player player:serverInfo.keySet()) {
			try {
				ServerGuiSetting setting = serverInfo.get(player);
				LinkedList<Integer> l = new LinkedList<Integer>();
				l.add(0);
				setting.var = handleUpdate(setting.var, player, l, setting.base, null);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private VarType handleUpdate(VarType type, Player player, LinkedList<Integer> path, Object newObject, ParentVarType parent) {
		boolean isModified = false;
		if(type instanceof BasicVarType) {
			BasicVarType bType = (BasicVarType) type;
			if(bType.watched != null) {
				if(!bType.watched.equals(newObject.toString())) {
					isModified = true;
				}
			} else if(newObject == null) {
				isModified = true;
			}
			if(isModified) {
				type = resolveType(newObject, type, type.name, true, parent);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugInfoUpdate.class).setPath(path.toArray(new Integer[]{})).setInformation(type), player);
			}
			return bType;
		} else if(type instanceof NullVarType) {
			if(newObject != null) {
				type = resolveType(newObject, type, type.name, true, parent);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugTypePacket.class).setToTransmit(type).setPos(path.toArray(new Integer[]{})), player);
			}
			return type;
		} else if(type instanceof ArrayVarType) {
			ArrayVarType aType = (ArrayVarType) type;
			if(aType.watched.get() != null) {
				if(!aType.watched.get().equals(newObject)) {
					isModified = true;
				}
			} else if(newObject != null) {
				isModified = true;
			}
			if(!isModified) {
				for(int i=0;i<aType.objectType.size();i++) {
					path.addLast(i);
					aType.objectType.put(i, handleUpdate(aType.objectType.get(i), player, path, getArray(aType.watched.get())[i], aType));
					path.removeLast();
				}
			} else {
				type = resolveType(newObject, type, type.name, true, parent);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugTypePacket.class).setToTransmit(type).setPos(path.toArray(new Integer[]{})), player);
			}
			return type;
		} else if(type instanceof ExtendedVarType) {
			ExtendedVarType eType = (ExtendedVarType) type;
			if(eType.watched != null) {
				if(eType.watched.get() != null) {
					if(!eType.watched.get().equals(newObject)) {
						isModified = true;
					}
				}
			}
			if(newObject != null && !isModified) {
				for(int i=0;i<eType.objectType.size();i++) {
					FieldPart part = eType.objectType.get(i);
					Object content = null;
					try {
						content = part.field.get(newObject);
					} catch(IllegalArgumentException e) {
						e.printStackTrace();
						isModified = true;
					} catch(IllegalAccessException e) {
						e.printStackTrace();
						isModified = true;
					}
					path.addLast(i);
					part.type = handleUpdate(part.type, player, path, content, eType);
					path.removeLast();
				}
			} else {
				type = resolveType(newObject, type, type.name, true, parent);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugTypePacket.class).setToTransmit(type).setPos(path.toArray(new Integer[]{})), player);
			}
			return type;
		} else {
			System.out.println("Unknown Type");
			return null;
		}
	}
	
	public void handleContentUpdatePacket(Integer[] pos, VarType setting) {
		if(pos.length == 0 || pos.length == 1) {
			for(int i = 0; i < baseNode.getChildCount(); i++) {
				baseNode.remove(i);
			}
			addParts(baseNode, setting);
			((DefaultTreeModel)tree.getModel()).reload(baseNode);
		} else {
			ExtendedDefaultMutableTreeNode node = getNodeForPathAndVarType(clientType,pos,null);
			if(node == null) return;
			if(setting instanceof BasicVarType) {
				node.setUserObject(setting.name + ": " + ((BasicVarType)setting).watched);
			} else if(setting instanceof NullVarType) {
				node.setUserObject(setting.name + ": null");
			} else {
				System.out.println("Can't directly Update this type.");
			}
			((DefaultTreeModel)tree.getModel()).reload(node);
		}
	}

	public void handleVarChangePacket(Integer[] path, String content, Player player) {
		ServerGuiSetting info = serverInfo.get(player);
		VarType pos = info.var;
		for(int i=1;i<path.length - 1;i++) {
			if(pos instanceof ExtendedVarType) {
				pos = ((ExtendedVarType)pos).objectType.get(path[i]).type;
			} else if(pos instanceof ArrayVarType) {
				pos = ((ArrayVarType)pos).objectType.get(path[i]);
			} else {
				new Exception("List unsorted for some reason. Accessing " + pos.name + ". Closing gui. (" + Arrays.toString(path) + ")").printStackTrace();
				return;
			}
		}
		if(pos instanceof ExtendedVarType) {
			FieldPart f = ((ExtendedVarType)pos).objectType.get(path[path.length - 1]);
			try {
				f.field.set(((ExtendedVarType) pos).watched.get(), getCasted(f.field.getType(), content));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(pos instanceof ArrayVarType) {
			try {
				setArray(((ArrayVarType)pos).watched.get(), path[path.length - 1], getCasted(((ArrayVarType)pos).watched.get().getClass().getComponentType(), content));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Object getCasted(Class<?> clazz, String content) {
		if(clazz == int.class || clazz == Integer.class) {
			return Integer.valueOf(content);
		} else if(clazz == byte.class || clazz == Byte.class) {
			return Byte.valueOf(content);
		} else if(clazz == short.class || clazz == Short.class) {
			return Short.valueOf(content);
		} else if(clazz == long.class || clazz == Long.class) {
			return Long.valueOf(content);
		} else if(clazz == float.class || clazz == Float.class) {
			return Float.valueOf(content);
		} else if(clazz == double.class || clazz == Double.class) {
			return Double.valueOf(content);
		} else if(clazz == char.class || clazz == Character.class) {
			return content.charAt(0);
		} else if(clazz == boolean.class || clazz == Boolean.class) {
			return Boolean.valueOf(content);
		} else if(clazz == UUID.class) {
			return UUID.fromString(content);
		} else {
			System.out.println("What type is that: " + clazz.getSimpleName() + "?");
		}
		return content;
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER, TickType.CLIENT);
	}
	
	@Override
	public String getLabel() {
		return "Logistics Debug Panel TickHandler";
	}
	

	@SideOnly(Side.CLIENT)
	public void handleTargetRequest() {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if(box == null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(TargetMode.None));
		} else if(box.typeOfHit == EnumMovingObjectType.TILE) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(TargetMode.Block).setAdditions(new Object[]{box.blockX,box.blockY,box.blockZ}));	
		} else if(box.typeOfHit == EnumMovingObjectType.ENTITY) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugTargetResponse.class).setMode(TargetMode.Entity).setAdditions(new Object[]{box.entityHit.entityId}));	
		}
	}

	public void targetResponse(TargetMode mode, EntityPlayer player, Object[] additions) {
		if(mode == TargetMode.None) {
			player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No Target Found"));
		} else if(mode == TargetMode.Block) {
			int x = (Integer) additions[0];
			int y = (Integer) additions[1];
			int z = (Integer) additions[2];
			player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Checking Block at: x:" + x + " y:" + y + " z:" + z));
			int id = player.worldObj.getBlockId(x, y, z);
			player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Found Block with Id: " + id));
			TileEntity tile = player.worldObj.getBlockTileEntity(x, y, z);
			if(tile == null) {
				player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No TileEntity found"));
			} else {
				player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Starting Debuging of TileEntity: " + tile.getClass().getSimpleName()));
				this.startWatchingOf(tile, (Player)player);
			}
		} else if(mode == TargetMode.Entity) {
			int entityId = (Integer) additions[0];
			Entity entitiy = player.worldObj.getEntityByID(entityId);
			if(entitiy == null) {
				player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No Entity found"));
			} else {
				player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Starting Debuging of Entity: " + entitiy.getClass().getSimpleName()));
				this.startWatchingOf(entitiy, (Player)player);
			}
		}
	}

	@Override
	public void treeExpanded(TreeExpansionEvent treeexpansionevent) {
		if(treeexpansionevent.getPath().getLastPathComponent() instanceof ExtendedDefaultMutableTreeNode && ((ExtendedDefaultMutableTreeNode)treeexpansionevent.getPath().getLastPathComponent()).type != null) {
			ExtendedDefaultMutableTreeNode node = (ExtendedDefaultMutableTreeNode) treeexpansionevent.getPath().getLastPathComponent();
			if(node.loaded) return;
			LinkedList<Integer> path = new LinkedList<Integer>();
			ParentVarType type = node.type;
			while(type != null) {
				path.addFirst(type.i);
				type = type.parent;
			}
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugExpandPart.class).setTree(path.toArray(new Integer[]{})));
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON3) {
			if(tree.getLastSelectedPathComponent() instanceof ExtendedDefaultMutableTreeNode) {
				final ExtendedDefaultMutableTreeNode node = (ExtendedDefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if(node.getUserObject().equals("Fields:") || node.getUserObject().equals("Methods:") || node.getChildCount() > 0 || !(node.base instanceof BasicVarType || node.base instanceof NullVarType)) return;
				JPopupMenu popup = new JPopupMenu();
				popup.setInvoker(tree);
				JMenuItem edit = new JMenuItem("Edit");
				edit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String old = "null";
						if(node.base instanceof BasicVarType) {
							old = ((BasicVarType)node.base).watched;
						}
						String s = (String)JOptionPane.showInputDialog(null, "Please enter the new content for " + node.base.name + ":", "Change Variable",  JOptionPane.PLAIN_MESSAGE, null, null, old);
						LinkedList<Integer> path = new LinkedList<Integer>();
						path.add(node.base.i);
						ParentVarType type = ((ExtendedDefaultMutableTreeNode)node).type;
						if(type == null) {
							type = ((ExtendedDefaultMutableTreeNode)node.getParent()).type;	
						}
						if(type == null) {
							type = ((ExtendedDefaultMutableTreeNode)node.getParent().getParent()).type;	
						}
						while(type != null) {
							path.addFirst(type.i);
							type = type.parent;
						}
						MainProxy.sendPacketToServer(PacketHandler.getPacket(DebugSetVarContent.class).setPath(path.toArray(new Integer[]{})).setContent(s));
					}
				});
				popup.add(edit);
				popup.setLocation(event.getLocationOnScreen());
				popup.setVisible(true);
			}
		} else if(event.getButton() == MouseEvent.BUTTON1) {
			if(event.getClickCount() == 2) {
				if(tree.getLastSelectedPathComponent() instanceof MethodDefaultMutableTreeNode) {
					final MethodDefaultMutableTreeNode node = (MethodDefaultMutableTreeNode) tree.getLastSelectedPathComponent();
					Object[] args = new String[node.type.param.length];
					for(int i=0;i<node.type.param.length;i++) {
						Object[] proposal = null;
						if(node.type.param[i].equals("boolean")) {
							proposal = new Object[]{true, false};
						}
						args[i] = JOptionPane.showInputDialog(null, "Please enter the argument " + i + " of type " + node.type.param[i] + ":", "Enter Paramenter",  JOptionPane.PLAIN_MESSAGE, null, proposal, null);
					}
					//TODO
				}
			}
		}
	}

	@Override public void treeCollapsed(TreeExpansionEvent treeexpansionevent) {}
	@Override public void mouseEntered(MouseEvent arg0) {}
	@Override public void mouseExited(MouseEvent arg0) {}
	@Override public void mousePressed(MouseEvent arg0) {}
	@Override public void mouseReleased(MouseEvent arg0) {}
	
	transient private static DebugGuiTickHandler instance;
	private DebugGuiTickHandler() {}
	
	public static DebugGuiTickHandler instance() {
		if(instance == null) {
			instance = new DebugGuiTickHandler();
		}
		return instance;
	}
}

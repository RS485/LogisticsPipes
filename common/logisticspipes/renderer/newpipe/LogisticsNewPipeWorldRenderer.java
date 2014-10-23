package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.IIconProvider;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.textures.Textures;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Quartet;
import logisticspipes.utils.tuples.Triplet;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class LogisticsNewPipeWorldRenderer implements ISimpleBlockRenderingHandler {
	
	enum Edge {
		Upper_North(ForgeDirection.UP, ForgeDirection.NORTH),
		Upper_South(ForgeDirection.UP, ForgeDirection.SOUTH),
		Upper_East(ForgeDirection.UP, ForgeDirection.EAST),
		Upper_West(ForgeDirection.UP, ForgeDirection.WEST),
		Lower_North(ForgeDirection.DOWN, ForgeDirection.NORTH),
		Lower_South(ForgeDirection.DOWN, ForgeDirection.SOUTH),
		Lower_East(ForgeDirection.DOWN, ForgeDirection.EAST),
		Lower_West(ForgeDirection.DOWN, ForgeDirection.WEST),
		Middle_North_West(ForgeDirection.NORTH, ForgeDirection.WEST),
		Middle_North_East(ForgeDirection.NORTH, ForgeDirection.EAST),
		Lower_South_East(ForgeDirection.SOUTH, ForgeDirection.EAST),
		Lower_South_West(ForgeDirection.SOUTH, ForgeDirection.WEST);
		final ForgeDirection part1;
		final ForgeDirection part2;
		Edge(ForgeDirection part1, ForgeDirection part2) {
			this.part1 = part1;
			this.part2 = part2;
		}
	}
	
	enum UpDown {
		UP("U", ForgeDirection.UP), DOWN("D", ForgeDirection.DOWN);
		final String s;
		final ForgeDirection dir;
		UpDown(String s, ForgeDirection dir) {
			this.s = s;
			this.dir = dir;
		}
	}
	
	enum NorthSouth {
		NORTH("N", ForgeDirection.NORTH), SOUTH("S", ForgeDirection.SOUTH);
		final String s;
		final ForgeDirection dir;
		NorthSouth(String s, ForgeDirection dir) {
			this.s = s;
			this.dir = dir;
		}
	}
	
	enum EastWest {
		EAST("E", ForgeDirection.EAST), WEST("W", ForgeDirection.WEST);
		final String s;
		final ForgeDirection dir;
		EastWest(String s, ForgeDirection dir) {
			this.s = s;
			this.dir = dir;
		}
	}

	enum Corner {
		UP_NORTH_WEST	(UpDown.UP, 	NorthSouth.NORTH, EastWest.WEST),
		UP_NORTH_EAST	(UpDown.UP, 	NorthSouth.NORTH, EastWest.EAST),
		UP_SOUTH_WEST	(UpDown.UP, 	NorthSouth.SOUTH, EastWest.WEST),
		UP_SOUTH_EAST	(UpDown.UP, 	NorthSouth.SOUTH, EastWest.EAST),
		DOWN_NORTH_WEST	(UpDown.DOWN, 	NorthSouth.NORTH, EastWest.WEST),
		DOWN_NORTH_EAST	(UpDown.DOWN, 	NorthSouth.NORTH, EastWest.EAST),
		DOWN_SOUTH_WEST	(UpDown.DOWN, 	NorthSouth.SOUTH, EastWest.WEST),
		DOWN_SOUTH_EAST	(UpDown.DOWN, 	NorthSouth.SOUTH, EastWest.EAST);
		final UpDown ud;
		final NorthSouth ns;
		final EastWest ew;
		Corner(UpDown ud, NorthSouth ns, EastWest ew) {
			this.ud = ud;
			this.ns = ns;
			this.ew = ew;
		}
	}
	
	enum Turn {
		NORTH_SOUTH	(ForgeDirection.NORTH, 	ForgeDirection.SOUTH),
		EAST_WEST	(ForgeDirection.EAST, 	ForgeDirection.WEST),
		UP_DOWN		(ForgeDirection.UP, 	ForgeDirection.DOWN);
		final ForgeDirection dir1;
		final ForgeDirection dir2;
		Turn(ForgeDirection dir1, ForgeDirection dir2) {
			this.dir1 = dir1;
			this.dir2 = dir2;
		}
	}
	
	enum Turn_Corner {
		UP_NORTH_WEST_TURN_NORTH_SOUTH	(Corner.UP_NORTH_WEST, Turn.NORTH_SOUTH, 1),
		UP_NORTH_WEST_TURN_EAST_WEST	(Corner.UP_NORTH_WEST, Turn.EAST_WEST, 14),
		UP_NORTH_WEST_TURN_UP_DOWN		(Corner.UP_NORTH_WEST, Turn.UP_DOWN, 23),
		UP_NORTH_EAST_TURN_NORTH_SOUTH	(Corner.UP_NORTH_EAST, Turn.NORTH_SOUTH, 2),
		UP_NORTH_EAST_TURN_EAST_WEST	(Corner.UP_NORTH_EAST, Turn.EAST_WEST, 9),
		UP_NORTH_EAST_TURN_UP_DOWN		(Corner.UP_NORTH_EAST, Turn.UP_DOWN, 22),
		UP_SOUTH_WEST_TURN_NORTH_SOUTH	(Corner.UP_SOUTH_WEST, Turn.NORTH_SOUTH, 6),
		UP_SOUTH_WEST_TURN_EAST_WEST	(Corner.UP_SOUTH_WEST, Turn.EAST_WEST, 13),
		UP_SOUTH_WEST_TURN_UP_DOWN		(Corner.UP_SOUTH_WEST, Turn.UP_DOWN, 24),
		UP_SOUTH_EAST_TURN_NORTH_SOUTH	(Corner.UP_SOUTH_EAST, Turn.NORTH_SOUTH, 5),
		UP_SOUTH_EAST_TURN_EAST_WEST	(Corner.UP_SOUTH_EAST, Turn.EAST_WEST, 10),
		UP_SOUTH_EAST_TURN_UP_DOWN		(Corner.UP_SOUTH_EAST, Turn.UP_DOWN, 21),
		DOWN_NORTH_WEST_TURN_NORTH_SOUTH(Corner.DOWN_NORTH_WEST, Turn.NORTH_SOUTH, 4),
		DOWN_NORTH_WEST_TURN_EAST_WEST	(Corner.DOWN_NORTH_WEST, Turn.EAST_WEST, 15),
		DOWN_NORTH_WEST_TURN_UP_DOWN	(Corner.DOWN_NORTH_WEST, Turn.UP_DOWN, 20),
		DOWN_NORTH_EAST_TURN_NORTH_SOUTH(Corner.DOWN_NORTH_EAST, Turn.NORTH_SOUTH, 3),
		DOWN_NORTH_EAST_TURN_EAST_WEST	(Corner.DOWN_NORTH_EAST, Turn.EAST_WEST, 12),
		DOWN_NORTH_EAST_TURN_UP_DOWN	(Corner.DOWN_NORTH_EAST, Turn.UP_DOWN, 17),
		DOWN_SOUTH_WEST_TURN_NORTH_SOUTH(Corner.DOWN_SOUTH_WEST, Turn.NORTH_SOUTH, 7),
		DOWN_SOUTH_WEST_TURN_EAST_WEST	(Corner.DOWN_SOUTH_WEST, Turn.EAST_WEST, 16),
		DOWN_SOUTH_WEST_TURN_UP_DOWN	(Corner.DOWN_SOUTH_WEST, Turn.UP_DOWN, 19),
		DOWN_SOUTH_EAST_TURN_NORTH_SOUTH(Corner.DOWN_SOUTH_EAST, Turn.NORTH_SOUTH, 8),
		DOWN_SOUTH_EAST_TURN_EAST_WEST	(Corner.DOWN_SOUTH_EAST, Turn.EAST_WEST, 11),
		DOWN_SOUTH_EAST_TURN_UP_DOWN	(Corner.DOWN_SOUTH_EAST, Turn.UP_DOWN, 18);
		final Corner corner;
		final Turn turn;
		final int number;
		Turn_Corner(Corner corner, Turn turn, int number) {
			this.corner = corner;
			this.turn = turn;
			this.number = number;
		}
		
		public ForgeDirection getPointer() {
			List<ForgeDirection> canidates = new ArrayList<ForgeDirection>();
			canidates.add(corner.ew.dir);
			canidates.add(corner.ns.dir);
			canidates.add(corner.ud.dir);
			if(canidates.contains(turn.dir1)) {
				return turn.dir1;
			} else if(canidates.contains(turn.dir2)) {
				return turn.dir2;
			} else {
				throw new UnsupportedOperationException(this.name());
			}
		}
	}
	
	enum SupportOri {
		UP_DOWN("U"), SIDE("S");
		final String s;
		SupportOri(String s) {
			this.s = s;
		}
	}
	
	enum Support {
		UP_UP(ForgeDirection.UP, SupportOri.UP_DOWN),
		UP_SIDE(ForgeDirection.UP, SupportOri.SIDE),
		DOWN_UP(ForgeDirection.DOWN, SupportOri.UP_DOWN),
		DOWN_SIDE(ForgeDirection.DOWN, SupportOri.SIDE),
		NORTH_UP(ForgeDirection.NORTH, SupportOri.UP_DOWN),
		NORTH_SIDE(ForgeDirection.NORTH, SupportOri.SIDE),
		SOUTH_UP(ForgeDirection.SOUTH, SupportOri.UP_DOWN),
		SOUTH_SIDE(ForgeDirection.SOUTH, SupportOri.SIDE),
		EAST_UP(ForgeDirection.EAST, SupportOri.UP_DOWN),
		EAST_SIDE(ForgeDirection.EAST, SupportOri.SIDE),
		WEST_UP(ForgeDirection.WEST, SupportOri.UP_DOWN),
		WEST_SIDE(ForgeDirection.WEST, SupportOri.SIDE);
		Support(ForgeDirection dir, SupportOri ori) {
			this.dir = dir;
			this.ori = ori;
		}
		final ForgeDirection dir;
		final SupportOri ori;
	}
	
	enum Mount {
		UP_NORTH(ForgeDirection.UP, ForgeDirection.NORTH),
		UP_SOUTH(ForgeDirection.UP, ForgeDirection.SOUTH),
		UP_EAST(ForgeDirection.UP, ForgeDirection.EAST),
		UP_WEST(ForgeDirection.UP, ForgeDirection.WEST),
		DOWN_NORTH(ForgeDirection.DOWN, ForgeDirection.NORTH),
		DOWN_SOUTH(ForgeDirection.DOWN, ForgeDirection.SOUTH),
		DOWN_EAST(ForgeDirection.DOWN, ForgeDirection.EAST),
		DOWN_WEST(ForgeDirection.DOWN, ForgeDirection.WEST),
		NORTH_UP(ForgeDirection.NORTH, ForgeDirection.UP),
		NORTH_DOWN(ForgeDirection.NORTH, ForgeDirection.DOWN),
		NORTH_EAST(ForgeDirection.NORTH, ForgeDirection.EAST),
		NORTH_WEST(ForgeDirection.NORTH, ForgeDirection.WEST),
		SOUTH_UP(ForgeDirection.SOUTH, ForgeDirection.UP),
		SOUTH_DOWN(ForgeDirection.SOUTH, ForgeDirection.DOWN),
		SOUTH_EAST(ForgeDirection.SOUTH, ForgeDirection.EAST),
		SOUTH_WEST(ForgeDirection.SOUTH, ForgeDirection.WEST),
		EAST_UP(ForgeDirection.EAST, ForgeDirection.UP),
		EAST_DOWN(ForgeDirection.EAST, ForgeDirection.DOWN),
		EAST_NORTH(ForgeDirection.EAST, ForgeDirection.NORTH),
		EAST_SOUTH(ForgeDirection.EAST, ForgeDirection.SOUTH),
		WEST_UP(ForgeDirection.WEST, ForgeDirection.UP),
		WEST_DOWN(ForgeDirection.WEST, ForgeDirection.DOWN),
		WEST_NORTH(ForgeDirection.WEST, ForgeDirection.NORTH),
		WEST_SOUTH(ForgeDirection.WEST, ForgeDirection.SOUTH);
		ForgeDirection dir;
		ForgeDirection side;
		Mount(ForgeDirection dir, ForgeDirection side) {
			this.dir = dir;
			this.side = side;
		}
	}
	
	private static Map<ForgeDirection, List<CCModel>> sideNormal = new HashMap<ForgeDirection, List<CCModel>>();
	private static Map<ForgeDirection, List<CCModel>> sideBC = new HashMap<ForgeDirection, List<CCModel>>();
	private static Map<Edge, CCModel> edges = new HashMap<Edge, CCModel>();
	private static Map<Corner, List<CCModel>> corners_M = new HashMap<Corner, List<CCModel>>();
	private static Map<Corner, List<CCModel>> corners_I3 = new HashMap<Corner, List<CCModel>>();
	private static Map<Turn_Corner, CCModel> corners_I = new HashMap<Turn_Corner, CCModel>();
	private static Map<Support, CCModel> supports = new HashMap<Support, CCModel>();
	private static Map<Turn_Corner, CCModel> spacers = new HashMap<Turn_Corner, CCModel>();
	private static Map<Mount, CCModel> mounts = new HashMap<Mount, CCModel>();

	private static Map<ForgeDirection, List<CCModel>> texturePlate_Inner = new HashMap<ForgeDirection, List<CCModel>>();
	private static Map<ForgeDirection, List<CCModel>> texturePlate_Outer = new HashMap<ForgeDirection, List<CCModel>>();
	private static Map<ForgeDirection, Pair<List<CCModel>, List<CCModel>>> sideTexturePlate = new HashMap<ForgeDirection, Pair<List<CCModel>, List<CCModel>>>();
	private static Map<Mount, List<CCModel>> textureConnectorPlate = new HashMap<Mount, List<CCModel>>();
	private static Map<Edge, Quartet<CCModel, CCModel, CCModel, CCModel>> centerEdgeLEDs = new HashMap<Edge, Quartet<CCModel,CCModel,CCModel,CCModel>>();
	private static Map<ForgeDirection, List<CCModel>> sidedInnerLEDs = new HashMap<ForgeDirection, List<CCModel>>();
	private static Map<ForgeDirection, List<CCModel>> sidedOuterLEDs = new HashMap<ForgeDirection, List<CCModel>>();

	public static IconTransformation basicTexture;
	public static IconTransformation activeTexture;
	public static IconTransformation inactiveTexture;

	static {
		loadModels();
	}

	public static void loadModels() {
		try {
			Map<String, CCModel> pipePartModels = CCModel.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/PipeModel_result.obj"), 7, new Scale(1/100f));

			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				sideNormal.put(dir, new ArrayList<CCModel>());
				String grp = "Side_" + getDirAsString_Type1(dir);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						sideNormal.get(dir).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if(sideNormal.get(dir).size() != 4) throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + sideNormal.get(dir).size());
			}
			
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				sideBC.put(dir, new ArrayList<CCModel>());
				String grp = "Side_BC_" + getDirAsString_Type1(dir);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						sideBC.get(dir).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if(sideBC.get(dir).size() != 8) throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + sideBC.get(dir).size());
			}
			
			for(Edge edge:Edge.values()) {
				String grp;
				if(edge.part1 == ForgeDirection.UP || edge.part1 == ForgeDirection.DOWN) {
					grp = "Edge_M_" + getDirAsString_Type1(edge.part1) + "_" + getDirAsString_Type1(edge.part2);
				} else {
					grp = "Edge_M_S_" + getDirAsString_Type1(edge.part1) + getDirAsString_Type1(edge.part2);
				}
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						edges.put(edge, compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if(edges.get(edge) == null) throw new RuntimeException("Couldn't load " + edge.name() + " (" + grp + ")");
			}
			
			for(Corner corner:Corner.values()) {
				corners_M.put(corner, new ArrayList<CCModel>());
				String grp = "Corner_M_" + corner.ud.s + "_" + corner.ns.s + corner.ew.s;
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						corners_M.get(corner).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if(corners_M.get(corner).size() != 2) throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + "). Only loaded " + corners_M.get(corner).size());
			}
			
			for(Corner corner:Corner.values()) {
				corners_I3.put(corner, new ArrayList<CCModel>());
				String grp = "Corner_I3_" + corner.ud.s + "_" + corner.ns.s + corner.ew.s;
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						corners_I3.get(corner).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if(corners_I3.get(corner).size() != 2) throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + "). Only loaded " + corners_I3.get(corner).size());
			}
			
			for(Support support:Support.values()) {
				String grp = "Support_" + getDirAsString_Type1(support.dir) + "_" + support.ori.s;
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						supports.put(support, compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if(supports.get(support) == null) throw new RuntimeException("Couldn't load " + support.name() + " (" + grp + ")");
			}
			
			for(Turn_Corner corner:Turn_Corner.values()) {
				String grp = "Corner_I_" + corner.corner.ud.s + "_" + corner.corner.ns.s + corner.corner.ew.s;
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp)) {
						char c = ' ';
						if(!entry.getKey().endsWith(" " + grp)) {
							c = entry.getKey().charAt(entry.getKey().indexOf(" " + grp) + (" " + grp).length());
						}
						if(Character.isDigit(c)) {
							if(c == '2') {
								if(corner.turn != Turn.NORTH_SOUTH) continue;
							} else if(c == '1') {
								if(corner.turn != Turn.EAST_WEST) continue;
							} else {
								throw new UnsupportedOperationException();
							}
						} else {
							if(corner.turn != Turn.UP_DOWN) continue;
						}
						corners_I.put(corner, compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if(corners_I.get(corner) == null) throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + ")");
			}
			
			for(Turn_Corner corner:Turn_Corner.values()) {
				String grp = "Spacer" + corner.number;
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						spacers.put(corner, compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if(spacers.get(corner) == null) throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + ")");
			}
			
			for(Mount mount:Mount.values()) {
				String grp = "Mount_" + getDirAsString_Type1(mount.dir) + "_" + getDirAsString_Type1(mount.side);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						mounts.put(mount, compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if(mounts.get(mount) == null) throw new RuntimeException("Couldn't load " + mount.name() + " (" + grp + ")");
			}
			
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				texturePlate_Inner.put(dir, new ArrayList<CCModel>());
				String grp = "Inner_Plate_" + getDirAsString_Type1(dir);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp)) {
						texturePlate_Inner.get(dir).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if(texturePlate_Inner.get(dir).size() != 2) throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + texturePlate_Inner.get(dir).size());
			}
			
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				texturePlate_Outer.put(dir, new ArrayList<CCModel>());
				String grp = "Texture_Plate_" + getDirAsString_Type1(dir);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp)) {
						texturePlate_Outer.get(dir).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if(texturePlate_Outer.get(dir).size() != 2) throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + texturePlate_Outer.get(dir).size());
			}
			
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				sideTexturePlate.put(dir, new Pair<List<CCModel>, List<CCModel>>(new ArrayList<CCModel>(), new ArrayList<CCModel>()));
				String grp = "Texture_Side_" + getDirAsString_Type1(dir);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp)) {
						CCModel model = compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)));
						double sizeA = (model.bounds().max.x - model.bounds().min.x) + (model.bounds().max.y - model.bounds().min.y) + (model.bounds().max.z - model.bounds().min.z);
						if(sizeA < 0.5D) {
							sideTexturePlate.get(dir).getValue2().add(model);
						} else {
							sideTexturePlate.get(dir).getValue1().add(model);
						}
					}
				}
				if(sideTexturePlate.get(dir).getValue1().size() != 16) throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + sideTexturePlate.get(dir).getValue1().size());
				if(sideTexturePlate.get(dir).getValue2().size() != 16) throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + sideTexturePlate.get(dir).getValue2().size());
			}

			for(Mount mount:Mount.values()) {
				textureConnectorPlate.put(mount, new ArrayList<CCModel>());
				String grp = "Texture_Connector_" + getDirAsString_Type1(mount.dir) + "_" + getDirAsString_Type1(mount.side);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						textureConnectorPlate.get(mount).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if(textureConnectorPlate.get(mount).size() != 4) throw new RuntimeException("Couldn't load " + mount.name() + " (" + grp + "). Only loaded " + textureConnectorPlate.get(mount).size());
			}
			
			for(Edge edge:Edge.values()) {
				centerEdgeLEDs.put(edge, new Quartet<CCModel, CCModel, CCModel, CCModel>(null, null, null, null));
				for(int i=0;i<4;i++) {
					String grp = "Center_LED_" + (i+1) + "_" + getDirAsString_Type1(edge.part1) + getDirAsString_Type1(edge.part2);
					for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
						if(entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
							CCModel model = compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)).apply(new Translation(-0.5, -0.5, -0.5)).apply(new Scale(1.001D)).apply(new Translation(0.5, 0.5, 0.5)));
							if(i == 0) centerEdgeLEDs.get(edge).setValue1(model);
							if(i == 1) centerEdgeLEDs.get(edge).setValue2(model);
							if(i == 2) centerEdgeLEDs.get(edge).setValue3(model);
							if(i == 3) centerEdgeLEDs.get(edge).setValue4(model);
							break;
						}
					}
				}
				if(centerEdgeLEDs.get(edge).getValue1() == null || centerEdgeLEDs.get(edge).getValue2() == null || centerEdgeLEDs.get(edge).getValue3() == null || centerEdgeLEDs.get(edge).getValue4() == null) throw new RuntimeException("Couldn't load " + edge.name());
			}
			
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				sidedInnerLEDs.put(dir, new ArrayList<CCModel>());
				String grp = "Inner_LED_" + getDirAsString_Type1(dir);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp)) {
						sidedInnerLEDs.get(dir).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)).apply(new Translation(-0.5, -0.5, -0.5)).apply(new Scale(1.001D)).apply(new Translation(0.5, 0.5, 0.5))));
					}
				}
				if(sidedInnerLEDs.get(dir).size() != 4) throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + sidedInnerLEDs.get(dir).size());
			}
			
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				sidedOuterLEDs.put(dir, new ArrayList<CCModel>());
				String grp = "Outer_LED_" + getDirAsString_Type1(dir);
				for(Entry<String, CCModel> entry:pipePartModels.entrySet()) {
					if(entry.getKey().contains(" " + grp)) {
						sidedOuterLEDs.get(dir).add(compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)).apply(new Translation(-0.5, -0.5, -0.5)).apply(new Scale(1.001D)).apply(new Translation(0.5, 0.5, 0.5))));
					}
				}
				if(sidedOuterLEDs.get(dir).size() != 4) throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + sidedOuterLEDs.get(dir).size());
			}
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static String getDirAsString_Type1(ForgeDirection dir) {
		switch(dir) {
			case NORTH:
				return "N";
			case SOUTH:
				return "S";
			case EAST:
				return "E";
			case WEST:
				return "W";
			case UP:
				return "U";
			case DOWN:
				return "D";
			default:
				return "UNKNWON";
		}
	}

	private static CCModel compute(CCModel m) {
		m.computeNormals();
		m.computeLighting(LightModel.standardLightModel);
		return m;
	}
	
	public static void registerTextures(IIconRegister iconRegister) {
		basicTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/basic"));
		activeTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/active"));
		inactiveTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/inactive"));
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		Tessellator tess = Tessellator.instance;
		TileEntity tile = world.getTileEntity(x, y, z);
		LogisticsTileGenericPipe pipeTile = (LogisticsTileGenericPipe) tile;
		PipeRenderState renderState = pipeTile.renderState;
		
		if(pipeTile.pipe instanceof PipeBlockRequestTable) {
			IIconProvider icons = pipeTile.getPipeIcons();
			if (icons == null) return false;
			renderState.currentTexture = icons.getIcon(renderState.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
			((LogisticsBlockGenericPipe)block).setRenderAllSides();
			block.setBlockBounds(0, 0, 0, 1, 1, 1);
			renderer.setRenderBoundsFromBlock(block);
			renderer.renderStandardBlock(block, x, y, z);
			return true;
		}
		
		
		tess.addTranslation(0.00002F, 0.00002F, 0.00002F);
		SimpleServiceLocator.buildCraftProxy.pipeFacadeRenderer(renderer, (LogisticsBlockGenericPipe) block, renderState, x, y, z);
		SimpleServiceLocator.buildCraftProxy.pipePlugRenderer(renderer, (LogisticsBlockGenericPipe) block, renderState, x, y, z);
		SimpleServiceLocator.buildCraftProxy.pipeRobotStationRenderer(renderer, (LogisticsBlockGenericPipe) block, renderState, x, y, z);
		tess.addTranslation(-0.00002F, -0.00002F, -0.00002F);
		
		CCRenderState.reset();
		CCRenderState.useNormals = true;
		CCRenderState.alphaOverride = 0xff;

		int brightness = block.getMixedBrightnessForBlock(world, x, y, z);

		tess.setColorOpaque_F(1F, 1F, 1F);
		tess.setBrightness(brightness);
		
		tess.addTranslation(x, y, z);
		
		List<Pair<CCModel, IconTransformation>> objectsToRender = renderState.cachedRenderer;
		
		boolean solidSides[] = new boolean[6];
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			LPPosition pos = new LPPosition((TileEntity)pipeTile);
			pos.moveForward(dir);
			Block blockSide = pos.getBlock(pipeTile.getWorldObj());
			if(blockSide == null || !blockSide.isSideSolid(pipeTile.getWorldObj(), pos.getX(), pos.getY(), pos.getZ(), dir.getOpposite()) || renderState.pipeConnectionMatrix.isConnected(dir)) {
			} else {
				solidSides[dir.ordinal()] = true;
			}
		}
		if(!Arrays.equals(solidSides, renderState.solidSidesCache)) {
			renderState.solidSidesCache = solidSides.clone();
			objectsToRender = null;
		}
		
		if(objectsToRender == null) {
			objectsToRender = new ArrayList<Pair<CCModel, IconTransformation>>();
			fillObjectsToRenderList(objectsToRender, pipeTile, renderState);
			renderState.cachedRenderer = objectsToRender;
		}
		
		for(Pair<CCModel, IconTransformation> model:objectsToRender) {
			if(model == null) {
				CCRenderState.alphaOverride = 0x60;
			} else {
				model.getValue1().render(model.getValue2());
			}
		}
		CCRenderState.alphaOverride = 0xff;
		
		tess.addTranslation(-x, -y, -z);
		return true;
	}

	private void fillObjectsToRenderList(List<Pair<CCModel, IconTransformation>> objectsToRender, LogisticsTileGenericPipe pipeTile, PipeRenderState renderState) {
		List<Edge> edgesToRender = new ArrayList<Edge>(Arrays.asList(Edge.values()));
		Map<Corner, Integer> connectionAtCorner = new HashMap<Corner, Integer>();
		List<Mount> mountCanidates = new ArrayList<Mount>(Arrays.asList(Mount.values()));
		
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			if(renderState.pipeConnectionMatrix.isConnected(dir)) {
				if(renderState.pipeConnectionMatrix.isBCConnected(dir)) {
					for(CCModel model:sideBC.get(dir)) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(model, basicTexture));
					}
				} else {
					for(CCModel model:sideNormal.get(dir)) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(model, basicTexture));
					}
					/*
					for(CCModel model:sidedInnerLEDs.get(dir)) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(model, activeTexture));
					}
					for(CCModel model:sidedOuterLEDs.get(dir)) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(model, inactiveTexture));
					}
					*/
				}
				for(Edge edge:Edge.values()) {
					if(edge.part1 == dir || edge.part2 == dir) {
						edgesToRender.remove(edge);
						for(Mount mount:Mount.values()) {
							if((mount.dir == edge.part1 && mount.side == edge.part2) || (mount.dir == edge.part2 && mount.side == edge.part1)) {
								mountCanidates.remove(mount);
							}
						}
					}
				}
				for(Corner corner: Corner.values()) {
					if(corner.ew.dir == dir || corner.ns.dir == dir || corner.ud.dir == dir) {
						if(!connectionAtCorner.containsKey(corner)) {
							connectionAtCorner.put(corner, 1);
						} else {
							connectionAtCorner.put(corner, connectionAtCorner.get(corner) + 1);
						}
					}
				}
			}
		}
		
		for(Corner corner: Corner.values()) {
			int count = connectionAtCorner.containsKey(corner) ? connectionAtCorner.get(corner) : 0;
			if(count == 0) {
				for(CCModel model:corners_M.get(corner)) {
					objectsToRender.add(new Pair<CCModel, IconTransformation>(model, inactiveTexture));
				}
			} else if(count == 1) {
				for(Turn_Corner turn:Turn_Corner.values()) {
					if(turn.corner != corner) continue;
					if(renderState.pipeConnectionMatrix.isConnected(turn.getPointer())) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(spacers.get(turn), inactiveTexture));
						break;
					}
				}
			} else if(count == 2) {
				for(Turn_Corner turn:Turn_Corner.values()) {
					if(turn.corner != corner) continue;
					if(!renderState.pipeConnectionMatrix.isConnected(turn.getPointer())) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(corners_I.get(turn), inactiveTexture));
						break;
					}
				}
			} else if(count == 3) {
				for(CCModel model:corners_I3.get(corner)) {
					objectsToRender.add(new Pair<CCModel, IconTransformation>(model, inactiveTexture));
				}
			}
		}
		
		for(Edge edge: edgesToRender) {
			objectsToRender.add(new Pair<CCModel, IconTransformation>(edges.get(edge), basicTexture));
/*
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue1(), activeTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue2(), inactiveTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue3(), inactiveTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue4(), activeTexture));
*/
		}
		
		for(int i=0;i<6;i+=2) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			List<ForgeDirection> list = new ArrayList<ForgeDirection>(Arrays.asList(ForgeDirection.VALID_DIRECTIONS));
			list.remove(dir);
			list.remove(dir.getOpposite());
			if(renderState.pipeConnectionMatrix.isConnected(dir) &&
					renderState.pipeConnectionMatrix.isConnected(dir.getOpposite())) {
				boolean found = false;
				for(ForgeDirection dir2:list) {
					if(renderState.pipeConnectionMatrix.isConnected(dir2)) {
						found = true;
						break;
					}
				}
				if(!found) {
					switch(dir) {
						case DOWN:
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.EAST_SIDE), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.WEST_SIDE), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.NORTH_SIDE), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.SOUTH_SIDE), basicTexture));
							break;
						case NORTH:
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.EAST_UP), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.WEST_UP), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.UP_SIDE), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.DOWN_SIDE), basicTexture));
							break;
						case WEST:
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.UP_UP), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.DOWN_UP), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.NORTH_UP), basicTexture));
							objectsToRender.add(new Pair<CCModel, IconTransformation>(supports.get(Support.SOUTH_UP), basicTexture));
							break;
						default:break;
					}
				}
			}
		}
		
		boolean solidSides[] = new boolean[6];
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			LPPosition pos = new LPPosition((TileEntity)pipeTile);
			pos.moveForward(dir);
			Block blockSide = pos.getBlock(pipeTile.getWorldObj());
			if(blockSide == null || !blockSide.isSideSolid(pipeTile.getWorldObj(), pos.getX(), pos.getY(), pos.getZ(), dir.getOpposite()) || renderState.pipeConnectionMatrix.isConnected(dir)) {
				Iterator<Mount> iter = mountCanidates.iterator();
				while(iter.hasNext()) {
					Mount mount = iter.next();
					if(mount.dir == dir) {
						iter.remove();
					}
				}
			} else {
				solidSides[dir.ordinal()] = true;
			}
		}
		
		if(!mountCanidates.isEmpty()) {
			if(solidSides[ForgeDirection.DOWN.ordinal()]) {
				findOponentOnSameSide(mountCanidates, ForgeDirection.DOWN);
			} else if(solidSides[ForgeDirection.UP.ordinal()]) {
				findOponentOnSameSide(mountCanidates, ForgeDirection.UP);
			} else {
				removeFromSide(mountCanidates, ForgeDirection.DOWN);
				removeFromSide(mountCanidates, ForgeDirection.UP);
				if(mountCanidates.size() > 2) removeIfHasOponentSide(mountCanidates);
				if(mountCanidates.size() > 2) removeIfHasConnectedSide(mountCanidates);
				if(mountCanidates.size() > 2) 
					findOponentOnSameSide(mountCanidates, mountCanidates.get(0).dir);
			}
			
			if(LPConstants.DEBUG && mountCanidates.size() > 2) new RuntimeException("Trying to render " + mountCanidates.size() + " Mounts").printStackTrace();
			
			for(Mount mount:mountCanidates) {
				objectsToRender.add(new Pair<CCModel, IconTransformation>(mounts.get(mount), basicTexture));
			}
		}
		
		if(false /* Check for fluid Pipe */) {
			objectsToRender.add(null);
	
			for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
				if(!renderState.pipeConnectionMatrix.isConnected(dir)) {
					for(CCModel model:texturePlate_Outer.get(dir)) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(model, basicTexture));
					}
					for(CCModel model:texturePlate_Inner.get(dir)) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(model, basicTexture));
					}
				} else {
					for(CCModel model:sideTexturePlate.get(dir).getValue1()) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(model, basicTexture));
					}
				}
			}
			
			for(Mount mount:Mount.values()) {
				if(!renderState.pipeConnectionMatrix.isConnected(mount.dir) && renderState.pipeConnectionMatrix.isConnected(mount.side)) {
					for(CCModel model:textureConnectorPlate.get(mount)) {
						objectsToRender.add(new Pair<CCModel, IconTransformation>(model, basicTexture));
					}
				}
			}
		}
	}

	private void findOponentOnSameSide(List<Mount> mountCanidates, ForgeDirection dir) {
		boolean sides[] = new boolean[6];
		Iterator<Mount> iter = mountCanidates.iterator();
		while(iter.hasNext()) {
			Mount mount = iter.next();
			if(mount.dir != dir) {
				iter.remove();
			} else {
				sides[mount.side.ordinal()] = true;
			}
		}
		if(mountCanidates.size() <= 2) return;
		List<ForgeDirection> keep = new ArrayList<ForgeDirection>();
		if(sides[2] && sides[3]) {
			keep.add(ForgeDirection.NORTH);
			keep.add(ForgeDirection.SOUTH);
		} else if(sides[4] && sides[5]) {
			keep.add(ForgeDirection.EAST);
			keep.add(ForgeDirection.WEST);
		} else if(sides[0] && sides[1]) {
			keep.add(ForgeDirection.UP);
			keep.add(ForgeDirection.DOWN);
		}
		iter = mountCanidates.iterator();
		while(iter.hasNext()) {
			Mount mount = iter.next();
			if(!keep.contains(mount.side)) {
				iter.remove();
			}
		}
	}

	private void removeFromSide(List<Mount> mountCanidates, ForgeDirection dir) {
		Iterator<Mount> iter = mountCanidates.iterator();
		while(iter.hasNext()) {
			Mount mount = iter.next();
			if(mount.dir == dir) {
				iter.remove();
			}
		}
	}

	private void reduceToOnePerSide(List<Mount> mountCanidates, ForgeDirection dir, ForgeDirection pref) {
		boolean found = false;
		Iterator<Mount> iter = mountCanidates.iterator();
		while(iter.hasNext()) {
			Mount mount = iter.next();
			if(mount.dir != dir) continue;
			if(mount.side == pref) {
				found = true;
			}
		}
		if(!found) {
			reduceToOnePerSide(mountCanidates, dir);
		} else {
			iter = mountCanidates.iterator();
			while(iter.hasNext()) {
				Mount mount = iter.next();
				if(mount.dir != dir) continue;
				if(mount.side != pref) {
					iter.remove();
				}
			}
		}
	}
	
	private void reduceToOnePerSide(List<Mount> mountCanidates, ForgeDirection dir) {
		boolean found = false;
		Iterator<Mount> iter = mountCanidates.iterator();
		while(iter.hasNext()) {
			Mount mount = iter.next();
			if(mount.dir != dir) continue;
			if(found) {
				iter.remove();
			} else {
				found = true;
			}
		}
	}

	private void removeIfHasOponentSide(List<Mount> mountCanidates) {
		boolean sides[] = new boolean[6];
		Iterator<Mount> iter = mountCanidates.iterator();
		while(iter.hasNext()) {
			Mount mount = iter.next();
			sides[mount.dir.ordinal()] = true;
		}
		if(sides[2] && sides[3]) {
			removeFromSide(mountCanidates, ForgeDirection.EAST);
			removeFromSide(mountCanidates, ForgeDirection.WEST);
			reduceToOnePerSide(mountCanidates, ForgeDirection.NORTH);
			reduceToOnePerSide(mountCanidates, ForgeDirection.SOUTH);
		} else if(sides[4] && sides[5]) {
			removeFromSide(mountCanidates, ForgeDirection.NORTH);
			removeFromSide(mountCanidates, ForgeDirection.SOUTH);
			reduceToOnePerSide(mountCanidates, ForgeDirection.EAST);
			reduceToOnePerSide(mountCanidates, ForgeDirection.WEST);
		}
	}

	private void removeIfHasConnectedSide(List<Mount> mountCanidates) {
		boolean sides[] = new boolean[6];
		Iterator<Mount> iter = mountCanidates.iterator();
		while(iter.hasNext()) {
			Mount mount = iter.next();
			sides[mount.dir.ordinal()] = true;
		}
		for(int i=2;i<6;i++) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
			if(sides[dir.ordinal()] && sides[rot.ordinal()]) {
				reduceToOnePerSide(mountCanidates, dir, dir.getRotation(ForgeDirection.DOWN));
				reduceToOnePerSide(mountCanidates, rot, rot.getRotation(ForgeDirection.UP));
			}
		}
	}

	@Override
	public int getRenderId() {
		return LPConstants.pipeModel;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}
}
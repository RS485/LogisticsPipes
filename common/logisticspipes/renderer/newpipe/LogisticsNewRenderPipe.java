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
import logisticspipes.config.PlayerConfig;
import logisticspipes.pipefxhandlers.EntityModelFX;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.textures.Textures;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Quartet;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.CCRenderState.IVertexOperation;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.render.uv.UVTransformationList;
import codechicken.lib.render.uv.UVTranslation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.lwjgl.opengl.GL11;

public class LogisticsNewRenderPipe {

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
		UP("U", ForgeDirection.UP),
		DOWN("D", ForgeDirection.DOWN);

		final String s;
		final ForgeDirection dir;

		UpDown(String s, ForgeDirection dir) {
			this.s = s;
			this.dir = dir;
		}
	}

	enum NorthSouth {
		NORTH("N", ForgeDirection.NORTH),
		SOUTH("S", ForgeDirection.SOUTH);

		final String s;
		final ForgeDirection dir;

		NorthSouth(String s, ForgeDirection dir) {
			this.s = s;
			this.dir = dir;
		}
	}

	enum EastWest {
		EAST("E", ForgeDirection.EAST),
		WEST("W", ForgeDirection.WEST);

		final String s;
		final ForgeDirection dir;

		EastWest(String s, ForgeDirection dir) {
			this.s = s;
			this.dir = dir;
		}
	}

	enum Corner {
		UP_NORTH_WEST(UpDown.UP, NorthSouth.NORTH, EastWest.WEST),
		UP_NORTH_EAST(UpDown.UP, NorthSouth.NORTH, EastWest.EAST),
		UP_SOUTH_WEST(UpDown.UP, NorthSouth.SOUTH, EastWest.WEST),
		UP_SOUTH_EAST(UpDown.UP, NorthSouth.SOUTH, EastWest.EAST),
		DOWN_NORTH_WEST(UpDown.DOWN, NorthSouth.NORTH, EastWest.WEST),
		DOWN_NORTH_EAST(UpDown.DOWN, NorthSouth.NORTH, EastWest.EAST),
		DOWN_SOUTH_WEST(UpDown.DOWN, NorthSouth.SOUTH, EastWest.WEST),
		DOWN_SOUTH_EAST(UpDown.DOWN, NorthSouth.SOUTH, EastWest.EAST);

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
		NORTH_SOUTH(ForgeDirection.NORTH, ForgeDirection.SOUTH),
		EAST_WEST(ForgeDirection.EAST, ForgeDirection.WEST),
		UP_DOWN(ForgeDirection.UP, ForgeDirection.DOWN);

		final ForgeDirection dir1;
		final ForgeDirection dir2;

		Turn(ForgeDirection dir1, ForgeDirection dir2) {
			this.dir1 = dir1;
			this.dir2 = dir2;
		}
	}

	enum PipeTurnCorner {
		UP_NORTH_WEST_TURN_NORTH_SOUTH(Corner.UP_NORTH_WEST, Turn.NORTH_SOUTH, 1),
		UP_NORTH_WEST_TURN_EAST_WEST(Corner.UP_NORTH_WEST, Turn.EAST_WEST, 14),
		UP_NORTH_WEST_TURN_UP_DOWN(Corner.UP_NORTH_WEST, Turn.UP_DOWN, 23),
		UP_NORTH_EAST_TURN_NORTH_SOUTH(Corner.UP_NORTH_EAST, Turn.NORTH_SOUTH, 2),
		UP_NORTH_EAST_TURN_EAST_WEST(Corner.UP_NORTH_EAST, Turn.EAST_WEST, 9),
		UP_NORTH_EAST_TURN_UP_DOWN(Corner.UP_NORTH_EAST, Turn.UP_DOWN, 22),
		UP_SOUTH_WEST_TURN_NORTH_SOUTH(Corner.UP_SOUTH_WEST, Turn.NORTH_SOUTH, 6),
		UP_SOUTH_WEST_TURN_EAST_WEST(Corner.UP_SOUTH_WEST, Turn.EAST_WEST, 13),
		UP_SOUTH_WEST_TURN_UP_DOWN(Corner.UP_SOUTH_WEST, Turn.UP_DOWN, 24),
		UP_SOUTH_EAST_TURN_NORTH_SOUTH(Corner.UP_SOUTH_EAST, Turn.NORTH_SOUTH, 5),
		UP_SOUTH_EAST_TURN_EAST_WEST(Corner.UP_SOUTH_EAST, Turn.EAST_WEST, 10),
		UP_SOUTH_EAST_TURN_UP_DOWN(Corner.UP_SOUTH_EAST, Turn.UP_DOWN, 21),
		DOWN_NORTH_WEST_TURN_NORTH_SOUTH(Corner.DOWN_NORTH_WEST, Turn.NORTH_SOUTH, 4),
		DOWN_NORTH_WEST_TURN_EAST_WEST(Corner.DOWN_NORTH_WEST, Turn.EAST_WEST, 15),
		DOWN_NORTH_WEST_TURN_UP_DOWN(Corner.DOWN_NORTH_WEST, Turn.UP_DOWN, 20),
		DOWN_NORTH_EAST_TURN_NORTH_SOUTH(Corner.DOWN_NORTH_EAST, Turn.NORTH_SOUTH, 3),
		DOWN_NORTH_EAST_TURN_EAST_WEST(Corner.DOWN_NORTH_EAST, Turn.EAST_WEST, 12),
		DOWN_NORTH_EAST_TURN_UP_DOWN(Corner.DOWN_NORTH_EAST, Turn.UP_DOWN, 17),
		DOWN_SOUTH_WEST_TURN_NORTH_SOUTH(Corner.DOWN_SOUTH_WEST, Turn.NORTH_SOUTH, 7),
		DOWN_SOUTH_WEST_TURN_EAST_WEST(Corner.DOWN_SOUTH_WEST, Turn.EAST_WEST, 16),
		DOWN_SOUTH_WEST_TURN_UP_DOWN(Corner.DOWN_SOUTH_WEST, Turn.UP_DOWN, 19),
		DOWN_SOUTH_EAST_TURN_NORTH_SOUTH(Corner.DOWN_SOUTH_EAST, Turn.NORTH_SOUTH, 8),
		DOWN_SOUTH_EAST_TURN_EAST_WEST(Corner.DOWN_SOUTH_EAST, Turn.EAST_WEST, 11),
		DOWN_SOUTH_EAST_TURN_UP_DOWN(Corner.DOWN_SOUTH_EAST, Turn.UP_DOWN, 18);

		final Corner corner;
		final Turn turn;
		final int number;

		PipeTurnCorner(Corner corner, Turn turn, int number) {
			this.corner = corner;
			this.turn = turn;
			this.number = number;
		}

		public ForgeDirection getPointer() {
			List<ForgeDirection> canidates = new ArrayList<ForgeDirection>();
			canidates.add(corner.ew.dir);
			canidates.add(corner.ns.dir);
			canidates.add(corner.ud.dir);
			if (canidates.contains(turn.dir1)) {
				return turn.dir1;
			} else if (canidates.contains(turn.dir2)) {
				return turn.dir2;
			} else {
				throw new UnsupportedOperationException(name());
			}
		}
	}

	enum PipeSupportOri {
		UP_DOWN("U"),
		SIDE("S");

		final String s;

		PipeSupportOri(String s) {
			this.s = s;
		}
	}

	enum PipeSupport {
		UP_UP(ForgeDirection.UP, PipeSupportOri.UP_DOWN),
		UP_SIDE(ForgeDirection.UP, PipeSupportOri.SIDE),
		DOWN_UP(ForgeDirection.DOWN, PipeSupportOri.UP_DOWN),
		DOWN_SIDE(ForgeDirection.DOWN, PipeSupportOri.SIDE),
		NORTH_UP(ForgeDirection.NORTH, PipeSupportOri.UP_DOWN),
		NORTH_SIDE(ForgeDirection.NORTH, PipeSupportOri.SIDE),
		SOUTH_UP(ForgeDirection.SOUTH, PipeSupportOri.UP_DOWN),
		SOUTH_SIDE(ForgeDirection.SOUTH, PipeSupportOri.SIDE),
		EAST_UP(ForgeDirection.EAST, PipeSupportOri.UP_DOWN),
		EAST_SIDE(ForgeDirection.EAST, PipeSupportOri.SIDE),
		WEST_UP(ForgeDirection.WEST, PipeSupportOri.UP_DOWN),
		WEST_SIDE(ForgeDirection.WEST, PipeSupportOri.SIDE);

		PipeSupport(ForgeDirection dir, PipeSupportOri ori) {
			this.dir = dir;
			this.ori = ori;
		}

		final ForgeDirection dir;
		final PipeSupportOri ori;
	}

	enum PipeMount {
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

		PipeMount(ForgeDirection dir, ForgeDirection side) {
			this.dir = dir;
			this.side = side;
		}
	}

	//Pipe Models
	static Map<ForgeDirection, List<CCModel>> sideNormal = new HashMap<ForgeDirection, List<CCModel>>();
	static Map<ForgeDirection, List<CCModel>> sideBC = new HashMap<ForgeDirection, List<CCModel>>();
	static Map<Edge, CCModel> edges = new HashMap<Edge, CCModel>();
	static Map<Corner, List<CCModel>> corners_M = new HashMap<Corner, List<CCModel>>();
	static Map<Corner, List<CCModel>> corners_I3 = new HashMap<Corner, List<CCModel>>();
	static Map<PipeTurnCorner, CCModel> corners_I = new HashMap<PipeTurnCorner, CCModel>();
	static Map<PipeSupport, CCModel> supports = new HashMap<PipeSupport, CCModel>();
	static Map<PipeTurnCorner, CCModel> spacers = new HashMap<PipeTurnCorner, CCModel>();
	static Map<PipeMount, CCModel> mounts = new HashMap<PipeMount, CCModel>();

	static Map<ForgeDirection, List<CCModel>> texturePlate_Inner = new HashMap<ForgeDirection, List<CCModel>>();
	static Map<ForgeDirection, List<CCModel>> texturePlate_Outer = new HashMap<ForgeDirection, List<CCModel>>();
	static Map<ForgeDirection, Quartet<List<CCModel>, List<CCModel>, List<CCModel>, List<CCModel>>> sideTexturePlate = new HashMap<ForgeDirection, Quartet<List<CCModel>, List<CCModel>, List<CCModel>, List<CCModel>>>();
	static Map<PipeMount, List<CCModel>> textureConnectorPlate = new HashMap<PipeMount, List<CCModel>>();
	static Map<Edge, Quartet<CCModel, CCModel, CCModel, CCModel>> centerEdgeLEDs = new HashMap<Edge, Quartet<CCModel, CCModel, CCModel, CCModel>>();
	static Map<ForgeDirection, List<CCModel>> sidedInnerLEDs = new HashMap<ForgeDirection, List<CCModel>>();
	static Map<ForgeDirection, List<CCModel>> sidedOuterLEDs = new HashMap<ForgeDirection, List<CCModel>>();

	static Map<ScaleObject, CCModel> scaleMap = new HashMap<ScaleObject, CCModel>();

	@Data
	@AllArgsConstructor
	private static class ScaleObject {

		private final CCModel original;
		private final double scale;
	}

	static CCModel innerTransportBox;

	//Pipe Textures
	public static IconTransformation basicPipeTexture;
	public static IconTransformation inactiveTexture;
	public static IconTransformation glassCenterTexture;
	public static IconTransformation innerBoxTexture;
	public static IconTransformation statusTexture;
	public static IconTransformation statusBCTexture;

	static {
		LogisticsNewRenderPipe.loadModels();
	}

	public static void loadModels() {
		try {
			Map<String, CCModel> pipePartModels = CCModel.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/PipeModel_result.obj"), 7, new Scale(1 / 100f));

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sideNormal.put(dir, new ArrayList<CCModel>());
				String grp = "Side_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.sideNormal.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.sideNormal.get(dir).size() != 4) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideNormal.get(dir).size());
				}
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sideBC.put(dir, new ArrayList<CCModel>());
				String grp = "Side_BC_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.sideBC.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.sideBC.get(dir).size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideBC.get(dir).size());
				}
			}

			for (Edge edge : Edge.values()) {
				String grp;
				if (edge.part1 == ForgeDirection.UP || edge.part1 == ForgeDirection.DOWN) {
					grp = "Edge_M_" + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part1) + "_" + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part2);
				} else {
					grp = "Edge_M_S_" + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part1) + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part2);
				}
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.edges.put(edge, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.edges.get(edge) == null) {
					throw new RuntimeException("Couldn't load " + edge.name() + " (" + grp + ")");
				}
			}

			for (Corner corner : Corner.values()) {
				LogisticsNewRenderPipe.corners_M.put(corner, new ArrayList<CCModel>());
				String grp = "Corner_M_" + corner.ud.s + "_" + corner.ns.s + corner.ew.s;
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.corners_M.get(corner).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.corners_M.get(corner).size() != 2) {
					throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.corners_M.get(corner).size());
				}
			}

			for (Corner corner : Corner.values()) {
				LogisticsNewRenderPipe.corners_I3.put(corner, new ArrayList<CCModel>());
				String grp = "Corner_I3_" + corner.ud.s + "_" + corner.ns.s + corner.ew.s;
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.corners_I3.get(corner).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.corners_I3.get(corner).size() != 2) {
					throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.corners_I3.get(corner).size());
				}
			}

			for (PipeSupport support : PipeSupport.values()) {
				String grp = "Support_" + LogisticsNewRenderPipe.getDirAsString_Type1(support.dir) + "_" + support.ori.s;
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.supports.put(support, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.supports.get(support) == null) {
					throw new RuntimeException("Couldn't load " + support.name() + " (" + grp + ")");
				}
			}

			for (PipeTurnCorner corner : PipeTurnCorner.values()) {
				String grp = "Corner_I_" + corner.corner.ud.s + "_" + corner.corner.ns.s + corner.corner.ew.s;
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						char c = ' ';
						if (!entry.getKey().endsWith(" " + grp)) {
							c = entry.getKey().charAt(entry.getKey().indexOf(" " + grp) + (" " + grp).length());
						}
						if (Character.isDigit(c)) {
							if (c == '2') {
								if (corner.turn != Turn.NORTH_SOUTH) {
									continue;
								}
							} else if (c == '1') {
								if (corner.turn != Turn.EAST_WEST) {
									continue;
								}
							} else {
								throw new UnsupportedOperationException();
							}
						} else {
							if (corner.turn != Turn.UP_DOWN) {
								continue;
							}
						}
						LogisticsNewRenderPipe.corners_I.put(corner, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.corners_I.get(corner) == null) {
					throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + ")");
				}
			}

			for (PipeTurnCorner corner : PipeTurnCorner.values()) {
				String grp = "Spacer" + corner.number;
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.spacers.put(corner, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.spacers.get(corner) == null) {
					throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + ")");
				}
			}

			for (PipeMount mount : PipeMount.values()) {
				String grp = "Mount_" + LogisticsNewRenderPipe.getDirAsString_Type1(mount.dir) + "_" + LogisticsNewRenderPipe.getDirAsString_Type1(mount.side);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.mounts.put(mount, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.mounts.get(mount) == null) {
					throw new RuntimeException("Couldn't load " + mount.name() + " (" + grp + ")");
				}
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.texturePlate_Inner.put(dir, new ArrayList<CCModel>());
				String grp = "Inner_Plate_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						LogisticsNewRenderPipe.texturePlate_Inner.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.texturePlate_Inner.get(dir).size() != 2) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.texturePlate_Inner.get(dir).size());
				}
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.texturePlate_Outer.put(dir, new ArrayList<CCModel>());
				String grp = "Texture_Plate_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						LogisticsNewRenderPipe.texturePlate_Outer.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)).apply(new Translation(-0.5, -0.5, -0.5)).apply(new Scale(1.001D)).apply(new Translation(0.5, 0.5, 0.5))));
					}
				}
				if (LogisticsNewRenderPipe.texturePlate_Outer.get(dir).size() != 2) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.texturePlate_Outer.get(dir).size());
				}
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sideTexturePlate.put(dir, new Quartet<List<CCModel>, List<CCModel>, List<CCModel>, List<CCModel>>(new ArrayList<CCModel>(), new ArrayList<CCModel>(), new ArrayList<CCModel>(), new ArrayList<CCModel>()));
				String grp = "Texture_Side_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						CCModel model = LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)));
						double sizeA = (model.bounds().max.x - model.bounds().min.x) + (model.bounds().max.y - model.bounds().min.y) + (model.bounds().max.z - model.bounds().min.z);
						double dis = Math.pow(model.bounds().min.x - 0.5D, 2) + Math.pow(model.bounds().min.y - 0.5D, 2) + Math.pow(model.bounds().min.z - 0.5D, 2);
						if (sizeA < 0.5D) {
							if ((dis > 0.21 && dis < 0.23) || (dis > 0.37 && dis < 0.39)) {
								LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue4().add(model);
							} else if ((dis < 0.2 && dis > 0.18) || (dis < 0.36 && dis > 0.34)) {
								LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue2().add(model);
							} else {
								throw new UnsupportedOperationException("Dis: " + dis);
							}
						} else {
							if ((dis > 0.21 && dis < 0.23) || (dis > 0.37 && dis < 0.39)) {
								LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue3().add(model);
							} else if ((dis < 0.2 && dis > 0.18) || (dis < 0.36 && dis > 0.34)) {
								LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue1().add(model);
							} else {
								throw new UnsupportedOperationException("Dis: " + dis);
							}
						}
					}
				}
				if (LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue1().size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue1().size());
				}
				if (LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue2().size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue2().size());
				}
				if (LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue3().size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue3().size());
				}
				if (LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue4().size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue4().size());
				}
			}

			for (PipeMount mount : PipeMount.values()) {
				LogisticsNewRenderPipe.textureConnectorPlate.put(mount, new ArrayList<CCModel>());
				String grp = "Texture_Connector_" + LogisticsNewRenderPipe.getDirAsString_Type1(mount.dir) + "_" + LogisticsNewRenderPipe.getDirAsString_Type1(mount.side);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.textureConnectorPlate.get(mount).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.textureConnectorPlate.get(mount).size() != 4) {
					throw new RuntimeException("Couldn't load " + mount.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.textureConnectorPlate.get(mount).size());
				}
			}

			for (Edge edge : Edge.values()) {
				LogisticsNewRenderPipe.centerEdgeLEDs.put(edge, new Quartet<CCModel, CCModel, CCModel, CCModel>(null, null, null, null));
				for (int i = 0; i < 4; i++) {
					String grp = "Center_LED_" + (i + 1) + "_" + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part1) + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part2);
					for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
						if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
							CCModel model = LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)).apply(new Translation(-0.5, -0.5, -0.5)).apply(new Scale(1.001D)).apply(new Translation(0.5, 0.5, 0.5)));
							if (i == 0) {
								LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).setValue1(model);
							}
							if (i == 1) {
								LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).setValue2(model);
							}
							if (i == 2) {
								LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).setValue3(model);
							}
							if (i == 3) {
								LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).setValue4(model);
							}
							break;
						}
					}
				}
				if (LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).getValue1() == null || LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).getValue2() == null || LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).getValue3() == null || LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).getValue4() == null) {
					throw new RuntimeException("Couldn't load " + edge.name());
				}
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sidedInnerLEDs.put(dir, new ArrayList<CCModel>());
				String grp = "Inner_LED_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						LogisticsNewRenderPipe.sidedInnerLEDs.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)).apply(new Translation(-0.5, -0.5, -0.5)).apply(new Scale(1.001D)).apply(new Translation(0.5, 0.5, 0.5))));
					}
				}
				if (LogisticsNewRenderPipe.sidedInnerLEDs.get(dir).size() != 4) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sidedInnerLEDs.get(dir).size());
				}
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sidedOuterLEDs.put(dir, new ArrayList<CCModel>());
				String grp = "Outer_LED_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, CCModel> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						LogisticsNewRenderPipe.sidedOuterLEDs.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)).apply(new Translation(-0.5, -0.5, -0.5)).apply(new Scale(1.001D)).apply(new Translation(0.5, 0.5, 0.5))));
					}
				}
				if (LogisticsNewRenderPipe.sidedOuterLEDs.get(dir).size() != 4) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sidedOuterLEDs.get(dir).size());
				}
			}

			pipePartModels = CCModel.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/PipeModel_Transport_Box.obj"), 7, new Scale(1 / 100f));

			LogisticsNewRenderPipe.innerTransportBox = LogisticsNewRenderPipe.compute(pipePartModels.get("InnerTransportBox").backfacedCopy().apply(new Translation(0.0, 0.0, 1.0)).apply(new Translation(-0.5, -0.5, -0.5)).apply(new Scale(0.99D)).apply(new Translation(0.5, 0.5, 0.5)));

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static String getDirAsString_Type1(ForgeDirection dir) {
		switch (dir) {
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

	public static CCModel compute(CCModel m) {
		m.computeNormals();
		m.computeLighting(LightModel.standardLightModel);
		return m;
	}

	public static void registerTextures(IIconRegister iconRegister) {
		if (LogisticsNewRenderPipe.basicPipeTexture == null) {
			LogisticsNewRenderPipe.basicPipeTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel"));
			LogisticsNewRenderPipe.inactiveTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-inactive"));
			LogisticsNewRenderPipe.innerBoxTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/InnerBox"));
			LogisticsNewRenderPipe.glassCenterTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/Glass_Texture_Center"));
			LogisticsNewRenderPipe.statusTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-status"));
			LogisticsNewRenderPipe.statusBCTexture = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-status-BC"));
		} else {
			LogisticsNewRenderPipe.basicPipeTexture.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel");
			LogisticsNewRenderPipe.inactiveTexture.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-inactive");
			LogisticsNewRenderPipe.innerBoxTexture.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/InnerBox");
			LogisticsNewRenderPipe.glassCenterTexture.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/Glass_Texture_Center");
			LogisticsNewRenderPipe.statusTexture.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-status");
			LogisticsNewRenderPipe.statusBCTexture.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-status-BC");
		}
	}

	private PlayerConfig config = LogisticsPipes.getClientPlayerConfig();

	public void renderTileEntityAt(LogisticsTileGenericPipe pipeTile, double x, double y, double z, float f, double distance) {

		if (pipeTile.pipe instanceof PipeBlockRequestTable) {
			return;
		}
		if (pipeTile.pipe == null) {
			return;
		}
		PipeRenderState renderState = pipeTile.renderState;

		if (renderState.renderList != null && renderState.renderList.isInvalid()) {
			renderState.renderList = null;
		}

		if (renderState.renderList == null) {
			renderState.renderList = SimpleServiceLocator.renderListHandler.getNewRenderList();
		}

		if (distance > config.getRenderPipeDistance() * config.getRenderPipeDistance()) {
			if (config.isUseFallbackRenderer()) {
				renderState.forceRenderOldPipe = true;
			}
		} else {
			renderState.forceRenderOldPipe = false;
			boolean recalculateList = false;
			if (renderState.cachedRenderer == null) {
				List<RenderEntry> objectsToRender = new ArrayList<RenderEntry>();

				if (pipeTile.pipe != null && pipeTile.pipe.getSpecialRenderer() != null) {
					pipeTile.pipe.getSpecialRenderer().renderToList(pipeTile.pipe, objectsToRender);
				}

				if (pipeTile.pipe != null && pipeTile.pipe.actAsNormalPipe()) {
					fillObjectsToRenderList(objectsToRender, pipeTile, renderState);
				}

				renderState.cachedRenderer = objectsToRender;
				recalculateList = true;
			}
			if (!renderState.renderList.isFilled() || recalculateList) {
				ResourceLocation oldTexture = null;
				renderState.renderList.startListCompile();

				CCRenderState.reset();

				GL11.glNormal3f(0.0F, 0.0F, 1.0F);
				CCRenderState.useNormals = true;
				CCRenderState.alphaOverride = 0xff;

				int brightness = new LPPosition((TileEntity) pipeTile).getBlock(pipeTile.getWorldObj()).getMixedBrightnessForBlock(pipeTile.getWorldObj(), pipeTile.xCoord, pipeTile.yCoord, pipeTile.zCoord);
				CCRenderState.setBrightness(brightness);
				boolean tesselating = false;

				for (RenderEntry model : renderState.cachedRenderer) {
					ResourceLocation texture = model.getTexture();
					if (texture == null) {
						throw new NullPointerException();
					}
					if (texture != oldTexture || oldTexture == null) {
						if (tesselating) {
							CCRenderState.draw();
							tesselating = false;
						}
						oldTexture = texture;
						Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

						CCRenderState.startDrawing();
						tesselating = true;
					}
					model.getModel().render(model.getOperations());
				}
				if (tesselating) {
					CCRenderState.draw();
				}

				renderState.renderList.stopCompile();
			}
			if (renderState.renderList != null) {
				GL11.glPushMatrix();
				GL11.glTranslated(x, y, z);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
				renderState.renderList.render();
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glTranslated(-x, -y, -z);
				GL11.glPopMatrix();
			}
		}
	}

	private void fillObjectsToRenderList(List<RenderEntry> objectsToRender, LogisticsTileGenericPipe pipeTile, PipeRenderState renderState) {

		List<Edge> edgesToRender = new ArrayList<Edge>(Arrays.asList(Edge.values()));
		Map<Corner, Integer> connectionAtCorner = new HashMap<Corner, Integer>();
		List<PipeMount> mountCanidates = new ArrayList<PipeMount>(Arrays.asList(PipeMount.values()));

		int connectionCount = 0;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (renderState.pipeConnectionMatrix.isConnected(dir) || pipeTile.pipe.hasSpecialPipeEndAt(dir)) {
				connectionCount++;
				if (renderState.pipeConnectionMatrix.isBCConnected(dir) || renderState.pipeConnectionMatrix.isTDConnected(dir)) {
					IVertexOperation[] texture = new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture };
					if (renderState.textureMatrix.isRouted()) {
						if (renderState.textureMatrix.isRoutedInDir(dir)) {
							if (renderState.textureMatrix.isSubPowerInDir(dir)) {
								texture = new IVertexOperation[] { new UVTransformationList(new UVTranslation(0, +23F / 100), LogisticsNewRenderPipe.statusBCTexture) };
							} else {
								texture = new IVertexOperation[] { LogisticsNewRenderPipe.statusBCTexture };
							}
						} else {
							texture = new IVertexOperation[] { new UVTransformationList(new UVTranslation(0, -23F / 100), LogisticsNewRenderPipe.statusBCTexture) };
						}
					}
					for (CCModel model : LogisticsNewRenderPipe.sideBC.get(dir)) {
						objectsToRender.add(new RenderEntry(model, texture));
					}
				} else if (!pipeTile.pipe.hasSpecialPipeEndAt(dir)) {
					IVertexOperation[] texture = new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture };
					if (renderState.textureMatrix.isRouted()) {
						if (renderState.textureMatrix.isRoutedInDir(dir)) {
							if (renderState.textureMatrix.isSubPowerInDir(dir)) {
								texture = new IVertexOperation[] { new UVTransformationList(new UVTranslation(-2.5F / 10, 0), LogisticsNewRenderPipe.statusTexture) };
							} else {
								texture = new IVertexOperation[] { LogisticsNewRenderPipe.statusTexture };
							}
						} else {
							if (renderState.textureMatrix.isHasPowerUpgrade()) {
								if (renderState.textureMatrix.getPointedOrientation() == dir) {
									texture = new IVertexOperation[] { new UVTransformationList(new UVTranslation(+2.5F / 10, 0), LogisticsNewRenderPipe.statusTexture) };
								} else {
									texture = new IVertexOperation[] { new UVTransformationList(new UVTranslation(-2.5F / 10, 37F / 100), LogisticsNewRenderPipe.statusTexture) };
								}
							} else {
								if (renderState.textureMatrix.getPointedOrientation() == dir) {
									texture = new IVertexOperation[] { new UVTransformationList(new UVTranslation(+2.5F / 10, 37F / 100), LogisticsNewRenderPipe.statusTexture) };
								} else {
									texture = new IVertexOperation[] { new UVTransformationList(new UVTranslation(0, 37F / 100), LogisticsNewRenderPipe.statusTexture) };
								}
							}
						}
					}
					for (CCModel model : LogisticsNewRenderPipe.sideNormal.get(dir)) {
						Block block = new LPPosition((TileEntity) pipeTile).moveForward(dir).getBlock(pipeTile.getWorld());
						double[] bounds = { block.getBlockBoundsMinY(), block.getBlockBoundsMinZ(), block.getBlockBoundsMinX(), block.getBlockBoundsMaxY(), block.getBlockBoundsMaxZ(), block.getBlockBoundsMaxX() };
						double bound = bounds[dir.ordinal() / 2 + (dir.ordinal() % 2 == 0 ? 3 : 0)];
						ScaleObject key = new ScaleObject(model, bound);
						CCModel model2 = LogisticsNewRenderPipe.scaleMap.get(key);
						if (model2 == null) {
							model2 = model.copy();
							Vector3 min = model2.bounds().min;
							model2.apply(new Translation(min).inverse());
							double toAdd = 1;
							if (dir.ordinal() % 2 == 1) {
								toAdd = 1 + (bound / LPConstants.PIPE_MIN_POS);
								model2.apply(new Scale(dir.offsetX != 0 ? toAdd : 1, dir.offsetY != 0 ? toAdd : 1, dir.offsetZ != 0 ? toAdd : 1));
							} else {
								bound = 1 - bound;
								toAdd = 1 + (bound / LPConstants.PIPE_MIN_POS);
								model2.apply(new Scale(dir.offsetX != 0 ? toAdd : 1, dir.offsetY != 0 ? toAdd : 1, dir.offsetZ != 0 ? toAdd : 1));
								model2.apply(new Translation(dir.offsetX * bound, dir.offsetY * bound, dir.offsetZ * bound));
							}
							model2.apply(new Translation(min));
							LogisticsNewRenderPipe.scaleMap.put(key, model2);
						}
						objectsToRender.add(new RenderEntry(model2, texture));
					}
				}
				for (Edge edge : Edge.values()) {
					if (edge.part1 == dir || edge.part2 == dir) {
						edgesToRender.remove(edge);
						for (PipeMount mount : PipeMount.values()) {
							if ((mount.dir == edge.part1 && mount.side == edge.part2) || (mount.dir == edge.part2 && mount.side == edge.part1)) {
								mountCanidates.remove(mount);
							}
						}
					}
				}
				for (Corner corner : Corner.values()) {
					if (corner.ew.dir == dir || corner.ns.dir == dir || corner.ud.dir == dir) {
						if (!connectionAtCorner.containsKey(corner)) {
							connectionAtCorner.put(corner, 1);
						} else {
							connectionAtCorner.put(corner, connectionAtCorner.get(corner) + 1);
						}
					}
				}
			}
		}

		for (Corner corner : Corner.values()) {
			IconTransformation cornerTexture = LogisticsNewRenderPipe.basicPipeTexture;
			if (!renderState.textureMatrix.isHasPower() && renderState.textureMatrix.isRouted()) {
				cornerTexture = LogisticsNewRenderPipe.inactiveTexture;
			} else if (!renderState.textureMatrix.isRouted() && connectionCount > 2) {
				cornerTexture = LogisticsNewRenderPipe.inactiveTexture;
			}
			int count = connectionAtCorner.containsKey(corner) ? connectionAtCorner.get(corner) : 0;
			if (count == 0) {
				for (CCModel model : LogisticsNewRenderPipe.corners_M.get(corner)) {
					objectsToRender.add(new RenderEntry(model, new IVertexOperation[] { cornerTexture }));
				}
			} else if (count == 1) {
				for (PipeTurnCorner turn : PipeTurnCorner.values()) {
					if (turn.corner != corner) {
						continue;
					}
					if (renderState.pipeConnectionMatrix.isConnected(turn.getPointer()) || pipeTile.pipe.hasSpecialPipeEndAt(turn.getPointer())) {
						objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.spacers.get(turn), new IVertexOperation[] { cornerTexture }));
						break;
					}
				}
			} else if (count == 2) {
				for (PipeTurnCorner turn : PipeTurnCorner.values()) {
					if (turn.corner != corner) {
						continue;
					}
					if (!renderState.pipeConnectionMatrix.isConnected(turn.getPointer()) || pipeTile.pipe.hasSpecialPipeEndAt(turn.getPointer())) {
						objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.corners_I.get(turn), new IVertexOperation[] { cornerTexture }));
						break;
					}
				}
			} else if (count == 3) {
				for (CCModel model : LogisticsNewRenderPipe.corners_I3.get(corner)) {
					objectsToRender.add(new RenderEntry(model, new IVertexOperation[] { cornerTexture }));
				}
			}
		}

		for (Edge edge : edgesToRender) {
			objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.edges.get(edge), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));

			/*
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue1(), activeTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue2(), inactiveTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue3(), inactiveTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue4(), activeTexture));
			 */
		}

		for (int i = 0; i < 6; i += 2) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			List<ForgeDirection> list = new ArrayList<ForgeDirection>(Arrays.asList(ForgeDirection.VALID_DIRECTIONS));
			list.remove(dir);
			list.remove(dir.getOpposite());
			if (renderState.pipeConnectionMatrix.isConnected(dir) && renderState.pipeConnectionMatrix.isConnected(dir.getOpposite())) {
				boolean found = false;
				for (ForgeDirection dir2 : list) {
					if (renderState.pipeConnectionMatrix.isConnected(dir2)) {
						found = true;
						break;
					}
				}
				if (!found) {
					switch (dir) {
						case DOWN:
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.EAST_SIDE), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.WEST_SIDE), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.NORTH_SIDE), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.SOUTH_SIDE), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							break;
						case NORTH:
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.EAST_UP), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.WEST_UP), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.UP_SIDE), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.DOWN_SIDE), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							break;
						case WEST:
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.UP_UP), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.DOWN_UP), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.NORTH_UP), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.supports.get(PipeSupport.SOUTH_UP), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
							break;
						default:
							break;
					}
				}
			}
		}

		boolean solidSides[] = new boolean[6];
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			LPPosition pos = new LPPosition((TileEntity) pipeTile);
			pos.moveForward(dir);
			Block blockSide = pos.getBlock(pipeTile.getWorldObj());
			if (blockSide == null || !blockSide.isSideSolid(pipeTile.getWorldObj(), pos.getX(), pos.getY(), pos.getZ(), dir.getOpposite()) || renderState.pipeConnectionMatrix.isConnected(dir)) {
				Iterator<PipeMount> iter = mountCanidates.iterator();
				while (iter.hasNext()) {
					PipeMount mount = iter.next();
					if (mount.dir == dir) {
						iter.remove();
					}
				}
			} else {
				solidSides[dir.ordinal()] = true;
			}
		}

		if (!mountCanidates.isEmpty()) {
			if (solidSides[ForgeDirection.DOWN.ordinal()]) {
				findOponentOnSameSide(mountCanidates, ForgeDirection.DOWN);
			} else if (solidSides[ForgeDirection.UP.ordinal()]) {
				findOponentOnSameSide(mountCanidates, ForgeDirection.UP);
			} else {
				removeFromSide(mountCanidates, ForgeDirection.DOWN);
				removeFromSide(mountCanidates, ForgeDirection.UP);
				if (mountCanidates.size() > 2) {
					removeIfHasOponentSide(mountCanidates);
				}
				if (mountCanidates.size() > 2) {
					removeIfHasConnectedSide(mountCanidates);
				}
				if (mountCanidates.size() > 2) {
					findOponentOnSameSide(mountCanidates, mountCanidates.get(0).dir);
				}
			}

			if (LPConstants.DEBUG && mountCanidates.size() > 2) {
				new RuntimeException("Trying to render " + mountCanidates.size() + " Mounts").printStackTrace();
			}

			for (PipeMount mount : mountCanidates) {
				objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.mounts.get(mount), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
			}
		}

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (!renderState.pipeConnectionMatrix.isConnected(dir)) {
				for (CCModel model : LogisticsNewRenderPipe.texturePlate_Outer.get(dir)) {
					IconTransformation icon = Textures.LPnewPipeIconProvider.getIcon(renderState.textureMatrix.getTextureIndex());
					if (icon != null) {
						objectsToRender.add(new RenderEntry(model, new IVertexOperation[] { icon }));
					}
				}
			}
		}
		if (renderState.textureMatrix.isFluid()) {
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if (!renderState.pipeConnectionMatrix.isConnected(dir)) {
					for (CCModel model : LogisticsNewRenderPipe.texturePlate_Inner.get(dir)) {
						objectsToRender.add(new RenderEntry(model, new IVertexOperation[] { LogisticsNewRenderPipe.glassCenterTexture }));
					}
				} else {
					if (!renderState.textureMatrix.isRoutedInDir(dir)) {
						for (CCModel model : LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue1()) {
							objectsToRender.add(new RenderEntry(model, new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
						}
					}
				}
			}
		}
	}

	private void findOponentOnSameSide(List<PipeMount> mountCanidates, ForgeDirection dir) {
		boolean sides[] = new boolean[6];
		Iterator<PipeMount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			PipeMount mount = iter.next();
			if (mount.dir != dir) {
				iter.remove();
			} else {
				sides[mount.side.ordinal()] = true;
			}
		}
		if (mountCanidates.size() <= 2) {
			return;
		}
		List<ForgeDirection> keep = new ArrayList<ForgeDirection>();
		if (sides[2] && sides[3]) {
			keep.add(ForgeDirection.NORTH);
			keep.add(ForgeDirection.SOUTH);
		} else if (sides[4] && sides[5]) {
			keep.add(ForgeDirection.EAST);
			keep.add(ForgeDirection.WEST);
		} else if (sides[0] && sides[1]) {
			keep.add(ForgeDirection.UP);
			keep.add(ForgeDirection.DOWN);
		}
		iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			PipeMount mount = iter.next();
			if (!keep.contains(mount.side)) {
				iter.remove();
			}
		}
	}

	private void removeFromSide(List<PipeMount> mountCanidates, ForgeDirection dir) {
		Iterator<PipeMount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			PipeMount mount = iter.next();
			if (mount.dir == dir) {
				iter.remove();
			}
		}
	}

	private void reduceToOnePerSide(List<PipeMount> mountCanidates, ForgeDirection dir, ForgeDirection pref) {
		boolean found = false;
		Iterator<PipeMount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			PipeMount mount = iter.next();
			if (mount.dir != dir) {
				continue;
			}
			if (mount.side == pref) {
				found = true;
			}
		}
		if (!found) {
			reduceToOnePerSide(mountCanidates, dir);
		} else {
			iter = mountCanidates.iterator();
			while (iter.hasNext()) {
				PipeMount mount = iter.next();
				if (mount.dir != dir) {
					continue;
				}
				if (mount.side != pref) {
					iter.remove();
				}
			}
		}
	}

	private void reduceToOnePerSide(List<PipeMount> mountCanidates, ForgeDirection dir) {
		boolean found = false;
		Iterator<PipeMount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			PipeMount mount = iter.next();
			if (mount.dir != dir) {
				continue;
			}
			if (found) {
				iter.remove();
			} else {
				found = true;
			}
		}
	}

	private void removeIfHasOponentSide(List<PipeMount> mountCanidates) {
		boolean sides[] = new boolean[6];
		Iterator<PipeMount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			PipeMount mount = iter.next();
			sides[mount.dir.ordinal()] = true;
		}
		if (sides[2] && sides[3]) {
			removeFromSide(mountCanidates, ForgeDirection.EAST);
			removeFromSide(mountCanidates, ForgeDirection.WEST);
			reduceToOnePerSide(mountCanidates, ForgeDirection.NORTH);
			reduceToOnePerSide(mountCanidates, ForgeDirection.SOUTH);
		} else if (sides[4] && sides[5]) {
			removeFromSide(mountCanidates, ForgeDirection.NORTH);
			removeFromSide(mountCanidates, ForgeDirection.SOUTH);
			reduceToOnePerSide(mountCanidates, ForgeDirection.EAST);
			reduceToOnePerSide(mountCanidates, ForgeDirection.WEST);
		}
	}

	private void removeIfHasConnectedSide(List<PipeMount> mountCanidates) {
		boolean sides[] = new boolean[6];
		Iterator<PipeMount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			PipeMount mount = iter.next();
			sides[mount.dir.ordinal()] = true;
		}
		for (int i = 2; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
			if (sides[dir.ordinal()] && sides[rot.ordinal()]) {
				reduceToOnePerSide(mountCanidates, dir, dir.getRotation(ForgeDirection.DOWN));
				reduceToOnePerSide(mountCanidates, rot, rot.getRotation(ForgeDirection.UP));
			}
		}
	}

	public static void renderDestruction(CoreUnroutedPipe pipe, World worldObj, int x, int y, int z, EffectRenderer effectRenderer) {
		if (pipe.container != null && pipe.container.renderState != null && pipe.container.renderState.cachedRenderer != null) {
			for (RenderEntry entry : pipe.container.renderState.cachedRenderer) {
				CCModel model = entry.getModel().twoFacedCopy();
				Cuboid6 bounds = model.bounds();
				double xMid = (bounds.min.x + bounds.max.x) / 2;
				double yMid = (bounds.min.y + bounds.max.y) / 2;
				double zMid = (bounds.min.z + bounds.max.z) / 2;
				model.apply(new Translation(-xMid, -yMid, -zMid));
				effectRenderer.addEffect(new EntityModelFX(worldObj, x + xMid, y + yMid, z + zMid, model, entry.getOperations(), entry.getTexture()));
			}
		}
	}

	public static void renderBoxWithDir(ForgeDirection dir) {
		List<RenderEntry> objectsToRender = new ArrayList<RenderEntry>();
		List<Edge> edgesToRender = new ArrayList<Edge>(Arrays.asList(Edge.values()));
		Map<Corner, Integer> connectionAtCorner = new HashMap<Corner, Integer>();

		for (Edge edge : Edge.values()) {
			if (edge.part1 == dir || edge.part2 == dir) {
				edgesToRender.remove(edge);
			}
		}
		for (Corner corner : Corner.values()) {
			if (corner.ew.dir == dir || corner.ns.dir == dir || corner.ud.dir == dir) {
				if (!connectionAtCorner.containsKey(corner)) {
					connectionAtCorner.put(corner, 1);
				} else {
					connectionAtCorner.put(corner, connectionAtCorner.get(corner) + 1);
				}
			}
		}
		for (Corner corner : Corner.values()) {
			IconTransformation cornerTexture = LogisticsNewRenderPipe.basicPipeTexture;
			int count = connectionAtCorner.containsKey(corner) ? connectionAtCorner.get(corner) : 0;
			if (count == 0) {
				for (CCModel model : LogisticsNewRenderPipe.corners_M.get(corner)) {
					objectsToRender.add(new RenderEntry(model, new IVertexOperation[] { cornerTexture }));
				}
			} else if (count == 1) {
				for (PipeTurnCorner turn : PipeTurnCorner.values()) {
					if (turn.corner != corner) {
						continue;
					}
					if (turn.getPointer() == dir) {
						objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.spacers.get(turn), new IVertexOperation[] { cornerTexture }));
						break;
					}
				}
			} else if (count == 2) {
				for (PipeTurnCorner turn : PipeTurnCorner.values()) {
					if (turn.corner != corner) {
						continue;
					}
					if (turn.getPointer() == dir) {
						objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.corners_I.get(turn), new IVertexOperation[] { cornerTexture }));
						break;
					}
				}
			} else if (count == 3) {
				for (CCModel model : LogisticsNewRenderPipe.corners_I3.get(corner)) {
					objectsToRender.add(new RenderEntry(model, new IVertexOperation[] { cornerTexture }));
				}
			}
		}

		for (Edge edge : edgesToRender) {
			objectsToRender.add(new RenderEntry(LogisticsNewRenderPipe.edges.get(edge), new IVertexOperation[] { LogisticsNewRenderPipe.basicPipeTexture }));
		}
		for (RenderEntry model : objectsToRender) {
			model.getModel().render(model.getOperations());
		}
	}
}

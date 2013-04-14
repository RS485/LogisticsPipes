package logisticspipes.proxy.forestry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.items.ItemModule;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.interfaces.IForestryProxy;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.core.ItemInterface;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IAlleleSpecies;
import forestry.api.recipes.RecipeManagers;

public class ForestryProxy implements IForestryProxy {
	
	public ForestryProxy() {
		boolean initsuccessful = false;
		try {
			tileMachine = Class.forName("forestry.core.gadgets.TileMachine");
			machine_in_TileMachine = tileMachine.getDeclaredField("machine");
			machine_in_TileMachine.setAccessible(true);
			analyserClass = Class.forName("forestry.core.gadgets.MachineAnalyzer");
			Class<?> stringUtil = Class.forName("forestry.core.utils.StringUtil");
			localize = stringUtil.getDeclaredMethod("localize", new Class[]{String.class});
			localize.setAccessible(true);
			propolis = ItemInterface.getItem("propolis").getItem();
			pollen = ItemInterface.getItem("pollen").getItem();
			honey = LiquidDictionary.getLiquid("honey", 1500);	
			initsuccessful = true;
		} catch(Exception e) {
			if(LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
		}
		has_all = initsuccessful;
	}
	
	private Class<?> tileMachine;
	private Field machine_in_TileMachine;
	private Class<?> analyserClass;
	private Method localize;
	private Item propolis;
	private Item pollen;
	private LiquidStack honey;
	private final boolean has_all;

	/**
	 * Checks if item is bee via ItemIdentifier.
	 * @param item ItemIdentifier to check if is bee.
	 * @return Boolean, true if item is bee.
	 */
	@Override
	public boolean isBee(ItemIdentifier item) {
		return isBee(item.unsafeMakeNormalStack(1));
	}

	/**
	 * Checks if item is bee.
	 * @param item ItemStack to check if is bee.
	 * @return Boolean, true if item is bee.
	 */
	@Override
	public boolean isBee(ItemStack item) {
		if(!has_all) return false;
		return BeeManager.beeInterface.isBee(item);
	}

	/**
	 * First checks if item is bee, then returns boolean if its analyzed.
	 * Then it will check if its analyzed.
	 * @param item ItemIdentifier to check if is analyzed bee.
	 * @return Boolean, true if item is analyzed bee.
	 */
	@Override
	public boolean isAnalysedBee(ItemIdentifier item) {
		return isAnalysedBee(item.unsafeMakeNormalStack(1));
	}

	/**
	 * First checks if item is bee, then checks if its analyzed.
	 * @param item ItemStack to check if is analyzed bee.
	 * @return Boolean, true if item is analyzed bee.
	 */
	@Override
	public boolean isAnalysedBee(ItemStack item) {
		if(!isBee(item)) return false;
		return BeeManager.beeInterface.getBee(item).isAnalyzed();
	}
	
	/**
	 * Checks if a passed tile entity is a Forestry Analyzer.
	 * @param tile The TileEntity to check if is Forestry Analyzer.
	 * @return Boolean, true if tile is a Forestry Analyzer.
	 */
	@Override
	public boolean isTileAnalyser(TileEntity tile) {
		if(!has_all) return false;
		try {
			if(tileMachine.isAssignableFrom(tile.getClass())) {
				Object obj = machine_in_TileMachine.get(tile);
				if(analyserClass.isAssignableFrom(obj.getClass())) {
					return true;
				}
			}
		} catch (Exception e) {
			if(LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Checks if Forestry compat was successfully initialized.
	 * @return Boolean, true if Forestry was initialized without any problems.
	 */
	@Override
	public boolean forestryEnabled() {
		return has_all;
	}

	/**
	 * Checks if passed string allele was discovered by the player in passed world.
	 * @param allele The allele as a String.
	 * @param world The world to check in.
	 * @return Boolean, true if allele was discovered in world.
	 */
	@Override
	public boolean isKnownAlleleId(String allele, World world) {
		if(!has_all) return false;
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele) instanceof IAlleleBeeSpecies)) return false;
		if(!((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele)).isSecret()) return true;
		return BeeManager.breedingManager.getApiaristTracker(world, MainProxy.proxy.getClientPlayer().username).isDiscovered((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele));
	}

	/**
	 * Returns a String for the uid passed for allele name.
	 * @param uid The uid as string to get proper name for.
	 * @return String of the actual user-friendly name for the allele.
	 */
	@Override
	public String getAlleleName(String uid) {
		if(!has_all) return "";
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies)) return "";
		return ((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid)).getName();
	}
	
	/**
	 * Returns the first valid allele uid as String.
	 * @param world The world to check in.
	 * @return The first valid allele as uid.
	 */
	private String getFirstValidAllele(World world) {
		for(IAllele allele:AlleleManager.alleleRegistry.getRegisteredAlleles().values()) {
			if(allele instanceof IAlleleBeeSpecies && isKnownAlleleId(allele.getUID(), world)) {
				return allele.getUID();
			}
		}
		return "";
	}
	
	/**
	 * Returns the last valid allele uid as String.
	 * @param world The world to check in.
	 * @return The last valid allele as uid.
	 */
	private String getLastValidAllele(World world) {
		String uid = "";
		for(IAllele allele:AlleleManager.alleleRegistry.getRegisteredAlleles().values()) {
			if(allele instanceof IAlleleBeeSpecies && isKnownAlleleId(allele.getUID(), world)) {
				uid = allele.getUID();
			}
		}
		return uid;
	}

	/**
	 * Returns a String of a uid after the one passed in.
	 * @param uid The uid used as a reference.
	 * @param world The world to check in.
	 * @return String of uid after the one passed in.
	 */
	@Override
	public String getNextAlleleId(String uid, World world) {
		if(!has_all) return "";
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleBeeSpecies)) {
			return getFirstValidAllele(world);
		}
		boolean next = false;
		for(IAllele allele:AlleleManager.alleleRegistry.getRegisteredAlleles().values()) {
			if(allele instanceof IAlleleBeeSpecies) {
				if(next && isKnownAlleleId(allele.getUID(), world)) {
					return allele.getUID();
				} else if(allele.getUID().equals(uid)) {
					next = true;
				}
			}
		}
		return "";
	}
	
	/**
	 * Returns a String of a uid before the one passed in.
	 * @param uid The uid used as a reference.
	 * @param world
	 * @return String of uid before the one passed in.
	 */
	@Override
	public String getPrevAlleleId(String uid, World world) {
		if(!has_all) return "";
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleBeeSpecies)) {
			return getLastValidAllele(world);
		}
		IAllele lastAllele = null;
		for(IAllele allele:AlleleManager.alleleRegistry.getRegisteredAlleles().values()) {
			if(allele instanceof IAlleleBeeSpecies) {
				if(allele.getUID().equals(uid)) {
					if(lastAllele == null) {
						return "";
					}
					return lastAllele.getUID();
				} else if(isKnownAlleleId(allele.getUID(), world)) {
					lastAllele = allele;
				}
			}
		}
		return "";
	}

	/**
	 * Checks if passed ItemStack is bee, then returns its first allele.
	 * @param bee the ItemStack to get the first allele for.
	 * @return String of the first allele of bee.
	 */
	@Override
	public String getFirstAlleleId(ItemStack bee) {
		if(!isBee(bee)) return "";
		return BeeManager.beeInterface.getBee(bee).getGenome().getPrimaryAsBee().getUID();
	}
	
	/**
	 * Checks if passed ItemStack is bee, then returns its second allele.
	 * @param bee the ItemStack to get the second allele for.
	 * @return String of the second allele of bee.
	 */
	@Override
	public String getSecondAlleleId(ItemStack bee) {
		if(!isBee(bee)) return "";
		return BeeManager.beeInterface.getBee(bee).getGenome().getSecondaryAsBee().getUID();
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its a drone.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is a drone.
	 */
	@Override
	public boolean isDrone(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.isDrone(bee);
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its a princess.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is princess.
	 */
	@Override
	public boolean isPrincess(ItemStack bee) {
		if(!isBee(bee)) return false;
		if(isQueen(bee)) return false;
		if(isDrone(bee)) return false;
		return true;
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its a queen.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is queen.
	 */
	@Override
	public boolean isQueen(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.isMated(bee);
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its a purebred.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is a purebred bee.
	 */
	@Override
	public boolean isPurebred(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.SPECIES);
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its nocturnal.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is a nocturnal bee.
	 */
	@Override
	public boolean isNocturnal(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getNocturnal();
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its a purebred nocturnal.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is a purebred nocturnal bee.
	 */
	@Override
	public boolean isPureNocturnal(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getNocturnal() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.NOCTURNAL);
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its a tolerant flyer.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is a tolerant flyer bee.
	 */
	@Override
	public boolean isFlyer(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getTolerantFlyer();
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its a purebred tolerant flyer.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is a purebred tolerant flyer bee.
	 */
	@Override
	public boolean isPureFlyer(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getTolerantFlyer() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.TOLERANT_FLYER);
	}

	/**
	 * Checks if passed ItemStack is bee, then checks if its a cave dweller.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is a cave dweller bee.
	 */
	@Override
	public boolean isCave(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getCaveDwelling();
	}
	
	/**
	 * Checks if passed ItemStack is bee, then checks if its a purebred cave dweller.
	 * @param bee The ItemStack to check.
	 * @return Boolean, true if passed ItemStack is a purebred cave dweller bee.
	 */
	@Override
	public boolean isPureCave(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getCaveDwelling() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.CAVE_DWELLING);
	}

	/**
	 * Returns a special Forestry translation of the passed String.
	 * @param input The String to translate.
	 * @return The translated string.
	 */
	@Override
	public String getForestryTranslation(String input) {
		if(!has_all) return input;
		try {
			return (String) localize.invoke(null, new Object[]{input.toLowerCase()});
		} catch (Exception e) {
			if(LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
			return input;
		}
	}
	
	/**
	 * Void method, called to initialize LogisticsPipes' Forestry recipes.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void addCraftingRecipes() {
		if(!has_all) return;
		
		/* Carpenter recipes */
		RecipeManagers.carpenterManager.addRecipe(25, honey, new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER), new Object[] { 
			"CGC", 
			"r r", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), BuildCraftCore.ironGearItem, 
			Character.valueOf('r'), Item.redstone, 
		});
		
		RecipeManagers.carpenterManager.addRecipe(25, honey, new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER), new Object[] { 
			"CGC", 
			"r r", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1),
			Character.valueOf('r'), Item.redstone, 
		});
		
		RecipeManagers.carpenterManager.addRecipe(25, honey, new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEESINK), new Object[] { 
			"CrC", 
			"r r", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('r'), Item.redstone, 
		});
				
		RecipeManagers.carpenterManager.addRecipe(25, honey, new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.APIARISTREFILLER), new Object[] {
			" p ",
			"r r",
			"CwC",
			Character.valueOf('p'), pollen,
			Character.valueOf('C'), propolis,
			Character.valueOf('w'), BuildCraftTransport.pipeItemsWood,
			Character.valueOf('r'), Item.redstone,
		});
		
		RecipeManagers.carpenterManager.addRecipe(25, honey, new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.APIARISTTERMINUS), new Object[] { 
			"CGD", 
			"r r", 
			"DrC", 
			Character.valueOf('C'), "dyeBlack",
			Character.valueOf('D'), "dyePurple",
			Character.valueOf('G'), pollen, 
			Character.valueOf('r'), Item.redstone, 
		});
		
		
		
		RecipeManagers.carpenterManager.addRecipe(25, honey, new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0), new ItemStack(LogisticsPipes.LogisticsApiaristAnalyserPipe, 1, 0), new Object[] { 
			"CGC", 
			"r r", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), BuildCraftCore.ironGearItem, 
			Character.valueOf('r'), Item.redstone, 
		});
		
		RecipeManagers.carpenterManager.addRecipe(25, honey, new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0), new ItemStack(LogisticsPipes.LogisticsApiaristAnalyserPipe, 1, 0), new Object[] { 
			"CGC", 
			"r r", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
			Character.valueOf('r'), Item.redstone, 
		});
		
		RecipeManagers.carpenterManager.addRecipe(25, honey, new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0), new ItemStack(LogisticsPipes.LogisticsApiaristSinkPipe, 1, 0), new Object[] { 
			"CrC", 
			"r r", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('r'), Item.redstone, 
		});
		
		if (Configs.MANDATORY_CARPENTER_RECIPES) return;
				
		/* Regular recipes */
		CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), BuildCraftCore.ironGearItem, 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEESINK), new Object[] { 
			"CrC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK)
		});
		
		CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.APIARISTREFILLER), new Object[] {
			" p ",
			"rBr",
			"CwC",
			Character.valueOf('p'), pollen,
			Character.valueOf('C'), propolis,
			Character.valueOf('w'), BuildCraftTransport.pipeItemsWood,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK),
		});

		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.APIARISTTERMINUS), new Object[] { 
			"CGD", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), "dyeBlack",
			Character.valueOf('D'), "dyePurple",
			Character.valueOf('G'), pollen, 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		}));
		
		CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.LogisticsApiaristAnalyserPipe, 1, 0), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), BuildCraftCore.ironGearItem, 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0)
		});
		
		CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.LogisticsApiaristAnalyserPipe, 1, 0), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0)
		});
		
		CraftingManager.getInstance().addRecipe(new ItemStack(LogisticsPipes.LogisticsApiaristSinkPipe, 1, 0), new Object[] { 
			"CrC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0)
		});

		//ModLoader.addShapelessRecipe(new ItemStack(LogisticsPipes.LogisticsApiaristAnalyserPipe, 1, 0), new Object[]{new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER),new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0)});
		//ModLoader.addShapelessRecipe(new ItemStack(LogisticsPipes.LogisticsApiaristSinkPipe, 1, 0), new Object[]{new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEESINK),new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0)});
	}
	
	/**
	 * Used to get an icon index for a given allele.
	 * @param uid The uid String of the allele to get icon index for.
	 * @param phase special phase of the bee.
	 */
	@Override
	public int getIconIndexForAlleleId(String uid, int phase) {
		if(!has_all) return 0;
		if (!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies))
			return 0;
		IAlleleSpecies species = (IAlleleSpecies) forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid);
		int indexOffset = 0;
		if (species != null) {
			indexOffset = 16 * species.getHumidity().getIconIndex();
		}
		if (phase == 0)
			return indexOffset + 0 + 2;
		if (phase == 1) {
			return indexOffset + 3 + 2;
		}
		return indexOffset + 6 + 2;
	}

	/**
	 * Used to get an color as int for a given allele.
	 * @param uid The uid String of the allele to get color for.
	 * @param phase special phase of the bee.
	 */
	@Override
	public int getColorForAlleleId(String uid, int phase) {
		if(!has_all) return 0;
		if (!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies))
			return 0;
		IAlleleSpecies species = (IAlleleSpecies) forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid);
		//@TODO: fixme
		/*if (species != null) {
			if (phase == 0)
				return species.getPrimaryColor();
			if (phase == 1) {
				return species.getSecondaryColor();
			}
			return 16777215;
		}*/

		return 16777215;
	}

	/**
	 * Returns the number of render passes for given allele.
	 * @param uid The uid of the allele.
	 * @return The number of render passes for the allele.
	 */
	@Override
	public int getRenderPassesForAlleleId(String uid) {
		return 3;
	}

}

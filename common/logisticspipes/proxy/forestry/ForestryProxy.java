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
import forestry.api.genetics.IApiaristTracker;
import forestry.api.recipes.RecipeManagers;

public class ForestryProxy implements IForestryProxy {
	
	public ForestryProxy() {
		boolean initsuccessful = false;
		try {
			tileMachine = Class.forName("forestry.core.gadgets.TileMachine");
			machine_in_TileMachine = tileMachine.getDeclaredField("machine");
			machine_in_TileMachine.setAccessible(true);
			analyserClass = Class.forName("forestry.apiculture.gadgets.MachineAnalyzer");
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

	@Override
	public boolean isBee(ItemIdentifier item) {
		return isBee(item.makeNormalStack(1));
	}

	@Override
	public boolean isBee(ItemStack item) {
		if(!has_all) return false;
		return BeeManager.beeInterface.isBee(item);
	}

	@Override
	public boolean isAnalysedBee(ItemIdentifier item) {
		return isAnalysedBee(item.makeNormalStack(1));
	}

	@Override
	public boolean isAnalysedBee(ItemStack item) {
		if(!isBee(item)) return false;
		return BeeManager.beeInterface.getBee(item).isAnalyzed();
	}
	
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

	@Override
	public boolean forestryEnabled() {
		return has_all;
	}

	@Override
	public boolean isKnownAlleleId(String allele, World world) {
		if(!has_all) return false;
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele) instanceof IAlleleBeeSpecies)) return false;
		if(!((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele)).isSecret()) return true;

		// ugly clientside hack for forestry 1.6/1.7 api breakage
		IApiaristTracker apiaristTracker = null;
		try {
			Class<?> c = Class.forName("forestry.api.apiculture.IBreedingManager");
			Method getApiaristTracker = c.getMethod("getApiaristTracker", World.class);
			apiaristTracker = (IApiaristTracker) getApiaristTracker.invoke(BeeManager.breedingManager, world);
		} catch (Exception e) {
			if(LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
			try {
				Class<?> c = Class.forName("forestry.api.apiculture.IBreedingManager");
				Method getApiaristTracker = c.getMethod("getApiaristTracker", World.class, String.class);
				apiaristTracker = (IApiaristTracker) getApiaristTracker.invoke(BeeManager.breedingManager, world, MainProxy.proxy.getClientPlayer().username);
			} catch (Exception e1) {
				if(LogisticsPipes.DEBUG) {
					e1.printStackTrace();
				}
			}
		}
		if(apiaristTracker == null) {
			return false;
		}
		return apiaristTracker.isDiscovered((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele));
	}

	@Override
	public String getAlleleName(String uid) {
		if(!has_all) return "";
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies)) return "";
		return ((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid)).getName();
	}
	
	private String getFirstValidAllele(World world) {
		for(IAllele allele:AlleleManager.alleleRegistry.getRegisteredAlleles().values()) {
			if(allele instanceof IAlleleBeeSpecies && isKnownAlleleId(allele.getUID(), world)) {
				return allele.getUID();
			}
		}
		return "";
	}
	
	private String getLastValidAllele(World world) {
		String uid = "";
		for(IAllele allele:AlleleManager.alleleRegistry.getRegisteredAlleles().values()) {
			if(allele instanceof IAlleleBeeSpecies && isKnownAlleleId(allele.getUID(), world)) {
				uid = allele.getUID();
			}
		}
		return uid;
	}

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

	@Override
	public String getFirstAlleleId(ItemStack bee) {
		if(!isBee(bee)) return "";
		return BeeManager.beeInterface.getBee(bee).getGenome().getPrimaryAsBee().getUID();
	}

	@Override
	public String getSecondAlleleId(ItemStack bee) {
		if(!isBee(bee)) return "";
		return BeeManager.beeInterface.getBee(bee).getGenome().getSecondaryAsBee().getUID();
	}

	@Override
	public boolean isDrone(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.isDrone(bee);
	}

	@Override
	public boolean isPrincess(ItemStack bee) {
		if(!isBee(bee)) return false;
		if(isQueen(bee)) return false;
		if(isDrone(bee)) return false;
		return true;
	}

	@Override
	public boolean isQueen(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.isMated(bee);
	}

	@Override
	public boolean isPurebred(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.SPECIES);
	}

	@Override
	public boolean isNocturnal(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getNocturnal();
	}

	@Override
	public boolean isPureNocturnal(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getNocturnal() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.NOCTURNAL);
	}

	@Override
	public boolean isFlyer(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getTolerantFlyer();
	}

	@Override
	public boolean isPureFlyer(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getTolerantFlyer() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.TOLERANT_FLYER);
	}

	@Override
	public boolean isCave(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getCaveDwelling();
	}

	@Override
	public boolean isPureCave(ItemStack bee) {
		if(!isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getCaveDwelling() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.CAVE_DWELLING);
	}

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
		CraftingManager.getInstance().func_92051_a(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), BuildCraftCore.ironGearItem, 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		CraftingManager.getInstance().func_92051_a(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		CraftingManager.getInstance().func_92051_a(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BEESINK), new Object[] { 
			"CrC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK)
		});
		
		CraftingManager.getInstance().func_92051_a(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.APIARISTREFILLER), new Object[] {
			" p ",
			"rBr",
			"CwC",
			Character.valueOf('p'), pollen,
			Character.valueOf('C'), propolis,
			Character.valueOf('w'), BuildCraftTransport.pipeItemsWood,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK),
		});

		CraftingManager.getInstance().func_92051_a(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.APIARISTTERMINUS), new Object[] { 
			"CGD", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), "dyeBlack",
			Character.valueOf('D'), "dyePurple",
			Character.valueOf('G'), pollen, 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		CraftingManager.getInstance().func_92051_a(new ItemStack(LogisticsPipes.LogisticsApiaristAnalyserPipe, 1, 0), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), BuildCraftCore.ironGearItem, 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0)
		});
		
		CraftingManager.getInstance().func_92051_a(new ItemStack(LogisticsPipes.LogisticsApiaristAnalyserPipe, 1, 0), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 1), 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1, 0)
		});
		
		CraftingManager.getInstance().func_92051_a(new ItemStack(LogisticsPipes.LogisticsApiaristSinkPipe, 1, 0), new Object[] { 
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
	
	@Override
	public int getIconIndexForAlleleId(String uid, int phase) {
		if(!has_all) return 0;
		if (!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies))
			return 0;
		IAlleleSpecies species = (IAlleleSpecies) forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid);
		int indexOffset = 0;
		if (species != null) {
			indexOffset = 16 * species.getBodyType();
		}
		if (phase == 0)
			return indexOffset + 0 + 2;
		if (phase == 1) {
			return indexOffset + 3 + 2;
		}
		return indexOffset + 6 + 2;
	}

	@Override
	public int getColorForAlleleId(String uid, int phase) {
		if(!has_all) return 0;
		if (!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies))
			return 0;
		IAlleleSpecies species = (IAlleleSpecies) forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid);
		if (species != null) {
			if (phase == 0)
				return species.getPrimaryColor();
			if (phase == 1) {
				return species.getSecondaryColor();
			}
			return 16777215;
		}

		return 16777215;
	}

	@Override
	public int getRenderPassesForAlleleId(String uid) {
		return 3;
	}

}

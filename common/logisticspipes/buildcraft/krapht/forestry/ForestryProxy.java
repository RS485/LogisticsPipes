package logisticspipes.buildcraft.krapht.forestry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import logisticspipes.buildcraft.logisticspipes.items.ItemModule;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.krapht.ItemIdentifier;
import buildcraft.BuildCraftCore;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAlleleSpecies;

public class ForestryProxy implements IForestryProxy {
	
	public ForestryProxy() {
		try {
			tileMachine = Class.forName("forestry.core.gadgets.TileMachine");
			machine_in_TileMachine = tileMachine.getDeclaredField("machine");
			machine_in_TileMachine.setAccessible(true);
			analyserClass = Class.forName("forestry.apiculture.MachineAnalyzer");
			enumFilterTypeClass = Class.forName("forestry.pipes.EnumFilterType");
			getBeeType = enumFilterTypeClass.getDeclaredMethod("getType", new Class[]{ItemStack.class});
			getBeeType.setAccessible(true);
			stringUtil = Class.forName("forestry.core.utils.StringUtil");
			localize = stringUtil.getDeclaredMethod("localize", new Class[]{String.class});
			localize.setAccessible(true);
			ForestryItem = Class.forName("forestry.core.config.ForestryItem");
			beeDroneGEField = ForestryItem.getDeclaredField("beeDroneGE");
			beeDroneGEField.setAccessible(true);
			beeDroneGE = (Item) beeDroneGEField.get(null);
			propolisField = ForestryItem.getDeclaredField("propolis");
			propolisField.setAccessible(true);
			propolis = (Item) propolisField.get(null);
			has_all = true;
		} catch(Exception e) {
			if(mod_LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
		}
	}
	
	private Class<?> tileMachine;
	private Field machine_in_TileMachine;
	private Class<?> analyserClass;
	private Class<?> enumFilterTypeClass;
	private Method getBeeType;
	private Class<?> stringUtil;
	private Method localize;
	private Class<?> ForestryItem;
	private Field beeDroneGEField;
	private Item beeDroneGE;
	private Field propolisField;
	private Item propolis;
	private boolean has_all;

	@Override
	public boolean isBee(ItemIdentifier item) {
		return isBee(item.makeNormalStack(1));
	}

	@Override
	public boolean isBee(ItemStack item) {
		return BeeManager.beeInterface.isBee(item);
	}

	@Override
	public boolean isAnalysedBee(ItemIdentifier item) {
		return isAnalysedBee(item.makeNormalStack(1));
	}

	@Override
	public boolean isAnalysedBee(ItemStack item) {
		if(!BeeManager.beeInterface.isBee(item)) {
			return false;
		}
		return BeeManager.beeInterface.getBee(item).isAnalyzed();
	}
	
	public int getBeeAlleleCount() {
		int counter = 0;
		for(int i=0;i<AlleleManager.alleleRegistry.getRegisteredAlleles().size();i++) {
			if(AlleleManager.alleleRegistry.getRegisteredAlleles().get(i) instanceof IAlleleSpecies) counter++;
		}
		return counter;
	}
	
	@Override
	public boolean isTileAnalyser(TileEntity tile) {
		if(!has_all) {
			return false;
		}
		try {
			if(tileMachine.isAssignableFrom(tile.getClass())) {
				Object obj = machine_in_TileMachine.get(tile);
				if(analyserClass.isAssignableFrom(obj.getClass())) {
					return true;
				}
			}
		} catch (Exception e) {
			if(mod_LogisticsPipes.DEBUG) {
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
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele) instanceof IAlleleSpecies)) return false;
		if(!((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele)).isSecret()) return true;
		return BeeManager.breedingManager.getApiaristTracker(world).isDiscovered((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(allele));
	}

	@Override
	public String getAlleleName(String uid) {
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies)) return "";
		return ((IAlleleSpecies)forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid)).getName();
	}
	
	private String getFirstValidAllele() {
		for(int i=0;i<AlleleManager.alleleRegistry.getRegisteredAlleles().size();i++) {
			if(AlleleManager.alleleRegistry.getRegisteredAlleles().get(i) instanceof IAlleleSpecies) {
				return AlleleManager.alleleRegistry.getRegisteredAlleles().get(i).getUID();
			}
		}
		return "";
	}
	
	@Override
	public String getNextAlleleId(String uid) {
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies)) { 
			return getFirstValidAllele();
		}
		int index = 0;
		for(int i=0;i<AlleleManager.alleleRegistry.getRegisteredAlleles().size();i++) {
			if(AlleleManager.alleleRegistry.getRegisteredAlleles().get(i) instanceof IAlleleSpecies) {
				if(AlleleManager.alleleRegistry.getRegisteredAlleles().get(i).getUID().equals(uid)) {
					index = i;
					break;
				}
			}
		}
		for(int i=0;i<AlleleManager.alleleRegistry.getRegisteredAlleles().size();i++) {
			if(AlleleManager.alleleRegistry.getRegisteredAlleles().get(i) instanceof IAlleleSpecies) {
				if(index < i) {
					return AlleleManager.alleleRegistry.getRegisteredAlleles().get(i).getUID();
				}
			}
		}
		return "";
	}
	
	@Override
	public String getPrevAlleleId(String uid) {
		if(!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies)) { 
			return getFirstValidAllele();
		}
		int index = 0;
		for(int i=0;i<AlleleManager.alleleRegistry.getRegisteredAlleles().size();i++) {
			if(AlleleManager.alleleRegistry.getRegisteredAlleles().get(i) instanceof IAlleleSpecies) {
				if(AlleleManager.alleleRegistry.getRegisteredAlleles().get(i).getUID().equals(uid)) {
					index = i;
					break;
				}
			}
		}
		for(int i=AlleleManager.alleleRegistry.getRegisteredAlleles().size();i>=0;i--) {
			if(AlleleManager.alleleRegistry.getRegisteredAlleles().get(i) instanceof IAlleleSpecies) {
				if(index > i) {
					return AlleleManager.alleleRegistry.getRegisteredAlleles().get(i).getUID();
				}
			}
		}
		return "";
	}

	@Override
	public String getFirstAlleleId(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return ""; 
		return BeeManager.beeInterface.getBee(bee).getGenome().getPrimaryAsBee().getUID();
	}

	@Override
	public String getSecondAlleleId(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return ""; 
		return BeeManager.beeInterface.getBee(bee).getGenome().getSecondaryAsBee().getUID();
	}

	@Override
	public boolean isDrone(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		return BeeManager.beeInterface.isDrone(bee);
	}

	@Override
	public boolean isPrincess(ItemStack bee) {
		if(!has_all) return false;
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		try {
			Object answer = getBeeType.invoke(null, new Object[]{bee});
			Enum enumAnswer = (Enum) answer;
			return enumAnswer.name().equals("PRINCESS");
		} catch (Exception e) {
			if(mod_LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public boolean isQueen(ItemStack bee) {
		if(!has_all) return false;
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		try {
			Object answer = getBeeType.invoke(null, new Object[]{bee});
			Enum enumAnswer = (Enum) answer;
			return enumAnswer.name().equals("QUEEN");
		} catch (Exception e) {
			if(mod_LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public boolean isPurebred(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.SPECIES);
	}

	@Override
	public boolean isNocturnal(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getNocturnal();
	}

	@Override
	public boolean isPureNocturnal(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getNocturnal() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.NOCTURNAL);
	}

	@Override
	public boolean isFlyer(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getTolerantFlyer();
	}

	@Override
	public boolean isPureFlyer(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getTolerantFlyer() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.TOLERANT_FLYER);
	}

	@Override
	public boolean isCave(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getCaveDwelling();
	}

	@Override
	public boolean isPureCave(ItemStack bee) {
		if(!BeeManager.beeInterface.isBee(bee)) return false;
		return BeeManager.beeInterface.getBee(bee).getGenome().getCaveDwelling() && BeeManager.beeInterface.getBee(bee).isPureBred(EnumBeeChromosome.CAVE_DWELLING);
	}

	@Override
	public String getForestryTranslation(String input) {
		if(!has_all) return input;
		try {
			return (String) localize.invoke(null, new Object[]{input.toLowerCase()});
		} catch (Exception e) {
			if(mod_LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
			return input;
		}
	}

	@Override
	public void addCraftingRecipes() {
		if(!has_all) return;
		ModLoader.addRecipe(new ItemStack(mod_LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER), new Object[] { "CGC", "rBr", "CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('G'), BuildCraftCore.ironGearItem, 
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(mod_LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)});
		ModLoader.addRecipe(new ItemStack(mod_LogisticsPipes.ModuleItem, 1, ItemModule.BEESINK), new Object[] { "CrC", "rBr", "CrC", 
			Character.valueOf('C'), propolis,
			Character.valueOf('r'), Item.redstone, 
			Character.valueOf('B'), new ItemStack(mod_LogisticsPipes.ModuleItem, 1, ItemModule.ITEMSINK)});
		ModLoader.addShapelessRecipe(new ItemStack(mod_LogisticsPipes.LogisticsApiaristAnalyserPipe, 1, 0), new Object[]{new ItemStack(mod_LogisticsPipes.ModuleItem, 1, ItemModule.BEEANALYZER),new ItemStack(mod_LogisticsPipes.LogisticsBasicPipe, 1, 0)});
		ModLoader.addShapelessRecipe(new ItemStack(mod_LogisticsPipes.LogisticsApiaristSinkPipe, 1, 0), new Object[]{new ItemStack(mod_LogisticsPipes.ModuleItem, 1, ItemModule.BEESINK),new ItemStack(mod_LogisticsPipes.LogisticsBasicPipe, 1, 0)});
	}
	
	@Override
	public int getIconIndexForAlleleId(String uid, int phase) {
		if (!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies))
			return 0;
		IAlleleSpecies species = (IAlleleSpecies) forestry.api.genetics.AlleleManager.alleleRegistry
				.getAllele(uid);
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
		if (!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies))
			return 0;
		IAlleleSpecies species = (IAlleleSpecies) forestry.api.genetics.AlleleManager.alleleRegistry
				.getAllele(uid);
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

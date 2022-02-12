package com.elisis.gtnhlanth.common.tileentity;

import static com.elisis.gtnhlanth.util.DescTextLocalization.BLUEPRINT_INFO;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlockAdder;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_OIL_CRACKER;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_OIL_CRACKER_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_OIL_CRACKER_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_OIL_CRACKER_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.casingTexturePages;
import static gregtech.api.util.GT_StructureUtility.ofHatchAdder;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import com.elisis.gtnhlanth.loader.RecipeAdder;
import com.elisis.gtnhlanth.util.DescTextLocalization;
import com.github.bartimaeusnek.bartworks.common.loaders.ItemRegistry;
import com.github.bartimaeusnek.bartworks.util.BW_Util;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.GregTech_API;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Log;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class DissolutionTank extends GT_MetaTileEntity_EnhancedMultiBlockBase<DissolutionTank> implements IConstructable {
    
	private IStructureDefinition<DissolutionTank> multiDefinition = StructureDefinition.<DissolutionTank>builder()
		.addShape(mName, transpose(new String[][] {
			{"-sss-", "sssss", "sssss", "sssss", "-sss-"},
			{"sgggs", "g---g", "g---g", "g---g", "sgggs"},
			{"sgggs", "g---g", "g---g", "g---g", "sgggs"},
			{"ss~ss", "shhhs", "shhhs", "shhhs", "sssss"},
			{"s---s", "-----", "-----", "-----", "s---s"}
		}))
		.addElement('s', ofChain(
				ofHatchAdder(DissolutionTank::addInputToMachineList, 49, 1),
				ofHatchAdder(DissolutionTank::addOutputToMachineList, 49, 1),
				ofHatchAdder(DissolutionTank::addEnergyInputToMachineList, 49, 1),
				ofHatchAdder(DissolutionTank::addMaintenanceToMachineList, 49, 1),
				ofHatchAdder(DissolutionTank::addMufflerToMachineList, 49, 1),
				ofBlock(GregTech_API.sBlockCasings4, 1)
			))
		.addElement('h', ofBlock(GregTech_API.sBlockCasings1, 11))
		.addElement('g', ofBlockAdder(DissolutionTank::addGlass, ItemRegistry.bw_glasses[0], 1))
		
		
		.build();
		
	
				
				
		
		
		
	
	
	public DissolutionTank(String name) {
    	super(name);
    }

	public DissolutionTank(int id, String name, String nameRegional) {
		super(id, name, nameRegional);
	}

	
	@Override
    public IStructureDefinition<DissolutionTank> getStructureDefinition(){	
		return multiDefinition;
	}
	
	@Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return checkPiece(mName, 2, 3, 0);
    }
	
	@Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }
	
	private boolean addGlass(Block block, int meta) {
		if (block != ItemRegistry.bw_glasses[0]) {
			return false;
		}
		else {
			return true;
		}
		
	}
	
	@Override
	public boolean checkRecipe(ItemStack itemStack) {
		
		GT_Log.out.print("in checkRecipe");
		
		ArrayList<FluidStack> tFluidInputs = this.getStoredFluids();
		FluidStack[] tFluidInputArray = tFluidInputs.toArray(new FluidStack[0]);
		ItemStack[] tItems = this.getStoredInputs().toArray(new ItemStack[0]);
		long tVoltage = this.getMaxInputVoltage();
		
		
		for (GT_Recipe aRecipe : RecipeAdder.instance.DissolutionTankRecipes.mRecipeList) {
			GT_Log.out.print("input: " + aRecipe.mFluidInputs[0] + "\n");
			GT_Log.out.print("output: " + aRecipe.mFluidOutputs[0] + "\n");
		}
		
		GT_Log.out.print("tFluidInputArray " + Arrays.toString(tFluidInputArray));
		
		
		//Collection<GT_Recipe> tRecipes = RecipeAdder.instance.DigesterRecipes.mRecipeList;
		GT_Recipe tRecipe = RecipeAdder.instance.DissolutionTankRecipes.findRecipe(
				this.getBaseMetaTileEntity(), 
				this.doTickProfilingInThisTick, 
				tVoltage, 
				tFluidInputArray
			);
		
		if (tRecipe == null)
			return false;

		GT_Log.out.print("Recipe not null\n");
		if (tRecipe.isRecipeInputEqual(true, tFluidInputArray)) {
			
			this.mEfficiency = (10000 - (this.getIdealStatus() - this.getRepairStatus()) * 1000);
			this.mEfficiencyIncrease = 10000;
			this.calculateOverclockedNessMulti(tRecipe.mEUt, tRecipe.mDuration, 1, tVoltage);
			
			if (mMaxProgresstime == Integer.MAX_VALUE - 1 && this.mEUt == Integer.MAX_VALUE - 1)
				return false;
			
			if (this.mEUt > 0)
				this.mEUt = (-this.mEUt);	
			
			FluidStack majorGenericFluid = tRecipe.mFluidInputs[0];
			FluidStack minorGenericFluid = tRecipe.mFluidOutputs[1];
			
			FluidStack majorInput = null;
			FluidStack minorInput = null;
			
			FluidStack fluidInputOne = tFluidInputs.get(0);
			FluidStack fluidInputTwo = tFluidInputs.get(1);
			
			majorInput = (fluidInputOne.getUnlocalizedName() == majorGenericFluid.getUnlocalizedName() ? fluidInputOne : fluidInputTwo);
			if (fluidInputOne.getUnlocalizedName() == majorGenericFluid.getUnlocalizedName()) {
				if (fluidInputTwo.getUnlocalizedName() == minorGenericFluid.getUnlocalizedName()) {
					majorInput = fluidInputOne;
					minorInput = fluidInputTwo;
				}
				else
					return false; // No valid other input

			} else if (fluidInputTwo.getUnlocalizedName() == majorGenericFluid.getUnlocalizedName()) {
				if (fluidInputOne.getUnlocalizedName() == minorGenericFluid.getUnlocalizedName()) {
					majorInput = fluidInputTwo;
					minorInput = fluidInputOne;
				}
				else
					return false;
				
			}
			else
				return false;
			
			/*
			for (FluidStack fluid : tFluidInputs) {
				String name = fluid.getUnlocalizedName();
				if (name == majorGenericFluid.getUnlocalizedName())
					majorInput = fluid;
				
				else if (name == minorGenericFluid.getUnlocalizedName())
					minorInput = fluid;
			}
			*/
			if (majorInput == null || minorInput == null)
				return false;
			
			GT_Log.out.print("major " + majorInput.getLocalizedName());
			GT_Log.out.print("minor " + minorInput.getLocalizedName());
			
			if ((majorInput.amount / tRecipe.mSpecialValue) != (minorInput.amount))
				return false;
			
			this.mOutputFluids = new FluidStack[] {
					tRecipe.getFluidOutput(0)
			};
			return true;
		}
		return false;
	}
	
	@Override
	public int getMaxEfficiency(ItemStack itemStack) {
		return 10000;
	}
	
	@Override
    public Object getClientGUI(int id, InventoryPlayer playerInventory, IGregTechTileEntity metaTileEntity) {
        return new GT_GUIContainer_MultiMachine(playerInventory, metaTileEntity, getLocalName(), "DissolutionTank.png");
    }
	
	@Override
    public String[] getDescription() {
		final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("Heat Exchanger")
                .addInfo("Controller Block for the Large Heat Exchanger")
                .addInfo("More complicated than a Fusion Reactor. Seriously")
                .addInfo("Inputs are Hot Coolant or Lava")
                .addInfo("Outputs Coolant or Pahoehoe Lava and SH Steam/Steam")
                .addInfo("Read the wiki article to understand how it works")
                .addInfo("Then go to the Discord to understand the wiki")
                .addSeparator()
                .beginStructureBlock(3, 4, 3, false)
                .addController("Front bottom")
                .addCasingInfo("Stable Titanium Machine Casing", 20)
                .addOtherStructurePart("Titanium Pipe Casing", "Center 2 blocks")
                .addMaintenanceHatch("Any casing", 1)
                .addInputHatch("Hot fluid, bottom center", 2)
                .addInputHatch("Distilled water, any casing", 1)
                .addOutputHatch("Cold fluid, top center", 3)
                .addOutputHatch("Steam/SH Steam, any casing", 1)
                .toolTipFinisher("Gregtech");
		if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
	        return tt.getInformation();
	    } else {
	        return tt.getStructureInformation();
	    }
    }
	
	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity arg0) {
		return new DissolutionTank(this.mName);
	}
	
	@Override
	public void construct(ItemStack itemStack, boolean b) {
		buildPiece(mName, itemStack, b, 2, 3, 0);
		
	}
	
	@Override
	public String[] getStructureDescription(ItemStack arg0) {
		return DescTextLocalization.addText("DissolutionTank.hint", 4);
	}

	@Override
	public ITexture[] getTexture(IGregTechTileEntity te, byte side, byte facing, byte colorIndex, boolean active,
			boolean redstone) {
		
		if (side == facing) {
            if (active) return new ITexture[]{casingTexturePages[0][49],
                    TextureFactory.builder().addIcon(OVERLAY_FRONT_OIL_CRACKER_ACTIVE).extFacing().build(),
                    TextureFactory.builder().addIcon(OVERLAY_FRONT_OIL_CRACKER_ACTIVE_GLOW).extFacing().glow().build()};
            return new ITexture[]{casingTexturePages[0][49],
                    TextureFactory.builder().addIcon(OVERLAY_FRONT_OIL_CRACKER).extFacing().build(),
                    TextureFactory.builder().addIcon(OVERLAY_FRONT_OIL_CRACKER_GLOW).extFacing().glow().build()};
        }
        return new ITexture[]{casingTexturePages[0][49]};
        
	}

	@Override
	protected GT_Multiblock_Tooltip_Builder createTooltip() {
		final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("Dissolution Tank")
                .addInfo("Controller block for the Dissolution Tank")
                .addInfo(BLUEPRINT_INFO)
                .addSeparator()
                .addController("Front bottom")
                .addInputHatch("Hint block with dot 1")
                .addInputBus("Hint block with dot 1")
                .addOutputHatch("Hint block with dot 2")
                .addOutputBus("Hint block with dot 2")
                .addMaintenanceHatch("Hint block with dot 2")
                .toolTipFinisher("GTNH: Lanthanides");
        
        return tt;
	}

	@Override
	public boolean explodesOnComponentBreak(ItemStack arg0) {
		return false;
	}

	@Override
	public int getDamageToComponent(ItemStack arg0) {
		return 0;
	}

	    

}

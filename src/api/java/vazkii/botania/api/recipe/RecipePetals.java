/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 22, 2014, 2:02:44 PM (GMT)]
 */
package vazkii.botania.api.recipe;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class RecipePetals {

	private final ItemStack output;
	private final ImmutableList<Object> inputs;

	public RecipePetals(ItemStack output, Object... inputs) {
		this.output = output;

		ImmutableList.Builder<Object> inputsToSet = ImmutableList.builder();
		for(Object obj : inputs) {
			if(obj instanceof String || obj instanceof ItemStack)
				inputsToSet.add(obj);
			else throw new IllegalArgumentException("Invalid input");
		}

		this.inputs = inputsToSet.build();
	}

	public boolean matches(IItemHandler inv) {
		List<Object> inputsMissing = new ArrayList<>(inputs);

		for(int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack.isEmpty())
				break;

			int stackIndex = -1, oredictIndex = -1;

			for(int j = 0; j < inputsMissing.size(); j++) {
				Object input = inputsMissing.get(j);
				if(input instanceof String) {
					boolean found = false;
					for(ItemStack ostack : OreDictionary.getOres((String) input, false)) {
						if(OreDictionary.itemMatches(ostack, stack, false)) {
							oredictIndex = j;
							found = true;
							break;
						}
					}


					if(found)
						break;
				} else if(input instanceof ItemStack && compareStacks((ItemStack) input, stack)) {
					stackIndex = j;
					break;
				}
			}

			if(stackIndex != -1)
				inputsMissing.remove(stackIndex);
			else if(oredictIndex != -1)
				inputsMissing.remove(oredictIndex);
			else return false;
		}

		return inputsMissing.isEmpty();
	}

	private boolean compareStacks(ItemStack recipe, ItemStack supplied) {
		if(recipe.getItem() == supplied.getItem() && recipe.getItemDamage() == supplied.getItemDamage()) {
			//check that the user supplied nbt tag is a superset of the recipe item nbt tag
			//if the recipe doesn't have an NBT tag, the user supplied one doesn't matter, it is a superset
			if(!recipe.hasTagCompound()) return true;
			//if the recipe does have an NBT tag but the user supplied doesn't, also no way it's a superset
			if(!supplied.hasTagCompound()) return false;
			
			NBTTagCompound mergedNBT = supplied.getTagCompound().copy();
			mergedNBT.merge(recipe.getTagCompound());
			return supplied.getTagCompound().equals(mergedNBT);
		}
		
		return false;
	}

	public List<Object> getInputs() {
		return inputs;
	}

	public ItemStack getOutput() {
		return output;
	}

}

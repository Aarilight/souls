package com.kallgirl.souls.common.item;

import com.kallgirl.souls.common.ModObjects;
import com.kallgirl.souls.common.Config;
import com.kallgirl.souls.common.util.MobTarget;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class Soulbook extends Item {
	public static int getContainedEssence(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("ContainedEssence", 1)) {
			return tag.getByte("ContainedEssence") - Byte.MIN_VALUE;
		}
		return 0;
	}
	public static ItemStack setContainedEssence(ItemStack stack, int count) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) {
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
		}
		tag.setByte("ContainedEssence", (byte)(count + Byte.MIN_VALUE));
		return stack;
	}
	public Soulbook() {
		super("soulbook", 1);
		glint = true;
		addRecipeShapeless("dustEnder", Items.BOOK);
		Item self = this;
		GameRegistry.addRecipe(new IRecipe() {
			@ParametersAreNonnullByDefault
			@Override
			public boolean matches (InventoryCrafting inv, World worldIn) {
				int essenceCount = 0;
				boolean hasSoulbook = false;
				String target = null;
				int containedEssence = Byte.MIN_VALUE;
				int inventorySize = inv.getSizeInventory();
				for (int i = 0; i < inventorySize; i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack == null) continue;
					if (stack.getItem() == self) {
						if (hasSoulbook) return false;
						String itemTarget = MobTarget.getMobTarget(stack);
						if (itemTarget != null) {
							if (target != null && !itemTarget.equals(target))
								return false;
							target = itemTarget;
						}
						containedEssence = Soulbook.getContainedEssence(stack);
						hasSoulbook = true;
						continue;
					} else if (stack.getItem() == ModObjects.getItem("essence")) {
						String itemTarget = MobTarget.getMobTarget(stack);
						if (itemTarget == null || (target != null && !itemTarget.equals(target)))
							return false;
						target = itemTarget;
						essenceCount++;
						continue;
					}
					return false;
				}
				return hasSoulbook && essenceCount > 0 && containedEssence + essenceCount <= Config.getSoulInfo(target).neededForSoul;
			}

			@ParametersAreNonnullByDefault
			@Nullable
			@Override
			public ItemStack getCraftingResult (InventoryCrafting inv) {
				int essenceCount = 0;
				ItemStack soulbook = null;
				String target = null;
				int containedEssence = 0;
				int inventorySize = inv.getSizeInventory();
				for (int i = 0; i < inventorySize; i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack == null) continue;
					if (stack.getItem() == self) {
						if (soulbook != null) return null;
						String itemTarget = MobTarget.getMobTarget(stack);
						if (itemTarget != null) {
							if (target != null && !itemTarget.equals(target))
								return null;
							target = itemTarget;
						}
						containedEssence = Soulbook.getContainedEssence(stack);
						soulbook = stack;
						continue;
					} else if (stack.getItem() == ModObjects.getItem("essence")) {
						String itemTarget = MobTarget.getMobTarget(stack);
						if (itemTarget == null || (target != null && !itemTarget.equals(target)))
							return null;
						target = itemTarget;
						essenceCount++;
						continue;
					}
					return null;
				}
				if (soulbook != null && essenceCount > 0 && containedEssence + essenceCount <= Config.getSoulInfo(target).neededForSoul) {
					ItemStack newStack = soulbook.copy();
					MobTarget.setMobTarget(newStack, target);
					Soulbook.setContainedEssence(newStack, containedEssence + essenceCount);
					return newStack;
				}
				return null;
			}

			@Override
			public int getRecipeSize () {
				return 4;
			}

			@Nullable
			@Override
			public ItemStack getRecipeOutput () {
				return self.getItemStack(1);
			}

			@ParametersAreNonnullByDefault
			@Nonnull
			@Override
			public ItemStack[] getRemainingItems (InventoryCrafting inv) {
				return ForgeHooks.defaultRecipeGetRemainingItems(inv);
			}
		});
	}
	public ItemStack getStack(String mobTarget) {
		return getStack(mobTarget, 1);
	}
	public ItemStack getStack(String mobTarget, Integer count) {
		ItemStack stack = new ItemStack(this, count);
		MobTarget.setMobTarget(stack, mobTarget);
		Soulbook.setContainedEssence(stack, 0);
		return stack;
	}

	@Override
	public boolean hasEffect (ItemStack stack) {
		return MobTarget.getMobTarget(stack) != null;
	}

	@Nonnull
	@Override
	public String getUnlocalizedNameInefficiently (@Nonnull ItemStack stack) {
		String mobTarget = MobTarget.getMobTarget(stack);
		if (mobTarget == null) mobTarget = "unfocused";
		return super.getUnlocalizedNameInefficiently(stack).replace(
			":soulbook", ":soulbook." + MobTarget.fixMobTarget(mobTarget)
		);
	}

	@Override
	public boolean showDurabilityBar (ItemStack stack) {
		String mobTarget = MobTarget.getMobTarget(stack);
		if (mobTarget == null) return true;
		int containedEssence = Soulbook.getContainedEssence(stack);
		return containedEssence < Config.getSoulInfo(mobTarget).neededForSoul;
	}

	@Override
	public double getDurabilityForDisplay (ItemStack stack) {
		String mobTarget = MobTarget.getMobTarget(stack);
		if (mobTarget == null) return 1;
		int containedEssence = Soulbook.getContainedEssence(stack);
		return (1 - containedEssence / (double)Config.getSoulInfo(mobTarget).neededForSoul);
	}
}
package io.github.cpearl0.ctnhcore.common.machine.multiblock;

import com.enderio.base.common.init.EIOFluids;
import com.enderio.base.common.item.misc.EnderiosItem;
import com.enderio.machines.common.init.MachineBlocks;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SlaughterHouseMachine extends WorkableElectricMultiblockMachine {
    public UUID uuid = UUID.randomUUID();
    public List<String> mobList = new ArrayList<>();
    public double timeCost = 20;

    public SlaughterHouseMachine(IMachineBlockEntity holder) {
        super(holder);
    }


    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (!super.beforeWorking(recipe))
            return false;

        mobList.clear();
        getParts().forEach(part -> {
            part.getRecipeHandlers().forEach(trait -> {
                if (trait.getHandlerIO().equals(IO.IN) && trait.getCapability() == ItemRecipeCapability.CAP) {
                    trait.getContents().forEach(contents -> {
                        if (contents instanceof ItemStack item) {
                            if (item.is(MachineBlocks.POWERED_SPAWNER.asItem()) && item.hasTag()) {
                                var mob = item.getTag().getCompound("BlockEntityTag").getCompound("EntityStorage").getCompound("Entity").getString("id");
                                if(!mobList.contains(mob)){
                                    mobList.add(mob);
                                }
                            }
                        }
                    });
                }
            });
        });

        return !mobList.isEmpty();
    }
    public static GTRecipe recipeModifier(MetaMachine machine, GTRecipe recipe, OCParams params, OCResult result){
        ServerLevel level = (ServerLevel) machine.getLevel();
        GTRecipe newrecipe = recipe.copy();
        if(machine instanceof SlaughterHouseMachine smachine && !smachine.mobList.isEmpty()) {
            // 战利品模式
            double totalhealth = 0;
            List<Content> itemList = new ArrayList<>();
            for (int i = 0; i < (((SlaughterHouseMachine) machine).getTier() - 2) * 4; i++) {
                int index = level.getRandom().nextInt(smachine.mobList.size());
                String mob = smachine.mobList.get(index);
                if (mob.equals("minecraft:wither")){
                    itemList.add(new Content(SizedIngredient.create(Items.NETHER_STAR.getDefaultInstance()), 1, 1, 0, null, null));
                }
                var mobentity = EntityType.byString(mob).get().create(machine.getLevel());
                if (mobentity instanceof LivingEntity) {
                    if (((LivingEntity) mobentity).getArmorValue() != 0) {
                        var armor = ((LivingEntity) mobentity).getArmorValue();
                        totalhealth += ((LivingEntity) mobentity).getMaxHealth() / ((double) 20 / (armor + 20));
                    } else {
                        totalhealth += ((LivingEntity) mobentity).getMaxHealth();
                    }
                    var fakePlayer = new FakePlayer(level, new GameProfile(smachine.uuid, "slaughter"));
                    var loottable = Objects.requireNonNull(level.getServer()).getLootData().getLootTable(new ResourceLocation(mob.split(":")[0] + ":entities/" + mob.split(":")[1]));
                    try {
                        var lootparams = new LootParams.Builder((ServerLevel) machine.getLevel())
                                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer)
                                .withParameter(LootContextParams.THIS_ENTITY, mobentity)
                                .withParameter(LootContextParams.DAMAGE_SOURCE, new DamageSources(level.getServer().registryAccess()).mobAttack(fakePlayer))
                                .withParameter(LootContextParams.ORIGIN, machine.getPos().getCenter())
                                .create(loottable.getParamSet());
                    } catch(Exception e) {
                        return recipe.copy();
                    }
                    var loots = loottable.getRandomItems(lootparams);
                    loots.forEach(itemStack -> {
                        if (!itemStack.isEmpty()){
                            itemList.add(new Content(SizedIngredient.create(itemStack), 1, 1, 0, null, null));
                        }
                    });
                }
            }
            newrecipe.outputs.put(ItemRecipeCapability.CAP,itemList);
            newrecipe.outputs.put(FluidRecipeCapability.CAP, List.of(new Content(FluidIngredient.of(FluidStack.create(EIOFluids.XP_JUICE.get().getSource(), (long) (totalhealth * 5))), 1, 1, 0, null, null)));
            newrecipe.duration = (int)(totalhealth / (20 * (((SlaughterHouseMachine) machine).getTier() - 2) * 4) * 40);
        }
        return newrecipe;
    }
    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        var mobName = mobList.stream().map(mob -> EntityType.byString(mob).get().getDescription().getString()).toList();
        textList.add(textList.size(), Component.translatable("ctnh.multiblock.slaughter_house.mobcount", mobList.size(),mobName));
    }
}

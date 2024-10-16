package io.github.cpearl0.ctnhcore.common.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

public class DigestionTankMachine extends WorkableElectricMultiblockMachine {
    public double Machine_Temperature = 0;
    public double Efficiency = 1;
    public DigestionTankMachine(IMachineBlockEntity holder) {super(holder);}
    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(textList.size(), Component.translatable("ctnh.fermenting_tank.growing_temperature", String.format("%.1f",Machine_Temperature)).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        textList.add(textList.size(), Component.translatable("ctnh.fermenting_tank.growth_efficiency", String.format("%.1f", Efficiency * 100)));
    }
    public static GTRecipe recipeModifier(MetaMachine machine, GTRecipe recipe, OCParams params, OCResult result){
        if(!(machine instanceof DigestionTankMachine dmachine)) return recipe;
        var newrecipe = recipe.copy();
        if (dmachine.Machine_Temperature >= 36 && dmachine.Machine_Temperature <= 38) {
            dmachine.Efficiency *= 1.2;
        }
        else {
            dmachine.Efficiency /= Math.min(3, Math.pow(Math.max(36 - dmachine.Machine_Temperature, dmachine.Machine_Temperature - 38), 2) / 10 + 1);
        }
        newrecipe.duration = (int) (newrecipe.duration / dmachine.Machine_Temperature);
        return newrecipe;
    }
    @Override
    public void clientTick() {
        if(getLevel() != null){
            Machine_Temperature = Temperature.getTemperatureAt(getPos(),getLevel()) * 25;
        }
        super.clientTick();
    }
}
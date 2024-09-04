package io.github.cpearl0.ctnhcore.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.CopperBlockSet;
import io.github.cpearl0.ctnhcore.coldsweat.UnderfloorHeatingSystemTempModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.w3c.dom.Text;

import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static io.github.cpearl0.ctnhcore.registry.CTNHRegistration.REGISTRATE;

public class CTNHMultiblockMachines {
    static {
        REGISTRATE.creativeModeTab(() -> CTNHCreativeModeTabs.MACHINE);
    }

    public static final MultiblockMachineDefinition UNDERFLOOR_HEATING_SYSTEM = REGISTRATE.multiblock("underfloor_heating_system", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CTNHRecipeTypes.UNDERFLOOR_HEATING_SYSTEM)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAAAAAAAAAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAABAAAAAAA")
                    .aisle("AAAAAAAA@AAAAAAA")
                    .where("A", Predicates.blocks(AllBlocks.COPPER_SHINGLES.getStandard().get())
                            .or(Predicates.blocks(AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.EXPOSED,false).get()))
                            .or(Predicates.blocks(AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.WEATHERED,false).get()))
                            .or(Predicates.blocks(AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.OXIDIZED,false).get()))
                            .or(Predicates.blocks(AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.EXPOSED,true).get()))
                            .or(Predicates.blocks(AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.WEATHERED,true).get()))
                            .or(Predicates.blocks(AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.OXIDIZED,true).get()))
                            .or(Predicates.blocks(AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.UNAFFECTED,true).get()))
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where("B", Predicates.blocks(CASING_BRONZE_PIPE.get()))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
            .workableCasingRenderer(Create.asResource("block/copper/copper_shingles"),
                    GTCEu.id("block/multiblock/multiblock_tank"), false)
            .beforeWorking((machine,recipe) ->{
                var efficiency = getEfficiency(machine);
                machine.self().getHolder().self().getPersistentData().putDouble("efficiency",efficiency);
                return true;
            })
            .onWorking(machine -> {
                var pos = machine.self().getPos();
                var facing = machine.self().getFrontFacing();
                double efficiency = machine.self().getHolder().self().getPersistentData().getDouble("efficiency");
                if(machine.self().getOffsetTimer() % 20 == 0){
                    efficiency = getEfficiency(machine);
                    machine.self().getHolder().self().getPersistentData().putDouble("efficiency",efficiency);
                }
                AABB range = switch (facing) {
                    case NORTH -> AABB.of(BoundingBox.fromCorners(pos.offset(-23, 0, -16), pos.offset(24, 10, 31)));
                    case SOUTH -> AABB.of(BoundingBox.fromCorners(pos.offset(-24, 0, -31), pos.offset(23, 10, 16)));
                    case WEST -> AABB.of(BoundingBox.fromCorners(pos.offset(-16, 0, -24), pos.offset(31, 10, 23)));
                    case EAST -> AABB.of(BoundingBox.fromCorners(pos.offset(-31, 0, -23), pos.offset(16, 10, 24)));
                    default -> throw new IllegalStateException("Unexpected value: " + facing);
                };
                UnderfloorHeatingSystemTempModifier.UNDERFLOOR_HEATING_SYSTEM_RANGE.put(range,efficiency);
                return true;
            })
            .afterWorking(machine -> {
                var pos = machine.self().getPos();
                var facing = machine.self().getFrontFacing();
                AABB range = switch (facing) {
                    case NORTH -> AABB.of(BoundingBox.fromCorners(pos.offset(-23, 0, -16), pos.offset(24, 10, 31)));
                    case SOUTH -> AABB.of(BoundingBox.fromCorners(pos.offset(-24, 0, -31), pos.offset(23, 10, 16)));
                    case WEST -> AABB.of(BoundingBox.fromCorners(pos.offset(-16, 0, -24), pos.offset(31, 10, 23)));
                    case EAST -> AABB.of(BoundingBox.fromCorners(pos.offset(-31, 0, -23), pos.offset(16, 10, 24)));
                    default -> throw new IllegalStateException("Unexpected value: " + facing);
                };
                UnderfloorHeatingSystemTempModifier.UNDERFLOOR_HEATING_SYSTEM_RANGE.remove(range);
            })
            .additionalDisplay((machine,display) -> {
                if(machine.isFormed()) {
                    double efficiency = machine.self().getHolder().self().getPersistentData().getDouble("efficiency");
//                if(efficiency == 0){
//                    efficiency = getEfficiency(machine);
//                }
                    display.add(display.size(), Component.translatable("multiblock.ctnhcore.underfloor_heating_system.efficiency", efficiency * 100));
                }
            })
            .register();

    public static double getEfficiency(IRecipeLogicMachine machine) {
        var pos = machine.self().getPos();
        var facing = machine.self().getFrontFacing();
        var level = machine.self().getLevel();
        AABB blocks = switch (facing) {
            case NORTH -> AABB.of(BoundingBox.fromCorners(pos.offset(-7, 0, 0), pos.offset(8, 1, 15)));
            case SOUTH -> AABB.of(BoundingBox.fromCorners(pos.offset(-8, 0, -15), pos.offset(7, 1, 0)));
            case WEST -> AABB.of(BoundingBox.fromCorners(pos.offset(0, 0, -8), pos.offset(15, 1, 7)));
            case EAST -> AABB.of(BoundingBox.fromCorners(pos.offset(-15, 0, -7), pos.offset(0, 1, 8)));
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        };
        int copper_shingles = (int) level.getBlockStates(blocks).map(BlockBehaviour.BlockStateBase::getBlock).filter(block -> block.getName() == AllBlocks.COPPER_SHINGLES.getStandard().get().getName()).count();
        int exposed_copper_shingles = (int) level.getBlockStates(blocks).map(BlockBehaviour.BlockStateBase::getBlock)
                .filter(block -> {
                    boolean b1 = block.getName() == AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.EXPOSED, true).get().getName();
                    boolean b2 = block.getName() == AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.UNAFFECTED, true).get().getName();
                    boolean b = block.getName() == AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.EXPOSED, false).get().getName();
                    return b1 || b2 || b;
                }).count();
        int weathered_copper_shingles = (int) level.getBlockStates(blocks).map(BlockBehaviour.BlockStateBase::getBlock)
                .filter(block -> {
                    boolean b1 = block.getName() == AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.WEATHERED, true).get().getName();
                    boolean b = block.getName() == AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.WEATHERED, false).get().getName();
                    return b1 || b;
                }).count();
        int oxidized_copper_shingles = (int) level.getBlockStates(blocks).map(BlockBehaviour.BlockStateBase::getBlock)
                .filter(block -> {
                    boolean b1 = block.getName() == AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.OXIDIZED, true).get().getName();
                    boolean b = block.getName() == AllBlocks.COPPER_SHINGLES.get(CopperBlockSet.BlockVariant.INSTANCE, WeatheringCopper.WeatherState.OXIDIZED, false).get().getName();
                    return b1 || b;
                }).count();
        return (copper_shingles + exposed_copper_shingles * 0.8 + weathered_copper_shingles * 0.75 + oxidized_copper_shingles * 0.6)/(copper_shingles + exposed_copper_shingles + weathered_copper_shingles + oxidized_copper_shingles);
    }
    public static void init() {

    }
}
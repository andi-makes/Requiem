/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.block;

import ladysnake.requiem.api.v1.block.ObeliskRune;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.record.RecordType;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.advancement.RequiemStats;
import ladysnake.requiem.common.screen.RiftScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class RiftRunestoneBlock extends InertRunestoneBlock implements ObeliskRune {
    public static final BooleanProperty FRIED = BooleanProperty.of("fried");

    public RiftRunestoneBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FRIED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FRIED);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!state.get(ACTIVATED) || !RemnantComponent.isIncorporeal(player)) {
            return ActionResult.PASS;
        } else if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            RunestoneBlockEntity.findObeliskOrigin(world, pos).ifPresent(
                origin -> {
                    player.openHandledScreen(state.createScreenHandlerFactory(world, origin));
                    player.incrementStat(RequiemStats.INTERACT_WITH_RIFT);
                }
            );

            return ActionResult.CONSUME;
        }
    }

    @Override
    public @Nullable NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof RunestoneBlockEntity controller) {
            return new RiftScreenHandlerFactory(
                controller.getCustomName().orElseGet(() -> new TranslatableText("requiem:container.obelisk_rift")),
                pos,
                GlobalRecordKeeper.get(world).getRecords().stream()
                    .filter(r -> r.get(RequiemRecordTypes.RIFT_OBELISK).isPresent())
                    .flatMap(r -> r.get(RecordType.BLOCK_ENTITY_POINTER).stream())
                    .filter(p -> p.getDimension() == world.getRegistryKey())
                    .map(GlobalPos::getPos)
                    .collect(Collectors.toSet()),
                controller::canBeUsedBy);
        }
        return null;
    }

    @Override
    protected boolean toggleRune(ServerWorld world, BlockPos runePos, @Nullable ObeliskMatch match, BlockState blockState) {
        if (match != null && match.coreWidth() > 1) {
            world.createExplosion(null, runePos.getX() + 0.5, runePos.getY() + 0.5, runePos.getZ() + 0.5, 1, true, Explosion.DestructionType.BREAK);
            world.playSound(null, runePos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 1, 1);
            blockState = blockState.with(FRIED, true);
        }
        return super.toggleRune(world, runePos, match, blockState);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public void applyEffect(ServerPlayerEntity target, int runeLevel, int obeliskWidth) {
        // nothing actually
    }
}
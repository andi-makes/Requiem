/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RequiemDialogueCommand {
    public static final String DIALOGUE_SUBCOMMAND = "dialogue";

    static LiteralArgumentBuilder<ServerCommandSource> dialogueSubcommand() {
        return literal(DIALOGUE_SUBCOMMAND)
            .requires(RequiemCommand.permission("dialogue.start"))
            // requiem dialogue start <dialogue> [players]
            .then(literal("start")
                .requires(RequiemCommand.permission("dialogue.start"))
                .then(argument("dialogue", IdentifierArgumentType.identifier())
                    .executes(context -> RequiemCommand.runOne(context.getSource().getPlayer(),
                        p -> startDialogue(context.getSource(), IdentifierArgumentType.getIdentifier(context, "dialogue"), p)
                    ))
                    .then(argument("players", EntityArgumentType.players())
                        .executes(context -> RequiemCommand.runMany(EntityArgumentType.getPlayers(context, "players"),
                            p -> startDialogue(context.getSource(), IdentifierArgumentType.getIdentifier(context, "dialogue"), p)))
                    )
                )
            );
    }

    private static void startDialogue(ServerCommandSource source, Identifier dialogue, ServerPlayerEntity player) {
        DialogueTracker.get(player).startDialogue(dialogue);
    }
}

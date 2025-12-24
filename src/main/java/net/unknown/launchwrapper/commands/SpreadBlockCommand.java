/*
 * Copyright (c) 2023 Unknown Network Developers and contributors.
 *
 * All rights reserved.
 *
 * NOTICE: This license is subject to change without prior notice.
 *
 * Redistribution and use in source and binary forms, *without modification*,
 *     are permitted provided that the following conditions are met:
 *
 * I. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 * II. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * III. Neither the name of Unknown Network nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 *
 * IV. This source code and binaries is provided by the copyright holders and contributors "AS-IS" and
 *     any express or implied warranties, including, but not limited to, the implied warranties of
 *     merchantability and fitness for a particular purpose are disclaimed.
 *     In not event shall the copyright owner or contributors be liable for
 *     any direct, indirect, incidental, special, exemplary, or consequential damages
 *     (including but not limited to procurement of substitute goods or services;
 *     loss of use data or profits; or business interruption) however caused and on any theory of liability,
 *     whether in contract, strict liability, or tort (including negligence or otherwise)
 *     arising in any way out of the use of this source code, event if advised of the possibility of such damage.
 */

package net.unknown.launchwrapper.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.unknown.launchwrapper.block.BlockWrapper;

// Command format: /<spreadblock|sb> <block> <enabled|y-level|blocks> ...
// Examples:
// /sb minecraft:gold_block enabled
// /sb minecraft:gold_block enabled true

// /sb minecraft:gold_block allowed-levels any true
// /sb minecraft:gold_block allowed-levels any false
// /sb minecraft:gold_block allowed-levels
// /sb minecraft:gold_block allowed-levels add minecraft:overworld
// /sb minecraft:gold_block allowed-levels remove minecraft:overworld

// /sb minecraft:gold_block y-level only
// /sb minecraft:gold_block y-level only true
// /sb minecraft:gold_block y-level level 64

// /sb minecraft:gold_block loop-count
// /sb minecraft:gold_block loop-count 4

// /sb minecraft:gold_block blocks any
// /sb minecraft:gold_block blocks any true
// /sb minecraft:gold_block blocks targets
// /sb minecraft:gold_block blocks targets reset
// /sb minecraft:gold_block blocks targets add minecraft:stone
// /sb minecraft:gold_block blocks targets remove minecraft:stone
// /sb minecraft:gold_block blocks do-not-targets
// /sb minecraft:gold_block blocks do-not-targets reset
// /sb minecraft:gold_block blocks do-not-targets add minecraft:stone
// /sb minecraft:gold_block blocks do-not-targets remove minecraft:stone
public class SpreadBlockCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.literal("spreadblock");

        HolderLookup.RegistryLookup<Block> blocks = buildContext.lookup(Registries.BLOCK).orElse(null);
        HolderLookup<Block> spreadBlocks = blocks.filterElements(block -> block instanceof BlockWrapper);

        builder.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.argument("spread_block", BlockStateArgument.block(buildContext))
                        .suggests((ctx, suggestionsBuilder) -> BlockStateParser.fillSuggestions(spreadBlocks, suggestionsBuilder, false, false))
                        .then(Commands.literal("enabled")
                                .executes(ctx -> {
                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                    ctx.getSource().sendSuccess(() -> Component.empty()
                                            .append(blockName(spreadBlock))
                                            .append("の伝播は")
                                            .append(Component.literal(spreadBlock.isSpreadEnabled() ? "有効" : "無効").withStyle(spreadBlock.isSpreadEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED))
                                            .append("です"), true);
                                    return spreadBlock.isSpreadEnabled() ? 1 : 0;
                                })
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            spreadBlock.setSpreadEnabled(BoolArgumentType.getBool(ctx, "enabled"));
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("の伝播を")
                                                    .append(Component.literal(spreadBlock.isSpreadEnabled() ? "有効" : "無効").withStyle(spreadBlock.isSpreadEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED))
                                                    .append("にしました"), true);
                                            return spreadBlock.isSpreadEnabled() ? 1 : 0;
                                        })))
                        .then(Commands.literal("allowed-levels")
                                .executes(ctx -> {
                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                    MutableComponent msg = Component.empty().append(blockName(spreadBlock)).append("の伝播許可ワールドは").append(Component.literal(spreadBlock.getSpreadAllowedLevels().size() + "個").withStyle(ChatFormatting.GOLD)).append("登録されています");
                                    for (ResourceKey<Level> spreadAllowedLevel : spreadBlock.getSpreadAllowedLevels()) {
                                        msg.append("\n- ").append(spreadAllowedLevel.identifier().toString());
                                    }
                                    ctx.getSource().sendSuccess(() -> msg, true);
                                    return spreadBlock.getSpreadAllowedLevels().size();
                                })
                                .then(Commands.literal("any")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("の任意ワールドでの伝播は")
                                                    .append(Component.literal(spreadBlock.isSpreadAllowedAnyLevel() ? "有効" : "無効").withStyle(spreadBlock.isSpreadAllowedAnyLevel() ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
                                                    .append("です"), true);
                                            return spreadBlock.getSpreadAllowedLevels().size();
                                        })
                                        .then(Commands.argument("any", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                    spreadBlock.setSpreadAllowedAnyLevel(BoolArgumentType.getBool(ctx, "any"));
                                                    ctx.getSource().sendSuccess(() -> Component.empty()
                                                            .append(blockName(spreadBlock))
                                                            .append("の任意ワールドでの伝播を")
                                                            .append(Component.literal(spreadBlock.isSpreadAllowedAnyLevel() ? "有効" : "無効").withStyle(spreadBlock.isSpreadAllowedAnyLevel() ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
                                                            .append("に設定しました"), true);
                                                    return spreadBlock.getSpreadAllowedLevels().size();
                                                })))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("level", DimensionArgument.dimension())
                                                .suggests((ctx, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(MinecraftServer.getServer().levelKeys(), suggestionsBuilder, ResourceKey::identifier, (key) -> Component.empty()))
                                                .executes(ctx -> {
                                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                    ResourceKey<Level> level = DimensionArgument.getDimension(ctx, "level").dimension();
                                                    if (spreadBlock.isSpreadAllowedLevel(level)) {
                                                        ctx.getSource().sendFailure(Component.literal("ワールドは既に伝播許可ワールドとして登録されています"));
                                                    } else {
                                                        spreadBlock.addSpreadAllowedLevel(level);
                                                        ctx.getSource().sendSuccess(() -> Component.empty().append(level.identifier().toString()).append("を").append(blockName(spreadBlock)).append("の伝播許可ワールドに追加しました"), true);
                                                    }
                                                    return spreadBlock.getSpreadAllowedLevels().size();
                                                })))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("level", DimensionArgument.dimension())
                                                .suggests((ctx, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(MinecraftServer.getServer().levelKeys(), suggestionsBuilder, ResourceKey::identifier, (key) -> Component.empty()))
                                                .executes(ctx -> {
                                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                    ResourceKey<Level> level = DimensionArgument.getDimension(ctx, "level").dimension();
                                                    if (spreadBlock.isSpreadAllowedLevel(level)) {
                                                        spreadBlock.removeSpreadAllowedLevel(level);
                                                        ctx.getSource().sendSuccess(() -> Component.empty().append(level.identifier().toString()).append("を").append(blockName(spreadBlock)).append("の伝播許可ワールドから削除しました"), true);
                                                    } else {
                                                        ctx.getSource().sendFailure(Component.literal("ワールドは伝播許可ワールドとして登録されていません"));
                                                    }
                                                    return spreadBlock.getSpreadAllowedLevels().size();
                                                }))))
                        .then(Commands.literal("y-level")
                                .then(Commands.literal("only")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("の指定Y座標のみに伝播は")
                                                    .append(Component.literal(spreadBlock.isOnlySpecifiedYLevel() ? "有効" : "無効").withStyle(spreadBlock.isOnlySpecifiedYLevel() ? ChatFormatting.GREEN : ChatFormatting.RED))
                                                    .append("です"), true);
                                            return spreadBlock.isOnlySpecifiedYLevel() ? 1 : 0;
                                        })
                                        .then(Commands.argument("only", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                    spreadBlock.setOnlySpecifiedYLevel(BoolArgumentType.getBool(ctx, "only"));
                                                    ctx.getSource().sendSuccess(() -> Component.empty()
                                                            .append(blockName(spreadBlock))
                                                            .append("の指定Y座標のみに伝播を")
                                                            .append(Component.literal(spreadBlock.isOnlySpecifiedYLevel() ? "有効" : "無効").withStyle(spreadBlock.isOnlySpecifiedYLevel() ? ChatFormatting.GREEN : ChatFormatting.RED))
                                                            .append("にしました"), true);
                                                    return spreadBlock.isOnlySpecifiedYLevel() ? 1 : 0;
                                                })))
                                .then(Commands.literal("level")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("の伝播指定Y座標は")
                                                    .append(Component.literal(String.valueOf(spreadBlock.getYLevel())).withStyle(ChatFormatting.GOLD))
                                                    .append("です"), true);
                                            return spreadBlock.getYLevel();
                                        })
                                        .then(Commands.argument("level", IntegerArgumentType.integer(-64, 320))
                                                .executes(ctx -> {
                                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                    spreadBlock.setYLevel(IntegerArgumentType.getInteger(ctx, "level"));
                                                    ctx.getSource().sendSuccess(() -> Component.empty()
                                                            .append(blockName(spreadBlock))
                                                            .append("の伝播指定Y座標を")
                                                            .append(Component.literal(String.valueOf(spreadBlock.getYLevel())).withStyle(ChatFormatting.GOLD))
                                                            .append("にしました"), true);
                                                    return spreadBlock.getYLevel();
                                                }))))
                        .then(Commands.literal("loop-count")
                                .executes(ctx -> {
                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                    ctx.getSource().sendSuccess(() -> Component.empty()
                                            .append(blockName(spreadBlock))
                                            .append("のループ回数は")
                                            .append(Component.literal(String.valueOf(spreadBlock.getLoopCount())).withStyle(ChatFormatting.GOLD))
                                            .append("です"), true);
                                    return spreadBlock.getLoopCount();
                                })
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            spreadBlock.setLoopCount(IntegerArgumentType.getInteger(ctx, "count"));
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("のループ回数を")
                                                    .append(Component.literal(String.valueOf(spreadBlock.getLoopCount())).withStyle(ChatFormatting.GOLD))
                                                    .append("にしました"), true);
                                            return spreadBlock.getLoopCount();
                                        }))
                                .then(Commands.literal("reset")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            spreadBlock.setLoopCount(4);
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("のループ回数を")
                                                    .append(Component.literal("4").withStyle(ChatFormatting.GOLD))
                                                    .append("にリセットしました"), true)
                                            ;
                                            return 4;
                                        }))
                                .then(Commands.literal("decrement")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            spreadBlock.setLoopCount(spreadBlock.getLoopCount() - 1);
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("のループ回数を")
                                                    .append(Component.literal(String.valueOf(spreadBlock.getLoopCount())).withStyle(ChatFormatting.GOLD))
                                                    .append("に減少しました"), true);
                                            return spreadBlock.getLoopCount();
                                        }))
                                .then(Commands.literal("increment")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            spreadBlock.setLoopCount(spreadBlock.getLoopCount() + 1);
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("のループ回数を")
                                                    .append(Component.literal(String.valueOf(spreadBlock.getLoopCount())).withStyle(ChatFormatting.GOLD))
                                                    .append("に増加しました"), true);
                                            return spreadBlock.getLoopCount();
                                        })))
                        .then(Commands.literal("blocks")
                                .then(Commands.literal("any")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            ctx.getSource().sendSuccess(() -> Component.empty()
                                                    .append(blockName(spreadBlock))
                                                    .append("の任意ブロックへの伝播は")
                                                    .append(Component.literal(spreadBlock.isSpreadAnyBlock() ? "有効" : "無効").withStyle(spreadBlock.isSpreadAnyBlock() ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
                                                    .append("です"), true);
                                            return spreadBlock.isSpreadAnyBlock() ? 1 : 0;
                                        })
                                        .then(Commands.argument("any", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                    spreadBlock.setSpreadAnyBlock(BoolArgumentType.getBool(ctx, "any"));
                                                    ctx.getSource().sendSuccess(() -> Component.empty()
                                                            .append(blockName(spreadBlock))
                                                            .append("の任意ブロックへの伝播を")
                                                            .append(Component.literal(spreadBlock.isSpreadAnyBlock() ? "有効" : "無効").withStyle(spreadBlock.isSpreadAnyBlock() ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
                                                            .append("にしました"), true);
                                                    return spreadBlock.isSpreadAnyBlock() ? 1 : 0;
                                                })))
                                .then(Commands.literal("targets")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            MutableComponent msg = Component.empty().append(blockName(spreadBlock)).append("の伝播対象ブロックは").append(Component.literal(spreadBlock.getSpreadBlocks().size() + "個").withStyle(ChatFormatting.GOLD)).append("登録されています");
                                            for (Block spreadBlockV : spreadBlock.getSpreadBlocks()) {
                                                msg.append("\n- ").append(blockName(spreadBlockV));
                                            }
                                            ctx.getSource().sendSuccess(() -> msg, true);
                                            return spreadBlock.getSpreadBlocks().size();
                                        })
                                        .then(Commands.literal("reset")
                                                .executes(ctx -> {
                                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                    spreadBlock.resetSpreadBlocks();
                                                    ctx.getSource().sendSuccess(() -> Component.empty().append(blockName(spreadBlock)).append("の伝播対象ブロックをリセットしました"), true);
                                                    return spreadBlock.getSpreadBlocks().size();
                                                }))
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("block", BlockStateArgument.block(buildContext))
                                                        .suggests((ctx, suggestionBuilder) -> BlockStateParser.fillSuggestions(blocks, suggestionBuilder, false, false))
                                                        .executes(ctx -> {
                                                            Block block = BlockStateArgument.getBlock(ctx, "block").getState().getBlock();
                                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                            if (spreadBlock.isSpreadBlock(block)) {
                                                                ctx.getSource().sendFailure(Component.literal("ブロックは既に伝播対象として登録されています"));
                                                            } else {
                                                                spreadBlock.addSpreadBlock(block);
                                                                ctx.getSource().sendSuccess(() -> Component.empty().append(blockName(block)).append("を").append(blockName(spreadBlock)).append("の伝播対象ブロックに追加しました"), true);
                                                            }
                                                            return spreadBlock.getSpreadBlocks().size();
                                                        })))
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("block", BlockStateArgument.block(buildContext))
                                                        .suggests((ctx, suggestionBuilder) -> BlockStateParser.fillSuggestions(blocks, suggestionBuilder, false, false))
                                                        .executes(ctx -> {
                                                            Block block = BlockStateArgument.getBlock(ctx, "block").getState().getBlock();
                                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                            if (spreadBlock.isSpreadBlock(block)) {
                                                                spreadBlock.removeSpreadBlock(block);
                                                                ctx.getSource().sendSuccess(() -> Component.empty().append(blockName(block)).append("を").append(blockName(spreadBlock)).append("の伝播対象ブロックから削除しました"), true);
                                                            } else {
                                                                ctx.getSource().sendFailure(Component.literal("ブロックは伝播対象として登録されていません"));
                                                            }
                                                            return spreadBlock.getSpreadBlocks().size();
                                                        }))))
                                .then(Commands.literal("do-not-targets")
                                        .executes(ctx -> {
                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                            MutableComponent msg = Component.empty().append(blockName(spreadBlock)).append("の伝播対象外ブロックは").append(Component.literal(spreadBlock.getDoNotSpreadBlocks().size() + "個").withStyle(ChatFormatting.GOLD)).append("登録されています");
                                            for (Block doNotSpreadBlock : spreadBlock.getDoNotSpreadBlocks()) {
                                                msg.append("\n- ").append(blockName(doNotSpreadBlock));
                                            }
                                            ctx.getSource().sendSuccess(() -> msg, true);
                                            return spreadBlock.getDoNotSpreadBlocks().size();
                                        })
                                        .then(Commands.literal("reset")
                                                .executes(ctx -> {
                                                    BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                    spreadBlock.resetDoNotSpreadBlocks();
                                                    ctx.getSource().sendSuccess(() -> Component.empty().append(blockName(spreadBlock)).append("の伝播対象外ブロックをリセットしました"), true);
                                                    return spreadBlock.getDoNotSpreadBlocks().size();
                                                }))
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("block", BlockStateArgument.block(buildContext))
                                                        .suggests((ctx, suggestionBuilder) -> BlockStateParser.fillSuggestions(blocks, suggestionBuilder, false, false))
                                                        .executes(ctx -> {
                                                            Block block = BlockStateArgument.getBlock(ctx, "block").getState().getBlock();
                                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                            if (spreadBlock.isDoNotSpreadBlock(block)) {
                                                                ctx.getSource().sendFailure(Component.literal("ブロックは既に伝播対象外として登録されています"));
                                                            } else {
                                                                spreadBlock.addDoNotSpreadBlock(block);
                                                                ctx.getSource().sendSuccess(() -> Component.empty().append(blockName(block)).append("を").append(blockName(spreadBlock)).append("の伝播対象外ブロックに追加しました"), true);
                                                            }
                                                            return spreadBlock.getDoNotSpreadBlocks().size();
                                                        })))
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("block", BlockStateArgument.block(buildContext))
                                                        .suggests((ctx, suggestionBuilder) -> BlockStateParser.fillSuggestions(blocks, suggestionBuilder, false, false))
                                                        .executes(ctx -> {
                                                            Block block = BlockStateArgument.getBlock(ctx, "block").getState().getBlock();
                                                            BlockWrapper spreadBlock = (BlockWrapper) BlockStateArgument.getBlock(ctx, "spread_block").getState().getBlock();
                                                            if (spreadBlock.isDoNotSpreadBlock(block)) {
                                                                spreadBlock.removeDoNotSpreadBlock(block);
                                                                ctx.getSource().sendSuccess(() -> Component.empty().append(blockName(block)).append("を").append(blockName(spreadBlock)).append("の伝播対象外ブロックから削除しました"), true);
                                                            } else {
                                                                ctx.getSource().sendFailure(Component.literal("ブロックは伝播対象外として登録されていません"));
                                                            }
                                                            return spreadBlock.getDoNotSpreadBlocks().size();
                                                        }))))));

        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(builder);

        // Alias Command /gb

        /*LiteralArgumentBuilder<CommandSourceStack> aliasBuilder = LiteralArgumentBuilder.literal("sb");
        aliasBuilder.requires(source -> source.hasPermission(2))
                .redirect(node);

        dispatcher.register(aliasBuilder);*/
    }

    /**
     * ブロックの名前を返します。(例: [石])
     *
     * @param block ブロック
     * @return ブロックの名前
     */
    private static Component blockName(Block block) {
        return new ItemStack(block.asItem()).getDisplayName();
    }
}
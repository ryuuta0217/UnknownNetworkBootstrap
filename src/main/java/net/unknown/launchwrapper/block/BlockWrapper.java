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

package net.unknown.launchwrapper.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.bukkit.craftbukkit.event.CraftEventFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BlockWrapper extends Block {
    public static final boolean DEFAULT_SPREAD_ENABLED = false;

    private final Block originalBlockInstance;
    private boolean spreadEnabled = DEFAULT_SPREAD_ENABLED;
    private boolean onlySpecifiedYLevel = false;
    private int yLevel = 64;
    private boolean spreadAnyBlock = false;
    private int loopCount = 4;
    private boolean spreadAllowedAnyLevel = false;

    private Set<ResourceKey<Level>> spreadAllowedLevels = new HashSet<>();
    private Set<Block> doNotSpreadBlocks = new HashSet<>();
    private Set<Block> spreadBlocks = new HashSet<>();

    public BlockWrapper(BlockBehaviour.Properties properties) {
        super(properties);
        this.originalBlockInstance = null;
        this.doNotSpreadBlocks.add(this);
    }

    public void delayedInitialize() {
        this.resetDoNotSpreadBlocks();
        this.resetSpreadBlocks();
    }

    public Block getOriginalBlockInstance() {
        return this.originalBlockInstance;
    }

    public boolean isSpreadEnabled() {
        return this.spreadEnabled;
    }

    public void setSpreadEnabled(boolean enabled) {
        this.spreadEnabled = enabled;
    }

    public boolean isSpreadAllowedAnyLevel() {
        return this.spreadAllowedAnyLevel;
    }

    public void setSpreadAllowedAnyLevel(boolean spreadAllowedAnyLevel) {
        this.spreadAllowedAnyLevel = spreadAllowedAnyLevel;
    }

    public Set<ResourceKey<Level>> getSpreadAllowedLevels() {
        return Collections.unmodifiableSet(this.spreadAllowedLevels);
    }

    public boolean isSpreadAllowedLevel(ResourceKey<Level> level) {
        return this.spreadAllowedLevels.contains(level);
    }

    public void addSpreadAllowedLevel(ResourceKey<Level> level) {
        this.spreadAllowedLevels.add(level);
    }

    public void addSpreadAllowedLevelAll(Collection<ResourceKey<Level>> c) {
        this.spreadAllowedLevels.addAll(c);
    }

    public void removeSpreadAllowedLevel(ResourceKey<Level> level) {
        this.spreadAllowedLevels.remove(level);
    }

    public void removeSpreadAllowedLevelAll(Collection<ResourceKey<Level>> c) {
        this.spreadAllowedLevels.removeAll(c);
    }

    public void resetSpreadAllowedLevels() {
        this.spreadAllowedLevels.clear();
    }

    public boolean isOnlySpecifiedYLevel() {
        return this.onlySpecifiedYLevel;
    }

    public void setOnlySpecifiedYLevel(boolean onlySpecifiedYLevel) {
        this.onlySpecifiedYLevel = onlySpecifiedYLevel;
    }

    public boolean isSpreadAnyBlock() {
        return this.spreadAnyBlock;
    }

    public void setSpreadAnyBlock(boolean spreadAnyBlock) {
        this.spreadAnyBlock = spreadAnyBlock;
    }

    public int getYLevel() {
        return this.yLevel;
    }

    public void setYLevel(int yLevel) {
        this.yLevel = yLevel;
    }

    public int getLoopCount() {
        return this.loopCount;
    }

    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    public Set<Block> getDoNotSpreadBlocks() {
        return Collections.unmodifiableSet(this.doNotSpreadBlocks);
    }

    public boolean isDoNotSpreadBlock(Block block) {
        return this.doNotSpreadBlocks.contains(block);
    }

    public void addDoNotSpreadBlock(Block block) {
        this.doNotSpreadBlocks.add(block);
    }

    public void addDoNotSpreadBlockAll(Collection<Block> c) {
        this.doNotSpreadBlocks.addAll(c);
    }

    public void removeDoNotSpreadBlock(Block block) {
        this.doNotSpreadBlocks.remove(block);
    }

    public void removeDoNotSpreadBlockAll(Collection<Block> c) {
        this.doNotSpreadBlocks.removeAll(c);
    }

    public void resetDoNotSpreadBlocks() {
        this.doNotSpreadBlocks.clear();
        this.doNotSpreadBlocks.add(Blocks.AIR);
        this.doNotSpreadBlocks.add(this);
    }

    public Set<Block> getSpreadBlocks() {
        return Collections.unmodifiableSet(this.spreadBlocks);
    }

    public boolean isSpreadBlock(Block block) {
        return this.spreadBlocks.contains(block);
    }

    public void addSpreadBlock(Block block) {
        this.spreadBlocks.add(block);
    }

    public void addSpreadBlockAll(Collection<Block> c) {
        this.spreadBlocks.addAll(c);
    }

    public void removeSpreadBlock(Block block) {
        this.spreadBlocks.remove(block);
    }

    public void removeSpreadBlockAll(Collection<Block> c) {
        this.spreadBlocks.removeAll(c);
    }

    public void resetSpreadBlocks() {
        this.spreadBlocks.clear();
        this.spreadBlocks.add(Blocks.GRASS_BLOCK);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!spreadEnabled) return;
        if (!this.spreadAllowedAnyLevel && !this.spreadAllowedLevels.contains(world.dimension())) return;

        ChunkAccess cachedBlockChunk = world.getChunkIfLoaded(pos);

        if (cachedBlockChunk == null) {
            return;
        }

        BlockState defaultState = this.defaultBlockState();

        for (int i = 0; i < loopCount; ++i) {
            BlockPos spreadPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

            if (onlySpecifiedYLevel && spreadPos.getY() != yLevel)  {
                continue;
            }

            if (pos.getX() == spreadPos.getX() && pos.getY() == spreadPos.getY() && pos.getZ() == spreadPos.getZ()) {
                continue;
            }

            ChunkAccess blockChunk;
            if (cachedBlockChunk.getPos().x == spreadPos.getX() >> 4 && cachedBlockChunk.getPos().z == spreadPos.getZ() >> 4) {
                blockChunk = cachedBlockChunk;
            } else {
                blockChunk = world.getChunkIfLoaded(spreadPos);
            }

            BlockState beforeBlockState = blockChunk.getBlockState(spreadPos);
            Block beforeBlock = beforeBlockState.getBlock();

            if (!doNotSpreadBlocks.contains(beforeBlock) && beforeBlockState.isSolid() && (spreadAnyBlock || spreadBlocks.contains(beforeBlock))) {
                CraftEventFactory.handleBlockSpreadEvent(world, pos, spreadPos, defaultState, Block.UPDATE_ALL);
            }
        }
    }
}

package net.unknown.launchwrapper.mixins;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.unknown.launchwrapper.linkchest.ChestTransportMode;
import net.unknown.launchwrapper.linkchest.IMixinChestBlockEntity;
import net.unknown.launchwrapper.linkchest.LinkedChest;
import net.unknown.launchwrapper.util.WrappedBlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(ChestBlockEntity.class)
public abstract class MixinChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity, IMixinChestBlockEntity {
    @Shadow private NonNullList<ItemStack> items;

    @Shadow protected abstract void setItems(NonNullList<ItemStack> list);

    private static final Map<UUID, LinkedChest> LINKED_CHESTS = Maps.newHashMap();
    private ChestTransportMode previousTransportMode = null;
    private ChestTransportMode transportMode = ChestTransportMode.DISABLED;
    private UUID previousUniqueId = null;
    private UUID linkUniqueId = null;

    protected MixinChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "load", at = @At("RETURN"))
    public void onLoad(CompoundTag nbt, CallbackInfo ci) {
        if(nbt.contains("Link")) {
            CompoundTag link = nbt.getCompound("Link");
            if(link.contains("Mode")) {
                this.previousTransportMode = this.transportMode;
                String modeStr = link.getString("Mode");
                ChestTransportMode mode = ChestTransportMode.DISABLED;
                if(modeStr.equalsIgnoreCase("sender")) mode = ChestTransportMode.SENDER;
                if(modeStr.equalsIgnoreCase("receiver")) mode = ChestTransportMode.RECEIVER;
                this.transportMode = mode;
            }

            if(link.contains("UUID")) {
                this.previousUniqueId = this.linkUniqueId;
                this.linkUniqueId = link.getUUID("UUID");
            }

            if(this.linkUniqueId != null) {
                LinkedChest lc;
                if(LINKED_CHESTS.containsKey(this.linkUniqueId)) {
                    lc = LINKED_CHESTS.get(this.linkUniqueId);
                } else {
                    lc = new LinkedChest(null, null);
                    LINKED_CHESTS.put(this.linkUniqueId, lc);
                }

                if(this.transportMode != ChestTransportMode.DISABLED) {
                    switch (this.transportMode) {
                        case SENDER -> lc.setSenderPos(new WrappedBlockPos(this.getLevel(), this.getBlockPos()));
                        case RECEIVER -> lc.setReceiverPos(new WrappedBlockPos(this.getLevel(), this.getBlockPos()));
                    }
                }
            }

            if(this.previousUniqueId != null) {
                if(LINKED_CHESTS.containsKey(this.previousUniqueId)) {
                    LinkedChest lc = LINKED_CHESTS.get(this.previousUniqueId);
                    switch (this.previousTransportMode) {
                        case SENDER -> lc.setSenderPos(null);
                        case RECEIVER -> lc.setReceiverPos(null);
                    }

                    if(lc.getSenderPos() == null && lc.getReceiverPos() == null) {
                        LINKED_CHESTS.remove(this.previousUniqueId);
                    }
                }
            }
        }
    }



    @Inject(method = "saveAdditional", at = @At("RETURN"))
    public void onSaveAdditional(CompoundTag nbt, CallbackInfo ci) {
        if(this.transportMode != ChestTransportMode.DISABLED && this.linkUniqueId != null) {
            CompoundTag link = new CompoundTag();

            link.putString("Mode", this.transportMode.name().toLowerCase());
            link.putUUID("UUID", this.linkUniqueId);

            nbt.put("Link", link);
        }
    }

    @Override
    public ChestTransportMode getChestTransportMode() {
        return this.transportMode;
    }

    @Override
    public UUID getLinkUniqueId() {
        return this.linkUniqueId;
    }

    @Override
    public void setLevel(@NotNull Level level) {
        if(this.transportMode != ChestTransportMode.DISABLED && this.linkUniqueId != null) {
            if(LINKED_CHESTS.containsKey(this.linkUniqueId)) {
                LinkedChest lc = LINKED_CHESTS.get(this.linkUniqueId);
                if(this.transportMode == ChestTransportMode.SENDER && lc.getSenderPos() != null) lc.getSenderPos().level(level);
                if(this.transportMode == ChestTransportMode.RECEIVER && lc.getReceiverPos() != null) lc.getReceiverPos().level(level);
            }
        }
        super.setLevel(level);
    }

    /**
     * Overwrite a getItems method.
     *
     * @author ryuuta0217
     */
    @Overwrite
    public NonNullList<ItemStack> getItems() {
        if(this.transportMode == ChestTransportMode.SENDER && this.linkUniqueId != null) {
            if(LINKED_CHESTS.containsKey(this.linkUniqueId)) {
                LinkedChest lc = LINKED_CHESTS.get(this.linkUniqueId);
                if(lc.getReceiverPos() != null) {
                    WrappedBlockPos wbp = lc.getReceiverPos();
                    if (wbp.level() != null) {
                        /*if(!wbp.level().isLoaded(wbp.blockPos())) {
                        wbp.level().getWorld().loadChunk(wbp.level().getChunkAt(wbp.blockPos()).getBukkitChunk());
                    }*/
                        BlockEntity be = wbp.level().getBlockEntity(wbp.blockPos());
                        if(be instanceof MixinChestBlockEntity receiverChest) {
                            if(receiverChest.getLinkUniqueId() != null) {
                                if(receiverChest.getLinkUniqueId().equals(this.getLinkUniqueId())) {
                                    if(receiverChest.getChestTransportMode() == ChestTransportMode.RECEIVER) {
                                        return receiverChest.getItems();
                                    }
                                }
                            }
                        }}
                }
            }
        }
        return this.items;
    }

    @Override
    public Map<UUID, LinkedChest> getLinks() {
        return LINKED_CHESTS;
    }
}

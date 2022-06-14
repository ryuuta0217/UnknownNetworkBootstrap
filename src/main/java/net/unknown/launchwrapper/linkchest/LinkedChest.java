package net.unknown.launchwrapper.linkchest;

import net.minecraft.core.BlockPos;
import net.unknown.launchwrapper.util.WrappedBlockPos;

import java.util.List;
import java.util.Set;

public class LinkedChest {
    private WrappedBlockPos senderPos = null;
    private WrappedBlockPos receiverPos = null;

    public LinkedChest(WrappedBlockPos senderPos, WrappedBlockPos receiverPos) {
        this.senderPos = senderPos;
        this.receiverPos = receiverPos;
    }

    public WrappedBlockPos getSenderPos() {
        return senderPos;
    }

    /*public WrappedBlockPos getWrappedPosInstance(BlockPos blockPos) {
        List<WrappedBlockPos> results = this.senderPos.stream().filter(wrapped -> wrapped.blockPos().equals(blockPos)).toList();
        if(results.size() > 0) return results.get(0);
        return null;
    }

    public void addAccessorPos(WrappedBlockPos accessorPos) {
        this.senderPos.add(accessorPos);
    }

    public void removeAccessorPos(WrappedBlockPos accessorPos) {
        this.senderPos.remove(accessorPos);
    }*/

    public void setSenderPos(WrappedBlockPos senderPos) {
        this.senderPos = senderPos;
    }

    public WrappedBlockPos getReceiverPos() {
        return receiverPos;
    }

    public void setReceiverPos(WrappedBlockPos receiverPos) {
        this.receiverPos = receiverPos;
    }
}

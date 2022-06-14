package net.unknown.launchwrapper.linkchest;

import net.minecraft.world.Container;

import java.util.Map;
import java.util.UUID;

public interface IMixinChestBlockEntity extends Container {

    Map<UUID, LinkedChest> getLinks();

    ChestTransportMode getChestTransportMode();

    UUID getLinkUniqueId();
}

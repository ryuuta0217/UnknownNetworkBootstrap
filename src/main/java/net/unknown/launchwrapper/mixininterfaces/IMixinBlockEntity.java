package net.unknown.launchwrapper.mixininterfaces;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IMixinBlockEntity {
    @Nullable UUID getPlacer();

    void setPlacer(@Nullable UUID placer);
}

package com.matthewperiut.fence_on_slab.fabric;

import com.matthewperiut.fence_on_slab.FenceOnSlab;
import net.fabricmc.api.ModInitializer;

public class FenceOnSlabFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FenceOnSlab.init();
    }
}

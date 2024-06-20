package com.matthewperiut.fence_on_slab.forge;

import com.matthewperiut.fence_on_slab.FenceOnSlab;
import net.minecraftforge.fml.common.Mod;

@Mod(FenceOnSlab.MOD_ID)
public class FenceOnSlabForge {
    public FenceOnSlabForge() {
        FenceOnSlab.init();
    }
}

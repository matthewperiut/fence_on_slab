package com.matthewperiut.fence_on_slab;

import net.minecraft.state.property.BooleanProperty;

import java.util.HashSet;
import java.util.Set;

public class FenceOnSlab {
    public static final String MOD_ID = "fence_on_slab";

    public static final BooleanProperty LOWER = BooleanProperty.of("lower");

    public static Set<String> FENCE_SLAB_SUPPORT = new HashSet<String>() {{
        add("oak_fence");
        add("spruce_fence");
        add("birch_fence");
        add("jungle_fence");
        add("acacia_fence");
        add("dark_oak_fence");
        add("mangrove_fence");
        add("cherry_fence");
        add("bamboo_fence");
        add("crimson_fence");
        add("warped_fence");
        add("nether_brick_fence");
    }};

    public static void init() {
    }
}
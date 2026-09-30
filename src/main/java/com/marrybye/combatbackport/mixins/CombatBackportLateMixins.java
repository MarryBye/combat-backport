package com.marrybye.combatbackport.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class CombatBackportLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.combatbackport.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        if (loadedMods.contains("TConstruct")) {
            mixins.add("ticon.MixinAbilityHelper");
            mixins.add("ticon.MixinCleaver");
        }
        return mixins;
    }
}

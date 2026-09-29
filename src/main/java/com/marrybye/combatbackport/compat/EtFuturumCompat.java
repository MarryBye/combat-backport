package com.marrybye.combatbackport.compat;

import java.lang.reflect.Field;

import cpw.mods.fml.common.Loader;

public class EtFuturumCompat {

    private static Boolean etFuturumLoaded = null;
    private static Boolean mendingEnabled = null;
    private static Integer mendingId = null;

    public static boolean isEtFuturumLoaded() {
        if (etFuturumLoaded == null) {
            etFuturumLoaded = Loader.isModLoaded("etfuturum");
        }
        return etFuturumLoaded;
    }

    public static boolean isEtFuturumMendingEnabled() {
        if (!isEtFuturumLoaded()) {
            return false;
        }
        if (mendingEnabled == null) {
            try {
                Class<?> cfgClass = Class.forName("ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions");
                Field field = cfgClass.getField("enableMending");
                mendingEnabled = field.getBoolean(null);
            } catch (Throwable t) {
                mendingEnabled = false;
            }
        }
        return mendingEnabled;
    }

    public static boolean isMendingEnabledInEtFuturum() {
        return isEtFuturumMendingEnabled();
    }

    public static int getEtFuturumMendingId() {
        if (!isEtFuturumMendingEnabled()) {
            return -1;
        }
        if (mendingId == null) {
            try {
                Class<?> cfgClass = Class.forName("ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions");
                Field field = cfgClass.getField("mendingID");
                mendingId = field.getInt(null);
            } catch (Throwable t) {
                mendingId = -1;
            }
        }
        return mendingId != null ? mendingId : -1;
    }
}

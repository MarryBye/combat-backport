package com.marrybye.combatbackport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.marrybye.combatbackport.combat.CombatManager;
import com.marrybye.combatbackport.combat.enchantment.EnchantmentMending;
import com.marrybye.combatbackport.combat.enchantment.EnchantmentSweepingEdge;
import com.marrybye.combatbackport.combat.item.ItemShield;
import com.marrybye.combatbackport.combat.recipe.RecipeShieldDecorate;
import com.marrybye.combatbackport.network.CombatPacketHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy {

    public static class ConflictRecord {

        public final String enchantKey;
        public final int configuredId;
        public final int assignedId;
        public final String conflictingName;
        public final boolean isInvalid;

        public ConflictRecord(String enchantKey, int configuredId, int assignedId, String conflictingName,
            boolean isInvalid) {
            this.enchantKey = enchantKey;
            this.configuredId = configuredId;
            this.assignedId = assignedId;
            this.conflictingName = conflictingName;
            this.isInvalid = isInvalid;
        }
    }

    public static final List<ConflictRecord> conflictRecords = new ArrayList<>();
    private static final Map<UUID, Integer> pendingNotifications = new ConcurrentHashMap<>();
    private static int clientTickCount = 0;
    private static boolean clientNotified = false;

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        CombatPacketHandler.init();
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(CombatManager.INSTANCE);

        if (Config.enableShield) {
            CombatBackport.shield = new ItemShield();
            GameRegistry.registerItem(CombatBackport.shield, "shield");
        }

        if (Config.enableSweepingEdgeEnchantment) {
            registerSweepingEdgeEnchantment();
        }

        if (Config.enableMendingEnchantment) {
            if (com.marrybye.combatbackport.compat.EtFuturumCompat.isMendingEnabledInEtFuturum()) {
                int efrId = com.marrybye.combatbackport.compat.EtFuturumCompat.getEtFuturumMendingId();
                CombatBackport.LOG.info(
                    "Et Futurum Requiem detected with Mending enabled (ID: {}). Deferring to Et Futurum's Mending.",
                    efrId);
            } else {
                registerMendingEnchantment();
            }
        }

        CombatBackport.LOG.info("Combat Backport initialized at version " + Tags.VERSION);
    }

    private int findFreeEnchantmentId(int startId) {
        int maxListLength = Enchantment.enchantmentsList != null ? Enchantment.enchantmentsList.length : 256;
        for (int i = startId; i < maxListLength; i++) {
            if (Enchantment.enchantmentsList[i] == null) {
                return i;
            }
        }
        for (int i = 1; i < Math.min(startId, maxListLength); i++) {
            if (Enchantment.enchantmentsList[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private void registerSweepingEdgeEnchantment() {
        int configuredId = Config.sweepingEdgeEnchantmentId;
        int maxListLength = Enchantment.enchantmentsList != null ? Enchantment.enchantmentsList.length : 256;
        int finalId = configuredId;
        boolean conflict = false;
        String conflictingName = null;
        boolean invalid = false;

        if (configuredId < 0 || configuredId >= maxListLength) {
            conflict = true;
            invalid = true;
            finalId = -1;
        } else if (Enchantment.enchantmentsList[configuredId] != null) {
            conflict = true;
            Enchantment existing = Enchantment.enchantmentsList[configuredId];
            conflictingName = existing.getName();
            finalId = -1;
        }

        if (conflict) {
            finalId = findFreeEnchantmentId(64);
        }

        if (finalId != -1) {
            CombatBackport.sweepingEdgeEnchantment = new EnchantmentSweepingEdge(finalId, 2);
            CombatBackport.LOG.info("Registered Sweeping Edge enchantment with ID: {}", finalId);

            if (conflict) {
                conflictRecords.add(
                    new ConflictRecord(
                        "enchantment.combatbackport.sweeping",
                        configuredId,
                        finalId,
                        conflictingName,
                        invalid));
                if (invalid) {
                    CombatBackport.LOG.warn(
                        "Configured Sweeping Edge enchantment ID {} is invalid. Automatically reassigned to free ID {}.",
                        configuredId,
                        finalId);
                } else {
                    CombatBackport.LOG.warn(
                        "Sweeping Edge enchantment ID {} is occupied by '{}'. Automatically reassigned to free ID {}.",
                        configuredId,
                        conflictingName,
                        finalId);
                }
            }
        } else {
            conflictRecords.add(
                new ConflictRecord("enchantment.combatbackport.sweeping", configuredId, -1, conflictingName, invalid));
            CombatBackport.LOG
                .error("Could not find any free enchantment ID for Sweeping Edge! Enchantment registration skipped.");
        }
    }

    private void registerMendingEnchantment() {
        int configuredId = Config.mendingEnchantmentId;
        int maxListLength = Enchantment.enchantmentsList != null ? Enchantment.enchantmentsList.length : 256;
        int finalId = configuredId;
        boolean conflict = false;
        String conflictingName = null;
        boolean invalid = false;

        if (configuredId < 0 || configuredId >= maxListLength) {
            conflict = true;
            invalid = true;
            finalId = -1;
        } else if (Enchantment.enchantmentsList[configuredId] != null) {
            conflict = true;
            Enchantment existing = Enchantment.enchantmentsList[configuredId];
            conflictingName = existing.getName();
            finalId = -1;
        }

        if (conflict) {
            finalId = findFreeEnchantmentId(64);
        }

        if (finalId != -1) {
            CombatBackport.mendingEnchantment = new EnchantmentMending(finalId, 2);
            CombatBackport.LOG.info("Registered Mending enchantment with ID: {}", finalId);

            if (conflict) {
                conflictRecords.add(
                    new ConflictRecord(
                        "enchantment.combatbackport.mending",
                        configuredId,
                        finalId,
                        conflictingName,
                        invalid));
                if (invalid) {
                    CombatBackport.LOG.warn(
                        "Configured Mending enchantment ID {} is invalid. Automatically reassigned to free ID {}.",
                        configuredId,
                        finalId);
                } else {
                    CombatBackport.LOG.warn(
                        "Mending enchantment ID {} is occupied by '{}'. Automatically reassigned to free ID {}.",
                        configuredId,
                        conflictingName,
                        finalId);
                }
            }
        } else {
            conflictRecords.add(
                new ConflictRecord("enchantment.combatbackport.mending", configuredId, -1, conflictingName, invalid));
            CombatBackport.LOG
                .error("Could not find any free enchantment ID for Mending! Enchantment registration skipped.");
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!conflictRecords.isEmpty() && event.player != null) {
            pendingNotifications.put(event.player.getUniqueID(), 20);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player != null) {
            pendingNotifications.remove(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || conflictRecords.isEmpty() || event.player == null) {
            return;
        }

        if (event.side == Side.SERVER) {
            UUID uuid = event.player.getUniqueID();
            Integer ticks = pendingNotifications.get(uuid);
            if (ticks != null) {
                if (ticks <= 0) {
                    pendingNotifications.remove(uuid);
                    sendConflictNotifications(event.player);
                } else {
                    pendingNotifications.put(uuid, ticks - 1);
                }
            }
        } else if (event.side == Side.CLIENT && MinecraftServer.getServer() == null) {
            if (!clientNotified) {
                clientTickCount++;
                if (clientTickCount >= 20) {
                    clientNotified = true;
                    sendConflictNotifications(event.player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        clientNotified = false;
        clientTickCount = 0;
    }

    private void sendConflictNotifications(EntityPlayer player) {
        if (player == null) {
            return;
        }

        for (ConflictRecord record : conflictRecords) {
            IChatComponent enchantNameComp = new ChatComponentTranslation(record.enchantKey);

            if (record.assignedId == -1) {
                player.addChatMessage(
                    new ChatComponentTranslation(
                        "chat.combatbackport.enchant_id_none",
                        enchantNameComp,
                        record.configuredId));
            } else if (record.isInvalid) {
                player.addChatMessage(
                    new ChatComponentTranslation(
                        "chat.combatbackport.enchant_id_invalid",
                        enchantNameComp,
                        record.configuredId,
                        record.assignedId));
            } else {
                IChatComponent existingComp = record.conflictingName != null
                    ? new ChatComponentTranslation(record.conflictingName)
                    : new ChatComponentText("unknown");
                player.addChatMessage(
                    new ChatComponentTranslation(
                        "chat.combatbackport.enchant_id_conflict",
                        enchantNameComp,
                        record.configuredId,
                        existingComp,
                        record.assignedId));
            }
        }
    }

    public void init(FMLInitializationEvent event) {
        if (Config.enableShield && CombatBackport.shield != null) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(CombatBackport.shield),
                    "PIP",
                    "PPP",
                    " P ",
                    'P',
                    "plankWood",
                    'I',
                    Items.iron_ingot));

            RecipeSorter.register(
                "combatbackport:shield_decorate",
                RecipeShieldDecorate.class,
                RecipeSorter.Category.SHAPELESS,
                "after:minecraft:shapeless");
            GameRegistry.addRecipe(new RecipeShieldDecorate());

        }

        if (Config.enableMendingEnchantment && CombatBackport.mendingEnchantment != null
            && !com.marrybye.combatbackport.compat.EtFuturumCompat.isMendingEnabledInEtFuturum()) {
            ItemStack mendingBook = new ItemStack(Items.enchanted_book);
            Items.enchanted_book.addEnchantment(mendingBook, new EnchantmentData(CombatBackport.mendingEnchantment, 1));
            WeightedRandomChestContent loot = new WeightedRandomChestContent(mendingBook, 1, 1, 1);
            ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, loot);
            ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, loot);
            ChestGenHooks.addItem(ChestGenHooks.PYRAMID_DESERT_CHEST, loot);
            ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, loot);
            ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_LIBRARY, loot);
        }
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {}
}

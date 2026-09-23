package com.marrybye.combatbackport.api;

/**
 * Interface injected into EntityPlayer for tracking attack cooldown state.
 */
public interface ICombatPlayer {

    /**
     * Gets the current attack charge percentage in the range [0.0, 1.0].
     *
     * @param adjustTicks Partial ticks or adjustment offset.
     * @return Charge from 0.0 (just swung) to 1.0 (fully charged).
     */
    float getCooledAttackStrength(float adjustTicks);

    /**
     * Resets the attack cooldown timer to 0.
     */
    void resetAttackCooldown();

    /**
     * Gets the number of ticks elapsed since the last swing or item switch.
     */
    int getTicksSinceLastSwing();

    /**
     * Sets the number of ticks elapsed since the last swing.
     */
    void setTicksSinceLastSwing(int ticks);

    /**
     * Gets the cooldown period in ticks for the currently held item.
     */
    float getAttackCooldownPeriod();
}

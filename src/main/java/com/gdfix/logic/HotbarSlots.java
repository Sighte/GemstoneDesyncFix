package com.gdfix.logic;

/**
 * Pure helper (no Minecraft dependencies) for the Break Reset Fix slot test.
 *
 * <p>In the player's own inventory menu (container id 0) the nine hotbar slots are the
 * container indices 36..44, and container slot {@code s} corresponds to hotbar index
 * {@code s - 36}. Break Reset Fix only cares about a server update to the slot the player
 * is currently holding, so the item swap does not reset the active mining swing.
 */
public final class HotbarSlots {

    public static final int FIRST_HOTBAR_SLOT = 36;
    public static final int LAST_HOTBAR_SLOT = 44;

    private HotbarSlots() {
    }

    /**
     * @param containerId  the packet's container id
     * @param slot         the container slot that was updated
     * @param selectedSlot the player's currently selected hotbar index (0..8)
     * @return {@code true} if this update targets the held hotbar slot of the player inventory
     */
    public static boolean isHeldHotbarSlot(int containerId, int slot, int selectedSlot) {
        return containerId == 0
                && slot >= FIRST_HOTBAR_SLOT
                && slot <= LAST_HOTBAR_SLOT
                && selectedSlot == slot - FIRST_HOTBAR_SLOT;
    }
}

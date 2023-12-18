package app.miyuki.miyukihopperfilter.listener;

import app.miyuki.miyukihopperfilter.MiyukiHopperFilter;
import com.google.common.collect.ImmutableList;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.DoubleChestInventory;

import java.util.Collections;
import java.util.List;

public class HopperFilterListener implements Listener {

    private final boolean blacklistModeEnabled;

    private final boolean whitelistModeEnabled;

    private final boolean chestFilterEnabled;

    public HopperFilterListener(MiyukiHopperFilter plugin) {
        var config = plugin.getConfig();
        blacklistModeEnabled = config.getBoolean("modes.blacklist");
        whitelistModeEnabled = config.getBoolean("modes.whitelist");
        chestFilterEnabled = config.getBoolean("chest-filter");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHopperItemMove(InventoryMoveItemEvent event) {
        var inventoryTarget = event.getDestination();
        var inventoryTargetOwnerLocation = inventoryTarget.getLocation();

        if (inventoryTargetOwnerLocation == null || event.getSource().getType() != InventoryType.HOPPER)
            return;

        var targetBlock = inventoryTargetOwnerLocation.getBlock();

        var attachedItemFrames = getAttachedItemFramesWithItemInside(targetBlock);
        if (attachedItemFrames.isEmpty())
            return;

        var canPass = attachedItemFrames.stream().anyMatch(itemFrame -> canPass(event.getItem().getType(), itemFrame));
        if (!canPass)
            event.setCancelled(true);
    }

    private static final List<Rotation> BLACKLIST_ROTATIONS = List.of(
            Rotation.CLOCKWISE_135,
            Rotation.CLOCKWISE_45,
            Rotation.COUNTER_CLOCKWISE_45,
            Rotation.FLIPPED_45
    );

    public boolean canPass(Material itemType, ItemFrame itemFrame) {

        var itemFrameItemType = itemFrame.getItem().getType();

        if (!BLACKLIST_ROTATIONS.contains(itemFrame.getRotation()) && whitelistModeEnabled)
            return itemFrameItemType == itemType;
        else if (blacklistModeEnabled)
            return itemFrameItemType != itemType;

        return false;
    }

    private List<ItemFrame> getAttachedItemFramesWithItemInside(Block target) {
        return getAttachedItemFramesWithItemInside(target, true);
    }

    private List<ItemFrame> getAttachedItemFramesWithItemInside(Block target, boolean checkChest) {

        var state = target.getState();

        if (chestFilterEnabled && checkChest && state instanceof Chest chest && chest.getInventory() instanceof DoubleChestInventory doubleChestInventory) {
            var doubleChest = doubleChestInventory.getHolder();
            if (doubleChest == null)
                return Collections.emptyList();

            var rightChest = (Chest) doubleChest.getRightSide();
            var leftChest = (Chest) doubleChest.getLeftSide();
            if (rightChest == null || leftChest == null)
                return Collections.emptyList();

            var rightChestLocation = rightChest.getLocation();
            var leftChestLocation = leftChest.getLocation();

            var rightChestItemFrames = getAttachedItemFramesWithItemInside(rightChestLocation.getBlock(), false);
            var leftChestItemFrames = getAttachedItemFramesWithItemInside(leftChestLocation.getBlock(), false);

            return ImmutableList.<ItemFrame>builder()
                    .addAll(rightChestItemFrames)
                    .addAll(leftChestItemFrames)
                    .build();
        }

        if (chestFilterEnabled) {
            if (target.getType() != Material.HOPPER
                    && target.getType() != Material.CHEST
                    && target.getType() != Material.TRAPPED_CHEST)
                return Collections.emptyList();
        } else {
            if (target.getType() != Material.HOPPER)
                return Collections.emptyList();
        }

        return target.getWorld().getNearbyEntities(target.getLocation(), 2, 1, 2, entity -> entity.getType() == EntityType.ITEM_FRAME)
                .stream()
                .map(ItemFrame.class::cast)
                .filter(itemFrame -> !itemFrame.getItem().getType().isAir())
                .filter(itemFrame -> itemFrame.getLocation().getBlock().getRelative(itemFrame.getAttachedFace()).equals(target))
                .toList();
    }

}

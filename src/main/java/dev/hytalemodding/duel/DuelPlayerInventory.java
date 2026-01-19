package dev.hytalemodding.duel;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Armazena o inventário e posição de um jogador antes do duelo
 */
public class DuelPlayerInventory {
    private final Vector3d position;
    private final List<ItemStack> storageItems;
    private final List<ItemStack> hotbarItems;
    private final List<ItemStack> armorItems;
    private final List<ItemStack> utilityItems;
    private final byte activeHotbarSlot;
    
    public DuelPlayerInventory(Vector3d position,
                              ItemContainer storage, ItemContainer hotbar, 
                              ItemContainer armor, ItemContainer utility,
                              byte activeHotbarSlot) {
        // Salvar apenas a posição (coordenadas)
        this.position = new Vector3d(position.getX(), position.getY(), position.getZ());
        
        // Copiar inventário
        this.storageItems = copyContainer(storage);
        this.hotbarItems = copyContainer(hotbar);
        this.armorItems = copyContainer(armor);
        this.utilityItems = copyContainer(utility);
        this.activeHotbarSlot = activeHotbarSlot;
    }
    
    private List<ItemStack> copyContainer(ItemContainer container) {
        List<ItemStack> items = new ArrayList<>();
        for (short i = 0; i < container.getCapacity(); i++) {
            ItemStack item = container.getItemStack(i);
            // Copiar o item criando um novo ItemStack
            if (item != null) {
                items.add(new ItemStack(item.getItemId(), item.getQuantity(), 
                    item.getDurability(), item.getMaxDurability(), item.getMetadata()));
            } else {
                items.add(null);
            }
        }
        return items;
    }
    
    public Vector3d getPosition() {
        return position;
    }
    
    public List<ItemStack> getStorageItems() {
        return storageItems;
    }
    
    public List<ItemStack> getHotbarItems() {
        return hotbarItems;
    }
    
    public List<ItemStack> getArmorItems() {
        return armorItems;
    }
    
    public List<ItemStack> getUtilityItems() {
        return utilityItems;
    }
    
    public byte getActiveHotbarSlot() {
        return activeHotbarSlot;
    }
}

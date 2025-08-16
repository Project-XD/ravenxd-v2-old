package keystrokesmod.module.impl.player;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keystrokesmod.event.network.AttackEntityEvent;
import keystrokesmod.event.network.SendPacketEvent;
import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.*;
import lombok.Getter;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import keystrokesmod.eventbus.annotations.EventListener;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class InvManager extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"OpenInv", "SpoofInv", "Basic"}, 1);
    private final ButtonSetting notWhileMoving = new ButtonSetting("Not while moving", false, new ModeOnly(mode, 1, 2));
    private final SliderSetting minStartDelay = new SliderSetting("Min start delay", 100, 0, 500, 10, "ms");
    private final SliderSetting maxStartDelay = new SliderSetting("Max start delay", 200, 0, 500, 10, "ms");
    private final ButtonSetting armor = new ButtonSetting("Armor", false);
    private final SliderSetting minArmorDelay = new SliderSetting("Min armor delay", 100, 0, 500, 10, "ms", armor::isToggled);
    private final SliderSetting maxArmorDelay = new SliderSetting("Max armor delay", 150, 0, 500, 10, "ms", armor::isToggled);
    private final SliderSetting maxBlocks = new SliderSetting("Max blocks", 512, -1, 512, 1);
    private final SliderSetting maxArrows = new SliderSetting("Max arrows", 64, -1, 512, 1);
    private final SliderSetting maxThrowable = new SliderSetting("Max throwable", 32, -1, 512, 16);
    private final SliderSetting minCleanDelay = new SliderSetting("Min clean delay", 100, 0, 500, 10, "ms");
    private final SliderSetting maxCleanDelay = new SliderSetting("Max clean delay", 150, 0, 500, 10, "ms");
    private final ButtonSetting sort = new ButtonSetting("Sort", false);
    private final SliderSetting minSortDelay = new SliderSetting("Min sort delay", 100, 0, 500, 10, "ms", sort::isToggled);
    private final SliderSetting maxSortDelay = new SliderSetting("Max sort delay", 100, 0, 500, 10, "ms", sort::isToggled);
    private final SliderSetting swordSlot = new SliderSetting("Sword slot", 1, 0, 9, 1, sort::isToggled);
    private final SliderSetting blockSlot = new SliderSetting("Block slot", 2, 0, 9, 1, sort::isToggled);
    private final SliderSetting bowSlot = new SliderSetting("Bow slot", 4, 0, 9, 1, sort::isToggled);
    private final SliderSetting foodSlot = new SliderSetting("Food slot", 5, 0, 9, 1, sort::isToggled);
    private final SliderSetting rodSlot = new SliderSetting("Rod slot", 7, 0, 9, 1, sort::isToggled);
    private final ButtonSetting shuffle = new ButtonSetting("Shuffle", false);

    private static final int INVENTORY_ROWS = 4, INVENTORY_COLUMNS = 9, ARMOR_SLOTS = 4;
    private static final int INVENTORY_SLOTS = (INVENTORY_ROWS * INVENTORY_COLUMNS) + ARMOR_SLOTS;

    private final CoolDown stopwatch = new CoolDown(0);
    private int chestTicks, attackTicks, placeTicks;
    @Getter
    private boolean moved, open;
    private long nextClick;
    private final List<Integer> blockSlots = new IntArrayList();
    private final List<Integer> arrowSlots = new IntArrayList();
    private final List<Integer> snowballEggSlots = new IntArrayList();
    private final List<ItemStackWithNumber> blockStacks = new ObjectArrayList<>();
    private final List<ItemStackWithNumber> foodStacks = new ObjectArrayList<>();

    public InvManager() {
        super("InvManager", category.player);
        this.registerSetting(
                mode, notWhileMoving,
                minStartDelay, maxStartDelay,
                armor, minArmorDelay, maxArmorDelay,
                maxBlocks, maxArrows, maxThrowable,
                minCleanDelay, maxCleanDelay,
                sort, minSortDelay, maxSortDelay,
                swordSlot, blockSlot,bowSlot, foodSlot, rodSlot,
                shuffle
        );
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minStartDelay, maxStartDelay);
        Utils.correctValue(minArmorDelay, maxArmorDelay);
        Utils.correctValue(minCleanDelay, maxCleanDelay);
        Utils.correctValue(minSortDelay, maxSortDelay);
    }

    @EventListener
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.ticksExisted <= 40) {
            return;
        }

        if (mc.currentScreen instanceof GuiChest) {
            this.chestTicks = 0;
        } else {
            this.chestTicks++;
        }

        this.attackTicks++;
        this.placeTicks++;

        // Calls stopwatch.reset() to simulate opening an inventory, checks for an open inventory to be legit.
        if (mode.getInput() == 0 && !(mc.currentScreen instanceof GuiInventory)) {
            this.stopwatch.start();
            return;
        }

        stopwatch.setCooldown(nextClick);
        if (!this.stopwatch.hasFinished() || this.chestTicks < 10 || this.attackTicks < 10 || this.placeTicks < 10) {
            this.closeInventory();
            return;
        }

        this.moved = false;

        int helmet = -1;
        int chestplate = -1;
        int leggings = -1;
        int boots = -1;

        int sword = -1;
        int pickaxe = -1;
        int axe = -1;
        int shovel = -1;
        int bow = -1;
        int rod = -1;
        blockStacks.clear();
        foodStacks.clear();

        int totalBlocks = 0, totalArrows = 0, totalSnowballsEggs = 0;

        blockSlots.clear();
        arrowSlots.clear();
        snowballEggSlots.clear();

        List<Integer> inventorySlots = new IntArrayList(IntStream.range(0, INVENTORY_SLOTS).toArray());
        if (shuffle.isToggled())
            Collections.shuffle(inventorySlots);

        for (int i : inventorySlots) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (!ContainerUtils.useful(stack) && item != Items.compass) {
                this.throwItem(i);
            }

            if (item == Items.arrow) {
                totalArrows += stack.stackSize;
                arrowSlots.add(i);
            } else if (item == Items.snowball || item == Items.egg) {
                totalSnowballsEggs += stack.stackSize;
                snowballEggSlots.add(i);
            }

            if (item instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor) item;
                final int reduction = this.armorReduction(stack);
                switch (armor.armorType) {
                    case 0:
                        if (helmet == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(helmet))) {
                            helmet = i;
                        }
                        break;

                    case 1:
                        if (chestplate == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(chestplate))) {
                            chestplate = i;
                        }
                        break;

                    case 2:
                        if (leggings == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(leggings))) {
                            leggings = i;
                        }
                        break;

                    case 3:
                        if (boots == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(boots))) {
                            boots = i;
                        }
                        break;
                }
            }

            if (item instanceof ItemSword && (int) swordSlot.getInput() != 0) {
                if (sword == -1) {
                    sword = i;
                } else if (damage(stack) > damage(mc.thePlayer.inventory.getStackInSlot(sword))) {
                    sword = i;
                }

                if (i != sword) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemPickaxe) {
                if (pickaxe == -1) {
                    pickaxe = i;
                } else if (mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(pickaxe))) {
                    pickaxe = i;
                }
                if (i != pickaxe) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemAxe) {
                if (axe == -1) {
                    axe = i;
                } else if (mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(axe))) {
                    axe = i;
                }

                if (i != axe) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemSpade) {
                if (shovel == -1) {
                    shovel = i;
                } else if (mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(shovel))) {
                    shovel = i;
                }

                if (i != shovel) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemBlock) {
                totalBlocks += stack.stackSize;
                blockSlots.add(i);
                blockStacks.add(new ItemStackWithNumber(stack, i));
            }

            if (item instanceof ItemBow) {
                if (bow == -1) {
                    bow = i;
                } else if (power(stack) > power(mc.thePlayer.inventory.getStackInSlot(bow))) {
                    bow = i;
                }

                if (i != bow) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemFood && (int) foodSlot.getInput() != 0) {
                foodStacks.add(new ItemStackWithNumber(stack, i));
            }

            if (item instanceof ItemFishingRod) {
                if (rod == -1) {
                    rod = i;
                }

                if (i != rod) {
                    this.throwItem(i);
                }
            }
        }

        if (armor.isToggled()) {
            for (int i = 0; i < INVENTORY_SLOTS; i++) {
                final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

                if (stack == null) {
                    continue;
                }

                final Item item = stack.getItem();

                if (item instanceof ItemArmor) {
                    final ItemArmor armor = (ItemArmor) item;

                    switch (armor.armorType) {
                        case 0:
                            if (i != helmet) {
                                this.throwItem(i);
                            }
                            break;

                        case 1:
                            if (i != chestplate) {
                                this.throwItem(i);
                            }
                            break;

                        case 2:
                            if (i != leggings) {
                                this.throwItem(i);
                            }
                            break;

                        case 3:
                            if (i != boots) {
                                this.throwItem(i);
                            }
                            break;
                    }
                }
            }

            if (helmet != -1 && helmet != 39) {
                this.equipItem(helmet);
            }

            if (chestplate != -1 && chestplate != 38) {
                this.equipItem(chestplate);
            }

            if (leggings != -1 && leggings != 37) {
                this.equipItem(leggings);
            }

            if (boots != -1 && boots != 36) {
                this.equipItem(boots);
            }
        }

        if (sort.isToggled()) {
            blockStacks.sort((a, b) -> {
                int sizeA = b.getStackSize();
                int sizeB = a.getStackSize();
                if (sizeA == sizeB) {
                    return Integer.compare(ContainerUtils.hashItem(b.getItemStack()),
                            ContainerUtils.hashItem(a.getItemStack()));
                }
                return Integer.compare(sizeA, sizeB);
            });

            if (!blockStacks.isEmpty()) {
                int slot = blockStacks.get(0).getNumber();
                if (slot != this.blockSlot.getInput() - 1)  // 修复老虎机
                    this.moveItem(slot, (int) this.blockSlot.getInput() - 37);
            }
        }

        if (totalBlocks > (int) maxBlocks.getInput()) {
            int excessBlocks = totalBlocks - (int) maxBlocks.getInput();

            for (int slot : Utils.reversed(blockSlots)) {
                if (excessBlocks <= 0) {
                    break;
                }
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);

                int stackSize = stack.stackSize;

                if (excessBlocks >= stackSize) {
                    this.throwItem(slot);
                    excessBlocks -= stackSize;
                } else {
                    this.throwItem(slot, stackSize - excessBlocks);
                    excessBlocks = 0;
                }
            }
        }

        if (totalArrows > (int) maxArrows.getInput()) {
            int excessArrows = totalArrows - (int) maxArrows.getInput();
            for (int slot : arrowSlots) {
                if (excessArrows <= 0) {
                    break;
                }
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
                int stackSize = stack.stackSize;
                if (excessArrows >= stackSize) {
                    this.throwItem(slot);
                    excessArrows -= stackSize;
                } else {
                    this.throwItem(slot, stackSize - excessArrows);
                    excessArrows = 0;
                }
            }
        }

        if (totalSnowballsEggs > (int) maxThrowable.getInput()) {
            int excessSnowballsEggs = totalSnowballsEggs - (int) maxThrowable.getInput();
            for (int slot : snowballEggSlots) {
                if (excessSnowballsEggs <= 0) {
                    break;
                }
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
                int stackSize = stack.stackSize;
                if (excessSnowballsEggs >= stackSize) {
                    this.throwItem(slot);
                    excessSnowballsEggs -= stackSize;
                } else {
                    this.throwItem(slot, stackSize - excessSnowballsEggs);
                    excessSnowballsEggs = 0;
                }
            }
        }

        if (sort.isToggled() && sword != -1 && sword != (int) swordSlot.getInput() - 1 && (int) swordSlot.getInput() != 0) {
            this.moveItem(sword, (int) swordSlot.getInput() - 37);
        }

        if (sort.isToggled() && bow != -1 && bow != (int) bowSlot.getInput() - 1 && (int) bowSlot.getInput() != 0) {
            this.moveItem(bow, (int) bowSlot.getInput() - 37);
        }

        if (sort.isToggled() && rod != -1 && rod != (int) rodSlot.getInput() - 1 && (int) rodSlot.getInput() != 0
                && (bow == -1 && (int) rodSlot.getInput() == (int) bowSlot.getInput())) {
            this.moveItem(rod, (int) rodSlot.getInput() - 37);
        }

        if (sort.isToggled()) {
            foodStacks.sort((a, b) -> {
                ItemFood foodA = (ItemFood) a.getItemStack().getItem();
                ItemFood foodB = (ItemFood) b.getItemStack().getItem();

                float priceA = foodB.getSaturationModifier(b.getItemStack());
                float priceB = foodA.getSaturationModifier(a.getItemStack());
                if (priceA == priceB)
                    return Float.compare(b.getStackSize(), a.getStackSize());
                return Float.compare(priceA, priceB);
            });

            if (!foodStacks.isEmpty()) {
                int slot = foodStacks.get(0).getNumber();
                if (slot != this.foodSlot.getInput() - 1)  // 修复老虎机
                    this.moveItem(slot, (int) foodSlot.getInput() - 37);
            }
        }

        if (this.canOpenInventory() && !this.moved) {
            this.closeInventory();
        }
    }

    @EventListener
    public void onAttack(AttackEntityEvent event) {
        this.attackTicks = 0;
    }

    @Override
    public void onDisable() {
        if (this.canOpenInventory()) {
            this.closeInventory();
        }
    }

    private void openInventory() {
        if (!this.open) {
            if ((int) mode.getInput() != 2)
                PacketUtils.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.open = true;
        }
    }

    private void closeInventory() {
        if (this.open) {
            if ((int) mode.getInput() != 2)
                PacketUtils.sendPacket(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            this.open = false;
        }
    }

    private boolean canOpenInventory() {
        switch ((int) mode.getInput()) {
            default:
            case 0:
                return false;
            case 1:
            case 2:
                return !notWhileMoving.isToggled() || !MoveUtil.isMoving();
        }
    }

    private void throwItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0)) {
            if (this.canOpenInventory()) {
                this.openInventory();
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 4, mc.thePlayer);

            this.nextClick = Math.round(Utils.randomizeDouble(minCleanDelay.getInput(), maxCleanDelay.getInput()));
            this.stopwatch.start();
            this.moved = true;
        }
    }

    private void throwItem(final int slot, final int amountLeft) {
        if ((!this.moved || this.nextClick <= 0)) {
            if (this.canOpenInventory()) {
                this.openInventory();
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 0, mc.thePlayer);

            for (int i = 0; i < amountLeft; i++) {
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 0, mc.thePlayer);
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, -999, 0, 0, mc.thePlayer);

            this.nextClick = Math.round(Utils.randomizeDouble(minCleanDelay.getInput(), maxCleanDelay.getInput()));
            this.stopwatch.start();
            this.moved = true;
        }
    }

    private void moveItem(final int slot, final int destination) {
        if ((!this.moved || this.nextClick <= 0)) {

            if (this.canOpenInventory()) {
                this.openInventory();
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), this.slot(destination), 2, mc.thePlayer);

            this.nextClick = Math.round(Utils.randomizeDouble(minSortDelay.getInput(), maxSortDelay.getInput()));
            this.stopwatch.start();
            this.moved = true;
        }
    }

    private void equipItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0)) {

            if (this.canOpenInventory()) {
                this.openInventory();
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 1, mc.thePlayer);

            this.nextClick = Math.round(Utils.randomizeDouble(minArmorDelay.getInput(), maxArmorDelay.getInput()));
            this.stopwatch.start();
            this.moved = true;
        }
    }

    private float damage(final @NotNull ItemStack stack) {
        final ItemSword sword = (ItemSword) stack.getItem();
        final int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        return (float) (sword.getDamageVsEntity() + level * 1.25);
    }

    private float power(final ItemStack stack) {
        final int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        return (float) level;
    }

    private float mineSpeed(final @NotNull ItemStack stack) {
        final Item item = stack.getItem();
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);

        switch (level) {
            case 1:
                level = 30;
                break;

            case 2:
                level = 69;
                break;

            case 3:
                level = 120;
                break;

            case 4:
                level = 186;
                break;

            case 5:
                level = 271;
                break;

            default:
                level = 0;
                break;
        }

        if (item instanceof ItemPickaxe) {
            final ItemPickaxe pickaxe = (ItemPickaxe) item;
            return pickaxe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemSpade) {
            final ItemSpade shovel = (ItemSpade) item;
            return shovel.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemAxe) {
            final ItemAxe axe = (ItemAxe) item;
            return axe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        }

        return 0;
    }

    private int armorReduction(final ItemStack stack) {
        final ItemArmor armor = (ItemArmor) stack.getItem();
        return armor.damageReduceAmount + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[]{stack}, DamageSource.generic);
    }

    private int slot(final int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }

        if (slot < 9) {
            return slot + 36;
        }

        return slot;
    }
    
    @EventListener
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.getPacket();

            if (!(packet.getStack() != null
                    && packet.getStack().getItem() == Items.water_bucket)) {
                this.placeTicks = 0;
            }
        }
    }

    @Override
    public String getInfo() {
        return mode.getSelected();
    }

    @Getter
    private static final class ItemStackWithNumber {
        private final ItemStack itemStack;
        private final int number;

        public ItemStackWithNumber(ItemStack itemStack, int number) {
            this.itemStack = itemStack;
            this.number = number;
        }

        public int getStackSize() {
            return itemStack.stackSize;
        }
    }
}

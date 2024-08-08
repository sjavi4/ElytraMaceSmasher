package me.autobot.elytramace.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode_MaceAttack {

    @Shadow
    @Final
    private ClientPacketListener connection;
    @Unique
    private boolean smash = false;

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;ensureHasSentCarriedItem()V", shift = At.Shift.AFTER))
    public void onAttackWithMace(Player player, Entity entity, CallbackInfo ci) {
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.MACE)) {
            return;
        }
        if (!player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            return;
        }
        if (player.isFallFlying() && player.fallDistance > 1.5F) {
            smash = true;
            this.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
    }

    @Inject(method = "attack", at = @At(value = "TAIL"))
    public void onAttackFinishedWithMace(Player player, Entity entity, CallbackInfo ci) {
        if (smash) {
            smash = false;
            this.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
    }
}

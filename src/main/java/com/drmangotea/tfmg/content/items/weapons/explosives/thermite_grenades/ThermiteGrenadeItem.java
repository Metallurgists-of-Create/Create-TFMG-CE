package com.drmangotea.tfmg.content.items.weapons.explosives.thermite_grenades;

import com.drmangotea.tfmg.registry.TFMGEntityTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ThermiteGrenadeItem extends Item {
    public final ThermiteGrenade.ChemicalColor flameColor;

    public ThermiteGrenadeItem(Properties p, ThermiteGrenade.ChemicalColor color) {
        super(p);
        this.flameColor = color;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, 60);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
		
		ThermiteGrenade grenade = switch (flameColor) {
			case GREEN -> new ThermiteGrenade(level, player, flameColor, TFMGEntityTypes.ZINC_GRENADE.get());
			case BLUE ->  new ThermiteGrenade(level, player, flameColor, TFMGEntityTypes.COPPER_GRENADE.get());
			case BASE ->  new ThermiteGrenade(level, player, flameColor, TFMGEntityTypes.THERMITE_GRENADE.get());
		};
		grenade.setItem(itemstack);
		grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.5F, 1.0F);
		level.addFreshEntity(grenade);


        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}

package com.drmangotea.tfmg.content.items.weapons.explosives.thermite_grenades;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.spark.Spark;
import com.drmangotea.tfmg.registry.TFMGEntityTypes;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThermiteGrenade extends ThrowableItemProjectile {
    public final ChemicalColor flameColor;

    public ThermiteGrenade(EntityType<? extends ThermiteGrenade> type, Level level) {
        super(type, level);
        this.flameColor = ChemicalColor.BLUE;
    }

    public ThermiteGrenade(Level level, LivingEntity p_37400_, ChemicalColor color, EntityType type) {
        super(type, p_37400_, level);
        this.flameColor = color;
    }

    protected Item getDefaultItem() {
        return TFMGItems.THERMITE_GRENADE.get();
    }

    private ParticleOptions getParticle() {
        return ParticleTypes.FLAME;
    }

    public void handleEntityEvent(byte p_37402_) {
        if (p_37402_ == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    protected void onHitEntity(EntityHitResult p_37404_) {
        super.onHitEntity(p_37404_);
    }

    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
		
		Level level = level();
		level.broadcastEntityEvent(this, (byte) 3);
		
		EntityEntry<? extends Spark> sparkType = switch (flameColor) {
		    case BASE -> TFMGEntityTypes.SPARK;
		    case GREEN -> TFMGEntityTypes.GREEN_SPARK;
		    case BLUE -> TFMGEntityTypes.BLUE_SPARK;
	    };

		for (int i=0; i<20;i++){
			float x= TFMG.RANDOM.nextFloat(360);
			float y= TFMG.RANDOM.nextFloat(360);
			float z= TFMG.RANDOM.nextFloat(360);

			Spark spark = sparkType.create(level);
			if (spark == null) continue;
			spark.moveTo(this.getX(), this.getY()+1, this.getZ());
			spark.shootFromRotation(this,x,y,z,0.2f,1);
			level.addFreshEntity(spark);
        }
		
		level.explode(this, this.getX(), this.getY(0.0625D), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
		this.discard();
    }

    @SuppressWarnings("unchecked")
    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        EntityType.Builder<ThermiteGrenade> entityBuilder = (EntityType.Builder<ThermiteGrenade>) builder;
        return entityBuilder.sized(.25f, .25f);
    }

    public enum ChemicalColor {
        BASE,
        GREEN,
        BLUE
    }
}
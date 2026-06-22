package so.alaz.cleansingpotions.entity;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.core.CleanseMode;

public class CleansingCloudEntity extends AreaEffectCloud {

    private static final int DURATION_TICKS = 600;
    private static final int WAIT_TICKS = 10;
    private static final int CLEANSE_INTERVAL_TICKS = 20;

    private CleanseMode mode = CleanseMode.ALL;
    private int sinceCleanse = 0;

    public CleansingCloudEntity(EntityType<? extends CleansingCloudEntity> type, Level level) {
        super(type, level);
    }

    public CleansingCloudEntity(Level level, double x, double y, double z) {
        this(ModEntities.CLEANSING_CLOUD, level);
        setPos(x, y, z);
    }

    public void setMode(CleanseMode mode) {
        this.mode = mode;
    }

    public void configure(float radius, int color) {
        setRadius(radius);
        setDuration(DURATION_TICKS);
        setWaitTime(WAIT_TICKS);
        setRadiusPerTick(-radius / (float) DURATION_TICKS);
        // A harmless water potion keeps the base cloud alive while it applies nothing of its own.
        setPotionContents(new PotionContents(Potions.WATER));
        // Entity-effect particles need an opaque color, or they render fully transparent.
        setCustomParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000 | color));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || !isAlive() || tickCount < WAIT_TICKS) {
            return;
        }
        if (++sinceCleanse < CLEANSE_INTERVAL_TICKS) {
            return;
        }
        sinceCleanse = 0;
        float r = getRadius();
        if (r <= 0) {
            return;
        }
        AABB box = new AABB(getX() - r, getY() - r, getZ() - r, getX() + r, getY() + r, getZ() + r);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.distanceToSqr(this) <= r * r) {
                CleansingPotions.cleanser().cleanse(entity, mode);
            }
        }
    }
}

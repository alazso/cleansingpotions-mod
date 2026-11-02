package so.alaz.cleansingpotions.entity;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.core.CleanseMode;

public class CleansingCloudEntity extends AreaEffectCloud {

    private static final int DURATION_TICKS = 600;
    private static final int WAIT_TICKS = 10;
    private static final int CLEANSE_INTERVAL_TICKS = 20;
    private static final String MODE_KEY = "cleanse_mode";

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
        // Entity-effect particles need an opaque color, or they render fully transparent.
        setCustomParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000 | color));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(MODE_KEY, mode.tag());
    }

    /** Reads the saved mode. A missing, blank, or unknown value loads as {@link CleanseMode#ALL}. */
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        mode = CleanseMode.fromConfig(input.getStringOr(MODE_KEY, ""), CleanseMode.ALL);
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
        CleansingPotions.cleanser().cleanseArea(level(), getX(), getY(), getZ(), r, mode);
    }
}

package so.alaz.cleansingpotions.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import so.alaz.cleansingpotions.Constants;

public final class ModEntities {

    public static final ResourceKey<EntityType<?>> CLEANSING_POTION_KEY = key("cleansing_potion");
    public static final ResourceKey<EntityType<?>> CLEANSING_CLOUD_KEY = key("cleansing_cloud");

    public static EntityType<CleansingPotionEntity> CLEANSING_POTION;
    public static EntityType<CleansingCloudEntity> CLEANSING_CLOUD;

    public static void build() {
        if (CLEANSING_POTION != null) {
            return;
        }
        CLEANSING_POTION = EntityType.Builder
            .<CleansingPotionEntity>of(CleansingPotionEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .build(CLEANSING_POTION_KEY);
        CLEANSING_CLOUD = EntityType.Builder
            .<CleansingCloudEntity>of(CleansingCloudEntity::new, MobCategory.MISC)
            .sized(6.0F, 0.5F)
            .build(CLEANSING_CLOUD_KEY);
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    private ModEntities() {
    }
}

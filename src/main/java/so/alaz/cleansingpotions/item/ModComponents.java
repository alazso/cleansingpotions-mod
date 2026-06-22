package so.alaz.cleansingpotions.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import so.alaz.cleansingpotions.Constants;
import so.alaz.cleansingpotions.core.CleanseMode;

public final class ModComponents {

    public static final ResourceLocation CLEANSE_MODE_ID =
        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "cleanse_mode");

    public static final Codec<CleanseMode> MODE_CODEC =
        Codec.STRING.xmap(ModComponents::parse, CleanseMode::tag);

    public static final StreamCodec<ByteBuf, CleanseMode> MODE_STREAM_CODEC =
        ByteBufCodecs.STRING_UTF8.map(ModComponents::parse, CleanseMode::tag);

    public static final DataComponentType<CleanseMode> CLEANSE_MODE =
        DataComponentType.<CleanseMode>builder()
            .persistent(MODE_CODEC)
            .networkSynchronized(MODE_STREAM_CODEC)
            .build();

    private static CleanseMode parse(String raw) {
        CleanseMode mode = CleanseMode.fromTag(raw);
        return mode == null ? CleanseMode.ALL : mode;
    }

    private ModComponents() {
    }
}

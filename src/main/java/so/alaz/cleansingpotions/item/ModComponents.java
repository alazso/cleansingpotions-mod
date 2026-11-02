package so.alaz.cleansingpotions.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.core.CleanseMode;

public final class ModComponents {

    public static final Identifier CLEANSE_MODE_ID = CleansingPotions.id("cleanse_mode");

    private static final Codec<CleanseMode> MODE_CODEC =
        Codec.STRING.xmap(CleanseMode::fromTag, CleanseMode::tag);

    private static final StreamCodec<ByteBuf, CleanseMode> MODE_STREAM_CODEC =
        ByteBufCodecs.STRING_UTF8.map(CleanseMode::fromTag, CleanseMode::tag);

    public static final DataComponentType<CleanseMode> CLEANSE_MODE =
        DataComponentType.<CleanseMode>builder()
            .persistent(MODE_CODEC)
            .networkSynchronized(MODE_STREAM_CODEC)
            .build();

    private ModComponents() {
    }
}

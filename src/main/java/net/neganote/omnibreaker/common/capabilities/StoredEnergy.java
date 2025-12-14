package net.neganote.omnibreaker.common.capabilities;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

public record StoredEnergy(int energy) {

    public static final Codec<StoredEnergy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("energy").forGetter(StoredEnergy::energy)).apply(instance, StoredEnergy::new));

    public static final StreamCodec<ByteBuf, StoredEnergy> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT,
            StoredEnergy::energy, StoredEnergy::new);
}

package me.Jonathon594.Mythria.Genetic;

import me.Jonathon594.Mythria.Managers.GeneSerializers;
import net.minecraft.nbt.CompoundNBT;

public class IntStatGene extends Gene {
    private final int value;

    public IntStatGene(GeneType type, int value) {
        super(type);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public GeneSerializer<IntStatGene> getSerializer() {
        return GeneSerializers.INT_STAT;
    }

    @Override
    public CompoundNBT toNBT(boolean writeSerializer) {
        return getSerializer().serialize(this, writeSerializer);
    }
}
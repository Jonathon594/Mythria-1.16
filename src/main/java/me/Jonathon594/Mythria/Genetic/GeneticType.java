package me.Jonathon594.Mythria.Genetic;

import me.Jonathon594.Mythria.Skin.SkinPart;
import me.Jonathon594.Mythria.SpawnGifts.SpawnGift;
import me.Jonathon594.Mythria.DataTypes.SpawnPos;
import me.Jonathon594.Mythria.Skin.SkinParts;
import me.Jonathon594.Mythria.MythriaRegistries;
import me.Jonathon594.Mythria.Util.MythriaResourceLocation;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class GeneticType extends ForgeRegistryEntry<GeneticType> {
    private final String displayName;
    private final SpawnPos spawnPos;
    private final Supplier<Genetic> factory;
    private SkinPart.Type specialSkinPartType = null;
    private RegistryKey<World> spawnDimension = World.OVERWORLD;
    private Genetic instance;
    private int startingAge;

    public GeneticType(String name, String displayName, SpawnPos spawnPos, Supplier<Genetic> factory, int startingAge) {
        setRegistryName(new MythriaResourceLocation(name));
        this.displayName = displayName;
        this.spawnPos = spawnPos;
        this.factory = factory;
        this.instance = factory.get();
        this.startingAge = startingAge;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SpawnPos getSpawnPos() {
        return spawnPos;
    }

    public Genetic createGenetic() {
        return factory.get();
    }

    public GeneticType setSpawnDimension(RegistryKey<World> spawnDimension) {
        this.spawnDimension = spawnDimension;
        return this;
    }

    public RegistryKey<World> getSpawnDimension() {
        return spawnDimension;
    }

    public SkinPart.Type getSpecialSkinPartType() {
        return specialSkinPartType;
    }

    public GeneticType setSpecialSkinPartType(SkinPart.Type specialSkinPartType) {
        this.specialSkinPartType = specialSkinPartType;
        return this;
    }

    public Genetic getDefaultInstance() {
        return instance;
    }

    public List<SkinPart> getAllowedHairs() {
        return SkinParts.getSkinPartsFor(SkinPart.Type.HAIR);
    }

    public List<SkinPart> getAllowedEyes() {
        return SkinParts.getSkinPartsFor(SkinPart.Type.EYES);
    }

    public List<SkinPart> getAllowedSkins() {
        return SkinParts.getSkinPartsFor(SkinPart.Type.SKIN);
    }

    public List<SkinPart> getAllowedClothes() {
        return SkinParts.getSkinPartsFor(SkinPart.Type.CLOTHING);
    }

    public Collection<SpawnGift> getAllowedSpawnGifts() {
        return MythriaRegistries.SPAWN_GIFTS.getValues();
    }

    public int getStartingAge() {
        return startingAge;
    }
}

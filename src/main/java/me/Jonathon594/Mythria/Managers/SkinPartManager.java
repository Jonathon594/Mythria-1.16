package me.Jonathon594.Mythria.Managers;

import me.Jonathon594.Mythria.DataTypes.SkinPart;
import me.Jonathon594.Mythria.Genetic.Genetic;
import me.Jonathon594.Mythria.Genetic.Genetics;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkinPartManager {
    private static final ArrayList<SkinPart> skinParts = new ArrayList<>();

    @SubscribeEvent
    public static void onRegisterSkinParts(RegistryEvent.Register<SkinPart> event) {
        event.getRegistry().registerAll(
                //Clothes
                new SkinPart("Primitive", "human_clothes_unis_primitive", SkinPart.Type.CLOTHING, true, true)
                        .addAllowedRace(Genetics.ELF, Genetics.FAE, Genetics.HUMAN, Genetics.ORC, Genetics.KATANA),
//                new SkinPart("Simple Chain", "human_clothes_unis_0", SkinPart.Type.CLOTHING, true, true),
//                new SkinPart("Peasant Dress", "human_clothes_female_0", SkinPart.Type.CLOTHING, false, true),
//                new SkinPart("Blacksmith Robes", "human_clothes_unis_1", SkinPart.Type.CLOTHING, true, true),
//                new SkinPart("Black Cloak", "human_clothes_unis_2", SkinPart.Type.CLOTHING, true, true),
                new SkinPart("Primitive", "skaeren_clothes_male_primitive", SkinPart.Type.CLOTHING, true, false)
                        .addAllowedRace(Genetics.SKAEREN),
                new SkinPart("Primitive", "skaeren_clothes_female_primitive", SkinPart.Type.CLOTHING, false, true)
                        .addAllowedRace(Genetics.SKAEREN),

                new SkinPart("Nude", "clothes_nude", SkinPart.Type.CLOTHING, true, true)
                        .addAllowedRace(Genetics.DRYAD),

                new SkinPart("Felixia", "deity_clothes_felixia", SkinPart.Type.CLOTHING, false, true),
                new SkinPart("Melinias", "deity_clothes_melinias", SkinPart.Type.CLOTHING, false, true),
                new SkinPart("Asana", "deity_clothes_asana", SkinPart.Type.CLOTHING, false, true),
                new SkinPart("Kasai", "deity_clothes_kasai", SkinPart.Type.CLOTHING, false, true),
                new SkinPart("Raika", "deity_clothes_raika", SkinPart.Type.CLOTHING, false, true),
                new SkinPart("Selina", "deity_clothes_selina", SkinPart.Type.CLOTHING, false, true),
                new SkinPart("Lilasia", "deity_clothes_lilasia", SkinPart.Type.CLOTHING, false, true),
                new SkinPart("Eliana", "deity_clothes_eliana", SkinPart.Type.CLOTHING, false, true),
                new SkinPart("Mal", "deity_clothes_mal", SkinPart.Type.CLOTHING, false, true),

                //Eyes
                new SkinPart("Brown", "human_eyes_male_0", SkinPart.Type.EYES, true, false),
                new SkinPart("Blue", "human_eyes_male_1", SkinPart.Type.EYES, true, false),
                new SkinPart("Green", "human_eyes_male_2", SkinPart.Type.EYES, true, false),
                new SkinPart("Brown", "human_eyes_female_0", SkinPart.Type.EYES, false, true),
                new SkinPart("Blue", "human_eyes_female_1", SkinPart.Type.EYES, false, true),
                new SkinPart("Green", "human_eyes_female_2", SkinPart.Type.EYES, false, true),
                new SkinPart("Blue - Green", "human_eyes_female_3", SkinPart.Type.EYES, false, true),

                new SkinPart("Felixia", "deity_eyes_felixia", SkinPart.Type.EYES, false, true),
                new SkinPart("Melinias", "deity_eyes_melinias", SkinPart.Type.EYES, false, true),
                new SkinPart("Asana", "deity_eyes_asana", SkinPart.Type.EYES, false, true),
                new SkinPart("Kasai", "deity_eyes_kasai", SkinPart.Type.EYES, false, true),
                new SkinPart("Raika", "deity_eyes_raika", SkinPart.Type.EYES, false, true),
                new SkinPart("Selina", "deity_eyes_selina", SkinPart.Type.EYES, false, true),
                new SkinPart("Lilasia", "deity_eyes_lilasia", SkinPart.Type.EYES, false, true),
                new SkinPart("Eliana", "deity_eyes_eliana", SkinPart.Type.EYES, false, true),
                new SkinPart("Mal", "deity_eyes_mal", SkinPart.Type.EYES, false, true),

                //Hair
                new SkinPart("Blond, Scruffy, Beard", "human_hair_male_0", SkinPart.Type.HAIR, true, false),
                new SkinPart("Ginger, Scruffy, Beard", "human_hair_male_1", SkinPart.Type.HAIR, true, false),
                new SkinPart("Brown, Scruffy, Beard", "human_hair_male_2", SkinPart.Type.HAIR, true, false),
                new SkinPart("Black, Scruffy, Beard", "human_hair_male_3", SkinPart.Type.HAIR, true, false),

                new SkinPart("Blond, Scruffy", "human_hair_male_4", SkinPart.Type.HAIR, true, false),
                new SkinPart("Ginger, Scruffy", "human_hair_male_5", SkinPart.Type.HAIR, true, false),
                new SkinPart("Brown, Scruffy", "human_hair_male_6", SkinPart.Type.HAIR, true, false),
                new SkinPart("Black, Scruffy", "human_hair_male_7", SkinPart.Type.HAIR, true, false),

                new SkinPart("Blond", "human_hair_female_0", SkinPart.Type.HAIR, false, true),
                new SkinPart("Ginger", "human_hair_female_1", SkinPart.Type.HAIR, false, true),
                new SkinPart("Brown", "human_hair_female_2", SkinPart.Type.HAIR, false, true),
                new SkinPart("Black", "human_hair_female_3", SkinPart.Type.HAIR, false, true),

                new SkinPart("Felixia", "deity_hair_felixia", SkinPart.Type.HAIR, false, true),
                new SkinPart("Melinias", "deity_hair_melinias", SkinPart.Type.HAIR, false, true),
                new SkinPart("Asana", "deity_hair_asana", SkinPart.Type.HAIR, false, true),
                new SkinPart("Kasai", "deity_hair_kasai", SkinPart.Type.HAIR, false, true),
                new SkinPart("Raika", "deity_hair_raika", SkinPart.Type.HAIR, false, true),
                new SkinPart("Selina", "deity_hair_selina", SkinPart.Type.HAIR, false, true),
                new SkinPart("Lilasia", "deity_hair_lilasia", SkinPart.Type.HAIR, false, true),
                new SkinPart("Mal", "deity_hair_mal", SkinPart.Type.HAIR, false, true),

                //Skin
                new SkinPart("White", "human_skin_unis_0", SkinPart.Type.SKIN, true, true)
                        .addAllowedRace(Genetics.HUMAN, Genetics.ELF, Genetics.FAE, Genetics.DRYAD),
                new SkinPart("Tan", "human_skin_unis_1", SkinPart.Type.SKIN, true, true)
                        .addAllowedRace(Genetics.HUMAN, Genetics.ELF, Genetics.FAE, Genetics.DRYAD, Genetics.SKAEREN, Genetics.KATANA),
                new SkinPart("Medium", "human_skin_unis_2", SkinPart.Type.SKIN, true, true)
                        .addAllowedRace(Genetics.HUMAN, Genetics.ELF, Genetics.FAE, Genetics.DRYAD, Genetics.SKAEREN, Genetics.KATANA),
                new SkinPart("Dark", "human_skin_unis_3", SkinPart.Type.SKIN, true, true)
                        .addAllowedRace(Genetics.HUMAN, Genetics.DRYAD, Genetics.SKAEREN, Genetics.KATANA),
                new SkinPart("Black", "human_skin_unis_4", SkinPart.Type.SKIN, true, true)
                        .addAllowedRace(Genetics.HUMAN, Genetics.SKAEREN, Genetics.KATANA),

                new SkinPart("Light", "orc_skin_unis_0", SkinPart.Type.SKIN, true, true).addAllowedRace(Genetics.ORC),
                new SkinPart("Mocha", "orc_skin_female_0", SkinPart.Type.SKIN, false, true).addAllowedRace(Genetics.ORC),
                new SkinPart("Medium", "orc_skin_unis_1", SkinPart.Type.SKIN, true, true).addAllowedRace(Genetics.ORC),
                new SkinPart("Dark", "orc_skin_unis_2", SkinPart.Type.SKIN, true, true).addAllowedRace(Genetics.ORC),

                new SkinPart("Blue Wings", "fae_wings_blue", SkinPart.Type.WINGS, true, true).addAllowedRace(Genetics.FAE),
                new SkinPart("Red Wings", "fae_wings_red", SkinPart.Type.WINGS, true, true).addAllowedRace(Genetics.FAE),
                new SkinPart("Green Wings", "fae_wings_green", SkinPart.Type.WINGS, true, true).addAllowedRace(Genetics.FAE),

                new SkinPart("Vines", "dryad_vines_0", SkinPart.Type.DRYAD_VINES, true, true)
                        .addAllowedRace(Genetics.DRYAD)
        );
    }

    public static void addSkinPart(SkinPart part) {
        skinParts.add(part);
    }

    public static SkinPart getSkinPart(ResourceLocation resourceLocation) {
        for (SkinPart skinPart : skinParts) {
            if (skinPart.getRegistryName().equals(resourceLocation)) return skinPart;
        }
        return null;
    }

    public static List<SkinPart> getSkinPartsFor(SkinPart.Type type, int gender, @Nullable Genetic race) {
        List<SkinPart> parts = new ArrayList<>();
        for (SkinPart part : skinParts) {
            if (race != null && !part.getAllowedRaces().isEmpty() && !part.getAllowedRaces().contains(race)) continue;
            if (!part.getType().equals(type)) continue;
            if (gender == 0 && !part.isMasculine()) continue;
            if (gender == 1 && !part.isFeminine()) continue;
            parts.add(part);
        }
        return parts;
    }

    public static List<String> getSkinPartNamesFor(SkinPart.Type type, int gender, Genetic race) {
        List<String> names = new ArrayList<>();
        for (SkinPart part : getSkinPartsFor(type, gender, race)) {
            names.add(part.getRegistryName().toString());
        }
        return names;
    }
}

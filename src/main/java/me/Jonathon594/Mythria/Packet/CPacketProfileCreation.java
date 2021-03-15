package me.Jonathon594.Mythria.Packet;

import me.Jonathon594.Mythria.Capability.MythriaPlayer.MythriaPlayerProvider;
import me.Jonathon594.Mythria.Capability.Profile.Profile;
import me.Jonathon594.Mythria.Capability.Profile.ProfileProvider;
import me.Jonathon594.Mythria.Skin.SkinPart;
import me.Jonathon594.Mythria.SpawnGifts.SpawnGift;
import me.Jonathon594.Mythria.Enum.Gender;
import me.Jonathon594.Mythria.Enum.PerkType;
import me.Jonathon594.Mythria.Genetic.Gene.Gene;
import me.Jonathon594.Mythria.Genetic.Gene.ISkinPartGene;
import me.Jonathon594.Mythria.Genetic.Genetic;
import me.Jonathon594.Mythria.Genetic.GeneticType;
import me.Jonathon594.Mythria.Managers.SpawnManager;
import me.Jonathon594.Mythria.Managers.StatManager;
import me.Jonathon594.Mythria.MythriaRegistries;
import me.Jonathon594.Mythria.Util.MythriaUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CPacketProfileCreation {
    private String firstName, middleName, lastName;
    private int month, day;
    private GeneticType geneticType;
    private Gender gender;
    private SkinPart hair, eyes, clothes, skin, unique;
    private SpawnGift gift;

    public CPacketProfileCreation(String firstName, String middleName, String lastName, int month,
                                  int day, GeneticType geneticType, Gender gender, SkinPart hair, SkinPart eyes, SkinPart clothes,
                                  SkinPart skin, @Nullable SkinPart unique, SpawnGift gift) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.month = month;
        this.day = day;
        this.geneticType = geneticType;
        this.gender = gender;
        this.hair = hair;
        this.eyes = eyes;
        this.clothes = clothes;
        this.skin = skin;
        this.unique = unique;
        this.gift = gift;
    }

    public CPacketProfileCreation(PacketBuffer packetBuffer) {
        firstName = packetBuffer.readString(32767);
        middleName = packetBuffer.readString(32767);
        lastName = packetBuffer.readString(32767);
        month = packetBuffer.readInt();
        day = packetBuffer.readInt();
        geneticType = MythriaRegistries.GENETICS.getValue(new ResourceLocation(packetBuffer.readString(32767)));
        gender = Gender.valueOf(packetBuffer.readString(32767));
        hair = MythriaRegistries.SKIN_PARTS.getValue(new ResourceLocation(packetBuffer.readString(32767)));
        eyes = MythriaRegistries.SKIN_PARTS.getValue(new ResourceLocation(packetBuffer.readString(32767)));
        clothes = MythriaRegistries.SKIN_PARTS.getValue(new ResourceLocation(packetBuffer.readString(32767)));
        skin = MythriaRegistries.SKIN_PARTS.getValue(new ResourceLocation(packetBuffer.readString(32767)));
        gift = MythriaRegistries.SPAWN_GIFTS.getValue(new ResourceLocation(packetBuffer.readString(32767)));

        String s = packetBuffer.readString(32767);
        if (!s.isEmpty()) unique = MythriaRegistries.SKIN_PARTS.getValue(new ResourceLocation(s));
    }

    public static void encode(CPacketProfileCreation msg, PacketBuffer packetBuffer) {
        packetBuffer.writeString(msg.getFirstName());
        packetBuffer.writeString(msg.getMiddleName());
        packetBuffer.writeString(msg.getLastName());
        packetBuffer.writeInt(msg.getMonth());
        packetBuffer.writeInt(msg.getDay());
        packetBuffer.writeString(msg.getGeneticType().getRegistryName().toString());
        packetBuffer.writeString(msg.getGender().name());
        packetBuffer.writeString(msg.getHair().getRegistryName().toString());
        packetBuffer.writeString(msg.getEyes().getRegistryName().toString());
        packetBuffer.writeString(msg.getClothes().getRegistryName().toString());
        packetBuffer.writeString(msg.getSkin().getRegistryName().toString());
        packetBuffer.writeString(msg.getGift().getRegistryName().toString());
        packetBuffer.writeString(msg.getUnique() == null ? "" : msg.getUnique().getRegistryName().toString());
    }

    public static void handle(CPacketProfileCreation msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            final ServerPlayerEntity serverPlayer = contextSupplier.get().getSender();
            Profile profile = ProfileProvider.getProfile(serverPlayer);
            profile.init();

            profile.setFirstName(msg.getFirstName());
            profile.setMiddleName(msg.getMiddleName());
            profile.setLastName(msg.getLastName());
            profile.setBirthDay(MythriaUtil.getDateFromAgeMonthDay(
                    msg.getGeneticType().getStartingAge(), msg.getMonth(), msg.getDay()));
            profile.setClothing(msg.getClothes());
            Genetic genetic = msg.getGeneticType().createGenetic();
            genetic.getHair().setSkinPart(msg.getHair());
            genetic.getEyes().setSkinPart(msg.getEyes());
            genetic.getSkin().setSkinPart(msg.getSkin());
            if (msg.getUnique() != null) {
                for (Gene gene : genetic.getExtraGenes()) {
                    if (!(gene instanceof ISkinPartGene)) continue;
                    ISkinPartGene skinPartGene = (ISkinPartGene) gene;
                    if (skinPartGene.getSkinPart().getType().equals(msg.getGeneticType().getSpecialSkinPartType())) {
                        skinPartGene.setSkinPart(msg.getUnique());
                    }
                }
            }
            profile.setGenetic(genetic);
            profile.setGender(msg.getGender());
            profile.setCreated(true);
            StatManager.applyInitialStats(profile, serverPlayer);
            profile.applyRandomStatModifiers();
            MythriaUtil.unlockDefaultRecipes(serverPlayer);
            serverPlayer.getFoodStats().setFoodLevel(20);
            profile.unlockPerkType(PerkType.SURVIVAL);
            SpawnManager.spawnInWorld(serverPlayer, profile);
            msg.getGift().apply(serverPlayer, profile);
            profile.sendDataPacket();
            profile.copySkinToMythriaPlayer(MythriaPlayerProvider.getMythriaPlayer(serverPlayer));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public GeneticType getGeneticType() {
        return geneticType;
    }

    public Gender getGender() {
        return gender;
    }

    public SkinPart getHair() {
        return hair;
    }

    public SkinPart getEyes() {
        return eyes;
    }

    public SkinPart getClothes() {
        return clothes;
    }

    public SkinPart getSkin() {
        return skin;
    }

    public SkinPart getUnique() {
        return unique;
    }

    public SpawnGift getGift() {
        return gift;
    }
}

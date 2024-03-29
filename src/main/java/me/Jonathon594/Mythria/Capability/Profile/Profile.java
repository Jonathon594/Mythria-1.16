package me.Jonathon594.Mythria.Capability.Profile;

import me.Jonathon594.Mythria.Ability.Ability;
import me.Jonathon594.Mythria.Capability.MythriaPlayer.MythriaPlayer;
import me.Jonathon594.Mythria.Const.EXPConst;
import me.Jonathon594.Mythria.Const.MythriaConst;
import me.Jonathon594.Mythria.DataTypes.Date;
import me.Jonathon594.Mythria.DataTypes.Genetic.Gene.ISkinPartGene;
import me.Jonathon594.Mythria.DataTypes.Genetic.Gene.LifeSpanGene;
import me.Jonathon594.Mythria.DataTypes.Genetic.Genetic;
import me.Jonathon594.Mythria.DataTypes.Genetic.GeneticTypes;
import me.Jonathon594.Mythria.DataTypes.HealthData;
import me.Jonathon594.Mythria.DataTypes.Perk;
import me.Jonathon594.Mythria.Enum.*;
import me.Jonathon594.Mythria.Managers.AbilityHandler;
import me.Jonathon594.Mythria.Managers.StatManager;
import me.Jonathon594.Mythria.Managers.TimeManager;
import me.Jonathon594.Mythria.MythriaPacketHandler;
import me.Jonathon594.Mythria.MythriaRegistries;
import me.Jonathon594.Mythria.Packet.PacketBindAbility;
import me.Jonathon594.Mythria.Packet.SPacketProfileCache;
import me.Jonathon594.Mythria.Packet.SPacketUpdateExperience;
import me.Jonathon594.Mythria.Skin.SkinPart;
import me.Jonathon594.Mythria.Skin.SkinParts;
import me.Jonathon594.Mythria.Util.MythriaResourceLocation;
import me.Jonathon594.Mythria.Util.MythriaUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

import java.util.*;

public class Profile implements IProfile {
    private final List<Perk> perks = new ArrayList<>();
    private final List<PerkType> unlockedPerkTypes = new ArrayList<>();
    private final EnumMap<Skill, Double> skillLevels = new EnumMap<>(Skill.class);
    private final EnumMap<Consumable, Double> consumables = new EnumMap<>(Consumable.class);
    private final EnumMap<Consumable.Nutrition, Double> nutrition = new EnumMap<>(Consumable.Nutrition.class);
    private final EnumMap<Consumable.Nutrition, Double> undigested_nutrition = new EnumMap<>(Consumable.Nutrition.class);
    private final HealthData healthData = new HealthData();
    private final EnumMap<Attribute, Integer> attributeValues = new EnumMap<>(Attribute.class);
    private final EnumMap<Deity, Integer> favorLevels = new EnumMap<>(Deity.class);
    private final EnumMap<StatType, Double> statModifiers = new EnumMap<>(StatType.class);
    private final Random random;
    private final ArrayList<Ability> abilities = new ArrayList<>();
    private final AbilityHandler abilityHandler = new AbilityHandler();
    private final Ability[] boundAbilities = new Ability[48];
    private String firstName = "";
    private String middleName = "";
    private String lastName = "";
    private Date birthDay = new Date();
    private Gender gender = Gender.MALE;
    private boolean created = false;
    private PlayerEntity player;
    private UUID profileUUID;
    private String ownerName;
    private String dataVersion;
    private boolean pregnant = false;
    private CompoundNBT pregMotherProfileData = new CompoundNBT();
    private CompoundNBT pregFatherProfileData = new CompoundNBT();
    private int pregBabyCount = 0;
    private int pregConceptionData = 0;
    private Long lastDisconnect = 0L;
    private Genetic genetic;
    private double bleeding = 0.0;
    private double playerLevelProgressBuffer;
    private SkinPart clothing = SkinParts.CLOTHES_PRIMITIVE;
    private int abilityPreset = 0;

    public Profile() {
        init();
        random = new Random();
    }

    public void addAbility(Ability ability) {
        if (!abilities.contains(ability)) {
            abilities.add(ability);
            abilityHandler.addAbilityInstance(ability, player);
        }
    }

    public void addConsumable(final Consumable consumable, double value) {
        if (value == 0) return;

        double oldValue = getConsumable(consumable);
        setConsumable(consumable, getConsumable(consumable) + value);

        if (consumable == Consumable.STAMINA && value < 0) {
            addFatigue(getConsumable(Consumable.STAMINA) - oldValue);
        }
    }

    public void addFavor(Deity deity, int add, int max) {
        favorLevels.put(deity, Math.min(getFavor(deity) + add, max));
    }

    public void addNutrition(Consumable.Nutrition nutrition, double value) {
        setNutrition(nutrition, this.nutrition.get(nutrition) + value);
    }

    public void addPerk(final Perk perk) {
        if (perk == null)
            return;
        if (player != null) MythriaUtil.addRecipesFromPerk(player, perk);
        if (perks.contains(perk))
            return;

        perks.add(perk);
        for (PerkType type : perk.getPerkTypeUnlocks()) {
            unlockPerkType(type);
        }
        if (player == null)
            return;
        if (player.world.isRemote)
            return;

        sendDataPacket();
    }

    public void addSingleSkillExperience(Skill skill, double xp, ServerPlayerEntity player, int effectiveLevel) {
        if (skill == null)
            return;

        final int level = getSkillLevel(skill);

        xp *= (1.0 + (Math.pow(effectiveLevel, 2) / 1000.0));

        double xpRate = 1.0;

        boolean isAtheist = hasFlag(AttributeFlag.ATHEISM);
        if (skill.isMental()) {
            xpRate += getStat(StatType.LEARN_RATE);
        }
        if (isAtheist) {
            xpRate += 0.25;
        }
        xp *= Math.max(xpRate, 0);
        if (xp <= 0) return;
        int plo = getPlayerLevel();
        skillLevels.put(skill, skillLevels.get(skill) + xp);
        calculateProgressTowardPlayerLevel();
        int pln = getPlayerLevel();
        MythriaPacketHandler.sendTo(new SPacketUpdateExperience(skill, skillLevels.get(skill)), player);
        final int lb = getSkillLevel(skill);
        if (lb > level)
            if (this.player != null) {
                this.player.sendMessage(new StringTextComponent(
                        String.format(MythriaConst.LEVE_UP_SKILL, MythriaUtil.capitalize(skill.name()), "(" + level + "->" + lb + ")")), Util.DUMMY_UUID);
            }

        if (pln > plo) {
            if (this.player != null) {
                this.player.sendMessage(new StringTextComponent(
                        String.format(MythriaConst.PLAYER_LEVEL_UP, pln)), Util.DUMMY_UUID);
            }
        }
    }

    public void addSkillExperience(final Skill type, double value, final ServerPlayerEntity p, int effectiveLevel) {
        if (type == null)
            return;

        addSingleSkillExperience(type, value, p, effectiveLevel);
    }

    public void applyRandomStatModifiers() {
        for (StatType type : StatType.values()) {
            double value = random.nextDouble() + 0.5;
            statModifiers.put(type, value);
        }
    }

    public void calculateProgressTowardPlayerLevel() {
        Double totalExperience = getTotalExperience();
        int playerLevel = MythriaUtil.getPlayerLevelForXP(totalExperience);
        double xpForNextLevel = MythriaUtil.getExperienceForPlayerLevel(playerLevel + 1);
        double xpForCurrentLevel = MythriaUtil.getExperienceForPlayerLevel(playerLevel);
        double xpProgress = totalExperience - xpForCurrentLevel;
        double xpDifference = xpForNextLevel - xpForCurrentLevel;
        double progress = xpProgress / xpDifference;
        playerLevelProgressBuffer = MathHelper.clamp(progress, 0, 1);
    }

    public void clear() {
        perks.clear();
        unlockedPerkTypes.clear();
        init();
        firstName = "";
        middleName = "";
        lastName = "";
        birthDay = new Date();
        gender = Gender.MALE;
        created = false;
        player = null;
        ownerName = null;
    }

    public void copySkinToMythriaPlayer(MythriaPlayer mythriaPlayer) {
        Genetic genetic = getGenetic();
        HashMap<SkinPart.Type, SkinPart> copy = new HashMap<>();
        copy.put(SkinPart.Type.HAIR, genetic.getHair().getSkinPart());
        copy.put(SkinPart.Type.EYES, genetic.getEyes().getSkinPart());
        copy.put(SkinPart.Type.SKIN, genetic.getSkin().getSkinPart());
        copy.put(SkinPart.Type.CLOTHING, clothing);
        genetic.getExtraGenes().forEach(gene -> {
            if (gene instanceof ISkinPartGene) {
                ISkinPartGene skinPartGene = (ISkinPartGene) gene;
                copy.put(skinPartGene.getSkinPart().getType(), skinPartGene.getSkinPart());
            }
        });
        for (SkinPart.Type type : SkinPart.Type.values()) {
            mythriaPlayer.setSkinPart(type, copy.get(type));
        }
        mythriaPlayer.setGender(getGender());
    }

    public void fromNBT(final CompoundNBT comp) {
        setFirstName(comp.getString("FirstName"));
        setMiddleName(comp.getString("MiddleName"));
        setLastName(comp.getString("LastName"));
        setOwnerName(comp.getString("OwnerName"));
        setBirthDay(new Date(comp.getInt("Birthday")));
        setGender(Gender.valueOf(comp.getString("Gender")));
        setCreated(comp.getBoolean("Created"));
        setDataVersion(comp.getString("DataVersion"));
        genetic.fromNBT(comp.getCompound("Genetic"));
        setPregnant(comp.getBoolean("Pregnant"));
        setPregFatherProfileData(comp.getCompound("PregFatherProfile"));
        setPregMotherProfileData(comp.getCompound("PregMotherProfile"));
        setPregBabyCount(comp.getInt("PregBabyCount"));
        setPregConceptionData(comp.getInt("PregConceptionDate"));
        setLastDisconnect(comp.getLong("LastDisconnect"));
        bleeding = comp.getDouble("bleeding");
        clothing = MythriaRegistries.SKIN_PARTS.getValue(new ResourceLocation(comp.getString("Clothing")));

        CompoundNBT abilityBindings = comp.getCompound("BoundAbilities");
        for (String key : abilityBindings.keySet()) {
            int index = Integer.parseInt(key);
            boundAbilities[index] = MythriaRegistries.ABILITIES.getValue(new ResourceLocation(abilityBindings.getString(key)));
        }

        healthData.fromNBT(comp.getCompound("HealthData"));

        final String uuids = comp.getString("UUID");
        if (uuids == null || uuids.isEmpty())
            setProfileUUID(UUID.randomUUID());
        else {
            final UUID uuid = UUID.fromString(uuids);
            setProfileUUID(uuid);
        }

        final ListNBT perkList = comp.getList("Perks", 8);
        for (INBT nbt : perkList) {
            String s = nbt.getString();
            final Perk pa = MythriaRegistries.PERKS.getValue(new ResourceLocation(s));
            if (pa != null) {
                perks.add(pa);
                for (PerkType type : pa.getPerkTypeUnlocks()) {
                    unlockPerkType(type);
                }
            }
        }

        final ListNBT abilityList = comp.getList("Abilities", 8);
        for (INBT nbt : abilityList) {
            String s = nbt.getString();
            Ability ability = MythriaRegistries.ABILITIES.getValue(new MythriaResourceLocation(s));
            if (ability != null)
                addAbility(ability);
        }

        final ListNBT perkTypeList = comp.getList("UnlockedPerkTypes", 8);
        for (INBT nbt : perkTypeList) {
            String s = nbt.getString();
            PerkType perkType = PerkType.valueOf(s);
            if (perkType != null)
                unlockPerkType(perkType);
        }

        CompoundNBT skills = comp.getCompound("Skills");
        for (final Skill cs : Skill.values()) {
            final Double value = skills.getDouble(cs.name());
            getSkillLevels().put(cs, value);
            MythriaUtil.getLevelForXP(value);
        }
        calculateProgressTowardPlayerLevel();

        CompoundNBT attributes = comp.getCompound("Attributes");
        for (final Attribute s : Attribute.values()) {
            final int v = attributes.getInt(s.name());
            attributeValues.put(s, v);
        }
        CompoundNBT consumables = comp.getCompound("Consumables");
        for (final Consumable s : Consumable.values()) {
            final double v = consumables.getDouble(s.name());
            getConsumables().put(s, v);
        }
        CompoundNBT nutritionTag = comp.getCompound("Nutrition");
        CompoundNBT digestedNutrition = nutritionTag.getCompound("Digested");
        for (final Consumable.Nutrition n : Consumable.Nutrition.values()) {
            final double v = digestedNutrition.getDouble(n.name());
            nutrition.put(n, v);
        }

        CompoundNBT undigestedNutrition = nutritionTag.getCompound("Undigested");
        for (final Consumable.Nutrition n : Consumable.Nutrition.values()) {
            final double v = undigestedNutrition.getDouble(n.name());
            undigested_nutrition.put(n, v);
        }

        CompoundNBT statModifiers = comp.getCompound("StatModifiers");
        for (final StatType s : StatType.values()) {
            final double v = statModifiers.getDouble(s.name());
            setStatModifier(s, v);

        }
        CompoundNBT favor = comp.getCompound("Favor");
        Set<String> tagsFavor = favor.keySet();
        for (String s : tagsFavor) {
            try {
                Deity d = Deity.valueOf(s);
                int v = favor.getInt(s);
                getFavorLevels().put(d, v);
            } catch (IllegalArgumentException e) {
                System.out.println("Error loading deity for favor.");
            }
        }
    }

    public CompoundNBT toNBT() {
        final CompoundNBT comp = new CompoundNBT();
        comp.putString("FirstName", getFirstName());
        comp.putString("MiddleName", getMiddleName());
        comp.putString("LastName", getLastName());
        if (getOwnerName() != null) comp.putString("OwnerName", getOwnerName());
        comp.putInt("Birthday", getBirthDay().getMGD());
        comp.putString("Gender", getGender().name());
        comp.putBoolean("Created", getCreated());
        comp.putString("DataVersion", getDataVersion() == null ? "" : getDataVersion());
        comp.put("Genetic", genetic.toNBT());
        comp.putBoolean("Pregnant", getPregnant());
        comp.put("PregFatherProfile", getPregFatherProfile());
        comp.put("PregMotherProfile", getPregMotherProfile());
        comp.putInt("PregBabyCount", getPregBabyCount());
        comp.putInt("PregConceptionDate", getPregConceptionDate());
        comp.putLong("LastDisconnect", getLastDisconnect());
        comp.put("HealthData", healthData.toNBT());
        comp.putString("Clothing", clothing.getRegistryName().toString());

        CompoundNBT abilityBindings = new CompoundNBT();
        for (int i = 0; i < boundAbilities.length; i++) {
            if (boundAbilities[i] != null) {
                abilityBindings.putString(String.valueOf(i), boundAbilities[i].getRegistryName().toString());
            }
        }
        comp.put("BoundAbilities", abilityBindings);

        if (getProfileUUID() == null)
            setProfileUUID(UUID.randomUUID());
        comp.putString("UUID", getProfileUUID().toString());

        ListNBT perkList = new ListNBT();
        for (final Perk pa : getPlayerSkills()) {
            perkList.add(StringNBT.valueOf(pa.getRegistryName().toString()));
        }
        comp.put("Perks", perkList);

        ListNBT abilityList = new ListNBT();
        for (Ability ability : abilities) {
            abilityList.add(StringNBT.valueOf(ability.getRegistryName().getPath()));
        }
        comp.put("Abilities", abilityList);

        ListNBT perkTypeList = new ListNBT();
        for (final PerkType type : unlockedPerkTypes) {
            perkTypeList.add(StringNBT.valueOf(type.name()));
        }
        comp.put("UnlockedPerkTypes", perkTypeList);

        CompoundNBT skills = new CompoundNBT();
        for (final Skill cs : Skill.values())
            skills.putDouble(cs.name(), getSkillLevels().get(cs));
        comp.put("Skills", skills);

        CompoundNBT attributes = new CompoundNBT();
        for (final Attribute s : Attribute.values()) {
            attributes.putInt(s.name(), attributeValues.get(s));
        }
        comp.put("Attributes", attributes);

        CompoundNBT consumables = new CompoundNBT();
        for (final Consumable s : Consumable.values())
            consumables.putDouble(s.name(), getConsumables().get(s));
        comp.put("Consumables", consumables);

        CompoundNBT nutritionTag = new CompoundNBT();
        CompoundNBT nutritionDigested = new CompoundNBT();
        CompoundNBT nutritionUndigested = new CompoundNBT();
        for (final Consumable.Nutrition s : Consumable.Nutrition.values()) {
            nutritionDigested.putDouble(s.name(), nutrition.get(s));
            nutritionUndigested.putDouble(s.name(), undigested_nutrition.get(s));
        }
        nutritionTag.put("Digested", nutritionDigested);
        nutritionTag.put("Undigested", nutritionUndigested);
        comp.put("Nutrition", nutritionTag);

        CompoundNBT statModifiers = new CompoundNBT();
        for (final StatType s : StatType.values())
            statModifiers.putDouble(s.name(), getStatModifier(s));
        comp.put("StatModifiers", statModifiers);

        CompoundNBT favor = new CompoundNBT();
        for (Map.Entry<Deity, Integer> e : getFavorLevels().entrySet()) {
            favor.putInt(e.getKey().name(), e.getValue());
        }
        comp.put("Favor", favor);
        return comp;
    }

    public ArrayList<Ability> getAbilities() {
        return abilities;
    }

    public AbilityHandler getAbilityHandler() {
        return abilityHandler;
    }

    public int getAbilityPreset() {
        return abilityPreset;
    }

    public Profile setAbilityPreset(int abilityPreset) {
        abilityPreset = MythriaUtil.wrapInt(abilityPreset, 0, 3);
        this.abilityPreset = abilityPreset;
        return this;
    }

    public EnumMap<Consumable.Nutrition, Double> getAllNutrition() {
        return nutrition;
    }

    public void setAllNutrition(EnumMap<Consumable.Nutrition, Double> values) {
        for (Map.Entry<Consumable.Nutrition, Double> entry : values.entrySet()) {
            nutrition.put(entry.getKey(), entry.getValue());
        }
    }

    public int getAttributeLevel(Attribute attribute) {
        return attributeValues.get(attribute);
    }

    public double getAverageNutrition() {
        HashMap<Double, Integer> weightMap = new HashMap<>();
        HashMap<Consumable.Nutrition, Integer> requiredNutrition = getGenetic().getNutrition().getRequiredNutrition();
        for (Consumable.Nutrition nutrition : requiredNutrition.keySet()) {
            weightMap.put(getNutrition(nutrition), requiredNutrition.get(nutrition));
        }

        return MythriaUtil.calculateWeightedAverage(weightMap);
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(final Date birthDay) {
        this.birthDay = birthDay;
    }

    public double getBleeding() {
        return bleeding;
    }

    public void setBleeding(double bleeding) {
        this.bleeding = Math.max(bleeding, 0);
    }

    public Ability getBoundAbility(int slot) {
        return boundAbilities[slot];
    }

    public SkinPart getClothing() {
        return clothing;
    }

    public void setClothing(SkinPart part) {
        clothing = part;
    }

    public double getConsumable(Consumable consumable) {
        return consumables.get(consumable);
    }

    public EnumMap<Consumable, Double> getConsumables() {
        return consumables;
    }

    public void setConsumables(EnumMap<Consumable, Double> consumables) {
        for (Map.Entry<Consumable, Double> entry : consumables.entrySet()) {
            this.consumables.put(entry.getKey(), entry.getValue());
        }
    }

    public boolean getCreated() {
        return created;
    }

    public void setCreated(final boolean created) {
        this.created = created;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public int getFavor(Deity d) {
        return favorLevels.getOrDefault(d, 0);
    }

    public EnumMap<Deity, Integer> getFavorLevels() {
        return favorLevels;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getFullName() {
        return getFirstName() + " " + getMiddleName() + " " + getLastName();
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(final Gender gender) {
        this.gender = gender;
    }

    public Genetic getGenetic() {
        return genetic;
    }

    public void setGenetic(Genetic genetic) {
        abilities.clear();
        this.genetic = genetic;
        addGrantedAbilities();
    }

    public HealthData getHealthData() {
        return healthData;
    }

    public Long getLastDisconnect() {
        return lastDisconnect;
    }

    public void setLastDisconnect(Long lastDisconnect) {
        this.lastDisconnect = lastDisconnect;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public double getNutrition(Consumable.Nutrition nutrition) {
        return this.nutrition.get(nutrition);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(final PlayerEntity player) {
        this.player = player;
        this.ownerName = player.getUniqueID().toString();
    }

    public int getPlayerLevel() {
        double totalExperience = getTotalExperience();
        return MythriaUtil.getPlayerLevelForXP(totalExperience);
    }

    public double getPlayerLevelProgressBuffer() {
        return playerLevelProgressBuffer;
    }

    public List<Perk> getPlayerSkills() {
        return perks;
    }

    public int getPregBabyCount() {
        return pregBabyCount;
    }

    public void setPregBabyCount(int pregBabyCount) {
        this.pregBabyCount = pregBabyCount;
    }

    public int getPregConceptionDate() {
        return pregConceptionData;
    }

    public CompoundNBT getPregFatherProfile() {
        return pregFatherProfileData;
    }

    public CompoundNBT getPregMotherProfile() {
        return pregMotherProfileData;
    }

    public boolean getPregnant() {
        return pregnant;
    }

    public void setPregnant(boolean pregnant) {
        this.pregnant = pregnant;
    }

    public UUID getProfileUUID() {
        return profileUUID;
    }

    public void setProfileUUID(final UUID profileUUID) {
        this.profileUUID = profileUUID;
    }

    public int getSkillLevel(final Skill type) {
        return MythriaUtil.getLevelForXP(skillLevels.get(type));
    }

    public EnumMap<Skill, Double> getSkillLevels() {
        return skillLevels;
    }

    public int getSpendableAttributePoints() {
        int totalAttributes = 0;
        for (int value : attributeValues.values()) {
            totalAttributes += value;
        }
        int playerLevel = getPlayerLevel();
        return playerLevel + 5 - totalAttributes;
    }

    public double getStat(StatType statType) {
        double modifier = 0;
        if (healthData.getStatModifiers().containsKey(statType)) {
            modifier = healthData.getStatModifiers().get(statType);
        }

        for (Consumable.Nutrition nutrition : Consumable.Nutrition.values()) {
            double value = getNutrition(nutrition);
            double prop = (value - 10) / 10.0;
            switch (nutrition) {
                case STARCH:
                    if (statType.equals(StatType.MAX_STAMINA)) modifier += prop * 25;
                    break;
                case FRUIT:
                    if (statType.equals(StatType.MAX_SPEED)) modifier += prop * 0.003;
                    break;
                case VEGETABLE:
                    if (statType.equals(StatType.MAX_WEIGHT)) modifier += prop * 25;
                    break;
                case MEAT:
                    if (statType.equals(StatType.MAX_HEALTH)) modifier += prop * 4;
                    break;
                case DAIRY:
                    if (statType.equals(StatType.MAX_MANA)) modifier += prop * 50;
                    break;
            }
        }

        if (player == null) return 1;

        Profile profile = ProfileProvider.getProfile(player);
        if (!profile.getCreated()) return 1;
        Genetic g = profile.getGenetic();
        switch (statType) {
            case MAX_STAMINA:
                return modifier + g.getBaseStamina() + getAttributeLevel(Attribute.ENDURANCE) * 50 * getStatModifier(statType);
            case MAX_SPEED:
                return modifier + g.getBaseSpeed() + getAttributeLevel(Attribute.AGILITY) * 0.001 * getStatModifier(statType);
            case MAX_WEIGHT:
                return modifier + g.getBaseWeight() + getAttributeLevel(Attribute.STRENGTH) * 12.5 * getStatModifier(statType);
            case MAX_HEALTH:
                return modifier + g.getBaseHealth() + getAttributeLevel(Attribute.VIGOR) * 2.5 * getStatModifier(statType);
            case LEARN_RATE:
                return modifier + (g.getBaseXP() - 1) + (getAttributeLevel(Attribute.INTELLIGENCE) * 0.05) * getStatModifier(statType);
            case HEAT_TOLLERANCE:
                return modifier + 0.25 * getStatModifier(statType) *
                        getAttributeLevel(Attribute.VITALITY) + (player.isPotionActive(Effects.FIRE_RESISTANCE) ? 2.5 : 0);
            case COLD_TOLLERANCE:
                return modifier + 0.125 * getStatModifier(statType) *
                        getAttributeLevel(Attribute.VITALITY) + (player.isPotionActive(Effects.FIRE_RESISTANCE) ? -5 : 0);
            case MAX_MANA:
                return Math.max(modifier + g.getBaseMana() + 50 * getStatModifier(statType)
                        * getAttributeLevel(Attribute.WILLPOWER), 0);
            case MANA_REGEN:
                return modifier + g.getBaseManaRegen() + 0.03 * getStatModifier(statType)
                        * getAttributeLevel(Attribute.WILLPOWER);
            case SWIM_SPEED:
                return g.getBaseSwimSpeed() + 0.02 * getStatModifier(statType) * getAttributeLevel(Attribute.AGILITY);
        }
        return 1;
    }

    public double getStatModifier(StatType s) {
        return statModifiers.get(s);
    }

    public double getUndigestedNutrition(Consumable.Nutrition nutrition) {
        return this.undigested_nutrition.get(nutrition);
    }

    public BirthSign getZodiac() {
        return TimeManager.getMonths().get(birthDay.getMonth()).getSign();
    }

    public boolean hasFlag(final AttributeFlag flag) {
        if (player == null) return false;
        for (final Perk pa : perks) {
            if (pa == null)
                continue;
            if (pa.getAttributeFlags().contains(flag))
                return true;
        }
        return false;
    }

    public boolean hasPerk(Perk perk) {
        for (Perk perk1 : perks) {
            if (perk1.equals(perk)) return true;
        }
        return false;
    }

    public void init() {
        genetic = GeneticTypes.HUMAN.createGenetic();
        for (final Skill sk : Skill.values())
            skillLevels.put(sk, (double) 0);
        for (final Attribute a : Attribute.values())
            attributeValues.put(a, 0);
        for (final Consumable s : Consumable.values())
            consumables.put(s, 0.0);
        for (final Consumable.Nutrition s : Consumable.Nutrition.values())
            nutrition.put(s, 0.0);
        for (final Consumable.Nutrition s : Consumable.Nutrition.values())
            undigested_nutrition.put(s, 0.0);
        for (Deity d : Deity.values()) {
            favorLevels.put(d, 0);
        }
        for (StatType type : StatType.values()) {
            statModifiers.put(type, 1.0);
        }
    }

    public void inseminate(Profile maleProfile, Profile femaleProfile) {
//        if (gender == 0) return;
//        if (!created) return;
//        if (pregnant) return;
//
//        Genetic primary = getGenetic();
//        int cycleLength = primary.getFertileCycleLength();
//        int cycleDay = MythriaUtil.wrapInt(TimeManager.getCurrentDate().getMGD(), 1, cycleLength);
//        int padding = 0;
//        if (cycleDay + padding > cycleLength / 4 && cycleDay - padding < cycleLength / 2) {
//            pregnant = true;
//            pregMotherProfileData = femaleProfile.toNBT();
//            pregFatherProfileData = maleProfile.toNBT();
//            pregBabyCount = (int) Math.floor(-((Math.log(random.nextDouble()) / -Math.log(89)) / Math.log(89)));
//            pregConceptionData = TimeManager.getCurrentDate().getMGD();
//        }
    }

    public boolean isPerkTypeUnlocked(PerkType type) {
        return unlockedPerkTypes.contains(type);
    }

    public void newDay(LivingEntity LivingEntity) {
        handleDeathChance(LivingEntity);
    }

    public void removeAttribute(Perk perk) {
        perks.remove(perk);

        if (player == null)
            return;
        if (player.world.isRemote)
            return;

        sendDataPacket();
    }

    public void sendDataPacket() {
        if (player != null && !player.world.isRemote) {
            MythriaPacketHandler.sendTo(new SPacketProfileCache(toNBT()), (ServerPlayerEntity) player);
        }
    }

    public void setAttributeLevel(Attribute attribute, int level) {
        attributeValues.put(attribute, level);
    }

    public void setBoundAbility(int slotIndex, Ability ability) {
        boundAbilities[slotIndex] = ability;

        if (player.world.isRemote) {
            MythriaPacketHandler.sendToServer(new PacketBindAbility(slotIndex, ability));
        }
    }

    public void setConsumable(final Consumable consumable, double value) {
        if (!created)
            return;
        switch (consumable) {
            case STAMINA:
                value = MathHelper.clamp(value, 0, getStat(StatType.MAX_STAMINA) * (1 - getConsumable(Consumable.FATIGUE)));
                consumables.put(consumable, value);
                break;
            case TEMPERATURE:
                value = MathHelper.clamp(value, 0, 20);
                consumables.put(consumable, value);
                break;
            case THIRST:
                value = MathHelper.clamp(value, 0, 20);
                consumables.put(consumable, value);
                break;
            case WEIGHT:
                value = MathHelper.clamp(value, 0, Double.MAX_VALUE);
                consumables.put(consumable, value);
                StatManager.UpdateSpeed(this, player);
                break;
            case FATIGUE:
                value = MathHelper.clamp(value, 0, 0.9);
                consumables.put(consumable, value);
                StatManager.UpdateSpeed(this, player);
                break;
            case PAIN:
                value = MathHelper.clamp(value, 0, 20);
                consumables.put(consumable, value);
                break;
            case TORPOR:
                value = MathHelper.clamp(value, 0, 20);
                consumables.put(consumable, value);
                break;
            case MANA:
                value = MathHelper.clamp(value, 0, getStat(StatType.MAX_MANA));
                consumables.put(consumable, value);
                break;
            case PLEASURE:
                value = MathHelper.clamp(value, 0, 20);
                consumables.put(consumable, value);
                break;
            case BLOOD:
                value = MathHelper.clamp(value, 0, 20);
                consumables.put(consumable, value);
                if (value == 0) {
                    player.setHealth(0);
                }
                break;
        }
        if (player == null)
            return;
    }

    private void addFatigue(double staminaChange) {
        if (player.world.isRemote) return;
        if (staminaChange >= 0) return;
        final double fatigueMitigation = StatManager.getTotalFatigueMitigation(this);
        double ds = -staminaChange;
        double amount = ds / (getStat(StatType.MAX_STAMINA) * 10.0);
        addConsumable(Consumable.FATIGUE, amount * (1 - fatigueMitigation));
        addSkillExperience(Skill.AGILITY, ds * EXPConst.STAMINA_USE_TICK, (ServerPlayerEntity) player, 0);
    }

    public void setFavor(Deity d, int favor) {
        favorLevels.put(d, favor);
    }

    public void setNutrition(Consumable.Nutrition nutrition, double value) {
        this.nutrition.put(nutrition, MathHelper.clamp(value, 0, 20));
    }

    public void setPregConceptionData(int pregConceptionData) {
        this.pregConceptionData = pregConceptionData;
    }

    public void setPregFatherProfileData(CompoundNBT pregFatherProfileData) {
        this.pregFatherProfileData = pregFatherProfileData;
    }

    public void setPregMotherProfileData(CompoundNBT pregMotherProfileData) {
        this.pregMotherProfileData = pregMotherProfileData;
    }

    public void setStatModifier(StatType s, double v) {
        statModifiers.put(s, v);
    }

    public void setUndigestedNutrition(Consumable.Nutrition nutrition, double value) {
        this.undigested_nutrition.put(nutrition, MathHelper.clamp(value, 0, 20));
    }

    public void unlockPerkType(PerkType type) {
        if (!unlockedPerkTypes.contains(type)) unlockedPerkTypes.add(type);
    }

    private void addGrantedAbilities() {
        for (Ability ability : genetic.getGrantedAbilities()) {
            if (!abilities.contains(ability)) addAbility(ability);
        }
    }

    private double getTotalExperience() {
        double totalExperience = 0;
        for (double value : skillLevels.values()) {
            totalExperience += value;
        }
        return totalExperience;
    }

    private void handleDeathChance(LivingEntity LivingEntity) {
        if (!getCreated()) return;
        Genetic primaryGenetic = getGenetic();
        LifeSpanGene lifeSpanGene = primaryGenetic.getLifeSpanGene();
        if (lifeSpanGene.isImmortal()) return;
        int lifeExpectancy = lifeSpanGene.getStage(LifeSpanGene.LifeStage.ELDERLY);
        if (getBirthDay().getYearsFromCurrent() < lifeExpectancy) return;
        if (hasFlag(AttributeFlag.IMMORTALITY)) return;
        int ageMGD = TimeManager.getCurrentDate().getMGD() - getBirthDay().getMGD();
        final double dpll = ageMGD - (lifeExpectancy * TimeManager.getDaysPerYear());
        final double deathChance = dpll / TimeManager.getDaysPerYear() / 300D;
        if (random.nextDouble() < deathChance) {
            LivingEntity.setHealth(0);
        }
    }
}

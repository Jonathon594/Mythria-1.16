package me.Jonathon594.Mythria.DataTypes;

import me.Jonathon594.Mythria.Capability.Profile.Profile;
import me.Jonathon594.Mythria.Const.ColorConst;
import me.Jonathon594.Mythria.Enum.*;
import me.Jonathon594.Mythria.Managers.MaterialManager;
import me.Jonathon594.Mythria.MythriaRegistries;
import me.Jonathon594.Mythria.Util.MythriaResourceLocation;
import me.Jonathon594.Mythria.Util.MythriaUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class Perk extends ForgeRegistryEntry<Perk> {
    final HashMap<StatType, Double> statModifiers = new HashMap<>();
    //Cache
    private final List<Item> craftable = new ArrayList<>();
    private final List<Block> placable = new ArrayList<>();
    private final List<Block> breakable = new ArrayList<>();
    private final List<ResourceLocation> craftableItemTags = new ArrayList();
    private final List<ResourceLocation> craftableBlockTags = new ArrayList();
    private final List<ResourceLocation> placeableTags = new ArrayList();
    private final List<ResourceLocation> breakableTags = new ArrayList();
    private final Supplier<Perk> requiredPerk;
    private final HashMap<Skill, Integer> requiredSkills = new HashMap<>();
    private final HashMap<Deity, Integer> requiredFavor = new HashMap<>();
    private final List<AttributeFlag> attributeFlags = new ArrayList<>();
    private final Item menuIcon;
    private final HashMap<Attribute, Integer> requiredAttributes = new HashMap<>();
    private final List<String> excludedPerks = new ArrayList<>();
    private final PerkType type;
    private final List<PerkType> perkTypeUnlocks = new ArrayList<>();
    private String description = "";
    private String displayName;
    private int minimumAge;
    private double learnTime = 0;
    private double bonusFatigueMitigation = 0.0;

    public Perk(final String name, final PerkType type, final IItemProvider icon,
                final Skill relatedSkill, final int requiredLevel,
                final Supplier<Perk> requiredAttribute) {
        super();
        setRegistryName(new MythriaResourceLocation(name));
        this.type = type;
        this.menuIcon = icon.asItem();
        requiredPerk = requiredAttribute;
        if (relatedSkill != null) requiredSkills.put(relatedSkill, requiredLevel);
    }

    public Perk addAttributeFlag(final AttributeFlag flag) {
        attributeFlags.add(flag);
        return this;
    }

    public Perk addBreakable(final Block... blocks) {
        breakable.addAll(Arrays.asList(blocks));
        return this;
    }

    public Perk addBreakable(Collection<Block> blocks) {
        breakable.addAll(blocks);
        return this;
    }

    public Perk addBreakableBlockTag(ResourceLocation resourceLocation) {
        breakableTags.add(resourceLocation);
        return this;
    }

    public Perk addCraftable(final Object... items) {
        for (final Object i : items) {
            if (i instanceof Item) {
                craftable.add((Item) i);
            }
            if (i instanceof Block) {
                craftable.add(Item.getItemFromBlock((Block) i));
            }
        }

        return this;
    }

    public Perk addCraftableBlockTag(ResourceLocation resourceLocation) {
        craftableBlockTags.add(resourceLocation);
        return this;
    }

    public Perk addCraftableItemTag(ResourceLocation resourceLocation) {
        craftableItemTags.add(resourceLocation);
        return this;
    }

    public Perk addExcludedPerk(String perk) {
        excludedPerks.add(perk);
        return this;
    }

    public Perk addPerkTypeUnlock(PerkType type) {
        if (!perkTypeUnlocks.contains(type)) perkTypeUnlocks.add(type);
        return this;
    }

    public Perk addPlaceable(final Block... blocks) {
        placable.addAll(Arrays.asList(blocks));
        return this;
    }

    public Perk addPlaceableBlockTag(ResourceLocation resourceLocation) {
        placeableTags.add(resourceLocation);
        return this;
    }

    public Perk addRequiredAttribute(Attribute attribute, Integer value) {
        requiredAttributes.put(attribute, value);
        return this;
    }

    public Perk addRequiredFavor(Deity deity, int favor) {
        requiredFavor.put(deity, favor);
        return this;
    }

    public void addRequiredSkill(final Skill type, final int value) {
        requiredSkills.put(type, value);
    }

    public List<AttributeFlag> getAttributeFlags() {
        return attributeFlags;
    }

    public List<Block> getBreakable() {
        return breakable;
    }

    public List<Item> getCraftable() {
        return craftable;
    }

    public ArrayList<String> getDataLines(Profile p) {
        final ArrayList<String> lines = new ArrayList<>();
        if (requiredPerk != null) {
            Perk perk = requiredPerk.get();
            if (perk != null) {
                TextFormatting color = p.hasPerk(perk) ? ColorConst.SUCCESS_COLOR : ColorConst.CONT_COLOR;
                lines.add(ColorConst.MAIN_COLOR + "Prerequisite: " + color + perk.getDisplayName());
            }
        }
        if (minimumAge > 0) {
            int current = p.getBirthDay().getYearsFromCurrent();
            TextFormatting color = current >= minimumAge ? ColorConst.SUCCESS_COLOR : ColorConst.CONT_COLOR;
            lines.add(ColorConst.MAIN_COLOR + "Minimum Age: " + color + current + "/" + minimumAge);
        }

        for (final Entry<Skill, Integer> e : requiredSkills.entrySet()) {
            Integer current = p.getSkillLevel(e.getKey());
            TextFormatting color = current >= e.getValue() ? ColorConst.SUCCESS_COLOR : ColorConst.CONT_COLOR;

            lines.add(ColorConst.MAIN_COLOR + MythriaUtil.capitalize(e.getKey().name()) + ": " + color +
                    current + "/" + e.getValue());
        }

        for (Entry<StatType, Double> e : getStatModifiers().entrySet()) {
            lines.add(ColorConst.CONT_COLOR + e.getKey().name() + (e.getValue() > 0 ? ": +" : ": ") + e.getValue());
        }

        if (lines.size() > 0) lines.add("");

        if (requiredAttributes.size() > 0) {
            lines.add(ColorConst.HEAD_COLOR + "[Attributes]:");
            for (Entry<Attribute, Integer> e : requiredAttributes.entrySet()) {
                int current = p.getAttributeLevel(e.getKey());
                TextFormatting color = current >= e.getValue() ? ColorConst.SUCCESS_COLOR : ColorConst.CONT_COLOR;
                lines.add(ColorConst.MAIN_COLOR + "" + MythriaUtil.capitalize(e.getKey().name()) + ": " + color +
                        current + "/" + e.getValue());
            }
            lines.add("");
        }

        if (requiredFavor.size() > 0) {
            lines.add(ColorConst.HEAD_COLOR + "[Favor]:");
            for (Entry<Deity, Integer> e : requiredFavor.entrySet()) {
                int current = p.getFavor(e.getKey());
                TextFormatting color = current >= e.getValue() ? ColorConst.SUCCESS_COLOR : ColorConst.CONT_COLOR;
                lines.add(ColorConst.MAIN_COLOR + "" + MythriaUtil.capitalize(e.getKey().name()) + ": " + color +
                        current + "/" + e.getValue());
            }
            lines.add("");
        }

        if (excludedPerks.size() > 0) {
            lines.add(ColorConst.HEAD_COLOR + "[Excluded Perks]:");
            for (String s : excludedPerks) {
                Perk perk = MythriaRegistries.PERKS.getValue(new ResourceLocation(s));
                if (perk == null) continue;
                TextFormatting color = !p.hasPerk(perk) ? ColorConst.SUCCESS_COLOR : ColorConst.CONT_COLOR;
                lines.add(color + perk.getDisplayName());
            }
            lines.add("");
        }
        if (!description.isEmpty()) lines.add(ColorConst.HIGH_COLOR + description);
        return lines;
    }

    public String getDisplayName() {
        return displayName == null ? getRegistryName().getPath() : displayName;
    }

    public Perk setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public List<String> getExcludedPerks() {
        return excludedPerks;
    }

    public double getFatigueMitigation() {
        return bonusFatigueMitigation;
    }

    public ItemStack getItemStack() {
        final ItemStack is = new ItemStack(menuIcon, 1);
        is.setDisplayName(new StringTextComponent(getDisplayName()));
        return is;
    }

    public double getLearnTime() {
        return learnTime;
    }

    public Perk setLearnTime(final double learnTime) {
        this.learnTime = learnTime;
        return this;
    }

    public Item getMenuIcon() {
        return menuIcon;
    }

    public int getMinimumAge() {
        return minimumAge;
    }

    public Perk setMinimumAge(final int minimumAge) {
        this.minimumAge = minimumAge;
        return this;
    }

    public Perk getParent() {
        if (requiredPerk == null) {
            return null;
        }

        return requiredPerk.get();
    }

    public List<PerkType> getPerkTypeUnlocks() {
        return perkTypeUnlocks;
    }

    public List<Block> getPlacable() {
        return placable;
    }

    public HashMap<Attribute, Integer> getRequiredAttributes() {
        return requiredAttributes;
    }

    public Perk getRequiredPerk() {
        return requiredPerk.get();
    }

    public HashMap<Skill, Integer> getRequiredSkills() {
        return requiredSkills;
    }

    public HashMap<StatType, Double> getStatModifiers() {
        return statModifiers;
    }

    public PerkType getType() {
        return type;
    }

    public boolean hasRequiredFavor(Profile p) {
        for (Entry<Deity, Integer> e : requiredFavor.entrySet()) {
            if (p.getFavor(e.getKey()) < e.getValue()) return false;
        }
        return true;
    }

    public boolean hasRequiredPerks(final Profile p) {
        if (requiredPerk.get() == null) return true;
        final Perk perk = requiredPerk.get();
        if (perk == null) return false;
        return p.hasPerk(perk);
    }

    public boolean hasRequiredSkills(final Profile p) {
        for (final Entry<Skill, Integer> e : requiredSkills.entrySet())
            if (p.getSkillLevel(e.getKey()) < e.getValue())
                return false;
        return true;
    }

    public boolean isExcluded(Profile p) {
        for (String s : getExcludedPerks()) {
            Perk excluded = MythriaRegistries.PERKS.getValue(new ResourceLocation(s));
            if (excluded == null) continue;
            if (p.hasPerk(excluded)) {
                return true;
            }
        }
        return false;
    }

    public void onTagsUpdated() {
        List<Item> craftableTagItems = new ArrayList<>();
        for (ResourceLocation resourceLocation : craftableItemTags) {
            ITag<Item> itemITag = ItemTags.getCollection().get(resourceLocation);
            if (itemITag == null) continue;
            craftableTagItems.addAll(itemITag.getAllElements());
        }
        for (ResourceLocation resourceLocation : craftableBlockTags) {
            ITag<Block> blockITag = BlockTags.getCollection().get(resourceLocation);
            if (blockITag == null) continue;
            for (Block block : blockITag.getAllElements()) {
                craftableTagItems.add(block.asItem());
            }
        }
        for (Item item : craftableTagItems) {
            if (!craftable.contains(item)) craftable.add(item);
        }

        for (ResourceLocation resourceLocation : placeableTags) {
            ITag<Block> blockITag = BlockTags.getCollection().get(resourceLocation);
            if (blockITag == null) continue;
            for (Block block : blockITag.getAllElements()) {
                if (!placable.contains(block)) placable.add(block);
            }
        }

        for (ResourceLocation resourceLocation : breakableTags) {
            ITag<Block> blockITag = BlockTags.getCollection().get(resourceLocation);
            if (blockITag == null) continue;
            for (Block block : blockITag.getAllElements()) {
                if (!breakable.contains(block)) breakable.add(block);
            }
        }

        MaterialManager.registerCraftable(craftable, this);
        MaterialManager.registerBreakable(breakable, this);
        MaterialManager.registerPlaceable(placable, this);
    }

    public Perk setBonusFatigueMitigation(final double d) {
        bonusFatigueMitigation = d;
        return this;
    }

    @Override
    public String toString() {
        return getRegistryName().toString();
    }

    public Perk withDescription(String description) {
        this.description = description;
        return this;
    }

    private String getDescription() {
        return description;
    }
}

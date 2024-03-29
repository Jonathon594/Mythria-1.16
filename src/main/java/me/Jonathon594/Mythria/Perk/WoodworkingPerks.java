package me.Jonathon594.Mythria.Perk;

import com.google.common.collect.ImmutableList;
import me.Jonathon594.Mythria.Blocks.MythriaBlocks;
import me.Jonathon594.Mythria.DataTypes.Perk;
import me.Jonathon594.Mythria.DataTypes.RootPerk;
import me.Jonathon594.Mythria.Enum.Attribute;
import me.Jonathon594.Mythria.Enum.PerkType;
import me.Jonathon594.Mythria.Enum.Skill;
import me.Jonathon594.Mythria.Interface.IPerkRegistry;
import me.Jonathon594.Mythria.Items.MythriaItems;
import me.Jonathon594.Mythria.Mythria;
import me.Jonathon594.Mythria.Util.MythriaResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;

@ObjectHolder(Mythria.MODID)
public class WoodworkingPerks implements IPerkRegistry {
    public static final Perk WOODCARVING = null;
    public static final Perk WOODEN_WEAPONS = null;

    @Override
    public List<Perk> getPerks(PerkType type) {
        return ImmutableList.of(
                new RootPerk("woodcarving", type, MythriaItems.OAK_TOOL_HANDLE, Skill.CRAFTING, 0,
                        new ResourceLocation("minecraft:textures/block/oak_log.png"))
                        .withDescription("Wood seems soft enough to carve into other shapes.")
                        .addCraftableItemTag(new MythriaResourceLocation("tool_handles"))
                        .addCraftableItemTag(new MythriaResourceLocation("blade_handles"))
                        .addCraftableItemTag(new MythriaResourceLocation("saw_handles"))
                        .addCraftableItemTag(new MythriaResourceLocation("sticks"))
                        .addRequiredAttribute(Attribute.DEXTERITY, 2),

                new Perk("advanced_woodcarving", type, MythriaBlocks.OAK_TANNING_RACK, Skill.CRAFTING, 5, () -> WOODCARVING)
                        .setDisplayName("Advanced Woodcarving").withDescription("Combining different cut pieces of wood together can make larger structures")
                        .addCraftableBlockTag(new MythriaResourceLocation("tanning_racks"))
                        .addCraftableBlockTag(new MythriaResourceLocation("saw_horses"))
                        .addPerkTypeUnlock(PerkType.LEATHER_WORKING)
                        .addRequiredAttribute(Attribute.INTELLIGENCE, 3),

                new Perk("wooden_weapons", type, MythriaItems.OAK_CLUB, Skill.CRAFTING, 0, () -> WOODCARVING)
                        .setDisplayName("Basic Wooden Weapons").withDescription("With a big enough piece of wood cut the right way, you could hurt somebody.")
                        .addCraftableItemTag(new MythriaResourceLocation("clubs"))
                        .addRequiredAttribute(Attribute.INTELLIGENCE, 3),

                new Perk("advanced_wooden_weapons", type, MythriaItems.OAK_SPEAR, Skill.CRAFTING, 5, () -> WOODEN_WEAPONS)
                        .setDisplayName("The art of sharp").withDescription("If I were to sharpen this material...")
                        .addCraftableItemTag(new MythriaResourceLocation("spears"))
                        .addCraftableItemTag(new MythriaResourceLocation("arrow_shafts"))
                        .addPerkTypeUnlock(PerkType.FLETCHING)
                        .addRequiredAttribute(Attribute.INTELLIGENCE, 4)
        );
    }
}

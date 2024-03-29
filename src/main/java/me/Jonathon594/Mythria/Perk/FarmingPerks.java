package me.Jonathon594.Mythria.Perk;

import com.google.common.collect.ImmutableList;
import me.Jonathon594.Mythria.DataTypes.Perk;
import me.Jonathon594.Mythria.DataTypes.RootPerk;
import me.Jonathon594.Mythria.Enum.Attribute;
import me.Jonathon594.Mythria.Enum.PerkType;
import me.Jonathon594.Mythria.Enum.Skill;
import me.Jonathon594.Mythria.Interface.IPerkRegistry;
import me.Jonathon594.Mythria.Mythria;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;

@ObjectHolder(Mythria.MODID)
public class FarmingPerks implements IPerkRegistry {
    public static final Perk FARMING = null;

    @Override
    public List<Perk> getPerks(PerkType type) {
        return ImmutableList.of(
                new RootPerk("farming", type, Items.WHEAT_SEEDS, Skill.FARMING, 0,
                        new ResourceLocation("minecraft:textures/block/farmland.png"))
                        .withDescription("A hoe can soften the ground and allow you to grow plants.")
                        .addRequiredAttribute(Attribute.ENDURANCE, 4)
                        .addPlaceable(Blocks.FARMLAND, Blocks.WHEAT)
        );
    }
}

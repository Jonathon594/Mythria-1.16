package me.Jonathon594.Mythria.Tags;

import me.Jonathon594.Mythria.Util.MythriaResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

public class MythriaBlockTags {
    public static final ITag<Block> TREE_FELLER_LEAVES = wrapTag("tree_feller_leaves");
    public static final ITag<Block> TANNING_RACKS = wrapTag("tanning_racks");

    private static ITag<Block> wrapTag(String name) {
        return BlockTags.getCollection().get(new MythriaResourceLocation(name));
    }
}

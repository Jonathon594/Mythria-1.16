package me.Jonathon594.Mythria.Items;

import com.google.common.collect.Sets;
import me.Jonathon594.Mythria.Capability.Profile.Profile;
import me.Jonathon594.Mythria.Capability.Profile.ProfileProvider;
import me.Jonathon594.Mythria.Client.Renderer.Items.HammerItemRenderer;
import me.Jonathon594.Mythria.DataTypes.MythriaToolType;
import me.Jonathon594.Mythria.Enum.AttributeFlag;
import me.Jonathon594.Mythria.Enum.Consumable;
import me.Jonathon594.Mythria.Enum.Skill;
import me.Jonathon594.Mythria.Interface.IModularTool;
import me.Jonathon594.Mythria.Interface.IWeapon;
import me.Jonathon594.Mythria.Managers.Crafting.ConstructionManager;
import me.Jonathon594.Mythria.Managers.MaterialManager;
import me.Jonathon594.Mythria.Managers.MeleeCombatManager;
import me.Jonathon594.Mythria.Managers.StatManager;
import me.Jonathon594.Mythria.Mythria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.function.Supplier;

public class MythriaHammerItem extends ToolItem implements IModularTool, IWeapon {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private final double weight;
    private final Supplier<Item> toolHead;

    public MythriaHammerItem(String name, float damage, float speed, IItemTier tier, double weight, Supplier<Item> toolHead) {
        super(damage, speed, tier, EFFECTIVE_ON, new Item.Properties().group(ItemGroup.COMBAT)
                .addToolType(MythriaToolType.HAMMER, tier.getHarvestLevel())
                .setISTER(() -> HammerItemRenderer::new));
        setRegistryName(Mythria.MODID, name);
        this.weight = weight;
        this.toolHead = toolHead;
    }

    @Override
    public MeleeCombatManager getCombatManager() {
        return MeleeCombatManager.HAMMER_MANAGER;
    }

    @Override
    public AttributeFlag getFlagForParrying() {
        return AttributeFlag.HAMMER_ABILITY_PARRY;
    }

    @Override
    public Skill getUsageSkill() {
        return Skill.HEAVY_WEAPONS;
    }

    @Override
    public Item getToolHeadItem() {
        return toolHead.get();
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        ItemStack itemStack = player.getHeldItem(hand);
        World worldIn = context.getWorld();
        Profile profile = ProfileProvider.getProfile(player);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return ActionResultType.FAIL;
        } else {
            BlockState blockState = worldIn.getBlockState(pos);
            Block block = blockState.getBlock();
            if (ConstructionManager.isReinforced(block)) {
                if (!worldIn.isRemote) {
                    if (ConstructionManager.canBlockBeDemolishedBy(player, pos, player.world.getDimensionKey(), block)) {
                        double cost = MaterialManager.getStaminaCostForBreaking(blockState, worldIn, pos) * 4;
                        if (profile.getConsumable(Consumable.STAMINA) > cost) {
                            worldIn.destroyBlock(pos, true);
                            itemStack.damageItem(1, player, (playerEntity) ->
                                    playerEntity.sendBreakAnimation(new ItemUseContext(player, Hand.MAIN_HAND, null).getHand()));
                            profile.addConsumable(Consumable.STAMINA, -cost);
                        } else {
                            //MessageUtils.sendMessage(player, MythriaConst.CANT_DECONSTRUC);
                            return ActionResultType.PASS;
                        }
                    } else {
                        //MessageUtils.sendMessage(player, MythriaConst.CANT_DECONSTRUC);
                        return ActionResultType.PASS;
                    }
                }

                return ActionResultType.SUCCESS;
            } else {
                return ActionResultType.PASS;
            }

        }
    }
}

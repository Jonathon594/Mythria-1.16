package me.Jonathon594.Mythria.Ability;

import me.Jonathon594.Mythria.Capability.Profile.Profile;
import me.Jonathon594.Mythria.Capability.Profile.ProfileProvider;
import me.Jonathon594.Mythria.Enum.Consumable;
import me.Jonathon594.Mythria.Enum.StatType;
import me.Jonathon594.Mythria.Util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;

public class InstantDryadGrowthAbility extends InstantAbility {
    private final ArrayList<Block> growables = new ArrayList<>();

    public InstantDryadGrowthAbility(String name) {
        super(name);
        for (Block b : ForgeRegistries.BLOCKS.getValues()) {
            if (b instanceof IGrowable) growables.add(b);
        }
    }

    @Override
    public void onInstantActivate(AbilityInstance abilityInstance) {
        if (!abilityInstance.isOnCooldown()) {
            PlayerEntity playerEntity = abilityInstance.getOwner();
            World world = playerEntity.world;
            Profile profile = ProfileProvider.getProfile(playerEntity);
            if (world.isRemote) return;
            BlockPos pos = playerEntity.getPosition().down();
            ArrayList<BlockPos> positions = new ArrayList<>();
            BlockUtils.getConnected(world, pos, positions, growables, Integer.MAX_VALUE, 10, 12, pos);

            Consumable mana = Consumable.MANA;
            double playerMana = profile.getConsumable(mana);
            double maxMana = profile.getStat(StatType.MAX_MANA);
            if (playerMana > maxMana) playerMana = maxMana;


            int count = 10;
            for (int i = 0; i < Math.min(count, positions.size()); i++) {
                if (positions.isEmpty()) break;
                Collections.shuffle(positions);
                BlockPos growPos = positions.get(i);
                BlockState growState = world.getBlockState(growPos);
                if (growState.getBlock() instanceof IGrowable) {
                    IGrowable growable = (IGrowable) growState.getBlock();
                    if (growable.canGrow(world, growPos, growState, world.isRemote)) {
                        int cost = 20;
                        if (playerMana < cost) break;
                        playerMana -= cost;
                        growable.grow((ServerWorld) world, random, growPos, growState);
                        abilityInstance.setCooldown(100);
                    }
                }
            }

            if (playerMana != profile.getConsumable(Consumable.MANA)) profile.setConsumable(mana, playerMana);
        }
    }
}

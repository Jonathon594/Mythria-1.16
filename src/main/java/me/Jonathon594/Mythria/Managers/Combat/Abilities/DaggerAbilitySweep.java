package me.Jonathon594.Mythria.Managers.Combat.Abilities;

import me.Jonathon594.Mythria.Capability.Profile.Profile;
import me.Jonathon594.Mythria.Enum.AttributeFlag;
import me.Jonathon594.Mythria.Event.CombatEvent;
import me.Jonathon594.Mythria.Interface.ICombatAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class DaggerAbilitySweep implements ICombatAbility {
    @Override
    public AttributeFlag getRequiredFlag() {
        return AttributeFlag.DAGGER_ABILITY_SWEEP;
    }

    @Override
    public double getStaminaMultiplier() {
        return 1.5;
    }

    @Override
    public void onCombatPost(PlayerEntity player, Profile profile, Entity target, CombatEvent.Post postEvent) {
        postEvent.setShouldSweep(true);
    }

    @Override
    public void onCombatPre(PlayerEntity player, Profile profile, Entity target, CombatEvent.Pre preEvent) {
    }
}

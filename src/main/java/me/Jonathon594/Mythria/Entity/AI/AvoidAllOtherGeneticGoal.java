package me.Jonathon594.Mythria.Entity.AI;

import me.Jonathon594.Mythria.Capability.Profile.ProfileProvider;
import me.Jonathon594.Mythria.DataTypes.Genetic.Genetic;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.player.PlayerEntity;

public class AvoidAllOtherGeneticGoal extends AvoidEntityGoal<PlayerEntity> {
    private final Genetic genetic;

    public AvoidAllOtherGeneticGoal(CreatureEntity creatureEntity, Genetic genetic) {
        super(creatureEntity, PlayerEntity.class, 6.0f, 1.0, 1.2);
        this.genetic = genetic;
    }

    @Override
    public boolean shouldExecute() {
        boolean avoid = super.shouldExecute();
        if (avoidTarget != null) {
            if (ProfileProvider.getProfile(avoidTarget).getGenetic().equals(genetic)) {
                avoid = false;
            }
        }
        return avoid;
    }
}

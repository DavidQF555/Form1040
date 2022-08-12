package io.github.davidqf555.minecraft.f1040.common.events;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.entities.TargetIndebtedGoal;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.registration.EntityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WorldEventSubscriber {

    private WorldEventSubscriber() {
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof IronGolemEntity) {
            ((IronGolemEntity) entity).targetSelector.addGoal(3, new TargetIndebtedGoal<>((MobEntity) entity, true));
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.world instanceof ServerWorld && !event.world.dimensionType().hasFixedTime() && event.world.getDayTime() % ServerConfigs.INSTANCE.taxPeriod.get() == 0) {
            event.world.players().forEach(player -> {
                if (!player.isCreative() && !player.isSpectator()) {
                    if (ServerConfigs.INSTANCE.villageRange.get() == -1 || ((ServerWorld) event.world).sectionsToVillage(SectionPos.of(player)) <= SectionPos.blockToSection(ServerConfigs.INSTANCE.villageRange.get())) {
                        Debt.add(player);
                        int min = ServerConfigs.INSTANCE.taxCollectorMin.get();
                        int max = ServerConfigs.INSTANCE.taxCollectorMax.get();
                        TaxCollectorEntity.spawn(player, EntityRegistry.TAX_COLLECTOR.get(), min, max);
                        if (Debt.isIndebted(player)) {
                            for (int i = 0; i < ServerConfigs.INSTANCE.ironGolemCount.get(); i++) {
                                TaxCollectorEntity.spawn(player, EntityType.IRON_GOLEM, min, max);
                            }
                        }
                    }
                }
            });
        }
    }

}

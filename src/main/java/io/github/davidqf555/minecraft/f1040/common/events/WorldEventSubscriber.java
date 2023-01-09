package io.github.davidqf555.minecraft.f1040.common.events;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.entities.ShadyBankerEntity;
import io.github.davidqf555.minecraft.f1040.common.entities.TargetIndebtedGoal;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.registration.EntityRegistry;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
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
        if (entity instanceof IronGolem) {
            ((IronGolem) entity).targetSelector.addGoal(3, new TargetIndebtedGoal<>((Mob) entity, true));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel && !event.world.dimensionType().hasFixedTime() && event.world.getDayTime() % ServerConfigs.INSTANCE.taxPeriod.get() == 0) {
            event.world.players().forEach(player -> {
                if (!player.isCreative() && !player.isSpectator()) {
                    if (ServerConfigs.INSTANCE.villageRange.get() == -1 || ((ServerLevel) event.world).sectionsToVillage(SectionPos.of(player)) <= SectionPos.blockToSection(ServerConfigs.INSTANCE.villageRange.get())) {
                        int min = ServerConfigs.INSTANCE.taxCollectorMin.get();
                        int max = ServerConfigs.INSTANCE.taxCollectorMax.get();
                        TaxCollectorEntity collector = TaxCollectorEntity.spawn(player, EntityRegistry.TAX_COLLECTOR.get(), min, max);
                        if (collector != null) {
                            Debt.add(player, collector.getTaxRate((ServerPlayer) player));
                            if (Debt.isIndebted(player)) {
                                for (int i = 0; i < ServerConfigs.INSTANCE.ironGolemCount.get(); i++) {
                                    TaxCollectorEntity.spawn(player, EntityType.IRON_GOLEM, min, max);
                                }
                            }
                        }

                        if (player.getRandom().nextDouble() < ServerConfigs.INSTANCE.shadyBankerRate.get()) {
                            ShadyBankerEntity entity = TaxCollectorEntity.spawn(player, EntityRegistry.SHADY_BANKER.get(), min, max);
                            if (entity != null) {
                                entity.setDespawn(6000);
                            }
                        }
                    }
                }
            });
        }
    }

}
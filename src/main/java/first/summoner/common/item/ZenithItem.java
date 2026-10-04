package first.summoner.common.item;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.attachmentEntity.Ellipse;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.sound.Playable;
import first.lyra.register.LyraAttachmentRegister;
import first.lyra.utils.EasingCurve;
import first.summoner.common.projectile.Zenith;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ZenithItem extends SwordItem {

    public ZenithItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (usedHand == InteractionHand.MAIN_HAND) {
            ItemCooldowns cooldowns = player.getCooldowns();
            if (!cooldowns.isOnCooldown(this)) {
                cooldowns.addCooldown(this, 2);
                if (!level.isClientSide()) {
                    Playable.play(SummonerSoundRegister.Zenith, level, player.position(), player.getSoundSource());
                    Vec3 lookAngle = player.getLookAngle();
                    Vec3 center = player.getBoundingBox().getCenter();
                    RandomSource random = player.getRandom();
                    Vec3 eyePos = player.getEyePosition();
                    int reach = 32;
                    Vec3 endEndPos = eyePos.add(lookAngle.scale(reach));
                    EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eyePos, endEndPos, player.getBoundingBox().inflate(reach), entity -> entity instanceof LivingEntity living && living.isAlive() && living != player, reach * reach);
                    if (entityHit != null && entityHit.getEntity() instanceof LivingEntity) {
                        endEndPos = entityHit.getLocation();
                    }
                    TargetCache targetCache = player.getData(LyraAttachmentRegister.TargetCache);
                    List<LivingEntity> targetList = targetCache.getEntitiesInRadius(endEndPos, 32, null);
                    LivingEntity best = null;
                    if (!targetList.isEmpty()) {
                        int searchRange = 32;
                        double fovAngle = 30;
                        double distanceWeight = 0.2;
                        double bestScore = Double.MAX_VALUE;
                        for (LivingEntity living : targetList) {
                            Vec3 targetPoint = living.getBoundingBox().getCenter();
                            Vec3 toEntity = targetPoint.subtract(center);
                            double angle = Math.toDegrees(Math.acos(toEntity.dot(lookAngle) / toEntity.length()));
                            if (angle <= fovAngle) {
                                // 角度、距离分别归一化到 [0,1]
                                double normalizedAngle = angle / fovAngle;
                                double normalizedDistance = toEntity.length() / searchRange;
                                // 综合分数：按权重合成角度与距离，越小越优先
                                double score = (1.0 - distanceWeight) * normalizedAngle + distanceWeight * normalizedDistance;
                                if (score < bestScore) {
                                    bestScore = score;
                                    best = living;
                                }
                            }
                        }
                    }
                    float distance = (float) eyePos.distanceTo(endEndPos);
                    for (int i = 1; i < 4; i++) {
                        Zenith zenith = new Zenith();
                        zenith.setOwner(player);
                        zenith.setDamage((float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) + 1.9f);
                        zenith.setKnockback(1);
                        Vec3 endPos = endEndPos.add(Vec3.ZERO.subtract(Vec3.ZERO.offsetRandom(random, distance * 0.2f)));
                        if (i == 1) {
                            zenith.renderType = Zenith.RenderType.ZENITH;
                        }
                        if (zenith.renderType != Zenith.RenderType.ZENITH) {
                            List<LivingEntity> radius = zenith.getTargetCache().getEntitiesInRadius(endPos, 10, null);
                            if (!radius.isEmpty()) {
                                endPos = radius.get(player.getRandom().nextInt(radius.size())).getBoundingBox().getCenter();
                            } else {
                                if (best != null) {
                                    endPos = best.getBoundingBox().getCenter();
                                }
                            }
                        }
                        Vec3 startPos = center.add(center.subtract(endPos).normalize().scale(1.5f));
                        Vec3 normal = Ellipse.randomPlaneNormal(random, endPos, startPos);
                        float distanceTo = (float) (startPos.distanceTo(endPos));
                        float curvature = 0.3f + random.nextFloat() * 0.6f - Math.min(0.1f, distanceTo * 0.05f);
                        if (distanceTo < 4) {
                            curvature *= 1 + ((4 - distanceTo) / 4);
                        }
                        Ellipse ellipse = new Ellipse(endPos, startPos, normal, curvature);
                        ArrayList<PathNode> list = new ArrayList<>();
                        for (int j = i; j < 10 + i; j++) {
                            float progress = (float) j / (9 + i);
                            progress = EasingCurve.bezier()
                                    .control(0.1f)
                                    .control(0.2f)
                                    .control(0.3f)
                                    .control(0.4f)
                                    .control(0.45f)
                                    .control(0.475f)
                                    .control(0.5f)
                                    .control(0.525f)
                                    .control(0.55f)
                                    .control(0.6f)
                                    .control(0.7f)
                                    .control(0.8f)
                                    .control(0.9f)
                                    .control(1f)
                                    .build()
                                    .apply(progress);
                            Vec3 point = ellipse.getPoint(progress);
                            Vec3 tipPoint;
                            if (progress < 0.5) {
                                tipPoint = startPos.lerp(endPos, Mth.clamp(progress * 2, 0.01, 0.99));
                            } else {
                                tipPoint = endPos.lerp(startPos, Mth.clamp(progress * 2 - 1, 0.01, 0.99));
                            }
                            Vec3 tipDir = point.subtract(tipPoint).normalize();
                            list.add(zenith.getEulerNode(point, tipDir, normal));
                        }
                        list.add(list.getLast());
                        list.add(list.getLast());
                        zenith.init(list.getFirst());
                        zenith.setPath(list);
                        zenith.initialPosition = player.getPosition(1);
                        LyraHelper.get(player).add(zenith);
                    }
                }
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}

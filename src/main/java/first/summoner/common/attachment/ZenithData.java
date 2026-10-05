package first.summoner.common.attachment;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.sound.Playable;
import first.lyra.register.LyraAttachmentRegister;
import first.summoner.common.projectile.Zenith;
import first.summoner.register.SummonerItemRegister;
import first.summoner.register.SummonerSoundRegister;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.List;

public class ZenithData {

    private final Player owner;
    private float power = 0;

    public ZenithData(IAttachmentHolder holder) {
        if (holder instanceof Player player) {
            this.owner = player;
        } else {
            throw new IllegalArgumentException("ZenithData can only be used with an Player");
        }
    }

    public void tick() {
        if (owner.getMainHandItem().is(SummonerItemRegister.Zenith)) {
            if (power > 3.33f) {
                Playable.play(SummonerSoundRegister.Zenith, owner.level(), owner.position(), owner.getSoundSource());
                do {
                    power -= 3.33f;
                    Vec3 lookAngle = owner.getLookAngle();
                    Vec3 center = owner.getBoundingBox().getCenter();
                    RandomSource random = owner.getRandom();
                    Vec3 eyePos = owner.getEyePosition();
                    int reach = 32;
                    Vec3 endEndPos = eyePos.add(lookAngle.scale(reach));
                    EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(owner, eyePos, endEndPos, owner.getBoundingBox().inflate(reach), entity -> entity instanceof LivingEntity living && living.isAlive() && living != owner, reach * reach);
                    if (entityHit != null && entityHit.getEntity() instanceof LivingEntity) {
                        endEndPos = entityHit.getLocation();
                    }
                    TargetCache targetCache = owner.getData(LyraAttachmentRegister.TargetCache);
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
                    Zenith zenith = new Zenith();
                    zenith.setOwner(owner);
                    zenith.setDamage((float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    zenith.setKnockback(1);
                    Vec3 endPos = endEndPos.add(Vec3.ZERO.subtract(Vec3.ZERO.offsetRandom(random, distance * 0.2f)));
                    if ( random.nextFloat() < 0.33) {
                        zenith.renderType = Zenith.RenderType.ZENITH;
                    }
                    if (zenith.renderType != Zenith.RenderType.ZENITH) {
                        List<LivingEntity> radius = zenith.getTargetCache().getEntitiesInRadius(endPos, 10, null);
                        if (!radius.isEmpty()) {
                            endPos = radius.get(owner.getRandom().nextInt(radius.size())).getBoundingBox().getCenter();
                        } else {
                            if (best != null) {
                                endPos = best.getBoundingBox().getCenter();
                            }
                        }
                    }
                    zenith.offest = endPos.subtract(center);
                    zenith.target = best;
                    zenith.init(zenith.getRenderNode(1));
                    LyraHelper.get(owner).add(zenith);
                } while (power > 3.33f);
            }
        } else {
            power = 0;
        }
    }

    public boolean swing() {
        if (owner.getMainHandItem().is(SummonerItemRegister.Zenith)) {
            owner.swing(InteractionHand.MAIN_HAND, true);
            owner.resetAttackStrengthTicker();
            return true;
        }
        return false;
    }

    public void addPower() {
        power = (float) (power + owner.getAttributeValue(Attributes.ATTACK_SPEED) * 0.5);
    }

    public Player getOwner() {
        return owner;
    }
}

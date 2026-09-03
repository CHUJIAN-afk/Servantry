package first.summoner.common.attachment;

import first.lyra.common.damageInfo.IDamageSourceCritical;
import first.summoner.common.summonMark.SummonMarkType;
import first.summoner.register.SummonerAttachmentRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SummonMarkData implements AttachmentSyncHandler<SummonMarkData> {

    private final Random random = new Random();
    private LivingEntity target = null;
    private int duration = 0;
    private float power = 0;
    private float maxPower = 180;
    private Set<SummonMarkType> markTypes = new HashSet<>();
    private boolean changed = false;
    private float criticalDamage = 0;
    private float criticalArmorPierce = 0;
    private float criticalHitRate = 0;

    public float getCriticalDamage() {
        return criticalDamage;
    }

    public float getCriticalArmorPierce() {
        return criticalArmorPierce;
    }

    public float getCriticalHitRate() {
        return criticalHitRate;
    }

    public boolean isDamageCritical(DamageSource damageSource) {
        if (criticalHitRate > random.nextFloat()) {
            if (damageSource instanceof IDamageSourceCritical iDamageSourceCritical) {
                iDamageSourceCritical.lyra$setCritical(true);
            }
            return true;
        }
        return false;
    }

    @Nullable
    public LivingEntity getMarkTarget() {
        return target;
    }

    public void setMarkTarget(LivingEntity target) {
        if (this.target != target) {
            changed = true;
        }
        this.target = target;
    }

    public boolean isSummonMarkTarget(LivingEntity living) {
        return getMarkTarget() == living;
    }

    public Set<SummonMarkType> getMarkTypes() {
        return markTypes;
    }

    public void setMarkTypes(Set<SummonMarkType> markTypes) {
        if (!this.markTypes.equals(markTypes)) {
            changed = true;
            criticalDamage =  0;
            criticalArmorPierce = 0;
            criticalHitRate = 0;
            for (SummonMarkType markType : markTypes) {
                criticalDamage += markType.additionalDamage();
                criticalArmorPierce += markType.additionalArmorPierce();
                criticalHitRate += markType.criticalHitRate();
            }
        }
        this.markTypes = markTypes;
    }

    public int getDuration() {
        return duration;
    }

    public boolean usePower(float power) {
        if (this.power >= power) {
            setPower(this.power - power);
            return true;
        }
        return false;
    }

    public void setDuration(int duration) {
        if (this.duration != duration) {
            changed = true;
        }
        this.duration = duration;
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        float targetPower = Math.min(power, maxPower);
        if (this.power != targetPower) {
            changed = true;
        }
        this.power = targetPower;
    }

    public float getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(float maxPower) {
        if (this.maxPower != maxPower) {
            changed = true;
        }
        this.maxPower = maxPower;
    }

    public void tick(Player player) {
        if (!player.level().isClientSide()) {
            if (getDuration() > 0) {
                if (target != null && target.isAlive()) {
                    for (SummonMarkType type : markTypes) {
                        type.tick(target, player);
                    }
                } else {
                    setDuration(0);
                }
                setDuration(getDuration() - 1);
            }
            if (getDuration() <= 0) {
                setMarkTarget(null);
                setPower(0);
                setMarkTypes(new HashSet<>());
            }
            if (changed && !player.level().isClientSide()) {
                changed = false;

                player.syncData(SummonerAttachmentRegister.SummonMarkData);
            }
        }
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, @NotNull SummonMarkData data, boolean isSelf) {
        buf.writeInt(data.target != null ? data.target.getId() : -1);
    }

    @Override
    public SummonMarkData read(@NotNull IAttachmentHolder holder, @NotNull RegistryFriendlyByteBuf buf, @Nullable SummonMarkData oldData) {
        SummonMarkData data = oldData != null ? oldData : new SummonMarkData();
        int id = buf.readInt();
        if (((LivingEntity) holder).level().getEntity(id) instanceof LivingEntity living) {
            data.target = living;
        } else {
            data.target = null;
        }
        return data;
    }
}

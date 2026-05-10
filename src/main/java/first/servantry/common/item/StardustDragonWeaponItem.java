package first.servantry.common.item;

import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.item.IServantWeapon;
import first.servantry.common.servant.StardustDragon;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.SoundRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 星尘龙武器 - 处理多体节召唤。
 * <p>
 * 首次召唤创建3个体节，重复召唤增加体节。
 * 体节在玩家上方生成，按固定间距排列。
 * </p>
 */
public class StardustDragonWeaponItem extends Item implements IServantWeapon<StardustDragon> {

    private StardustDragon stardustDragon = null;

    public StardustDragonWeaponItem() {
        super(new Properties().rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public AttachmentEntityType<StardustDragon> getType() {
        return AttachmentEntityRegister.StardustDragon.get();
    }

    @Override
    public StardustDragon getDummyServant() {
        if (stardustDragon == null) {
            stardustDragon = getType().factory().get();
        }
        return stardustDragon;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundRegister.UseServantWeapon.get();
    }

    @Override
    public void handleSummon(Player player) {
        EntityData data = player.getData(AttachmentRegister.EntityData);
        AttachmentEntityType<StardustDragon> type = getType();

        // 查找现有体节
        List<StardustDragon> existing = data.getEntities().stream()
                .filter(e -> e instanceof StardustDragon)
                .map(e -> (StardustDragon) e)
                .toList();

        if (existing.isEmpty()) {
            // 首次召唤：创建多个体节
            summonInitialSegments(player, data, type);
        } else {
            // 增加体节
            addSegment(player, data, type, existing);
        }
    }

    /**
     * 首次召唤：创建初始体节组。
     */
    private void summonInitialSegments(Player player, EntityData data, AttachmentEntityType<StardustDragon> type) {
        // 检查栏位是否足够
        int initial_segments = 3;
        if (data.getUsedSlots() + 1 > data.getMaxServantSize(player)) {
            return;
        }
        Vec3 spawnPos = player.position().add(0, 3, 0);
        for (int i = 0; i < initial_segments; i++) {
            StardustDragon segment = type.factory().get();
            segment.setOwner(player);
            segment.setSegmentIndex(i);
            segment.setTotalSegments(initial_segments);
            if (data.summonServant(player, segment)) {
                // 在生成位置后方按间距排列
                segment.init(new PathNode(spawnPos.subtract(0, 0, i * segment.getSegmentDistance()), 0, 0, 0));
            }
        }
    }

    /**
     * 增加体节：在现有体节组末尾添加新体节。
     */
    private void addSegment(Player player, EntityData data, AttachmentEntityType<StardustDragon> type, List<StardustDragon> existing) {
        // 检查栏位是否足够
        if (data.getUsedSlots() + 1 > data.getMaxServantSize(player)) {
            return;
        }

        // 找到最大索引
        int maxIndex = existing.stream()
                .mapToInt(StardustDragon::getSegmentIndex)
                .max()
                .orElse(0);

        // 创建新体节
        StardustDragon newSegment = type.factory().get();
        newSegment.setOwner(player);
        newSegment.setSegmentIndex(maxIndex + 1);

        if (data.summonServant(player, newSegment)) {
            // 在最后一个体节位置初始化
            Vec3 lastPos = existing.stream()
                    .filter(e -> e.getSegmentIndex() == maxIndex)
                    .findFirst()
                    .map(StardustDragon::getPos)
                    .orElse(player.position().add(0, 3, 0));
            newSegment.init(new PathNode(lastPos, 0, 0, 0));
        }
    }

}

package first.servantry.api.damageInfo;

import com.mojang.blaze3d.vertex.VertexConsumer;
import first.servantry.Servantry;
import first.servantry.utils.EasingCurve;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * 伤害数字渲染数据。
 * <p>
 * 贴图布局：320×32，1~9,0 从左到右排列，每个数字 32×32 像素。
 * </p>
 */
public class DamageInfo {

    private static final ResourceLocation TEXTURE = Servantry.rl("textures/damage_font.png");

    /** 贴图中数字总数 */
    private static final int GLYPH_COUNT = 10;
    /** 每个数字在世界中的渲染尺寸 */
    public static final float RENDER_SIZE = 0.2f;
    /** 数字间距系数 */
    public static final float STEP_FACTOR = 0.75f;

    /** 静态缓存：每个 digit 对应的贴图 U 坐标 [digit] = {u0, u1} */
    private static final float[][] GLYPH_UV = new float[10][2];

    static {
        for (int d = 0; d < 10; d++) {
            // 贴图排列顺序：1~9,0，即 digit 1 在 index 0，digit 0 在 index 9
            int index = (d == 0) ? 9 : d - 1;
            GLYPH_UV[d][0] = (float) index / GLYPH_COUNT;
            GLYPH_UV[d][1] = (float) (index + 1) / GLYPH_COUNT;
        }
    }

    private final int color;
    private final int endColor;
    private int lastLife;
    private int life;
    private final int maxLife = 40;
    private final float damageAmount;
    private Vec3 lastPos;
    private Vec3 pos;
    private Vec3 velocity;
    private final float drag;
    private final boolean critical;
    private final float roll;

    /** 缓存：damageAmount 取整后的字符串 */
    private final String text;

    public DamageInfo(float damageAmount, Vec3 pos, Vec3 velocity, float drag, boolean critical, int color, int endColor, float roll) {
        this.color = color;
        this.endColor = endColor;
        this.lastLife = 0;
        this.life = 0;
        this.damageAmount = damageAmount;
        this.pos = pos;
        this.lastPos = pos;
        this.velocity = velocity;
        this.drag = drag;
        this.critical = critical;
        this.roll = roll;
        this.text = String.valueOf((int) damageAmount);
    }

    public void tick() {
        lastLife = life;
        life++;
        lastPos = pos;
        velocity = velocity.scale(drag);
        pos = pos.add(velocity);
    }

    // ===================== 渲染 =====================

    /** 获取伤害数字贴图 */
    public static ResourceLocation getTexture() {
        return TEXTURE;
    }

    /** 获取数字字符串（已缓存） */
    public String getText() {
        return text;
    }

    /** 渲染总宽度（含间距），考虑缩放 */
    public float getTotalWidth(float scale) {
        return text.length() * RENDER_SIZE * STEP_FACTOR * scale;
    }

    /**
     * 将本条伤害数字的顶点写入 VertexConsumer。
     * <p>
     * 调用方需已设置好 PoseStack（平移+旋转+居中偏移），
     * 此方法只负责逐字符写入 quad 顶点。
     * </p>
     */
    public void renderQuads(Matrix4f matrix, VertexConsumer consumer, float scale, float r, float g, float b, float a) {
        float size = RENDER_SIZE * scale;
        float step = size * STEP_FACTOR;
        float halfSize = size * 0.5f;

        for (int i = 0; i < text.length(); i++) {
            int digit = text.charAt(i) - '0';
            float u0 = GLYPH_UV[digit][0];
            float u1 = GLYPH_UV[digit][1];

            float x0 = i * step;
            float x1 = x0 + size;
            float y0 = -halfSize;
            float y1 = halfSize;

            consumer.addVertex(matrix, x0, y1, 0).setColor(r, g, b, a).setUv(u0, 1f).setOverlay(0).setLight(0xF000F0).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a).setUv(u1, 1f).setOverlay(0).setLight(0xF000F0).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x1, y0, 0).setColor(r, g, b, a).setUv(u1, 0f).setOverlay(0).setLight(0xF000F0).setNormal(0, 0, 1);
            consumer.addVertex(matrix, x0, y0, 0).setColor(r, g, b, a).setUv(u0, 0f).setOverlay(0).setLight(0xF000F0).setNormal(0, 0, 1);
        }
    }

    // ===================== 动画参数 =====================

    public int getRenderColor(float partialTick) {
        float progress = Mth.lerp(partialTick, lastLife, life) / maxLife;
        float flicker = (Mth.sin(progress * Mth.PI * 4f) + 1f) * 0.5f;
        float weight = flicker * (1f - progress);
        int r = Mth.lerpInt(weight, (endColor >> 16) & 0xFF, (color >> 16) & 0xFF);
        int g = Mth.lerpInt(weight, (endColor >> 8) & 0xFF, (color >> 8) & 0xFF);
        int b = Mth.lerpInt(weight, endColor & 0xFF, color & 0xFF);
        return (r << 16) | (g << 8) | b;
    }

    public float getRenderRoll(float partialTick) {
        float progress = Mth.lerp(partialTick, lastLife, life) / maxLife;
        return Mth.lerp(progress, roll, 0);
    }

    public float getRenderScale(float partialTick) {
        float progress = Mth.lerp(partialTick, lastLife, life) / maxLife;
        progress = EasingCurve.EASE_IN_OUT_QUAD.apply(progress);
        if (progress < 0.1) {
            return Mth.lerp(progress / 0.1f, 0.5f, 1.0f);
        }
        if (progress > 0.9f) {
            return Mth.lerp((progress - 0.9f) / 0.1f, 1.0f, 0.0f);
        }
        return 1;
    }

    public float getRenderAlpha(float partialTick) {
        float progress = Mth.lerp(partialTick, lastLife, life) / maxLife;
        progress = EasingCurve.EASE_IN_OUT_QUAD.apply(progress);
        if (progress < 0.2) {
            return Mth.lerp(progress / 0.2f, 0.2f, 1.0f);
        }
        if (progress > 0.9f) {
            return Mth.lerp((progress - 0.9f) / 0.1f, 1.0f, 0.0f);
        }
        return 1;
    }

    public int getColor() {
        return color;
    }

    public int getEndColor() {
        return endColor;
    }

    public Vec3 getRenderPos(float partialTick) {
        return lastPos.lerp(pos, partialTick);
    }

    public float getDamageAmount() {
        return damageAmount;
    }

    public boolean isRemove() {
        return life >= maxLife - 1;
    }

    public boolean isCritical() {
        return critical;
    }
}

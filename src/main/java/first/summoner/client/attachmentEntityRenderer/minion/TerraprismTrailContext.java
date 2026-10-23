package first.summoner.client.attachmentEntityRenderer.minion;

import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.RibbonTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.RenderKeyframe;
import first.summoner.common.minion.minion.Terraprism;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;

import java.util.*;

public class TerraprismTrailContext extends RibbonTrailContext<Terraprism> {

    private final Map<Terraprism, History> histories = new WeakHashMap<>();

    public TerraprismTrailContext() {
        upOffset = 1.015f;
        downOffset = -0.5f;
        historyLength = 3;
        colorFunction = Terraprism::getColor;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderContext<Terraprism> context) {
        if (!IrisCompat.isShadowPass() && context.color.getAlphaFloat() > 0f) {
            List<InterpolatedNode> nodes = buildSmoothNodes(context.entity, context.visualNode, context.partialTick);
            if (nodes.size() > 1) {
                List<TerraprismTrailPass.Vertex> vertices = new ArrayList<>((nodes.size() - 1) * 4);
                Vector3f tip = new Vector3f();
                Vector3f base = new Vector3f();
                Vector3f previousTip = new Vector3f();
                Vector3f previousBase = new Vector3f();
                for (int i = 0; i < nodes.size() - 1; i++) {
                    InterpolatedNode current = nodes.get(i);
                    InterpolatedNode previous = nodes.get(i + 1);
                    float progress = (float) i / (nodes.size() - 1);
                    float previousProgress = (float) (i + 1) / (nodes.size() - 1);
                    tip.set(0, 0, upOffset).rotate(current.rot());
                    base.set(0, 0, downOffset).rotate(current.rot());
                    previousTip.set(0, 0, upOffset).rotate(previous.rot());
                    previousBase.set(0, 0, downOffset).rotate(previous.rot());
                    int color = colorFunction.getColor(context.entity, progress, context.partialTick);
                    int previousColor = colorFunction.getColor(context.entity, previousProgress, context.partialTick);
                    float alpha = fadeOut.getFade(progress) * context.color.getAlphaFloat();
                    float previousAlpha = fadeOut.getFade(previousProgress) * context.color.getAlphaFloat();
                    int tipColor = packColor(color, alpha * 0.8f);
                    int baseColor = packColor(color, alpha * 0.08f);
                    int previousTipColor = packColor(previousColor, previousAlpha * 0.8f);
                    int previousBaseColor = packColor(previousColor, previousAlpha * 0.08f);
                    vertices.add(new TerraprismTrailPass.Vertex(current.pos().add(tip.x, tip.y, tip.z), tipColor, 0, 0));
                    vertices.add(new TerraprismTrailPass.Vertex(current.pos().add(base.x, base.y, base.z), baseColor, 1, 0));
                    vertices.add(new TerraprismTrailPass.Vertex(previous.pos().add(previousBase.x, previousBase.y, previousBase.z), previousBaseColor, 1, 1));
                    vertices.add(new TerraprismTrailPass.Vertex(previous.pos().add(previousTip.x, previousTip.y, previousTip.z), previousTipColor, 0, 1));
                }
                TerraprismTrailPass.submit(vertices);
            }
        }
    }

    @Override
    protected List<InterpolatedNode> buildSmoothNodes(Terraprism entity, PathNode visualNode, float partialTick) {
        History history = histories.computeIfAbsent(entity, key -> new History());
        int tick = entity.getTickCount();
        if (tick != history.serverTick) {
            if (tick < history.serverTick) {
                history.samples.clear();
            }
            List<RenderKeyframe> frames = entity.getRenderKeyframes();
            if (frames.isEmpty()) {
                List<PathNode> nodes = entity.getHistoryNodes();
                frames = List.of(new RenderKeyframe(0f, nodes.get(Math.min(1, nodes.size() - 1))), new RenderKeyframe(1f, entity.getCurrentPathNode()));
            }
            for (RenderKeyframe frame : frames) {
                double time = tick - 1.0 + frame.partialTick();
                if (!history.samples.isEmpty() && history.samples.getLast().time == time) {
                    history.samples.removeLast();
                }
                history.samples.addLast(new Sample(time, frame.node()));
            }
            while (!history.samples.isEmpty() && history.samples.getFirst().time < tick - historyLength - 1) {
                history.samples.removeFirst();
            }
            history.serverTick = tick;
            history.clientTick = entity.getOwner().tickCount;
        }
        double time = tick - 1.0 + Math.min(1f, entity.getOwner().tickCount - history.clientTick + partialTick);
        double tail = time - historyLength;
        List<InterpolatedNode> nodes = new ArrayList<>();
        nodes.add(new InterpolatedNode(visualNode.pos(), visualNode.toQuaternion()));
        Sample newer = new Sample(time, visualNode);
        double length = 0;
        var samples = history.samples.descendingIterator();
        while (samples.hasNext()) {
            Sample sample = samples.next();
            if (sample.time < time) {
                boolean end = sample.time < tail;
                PathNode node = end ? sample.node.lerp(newer.node, (float) ((tail - sample.time) / (newer.time - sample.time))) : sample.node;
                double distance = newer.node.pos().distanceTo(node.pos());
                if (length + distance > 12) {
                    node = newer.node.lerp(node, (float) ((12 - length) / distance));
                    end = true;
                }
                nodes.add(new InterpolatedNode(node.pos(), node.toQuaternion()));
                if (end) {
                    break;
                }
                length += distance;
                newer = sample;
            }
        }
        return nodes;
    }

    private static class History {
        private final ArrayDeque<Sample> samples = new ArrayDeque<>();
        private int serverTick = -1;
        private int clientTick;
    }

    private record Sample(double time, PathNode node) {
    }
}

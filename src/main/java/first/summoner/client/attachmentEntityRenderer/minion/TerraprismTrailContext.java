package first.summoner.client.attachmentEntityRenderer.minion;

import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import first.lyra.client.render.RenderContext;
import first.lyra.client.render.trail.RibbonTrailContext;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.RenderKeyframe;
import first.summoner.common.minion.minion.Terraprism;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

public class TerraprismTrailContext extends RibbonTrailContext<Terraprism> {

    private final Map<Terraprism, History> histories = new WeakHashMap<>();

    public TerraprismTrailContext() {
        upOffset = 0;
        downOffset = -1.515f;
        historyLength = 3;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, RenderContext<Terraprism> context) {
        if (!IrisCompat.isShadowPass() && context.color.getAlphaFloat() > 0f) {
            List<InterpolatedNode> nodes = buildSmoothNodes(context.entity, context.visualNode, context.partialTick);
            if (nodes.size() > 1) {
                double[] lengths = new double[nodes.size()];
                for (int i = 1; i < nodes.size(); i++) {
                    lengths[i] = lengths[i - 1] + nodes.get(i - 1).pos().distanceTo(nodes.get(i).pos());
                }
                double length = lengths[lengths.length - 1];
                List<TerraprismTrailPass.Vertex> vertices = new ArrayList<>((nodes.size() - 1) * 4);
                List<TerraprismTrailPass.Vertex> highlight = new ArrayList<>((nodes.size() - 1) * 4);
                int color = context.color.argbInt();
                float opacity = context.color.getAlphaFloat();
                Edge previous = edge(nodes.getFirst(), 0f, color, opacity);
                for (int i = 1; i < nodes.size(); i++) {
                    float progress = length > 1.0E-6 ? (float) (lengths[i] / length) : (float) i / (nodes.size() - 1);
                    Edge current = edge(nodes.get(i), progress, color, opacity);
                    vertices.add(previous.tip);
                    vertices.add(previous.base);
                    vertices.add(current.base);
                    vertices.add(current.tip);
                    highlight.add(previous.highlightTip);
                    highlight.add(previous.highlightBase);
                    highlight.add(current.highlightBase);
                    highlight.add(current.highlightTip);
                    previous = current;
                }
                TerraprismTrailPass.submit(vertices, highlight);
            }
        }
    }

    private Edge edge(InterpolatedNode node, float progress, int color, float opacity) {
        Vector3f direction = new Vector3f(0, 0, 1).rotate(node.rot());
        Vec3 axis = new Vec3(direction.x, direction.y, direction.z);
        Vec3 tip = node.pos().add(axis.scale(upOffset));
        float width = (upOffset - downOffset) * (1f - progress);
        float alpha = fadeOut.getFade(progress) * opacity;
        int bodyColor = packColor(color, alpha * 0.5f);
        int highlightColor = packColor(color, alpha * 0.35f);
        float margin = Math.min(0.008f, width * 0.15f);
        float height = Math.min(0.04f, width * 0.5f);
        return new Edge(
                new TerraprismTrailPass.Vertex(tip, bodyColor, 0, progress),
                new TerraprismTrailPass.Vertex(tip.subtract(axis.scale(width)), bodyColor, 1, progress),
                new TerraprismTrailPass.Vertex(tip.subtract(axis.scale(margin)), highlightColor, 0, progress),
                new TerraprismTrailPass.Vertex(tip.subtract(axis.scale(margin + height)), highlightColor, 1, progress));
    }

    private record Edge(TerraprismTrailPass.Vertex tip, TerraprismTrailPass.Vertex base,
                        TerraprismTrailPass.Vertex highlightTip, TerraprismTrailPass.Vertex highlightBase) {
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

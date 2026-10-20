package org.confluence.mod.client.summoner.renderer.minion;

import org.confluence.mod.Confluence;
import org.confluence.mod.client.summoner.AbstractAttachmentEntityGeoRenderer;
import org.confluence.mod.client.summoner.ModelContext;
import org.confluence.mod.client.summoner.RenderContext;
import org.confluence.mod.client.summoner.trail.ConeTrailContext;
import org.confluence.mod.common.summoner.attachmentEntity.PathNode;
import org.confluence.mod.common.summoner.minion.Twins;

public class TwinsRenderer extends AbstractAttachmentEntityGeoRenderer<Twins> {

    public TwinsRenderer() {
        super(twins -> Confluence.asResource(twins.isLaserEye() ? "entity/boss/retinazer" : "entity/boss/spazmatism"));
    }

    @Override
    protected RenderContext<Twins> createContext(Twins twins, PathNode visualNode, float partialTick, int packedLight) {
        int trailTimer = twins.getTrailTimer();
        return new RenderContext<>(twins, visualNode, partialTick, packedLight)
                .trail(new ConeTrailContext<Twins>()
                        .maxRadius(0.2F)
                        .minRadiusRatio(0.75F)
                        .resolution(4)
                        .timer(trailTimer)
                        .colorRGB(twins.isLaserEye() ? 0xFF3030 : 0x48FF5A)
                        .historyLength(trailTimer / 2)
                        .fadeOut(progress -> (1 - progress) * (1 - progress)))
                .model(new ModelContext()
                        .scale(0.35F)
                        .alphaDistanceFactor(1.5F));
    }
}

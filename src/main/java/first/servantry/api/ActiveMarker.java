package first.servantry.api;

import first.servantry.api.register.MarkerType;
import first.servantry.api.register.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ActiveMarker {
    private final MarkerType type;
    private int remainingTicks;
    private boolean hited;

    public ActiveMarker(MarkerType type, int durationTicks) {
        this.type = type;
        this.remainingTicks = durationTicks;
        this.hited = false;
    }

    public void tick() {
        if (this.remainingTicks > 0) this.remainingTicks--;
    }

    public boolean isExpired() { return this.remainingTicks <= 0; }

    public MarkerType getType() { return type; }
    public boolean isHited() { return hited; }
    public void setHited(boolean hited) { this.hited = hited; }
    public int getRemainingTicks() { return remainingTicks; }

    public void write(RegistryFriendlyByteBuf buf) {
        ResourceLocation location = Registries.MARKER_TYPES.getKey(this.type);
        assert location != null;
        buf.writeResourceLocation(location);
        buf.writeInt(this.remainingTicks);
        buf.writeBoolean(this.hited);
    }

    public static ActiveMarker read(RegistryFriendlyByteBuf buf) {
        ActiveMarker marker = new ActiveMarker(Registries.MARKER_TYPES.get(buf.readResourceLocation()), buf.readInt());
        marker.setHited(buf.readBoolean());
        return marker;
    }

}
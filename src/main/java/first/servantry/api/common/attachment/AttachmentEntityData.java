package first.servantry.api.common.attachment;

import first.servantry.api.core.AttachmentEntityDispatcher;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class AttachmentEntityData implements AttachmentSyncHandler<AttachmentEntityData> {

    public final AttachmentEntityDispatcher dispatcher = AttachmentEntityDispatcher.getInstance();

    @Override
    public void write(RegistryFriendlyByteBuf buf, AttachmentEntityData data, boolean b) {

    }

    @Override
    public @Nullable AttachmentEntityData read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable AttachmentEntityData data) {
        return null;
    }

}

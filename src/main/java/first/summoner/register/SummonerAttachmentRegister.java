package first.summoner.register;

import first.summoner.Summoner;
import first.summoner.common.attachment.CurioData;
import first.summoner.common.attachment.GildedWandData;
import first.summoner.common.attachment.SummonMarkData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashSet;

public class SummonerAttachmentRegister {

    private static final DeferredRegister<AttachmentType<?>> Register = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Summoner.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CurioData>> CurioData =
            Register.register("curio_data", () -> AttachmentType.builder(() -> new CurioData(new HashSet<>()))
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SummonMarkData>> SummonMarkData =
            Register.register("summon_mark_data", () -> AttachmentType.builder(SummonMarkData::new)
                    .sync(new SummonMarkData())
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GildedWandData>> GildedWandData =
            Register.register("gilded_wand_data", () -> AttachmentType.builder(GildedWandData::new)
                    .build());

    public static void register(IEventBus eventBus) {
        Register.register(eventBus);
    }
}

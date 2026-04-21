package first.servantry.api.event;

import first.servantry.api.client.ServantRenderDispatcher;
import first.servantry.api.client.ServantRenderer;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import net.neoforged.bus.api.Event;

public class ServantRenderRegisterEvent extends Event {

    public ServantRenderRegisterEvent() {
    }

    public <T extends Servant> void register(ServantType<T> type, ServantRenderer<T> renderer) {
        ServantRenderDispatcher.register(type, renderer);
    }

}

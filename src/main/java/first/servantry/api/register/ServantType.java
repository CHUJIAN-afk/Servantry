package first.servantry.api.register;

import first.servantry.api.servant.Servant;

import java.util.function.Supplier;

public record ServantType<T extends Servant>(Supplier<T> factory) {

}

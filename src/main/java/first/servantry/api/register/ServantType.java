package first.servantry.api.register;

import first.servantry.api.servant.PathNode;
import first.servantry.api.servant.Servant;

import java.util.function.Function;

public record ServantType<T extends Servant>(Function<PathNode, T> factory) {

}

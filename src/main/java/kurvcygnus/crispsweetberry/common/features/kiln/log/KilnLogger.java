package kurvcygnus.crispsweetberry.common.features.kiln.log;

import org.jetbrains.annotations.NotNull;

public final class KilnLogger
{
    public static @NotNull String getPrefixTemplate(@NotNull String method, @NotNull String behavior) { return "[Kiln$BE#" + method + "][" + behavior + "]"; }
}

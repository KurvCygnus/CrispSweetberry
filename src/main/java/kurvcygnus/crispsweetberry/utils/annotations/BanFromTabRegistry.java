package kurvcygnus.crispsweetberry.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation to make it exclusive of auto registry.<br>
 * <b><i>It only works on items.</i></b>
 * @implSpec Usage Example:<pre>{@code
 *     @BanFromTabRegistry
 *     public static final Holder<Item> Bar = ...
 * }</pre>
 * @see kurvcygnus.crispsweetberry.client.init.CrispCreativeTabsRegistryEvent#tabRegistryEvent Usage
 * @author Kurv
 * @since CSB Release 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BanFromTabRegistry {}

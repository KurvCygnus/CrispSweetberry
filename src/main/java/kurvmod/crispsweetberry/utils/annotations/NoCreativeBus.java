package kurvmod.crispsweetberry.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * IDK what 2 say about this, honestly.
 * @see kurvmod.crispsweetberry.events.init.CrispCreativeTabsRegistryEvent
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoCreativeBus {}

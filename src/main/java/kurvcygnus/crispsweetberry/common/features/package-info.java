/**
 * This package contains large, self-contained gameplay features.
 * A feature usually consists of:<ul>
 * <li>Blocks / Items</li>
 * <li>BlockEntities</li>
 * <li>Menus / Screens</li>
 * <li>Recipes and internal logic</li>
 * </ul>
 * <b>Features should be internally cohesive and should NOT directly depend on
 * the internal implementation of other features</b>.
 * @implNote DO NOT add {@link javax.annotation.ParametersAreNonnullByDefault @ParametersAreNonnullByDefault} in package-infos,
 * this will lead to potential issues, errors, and footguns.
 */
package kurvcygnus.crispsweetberry.common.features;
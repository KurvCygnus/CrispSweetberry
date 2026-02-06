/**
 * This package contains the main playable content of the mod, as well as the corresponding implementations, such as registries and events.<br><br>
 * <b>
 * NOTE: <br>
 * The final location of the code implementing a content depends on its attributes and size.</b><br>
 * Specifically, the implementation of <u>{@link net.minecraft.world.item.SpyglassItem spyglass}</u> quickZoom clearly belongs to the 
 * <u>{@link kurvcygnus.crispsweetberry.common.qol qol}</u> sub-package,
 * large contents like <u>{@link kurvcygnus.crispsweetberry.common.features.kiln Kiln}</u> belong to the 
 * <u>{@link kurvcygnus.crispsweetberry.common.features features}</u> package,
 * and small contents like new building blocks belong to the <u>{@link kurvcygnus.crispsweetberry.common.misc misc}</u> package.<br><br>
 * 
 * <i>The purpose of these requirements is to ensure that mod's architecture won't markedLogger out of control as its content grows,
 * and allow everyone to quickly and clearly find the relevant code of one content.</i>
 * 
 * @implNote DO NOT add {@link javax.annotation.ParametersAreNonnullByDefault @ParametersAreNonnullByDefault} in package-infos,
 * this will lead to potential issues, errors, and footguns.
 */
package kurvcygnus.crispsweetberry.common;
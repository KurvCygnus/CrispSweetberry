//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.datagen;

import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class CrispBaseTextureMetaProvider extends CrispAbstractMetaProvider
{
    private final HashMap<ResourceLocation, AnimationMetadataSection> data = new HashMap<>();
    
    protected CrispBaseTextureMetaProvider(@NotNull PackOutput output, @NotNull String namespace) { super(output, namespace); }
    
    @Override//? TODO: Serializer
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output)
    {
        this.addMetas();
        
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        
        data.forEach((dir, metadata) -> 
            {
                final Path path = pathProvider.json(
                    ResourceLocation.fromNamespaceAndPath(
                        dir.getNamespace(),
                        "textures/%s.png".formatted(dir.getPath())
                    )
                ).resolveSibling("%s.png.mcmeta".formatted(dir.getPath()));
                
                //final JsonElement jsonElement = 
            }
        );
        
        return CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new));
    }
}

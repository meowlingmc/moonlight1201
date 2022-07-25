package net.mehvahdjukaar.moonlight.api.platform.fabric;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.client.model.fabric.FabricModelLoaderRegistry;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.core.misc.fabric.ITextureAtlasSpriteExtension;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.ModelManagerAccessor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientPlatformHelperImpl {

    public static void registerRenderType(Block block, RenderType type) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, type);
    }

    public static void addParticleRegistration(Consumer<ClientPlatformHelper.ParticleEvent> eventListener) {
        eventListener.accept(ClientPlatformHelperImpl::registerParticle);
    }

    private static <P extends ParticleType<T>, T extends ParticleOptions> void registerParticle(P type, ClientPlatformHelper.ParticleFactory<T> registration) {
        ParticleFactoryRegistry.getInstance().register(type, registration::create);
    }

    public static void addEntityRenderersRegistration(Consumer<ClientPlatformHelper.EntityRendererEvent> eventListener) {
        eventListener.accept(EntityRendererRegistry::register);
    }

    public static void addBlockEntityRenderersRegistration(Consumer<ClientPlatformHelper.BlockEntityRendererEvent> eventListener) {
        eventListener.accept(BlockEntityRendererRegistry::register);
    }

    public static void addBlockColorsRegistration(Consumer<ClientPlatformHelper.BlockColorEvent> eventListener) {
        eventListener.accept(ColorProviderRegistry.BLOCK::register);
    }

    public static void addItemColorsRegistration(Consumer<ClientPlatformHelper.ItemColorEvent> eventListener) {
        eventListener.accept(ColorProviderRegistry.ITEM::register);
    }

    public static void addAtlasTextureCallback(ResourceLocation atlasLocation, Consumer<ClientPlatformHelper.AtlasTextureEvent> eventListener) {
        ClientSpriteRegistryCallback.event(atlasLocation).register(((atlasTexture, registry) -> {
            eventListener.accept(registry::register);
        }));
    }

    public static void registerReloadListener(PreparableReloadListener listener, ResourceLocation name) {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return name;
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return listener.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }
        });
    }

    public static void addModelLayerRegistration(Consumer<ClientPlatformHelper.ModelLayerEvent> eventListener) {
        eventListener.accept((a, b) -> EntityModelLayerRegistry.registerModelLayer(a, b::get));
    }

    public static void addSpecialModelRegistration(Consumer<ClientPlatformHelper.SpecialModelEvent> eventListener) {
        eventListener.accept(r -> {
            ModelLoadingRegistryImpl.INSTANCE.registerModelProvider((m, loader) -> loader.accept(r));
        });
    }

    public static void addTooltipComponentRegistration(Consumer<ClientPlatformHelper.TooltipComponentEvent> eventListener) {
        eventListener.accept(ClientPlatformHelperImpl::tooltipReg);
    }

    private static <T extends TooltipComponent> void tooltipReg(Class<T> tClass, Function<? super T, ? extends ClientTooltipComponent> factory) {
        TooltipComponentCallback.EVENT.register(data -> tClass.isAssignableFrom(data.getClass()) ? factory.apply((T) data) : null);
    }


    public static void addModelLoaderRegistration(Consumer<ClientPlatformHelper.ModelLoaderEvent> eventListener) {
        eventListener.accept(FabricModelLoaderRegistry::registerLoader);
    }


    public static int getPixelRGBA(TextureAtlasSprite sprite, int frameIndex, int x, int y) {
        return ((ITextureAtlasSpriteExtension) sprite).getPixelRGBA(frameIndex, x, y);
    }

    public static BakedModel getModel(ModelManager modelManager, ResourceLocation modelLocation) {
        return ((ModelManagerAccessor) modelManager).getBakedRegistry().getOrDefault(modelLocation, modelManager.getMissingModel());
    }


    public static void renderBlock(long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state, Level level, BlockPos pos, BlockRenderDispatcher blockRenderer) {
        blockRenderer.getModelRenderer().tesselateBlock(level, blockRenderer.getBlockModel(state), state, pos, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)),
                false, RandomSource.create(), seed, OverlayTexture.NO_OVERLAY);
    }

    public static Path getModIcon(String modId) {
        var container = FabricLoader.getInstance().getModContainer(modId).get();
        return container.getMetadata().getIconPath(512).flatMap(container::findPath).orElse(null);
    }

    public static BlockModel parseBlockModel(JsonElement json) {
        return BlockModel.fromString(json.toString()); //sub optimal... too bad
    }
}
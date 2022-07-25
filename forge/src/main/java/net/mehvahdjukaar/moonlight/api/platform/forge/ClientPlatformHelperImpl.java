package net.mehvahdjukaar.moonlight.api.platform.forge;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
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
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ClientPlatformHelperImpl {

    public static void registerRenderType(Block block, RenderType type) {
        //from 0.64 we should register render types in out model jsons
        //TODO: remove
        ItemBlockRenderTypes.setRenderLayer(block, type);
    }

    public static void addParticleRegistration(Consumer<ClientPlatformHelper.ParticleEvent> eventListener) {
        Consumer<RegisterParticleProvidersEvent> eventConsumer = event -> {
            W w = new W(event);
            eventListener.accept(w::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    private record W(RegisterParticleProvidersEvent event) {
        public <T extends ParticleOptions> void register(ParticleType<T> type, ClientPlatformHelper.ParticleFactory<T> provider) {
            this.event.register(type, provider::create);
        }
    }

    public static void addEntityRenderersRegistration(Consumer<ClientPlatformHelper.EntityRendererEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event ->
                eventListener.accept(event::registerEntityRenderer);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addBlockEntityRenderersRegistration(Consumer<ClientPlatformHelper.BlockEntityRendererEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event ->
                eventListener.accept(event::registerBlockEntityRenderer);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addBlockColorsRegistration(Consumer<ClientPlatformHelper.BlockColorEvent> eventListener) {
        Consumer<RegisterColorHandlersEvent.Block> eventConsumer = event -> eventListener.accept(event::register);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addItemColorsRegistration(Consumer<ClientPlatformHelper.ItemColorEvent> eventListener) {
        Consumer<RegisterColorHandlersEvent.Item> eventConsumer = event -> eventListener.accept(event::register);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addAtlasTextureCallback(ResourceLocation atlasLocation, Consumer<ClientPlatformHelper.AtlasTextureEvent> eventListener) {
        Consumer<TextureStitchEvent.Pre> eventConsumer = event -> {
            if (event.getAtlas().location() == atlasLocation) {
                eventListener.accept(event::addSprite);
            }
        };
    }

    public static void registerReloadListener(PreparableReloadListener listener, ResourceLocation location) {
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager())
                .registerReloadListener(listener);
    }

    public static void addModelLayerRegistration(Consumer<ClientPlatformHelper.ModelLayerEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterLayerDefinitions> eventConsumer = event -> {
            eventListener.accept(event::registerLayerDefinition);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addSpecialModelRegistration(Consumer<ClientPlatformHelper.SpecialModelEvent> eventListener) {
        Consumer<ModelEvent.RegisterAdditional> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addTooltipComponentRegistration(Consumer<ClientPlatformHelper.TooltipComponentEvent> eventListener) {
        Consumer<RegisterClientTooltipComponentFactoriesEvent> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addModelLoaderRegistration(Consumer<ClientPlatformHelper.ModelLoaderEvent> eventListener) {
        Consumer<ModelEvent.RegisterGeometryLoaders> eventConsumer = event -> {
            eventListener.accept((i, l) -> event.register(i.getPath(), (IGeometryLoader<?>) l));
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }


    public static int getPixelRGBA(TextureAtlasSprite sprite, int frameIndex, int x, int y) {
        return sprite.getPixelRGBA(frameIndex, x, y);
    }

    public static BakedModel getModel(ModelManager modelManager, ResourceLocation modelLocation) {
        return modelManager.getModel(modelLocation);
    }

    public static void renderBlock(long seed, PoseStack matrixStack, MultiBufferSource buffer, BlockState blockstate,
                                   Level level, BlockPos blockpos, BlockRenderDispatcher dispatcher) {

        BakedModel model = dispatcher.getBlockModel(blockstate);
        for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(seed), ModelData.EMPTY)) {
            dispatcher.getModelRenderer().tesselateBlock(level, model, blockstate, blockpos, matrixStack, buffer.getBuffer(renderType), false, RandomSource.create(), seed,
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
        }
    }

    @Nullable
    public static Path getModIcon(String modId) {
        var m = ModList.get().getModContainerById(modId);
        if (m.isPresent()) {
            IModInfo mod = m.get().getModInfo();
            IModFile file = mod.getOwningFile().getFile();

            var logo = mod.getLogoFile().orElse(null);
            if (logo != null && file != null) {
                Path logoPath = file.findResource(logo);
                if (Files.exists(logoPath)) {
                    return logoPath;
                }
            }
        }
        return null;
    }

    public static BlockModel parseBlockModel(JsonElement json) {
        return ExtendedBlockModelDeserializer.INSTANCE.getAdapter(BlockModel.class).fromJsonTree(json);
    }


}
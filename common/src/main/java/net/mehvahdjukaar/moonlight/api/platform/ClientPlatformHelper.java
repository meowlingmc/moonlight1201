package net.mehvahdjukaar.moonlight.api.platform;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientPlatformHelper {

    @ExpectPlatform
    public static void registerRenderType(Block block, RenderType type) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerReloadListener(PreparableReloadListener listener, ResourceLocation location) {
        throw new AssertionError();
    }

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface ParticleFactory<T extends ParticleOptions> {
        @NotNull ParticleProvider<T> create(SpriteSet spriteSet);
    }

    @FunctionalInterface
    public interface ParticleEvent {
        <P extends ParticleType<T>, T extends ParticleOptions> void register(P particleType, ParticleFactory<T> factory);
    }

    @ExpectPlatform
    public static void addParticleRegistration(Consumer<ParticleEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface EntityRendererEvent {
        <E extends Entity> void register(EntityType<? extends E> entity, EntityRendererProvider<E> renderer);
    }

    @ExpectPlatform
    public static void addEntityRenderersRegistration(Consumer<EntityRendererEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockEntityRendererEvent {
        <E extends BlockEntity> void register(BlockEntityType<? extends E> blockEntity, BlockEntityRendererProvider<E> renderer);
    }

    @ExpectPlatform
    public static void addBlockEntityRenderersRegistration(Consumer<BlockEntityRendererEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockColorEvent {
        void register(BlockColor color, Block... block);
    }

    @ExpectPlatform
    public static void addBlockColorsRegistration(Consumer<BlockColorEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ItemColorEvent {
        void register(ItemColor color, ItemLike... block);
    }

    @ExpectPlatform
    public static void addItemColorsRegistration(Consumer<ItemColorEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ModelLayerEvent {
        void register(ModelLayerLocation modelLayer, Supplier<LayerDefinition> provider);
    }

    @ExpectPlatform
    public static void addModelLayerRegistration(Consumer<ModelLayerEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface SpecialModelEvent {
        void register(ResourceLocation modelLocation);
    }

    @ExpectPlatform
    public static void addSpecialModelRegistration(Consumer<SpecialModelEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ModelLoaderEvent {
        void register(ResourceLocation id, CustomModelLoader loader);
    }

    @ExpectPlatform
    public static void addModelLoaderRegistration(Consumer<ModelLoaderEvent> eventListener) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BakedModel getModel(ModelManager modelManager, ResourceLocation modelLocation){
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface TooltipComponentEvent{
        <T extends TooltipComponent> void register(Class<T> type, Function<? super T, ? extends ClientTooltipComponent> factory);
    }

    @ExpectPlatform
    public static void addTooltipComponentRegistration(Consumer<TooltipComponentEvent> eventListener){
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface AtlasTextureEvent {
        void addSprite(ResourceLocation spriteLocation);
    }

    @ExpectPlatform
    public static void addAtlasTextureCallback(ResourceLocation atlasLocation, Consumer<AtlasTextureEvent> eventListener) {
        throw new AssertionError();
    }



    @ExpectPlatform
    public static void renderBlock(long seed, PoseStack matrixStack, MultiBufferSource buffer, BlockState blockstate, Level world, BlockPos blockpos, BlockRenderDispatcher blockRenderer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getPixelRGBA(TextureAtlasSprite sprite, int frameIndex, int x, int y) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockModel parseBlockModel(JsonElement json){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getModIcon(String modId) {
        throw new AssertionError();
    }

}
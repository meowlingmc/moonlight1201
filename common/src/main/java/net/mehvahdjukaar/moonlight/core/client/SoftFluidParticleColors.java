package net.mehvahdjukaar.moonlight.core.client;

import net.mehvahdjukaar.moonlight.api.client.TextureCache;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.GenericSimpleResourceReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.List;

public class SoftFluidParticleColors extends GenericSimpleResourceReloadListener {

    private static final HashMap<ResourceLocation, Integer> PARTICLE_COLORS = new HashMap<>();

    public SoftFluidParticleColors() {
        super("textures/soft_fluids", ".png"); //unused, just need for color reload
    }

    //adds all textures in this folder
    @Override
    public void apply(List<ResourceLocation> locations, ResourceManager manager, ProfilerFiller filler) {
        PARTICLE_COLORS.clear();

        //also using this to reset texture cache
        RenderedTexturesManager.clearCache();

        //also using for this
        TextureCache.clear();
    }

    public static int getParticleColor(Holder<SoftFluid> s) {
        if(PARTICLE_COLORS.isEmpty()){
            refreshParticleColors();
        }
        return PARTICLE_COLORS.getOrDefault(s.unwrapKey().get().location(), -1);
    }

    //TODO: possibly do it for ALL fluids, not only non grayscale ones
    private static void refreshParticleColors() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var fluids = SoftFluidRegistry.getRegistry(mc.level.registryAccess()).entrySet();

        for (var entry : fluids) {
            SoftFluid s = entry.getValue();
            ResourceLocation key = entry.getKey().location();
            if (!PARTICLE_COLORS.containsKey(key) && !s.isColored()) {
                ResourceLocation location = s.getStillTexture();
                if (location == null) continue;
                TextureAtlas textureMap = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
                TextureAtlasSprite sprite = textureMap.getSprite(location);
                int averageColor = -1;
                try {
                    averageColor = getColorFrom(sprite, s.getTintColor());
                } catch (Exception e) {
                    Moonlight.LOGGER.warn("Failed to load particle color for " + sprite + " using current resource pack. might be a broken png.mcmeta");
                }
                PARTICLE_COLORS.put(key, averageColor);
            }
        }
    }

    //credits to Random832
    @SuppressWarnings("ConstantConditions")
    private static int getColorFrom(TextureAtlasSprite sprite, int tint) {
        var c= sprite.contents();
        if (sprite == null || c.getFrameCount() == 0) return -1;

        int tintR = tint >> 16 & 255;
        int tintG = tint >> 8 & 255;
        int tintB = tint & 255;
        int total = 0, totalR = 0, totalB = 0, totalG = 0;

        for (int tryFrame = 0; tryFrame < c.getFrameCount(); tryFrame++) {
            try {
                for (int x = 0; x < c.width(); x++) {
                    for (int y = 0; y < c.height(); y++) {

                        int pixel = ClientHelper.getPixelRGBA(sprite, tryFrame, x, y);

                        // this is in 0xAABBGGRR format, not the usual 0xAARRGGBB.
                        int pixelB = pixel >> 16 & 255;
                        int pixelG = pixel >> 8 & 255;
                        int pixelR = pixel & 255;
                        ++total;
                        totalR += pixelR;
                        totalG += pixelG;
                        totalB += pixelB;
                    }
                }
                break;
            } catch (Exception e) {
                total = 0;
                totalR = 0;
                totalB = 0;
                totalG = 0;
            }
        }
        if (total <= 0) return -1;
        return FastColor.ARGB32.color(255,
                totalR / total * tintR / 255,
                totalG / total * tintG / 255,
                totalB / total * tintB / 255);
    }


}

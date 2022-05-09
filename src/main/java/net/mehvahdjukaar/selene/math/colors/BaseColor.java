package net.mehvahdjukaar.selene.math.colors;

import net.minecraft.util.Mth;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class BaseColor<T extends BaseColor<T>> {

    protected final float v0;
    protected final float v1;
    protected final float v2;
    protected final float v3;

    protected BaseColor(float v0, float v1, float v2, float v3) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public float distTo(T other) {
        return (float) Math.sqrt((this.v0 - other.v0) * (this.v0 - other.v0) +
                (this.v1 - other.v1) * (this.v1 - other.v1) +
                (this.v2 - other.v2) * (this.v2 - other.v2));
    }

    public T mixWith(T color) {
        return mixWith(color,0.5f);
    }

    public T mixWith(T color, float bias) {
        return color;
    }

    public abstract RGBColor asRGB();

    public HSLColor asHSL() {
        return this instanceof HSLColor c ? c : ColorSpaces.RGBtoHSL(this.asRGB());
    }

    public HSVColor asHSV() {
        return this instanceof HSVColor c ? c : ColorSpaces.RGBtoHSV(this.asRGB());
    }

    public XYZColor asXYZ() {
        return this instanceof XYZColor c ? c : ColorSpaces.RGBtoXYZ(this.asRGB());
    }

    public LABColor asLAB() {
        return this instanceof LABColor c ? c : ColorSpaces.XYZtoLAB(this.asXYZ());
    }

    public HCLColor asHCL() {
        return this instanceof HCLColor c ? c : ColorSpaces.LABtoHCL(this.asLAB());
    }

    public LUVColor asLUV() {
        return this instanceof LUVColor c ? c : ColorSpaces.XYZtoLUV(this.asXYZ());
    }

    public HCLVColor asHCLV() {
        return this instanceof HCLVColor c ? c : ColorSpaces.LUVtoHCLV(this.asLUV());
    }


    public static float weightedAverageAngles(float a, float b, float bias) {
        return Mth.rotLerp(bias, a * 360, b * 360) / 360f;
    }

    protected static float averageAngles(Float... angles) {
        float x = 0, y = 0;
        for (float a : angles) {
            assert a >= 0 && a <= 1;
            x += Math.cos((float) (a * Math.PI * 2));
            y += Math.sin((float) (a * Math.PI * 2));
        }
        double a = (Math.atan2(y, x) / (Math.PI * 2));
        return (float) a;
    }

    public abstract T fromRGB(RGBColor rgb);

}
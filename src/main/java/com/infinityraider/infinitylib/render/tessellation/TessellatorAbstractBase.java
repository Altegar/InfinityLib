package com.infinityraider.infinitylib.render.tessellation;

import com.infinityraider.infinitylib.reference.Constants;
import com.infinityraider.infinitylib.utility.math.TransformationMatrix;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public abstract class TessellatorAbstractBase implements ITessellator {
    /** Default color (white) */
    public static final int STANDARD_COLOR = 255;
    /** Default brightness (max) */
    public static final int STANDARD_BRIGHTNESS = 15 << 24;
    /** Default normal (up) */
    public static final Vec3f STANDARD_NORMAL = new Vec3f(0, 1, 0);

    /** Current transformation matrix */
    private final Deque<TransformationMatrix> matrices;

    /** Current vertex format */
    private VertexFormat format;
    /** Current normal */
    private Vec3f normal;
    /** Current color*/
    private int r, g, b, a;
    /** Current brightness value */
    private int l;
    /** Current tint index for the quad */
    private int tintIndex;
    /** Current diffuse lighting setting for the quad */
    private boolean applyDiffuseLighting;

    protected TessellatorAbstractBase() {
        this.matrices = new ArrayDeque<>();
        this.normal = STANDARD_NORMAL;
        this.tintIndex = -1;
        this.applyDiffuseLighting = false;
        this.resetMatrix();
    }
    /**
     * Method to start constructing quads
     * @param vertexFormat vertex format
     */
    @Override
    public final void startDrawingQuads(VertexFormat vertexFormat) {
        this.format = vertexFormat;
        this.onStartDrawingQuadsCall();
    }

    /**
     * Sub delegated method call of the startDrawingQuads() method to ensure correct call chain
     */
    protected abstract void onStartDrawingQuadsCall();

    /**
     * Method to finalize drawing
     */
    @Override
    public final void draw() {
        this.onDrawCall();
        this.format = null;
        this.normal = STANDARD_NORMAL;
        this.setColorRGBA(STANDARD_COLOR, STANDARD_COLOR, STANDARD_COLOR, STANDARD_COLOR);
        this.setBrightness(STANDARD_BRIGHTNESS);
        this.tintIndex = -1;
        this.applyDiffuseLighting = false;
        this.resetMatrix();
    }

    /**
     * Sub delegated method call of the draw() method to ensure correct call chain
     */
    protected abstract void onDrawCall();

    /**
     * Gets the current vertex format the tessellator is drawing with
     * @return the vertex format
     */
    @Override
    public final VertexFormat getVertexFormat() {
        return format;
    }

    @Override
    public void pushMatrix() {
        this.matrices.push(new TransformationMatrix(this.matrices.getFirst()));
    }

    @Override
    public void popMatrix() {
        if (this.matrices.size() > 1) {
            this.matrices.pop();
        } else {
            throw new IndexOutOfBoundsException("No matrix to pop!");
        }
    }

    /**
     * Adds a vertex
     * @param x the x-coordinate for the vertex
     * @param y the y-coordinate for the vertex
     * @param z the z-coordinate for the vertex
     * @param icon the icon
     * @param u u value for the vertex
     * @param v v value for the vertex
     */
    @Override
    public void addVertexWithUV(float x, float y, float z, TextureAtlasSprite icon, float u, float v) {
        if(icon == null) {
            icon = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }
        this.addVertexWithUV(x, y, z, icon.getInterpolatedU(u), icon.getInterpolatedV(v));
    }

    /**
     * Adds a vertex scaled by 1/16th of a block
     * @param x the x-coordinate for the vertex
     * @param y the y-coordinate for the vertex
     * @param z the z-coordinate for the vertex
     * @param icon the icon
     * @param u u value for the vertex
     * @param v v value for the vertex
     */
    @Override
    public void addScaledVertexWithUV(float x, float y, float z, TextureAtlasSprite icon, float u, float v) {
        addVertexWithUV(x * Constants.UNIT, y * Constants.UNIT, z * Constants.UNIT, icon, u, v);
    }

    /**
     * Adds a quad for a scaled face, the face is defined by minimum and maximum 2D coordinates
     * @param minX minimum 2D x-coordinate of the face
     * @param minY minimum 2D y-coordinate of the face
     * @param maxX maximum 2D x-coordinate of the face
     * @param maxY maximum 2D y-coordinate of the face
     * @param face orientation of the face
     * @param icon icon to render the face with
     * @param offset offset of the face along its normal
     */
    @Override
    public void drawScaledFace(float minX, float minY, float maxX, float maxY, EnumFacing face, TextureAtlasSprite icon, float offset) {
        float x1, x2, x3, x4;
        float y1, y2, y3, y4;
        float z1, z2, z3, z4;
        float u1, u2, u3, u4;
        float v1, v2, v3, v4;
        final int uv = 17;
        switch (face) {
            case UP: {
                x1 = x4 = maxX;
                x2 = x3 = minX;
                z1 = z2 = minY;
                z3 = z4 = maxY;
                y1 = y2 = y3 = y4 = offset;
                u2 = u3 = (minX % uv);
                u1 = u4 = (maxX % uv);
                v3 = v4 = maxY % uv;
                v1 = v2 = minY % uv;
                break;
            }
            case DOWN: {
                x1 = x2 = maxX;
                x3 = x4 = minX;
                z1 = z4 = minY;
                z2 = z3 = maxY;
                y1 = y2 = y3 = y4 = offset;
                u1 = u2 = maxX % uv;
                u3 = u4 = minX % uv;
                v1 = v4 = 16 - (minY % uv);
                v2 = v3 = 16 - (maxY % uv);
                break;
            }
            case WEST: {
                z1 = z2 = maxX;
                z3 = z4 = minX;
                y1 = y4 = minY;
                y2 = y3 = maxY;
                x1 = x2 = x3 = x4 = offset;
                u1 = u2 = maxX % uv;
                u3 = u4 = minX % uv;
                v1 = v4 = (16 - minY % uv);
                v2 = v3 = (16 - maxY % uv);
                break;
            }
            case EAST: {
                z1 = z2 = minX;
                z3 = z4 = maxX;
                y1 = y4 = minY;
                y2 = y3 = maxY;
                x1 = x2 = x3 = x4 = offset;
                u1 = u2 = (16 - minX % uv);
                u3 = u4 = (16 - maxX % uv);
                v1 = v4 = (16 - minY % uv);
                v2 = v3 = (16 - maxY % uv);
                break;
            }
            case NORTH: {
                x1 = x2 = maxX;
                x3 = x4 = minX;
                y1 = y4 = maxY;
                y2 = y3 = minY;
                z1 = z2 = z3 = z4 = offset;
                u1 = u2 = (16 - maxX % uv);
                u3 = u4 = (16 - minX % uv);
                v1 = v4 = (16 - maxY % uv);
                v2 = v3 = (16 - minY % uv);
                break;
            }
            case SOUTH: {
                x1 = x2 = maxX;
                x3 = x4 = minX;
                y1 = y4 = minY;
                y2 = y3 = maxY;
                z1 = z2 = z3 = z4 = offset;
                u1 = u2 = maxX % uv;
                u3 = u4 = minX % uv;
                v1 = v4 = (16 - minY % uv);
                v2 = v3 = (16 - maxY % uv);
                break;
            }
            default:
                return;
        }
        this.applyColorMultiplier(face);
        this.setNormal(new Vec3f(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ()));
        addScaledVertexWithUV(x1, y1, z1, icon, u1, v1);
        addScaledVertexWithUV(x2, y2, z2, icon, u2, v2);
        addScaledVertexWithUV(x3, y3, z3, icon, u3, v3);
        addScaledVertexWithUV(x4, y4, z4, icon, u4, v4);
    }

    /**
     * Adds two quads for a scaled face, this face will have both sides drawn.
     * The face is defined by minimum and maximum coordinates
     * @param minX minimum 2D x-coordinate of the face
     * @param minY minimum 2D y-coordinate of the face
     * @param maxX maximum 2D x-coordinate of the face
     * @param maxY maximum 2D y-coordinate of the face
     * @param face orientation of the face
     * @param icon icon to render the face with
     * @param offset offset of the face along its normal
     */
    @Override
    public void drawScaledFaceDouble(float minX, float minY, float maxX, float maxY, EnumFacing face, TextureAtlasSprite icon, float offset) {
        EnumFacing opposite;
        switch(face) {
            case NORTH: opposite = EnumFacing.SOUTH; break;
            case SOUTH: opposite = EnumFacing.NORTH; break;
            case EAST: opposite = EnumFacing.WEST; break;
            case WEST: opposite = EnumFacing.EAST; break;
            case UP: opposite = EnumFacing.DOWN; break;
            case DOWN: opposite = EnumFacing.UP; break;
            default:
                return;
        }
        this.drawScaledFace(minX, minY, maxX, maxY, face, icon, offset);
        this.drawScaledFace(minX, minY, maxX, maxY, opposite, icon, offset);
    }

    /**
     * Adds 6 quads for a scaled prism, the prism is defined by maximum and minimum 3D coordinates
     * @param minX minimum x-coordinate of the face
     * @param minY minimum y-coordinate of the face
     * @param minZ maximum z-coordinate of the face
     * @param maxX maximum x-coordinate of the face
     * @param maxY maximum y-coordinate of the face
     * @param maxZ maximum z-coordinate of the face
     * @param icon icon to render the prism with
     */
    @Override
    public void drawScaledPrism(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite icon) {
        //bottom
        drawScaledFace(minX, minZ, maxX, maxZ, EnumFacing.DOWN, icon, minY);
        //top
        drawScaledFace(minX, minZ, maxX, maxZ, EnumFacing.UP, icon, maxY);
        //north
        drawScaledFace(minX, minY, maxX, maxY, EnumFacing.NORTH, icon, minZ);
        //south
        drawScaledFace(minX, minY, maxX, maxY, EnumFacing.SOUTH, icon, maxZ);
        //west
        drawScaledFace(minZ, minY, maxZ, maxY, EnumFacing.WEST, icon, minX);
        //east
        drawScaledFace(minZ, minY, maxZ, maxY, EnumFacing.EAST, icon, maxX);
    }

    /**
     * Sets the translation components relative to the absolute coordinate system
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return this
     */
    @Override
    public TessellatorAbstractBase setTranslation(double x, double y, double z) {
        this.matrices.getFirst().setTranslation(x, y, z);
        return this;
    }

    /**
     * Sets the rotation components relative to the absolute coordinate system
     * @param angle rotation ange
     * @param x the x-direction
     * @param y the y-direction
     * @param z the z-direction
     * @return this
     */
    @Override
    public TessellatorAbstractBase setRotation(double angle, double x, double y, double z) {
        this.matrices.getFirst().setRotation(angle, x, y, z);
        return this;
    }

    /**
     * Translates the matrix by a vector defined by a BlockPos
     * @param pos the BlockPos
     * @return this
     */
    @Override
    public TessellatorAbstractBase translate(BlockPos pos) {
        this.translate(pos.getX(), pos.getY(), pos.getZ());
        return this;
    }

    /**
     * Translates the matrix by a vector defined by 3 coordinates
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return this
     */
    @Override
    public TessellatorAbstractBase translate(double x, double y, double z) {
        this.matrices.getFirst().multiplyRightWith(new TransformationMatrix(x, y, z));
        return this;
    }

    /**
     * Rotates the matrix by an angle around the given direction, rotation center is the current origin
     * @param angle angle to rotate by
     * @param x the x direction
     * @param y the y direction
     * @param z the z direction
     * @return this
     */
    @Override
    public TessellatorAbstractBase rotate(double angle, double x, double y, double z) {
        this.matrices.getFirst().multiplyRightWith(new TransformationMatrix(angle, x, y, z));
        return this;
    }

    /**
     * Scales along each axis with the corresponding factor
     * @param x the x-axis scale factor
     * @param y the y-axis scale factor
     * @param z the z-axis scale factor
     * @return this
     */
    @Override
    public TessellatorAbstractBase scale(double x, double y, double z) {
        this.matrices.getFirst().scale(x, y, z);
        return this;
    }

    /**
     * Applies a custom transformation
     * @param transformationMatrix transformation matrix defining the custom transformation
     * @return this
     */
    public TessellatorAbstractBase applyTransformation(TransformationMatrix transformationMatrix) {
        this.matrices.getFirst().multiplyRightWith(transformationMatrix);
        return this;
    }

    /**
     * Gets the current transformation matrix
     * @return the transformation matrix
     */
    public TransformationMatrix getTransformationMatrix() {
        return this.matrices.getFirst();
    }

    /**
     * Resets the transformation matrix
     * @return this
     */
    public TessellatorAbstractBase resetMatrix() {
        this.matrices.clear();
        this.matrices.push(new TransformationMatrix());
        return this;
    }

    /**
     * Gets a TextureAtlasSprite icon from a ResourceLocation
     * @param loc the ResourceLocation
     * @return the icon
     */
    @Override
    public TextureAtlasSprite getIcon(ResourceLocation loc) {
        return ModelLoader.defaultTextureGetter().apply(loc);
    }

    /**
     * Sets the normal for the tessellator
     * @param x the normal x direction
     * @param y the normal y direction
     * @param z the normal z direction
     * @return this
     */
    @Override
    public TessellatorAbstractBase setNormal(float x, float y, float z) {
        return this.setNormal(new Vec3f(x, y, z));
    }

    /**
     * Sets the normal for the tessellator
     * @param vec the normal vector
     * @return this
     */
    @Override
    public TessellatorAbstractBase setNormal(Vec3f vec) {
        this.normal = vec == null ? this.normal : vec;
        return this;
    }

    /**
     * Gets the current normal for the tessellator
     * @return the normal vector
     */
    @Override
    public Vec3f getNormal() {
        return this.normal;
    }

    /**
     * Sets the current opaque color multiplier for the quads
     * @param color the rgb color value
     * @return this
     */
    @Override
    public TessellatorAbstractBase setColor(int color) {
        this.setColorTransparent(color);
        this.a = 255;
        return this;
    }

    /**
     * Sets the current transparent color multiplier for the quads
     * @param color the rgba color value
     * @return this
     */
    @Override
    public TessellatorAbstractBase setColorTransparent(int color) {
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        int alpha = this.a;
        return this.setColorRGBA(red, green, blue, alpha);
    }

    /**
     * Gets the current color value as an rgb int
     * @return the color multiplier
     */
    @Override
    public int getColor() {
        return (this.r << 16) | (this.g << 8) | (this.b);
    }

    /**
     * Sets the current color value based on red, green and blue float values, all arguments should be between 0F and 1F
     * @param red the rgb red value
     * @param green the rgb green value
     * @param blue the rgb blue value
     * @return this
     */
    @Override
    public TessellatorAbstractBase setColorRGB_F(float red, float green, float blue) {
        return this.setColorRGBA_F(red, green, blue, 1.0F);
    }

    /**
     * Sets the current color value based on red, green, blue and alpha values, all arguments should be between 0F and 1F
     * @param red the rgb red value
     * @param green the rgb green value
     * @param blue the rgb blue value
     * @param alpha the rgb alpha value
     * @return this
     */
    @Override
    public TessellatorAbstractBase setColorRGBA_F(float red, float green, float blue, float alpha) {
        return this.setColorRGBA((int) (red * 255),(int) (green * 255),(int) (blue * 255),(int) (alpha * 255));
    }

    /**
     * Sets the current color value based on red, green and blue int values, all arguments should be between 0 and 255
     * @param red the rgb red value
     * @param green the rgb green value
     * @param blue the rgb blue value
     * @return this
     */
    @Override
    public TessellatorAbstractBase setColorRGB(int red, int green, int blue) {
        return this.setColorRGBA(red, green, blue, 255);
    }

    /**
     * Sets the current color value based on red, green, blue and alpha values, all arguments should be between 0 and 255
     * @param red the rgb red value
     * @param green the rgb green value
     * @param blue the rgb blue value
     * @param alpha the rgb alpha value
     * @return this
     */
    @Override
    public TessellatorAbstractBase setColorRGBA(int red, int green, int blue, int alpha) {
        this.r = red;
        this.g = green;
        this.b = blue;
        this.a = alpha;
        return this;
    }

    /**
     * @return current blue value as float, will be between 0 and 1
     */
    @Override
    public float getRedValueFloat() {
        return ( (float) getRedValueInt()) / 255.0F;
    }

    /**
     * @return current green value as float, will be between 0 and 1
     */
    @Override
    public float getGreenValueFloat() {
        return ( (float) getGreenValueInt()) / 255.0F;
    }

    /**
     * @return current blue value as float, will be between 0 and 1
     */
    @Override
    public float getBlueValueFloat() {
        return ( (float) getBlueValueInt()) / 255.0F;
    }

    /**
     * @return current alpha value as float, will be between 0 and 1
     */
    @Override
    public float getAlphaValueFloat() {
        return ( (float) getAlphaValueInt()) / 255.0F;
    }

    /**
     * @return current red value as int, will be between 0 and 255
     */
    @Override
    public int getRedValueInt() {
        return this.r;
    }

    /**
     * @return current green value as int, will be between 0 and 255
     */
    @Override
    public int getGreenValueInt() {
        return this.g;
    }

    /**
     * @return current red value as int, will be between 0 and 255
     */
    @Override
    public int getBlueValueInt() {
        return this.b;
    }

    /**
     * @return current alpha value as int, will be between 0 and 255
     */
    @Override
    public int getAlphaValueInt() {
        return this.a;
    }

    /**
     * Sets the brightness of the tessellator
     * @param value the brightness value
     * @return this
     */
    @Override
    public TessellatorAbstractBase setBrightness(int value) {
        this.l = value;
        return this;
    }

    /**
     * Gets the brightness of the tessellator
     * @return the brightness value
     */
    @Override
    public int getBrightness() {
        return this.l;
    }

    /**
     * Sets the tint index value to use for the quads
     * @param index the tint index
     * @return this
     */
    @Override
    public TessellatorAbstractBase setTintIndex(int index) {
        this.tintIndex = index;
        return this;
    }

    /**
     * Gets the current tint index value to use for the quads
     * @return the tint index
     */
    @Override
    public int getTintIndex() {
        return this.tintIndex;
    }

    /**
     * Sets if diffuse lighting should be applied to the quads
     * @param value the diffuse lighting setting
     * @return this
     */
    @Override
    public TessellatorAbstractBase setApplyDiffuseLighting(boolean value) {
        this.applyDiffuseLighting = value;
        return this;
    }

    /**
     * Gets if diffuse lighting is applied to the quads
     * @return the diffuse lighting setting
     */
    @Override
    public boolean getApplyDiffuseLighting() {
        return this.applyDiffuseLighting;
    }

    protected abstract void applyColorMultiplier(EnumFacing side);

    /**
     * Applies the transformation of the tessellator to a list of quads
     * @param quads the quads to be transformed
     * @return the list with transformed quads
     */
    protected List<BakedQuad> transformQuads(List<BakedQuad> quads) {
        List<BakedQuad> newQuads = new ArrayList<>();
        for(BakedQuad quad : quads) {
            VertexFormat format = quad.getFormat();
            float[][][] vertexData = new float[4][format.getElementCount()][];
            //unpack and transform vertex data
            for(int v = 0; v < 4; v++) {
                for(int e = 0; e < format.getElementCount(); e++) {
                    float[] data = new float[4];
                    LightUtil.unpack(quad.getVertexData(), data, format, v, e);
                    vertexData[v][e] = transformUnpackedVertexDataElement(format.getElement(e).getUsage(), data);
                }
            }
            //create new quad with the transformed vertex data
            newQuads.add(new UnpackedBakedQuad(vertexData, quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), format));
        }
        return newQuads;
    }

    /**
     * Transforms an unpacked vertex data element
     * @param type the type of the vertex data element
     * @param data the data of the vertex data element
     * @return the transformed, unpacked vertex data
     */
    protected float[] transformUnpackedVertexDataElement(VertexFormatElement.EnumUsage type, float[] data) {
        switch(type) {
            case POSITION:
            case NORMAL:
                double[] pos = this.getTransformationMatrix().transform(data[0], data[1], data[2]);
                data[0] = (float) pos[0];
                data[1] = (float) pos[1];
                data[2] = (float) pos[2];
                break;
            case COLOR:
                data[0] = getRedValueFloat();
                data[1] = getGreenValueFloat();
                data[2] = getBlueValueFloat();
                data[3] = getAlphaValueFloat();
                break;
        }
        return data;
    }
}

package appu26j.aguiu.gui.font;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.io.*;
import java.nio.*;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;

public class FontRenderer
{
    public static final int CHAR_DATA_MALLOC_SIZE = 96, FONT_TEXTURE_WIDTH = 512, FONT_TEXTURE_HEIGHT = 512;
    public static final int BAKE_FONT_FIRST_CHAR = 32, GLYPH_COUNT = CHAR_DATA_MALLOC_SIZE;
    private HashMap<Integer, Float> widths = new HashMap<>();
    private STBTTBakedChar.Buffer charData;
    private float ascent, pixelScale;
    private STBTTFontinfo fontInfo;
    private int textureID;
    private int fontSize;
    
    public FontRenderer(String fontLocation, int fontSize)
    {
        this.fontSize = fontSize;
        charData = STBTTBakedChar.malloc(CHAR_DATA_MALLOC_SIZE);
        fontInfo = STBTTFontinfo.create();
        
        try
        {
            ByteBuffer fontData = streamByteBuffer(get(fontLocation)), textureData = BufferUtils.createByteBuffer(FONT_TEXTURE_WIDTH * FONT_TEXTURE_HEIGHT);
            STBTruetype.stbtt_BakeFontBitmap(fontData, fontSize, textureData, FONT_TEXTURE_WIDTH, FONT_TEXTURE_HEIGHT, BAKE_FONT_FIRST_CHAR, charData);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                STBTruetype.stbtt_InitFont(fontInfo, fontData);
                pixelScale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, fontSize);
                IntBuffer ascentBuffer = stack.ints(0), descentBuffer = stack.ints(0), lineGapBuffer = stack.ints(0);
                STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascentBuffer, descentBuffer, lineGapBuffer);
                ascent = ascentBuffer.get(0) * pixelScale;
            }
            
            textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, FONT_TEXTURE_WIDTH, FONT_TEXTURE_HEIGHT, 0, GL_ALPHA, GL_UNSIGNED_BYTE, textureData);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        }
        
        catch (Exception e)
        {
        }
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer advanceWidth = stack.mallocInt(1);
            IntBuffer leftSideBearing = stack.mallocInt(1);
            String characters = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()1234567890`~-=_+[]{}\\|;':\",.<>/?";
            
            for (int character : characters.toCharArray())
            {
                widths.put(character, getRealWidth(character, advanceWidth, leftSideBearing));
            }
        }
    }
    
    public void drawString(String text, float x, float y, int color)
    {
        drawString(text, x, y, new Color(color, true));
    }
    
    public void drawString(String text, float x, float y, Color color)
    {
        y += ascent;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F, alpha = color.getAlpha() / 255F;
            FloatBuffer xBuffer = stack.floats(x), yBuffer = stack.floats(y);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            glEnable(GL_TEXTURE_2D);
            glColor4f(red, green, blue, alpha);
            glBindTexture(GL_TEXTURE_2D, textureID);
            glBegin(GL_TRIANGLES);
            int firstPoint = BAKE_FONT_FIRST_CHAR, lastPoint = (BAKE_FONT_FIRST_CHAR + GLYPH_COUNT) - 1;
            
            for (int i = 0; i < text.length(); i++)
            {
                int codePoint = text.codePointAt(i);
                
                if (codePoint == '\n')
                {
                    xBuffer.put(0, x);
                    yBuffer.put(0, y + yBuffer.get(0) + fontSize);
                    continue;
                }
                
                if (!(codePoint < firstPoint || codePoint > lastPoint))
                {
                    STBTruetype.stbtt_GetBakedQuad(charData, FONT_TEXTURE_WIDTH, FONT_TEXTURE_HEIGHT, codePoint - firstPoint, xBuffer, yBuffer, quad, true);
                    glTexCoord2f(quad.s0(), quad.t0());
                    glVertex2f(quad.x0(), quad.y0());
                    glTexCoord2f(quad.s0(), quad.t1());
                    glVertex2f(quad.x0(), quad.y1());
                    glTexCoord2f(quad.s1(), quad.t1());
                    glVertex2f(quad.x1(), quad.y1());
                    glTexCoord2f(quad.s1(), quad.t1());
                    glVertex2f(quad.x1(), quad.y1());
                    glTexCoord2f(quad.s1(), quad.t0());
                    glVertex2f(quad.x1(), quad.y0());
                    glTexCoord2f(quad.s0(), quad.t0());
                    glVertex2f(quad.x0(), quad.y0());
                }
            }
            
            glEnd();
            glDisable(GL_TEXTURE_2D);
        }
    }
    
    public float getWidth(String text)
    {
        float length = 0;
        
        for (int i = 0; i < text.length(); i++)
        {
            int character = text.charAt(i);
            
            if (widths.containsKey(character))
            {
                length += widths.get(character);
            }
        }
        
        return length;
    }
    
    private float getRealWidth(int character, IntBuffer advanceWidth, IntBuffer leftSideBearing)
    {
        STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, character, advanceWidth, leftSideBearing);
        return advanceWidth.get(0) * pixelScale;
    }
    
    public static ByteBuffer streamByteBuffer(InputStream inputStream)
    {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
        {
            byte[] data = new byte[4096];
            int read;
            
            while ((read = inputStream.read(data, 0, data.length)) != -1)
            {
                byteArrayOutputStream.write(data, 0, read);
            }
            
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(byteArrayOutputStream.size());
            byteBuffer.put(byteArrayOutputStream.toByteArray());
            byteBuffer.flip();
            return byteBuffer;
        }
        
        catch (Exception e)
        {
            return null;
        }
    }
    
    public static InputStream get(String location)
    {
        return FontRenderer.class.getClassLoader().getResourceAsStream(location);
    }
}

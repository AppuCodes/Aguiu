package appu26j.aguiu.gui;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;

public class Gui
{
    public static void drawRect(float x, float y, float width, float height, int color)
    {
        drawRect(x, y, width, height, new Color(color, true));
    }
    
    public static void drawRect(float x, float y, float width, float height, Color color)
    {
        float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F, alpha = color.getAlpha() / 255F;
        glColor4f(red, green, blue, alpha);
        glBegin(GL_TRIANGLES);
        glVertex2f(x, y);
        glVertex2f(x, height);
        glVertex2f(width, height);
        glVertex2f(width, y);
        glVertex2f(width, height);
        glVertex2f(x, y);
        glEnd();
    }
    
    public static void drawGradient(float x, float y, float width, float height, int topColor, int bottomColor)
    {
        drawGradient(x, y, width, height, new Color(topColor, true), new Color(bottomColor, true));
    }
    
    public static void drawGradient(float x, float y, float width, float height, Color topColor, Color bottomColor)
    {
        float topRed = topColor.getRed() / 255F, topGreen = topColor.getGreen() / 255F, topBlue = topColor.getBlue() / 255F, topAlpha = topColor.getAlpha() / 255F;
        float bottomRed = bottomColor.getRed() / 255F, bottomGreen = bottomColor.getGreen() / 255F, bottomBlue = bottomColor.getBlue() / 255F, bottomAlpha = bottomColor.getAlpha() / 255F;
        glColor4f(topRed, topGreen, topBlue, topAlpha);
        glBegin(GL_TRIANGLES);
        glVertex2f(x, y);
        glColor4f(bottomRed, bottomGreen, bottomBlue, bottomAlpha);
        glVertex2f(x, height);
        glVertex2f(width, height);
        glColor4f(topRed, topGreen, topBlue, topAlpha);
        glVertex2f(width, y);
        glColor4f(bottomRed, bottomGreen, bottomBlue, bottomAlpha);
        glVertex2f(width, height);
        glColor4f(topRed, topGreen, topBlue, topAlpha);
        glVertex2f(x, y);
        glEnd();
    }
    
    public static void drawHorzGradient(float x, float y, float width, float height, int leftColor, int rightColor)
    {
        drawHorzGradient(x, y, width, height, new Color(leftColor, true), new Color(rightColor, true));
    }
    
    public static void drawHorzGradient(float x, float y, float width, float height, Color leftColor, Color rightColor)
    {
        float topRed = leftColor.getRed() / 255F, topGreen = leftColor.getGreen() / 255F, topBlue = leftColor.getBlue() / 255F, topAlpha = leftColor.getAlpha() / 255F;
        float bottomRed = rightColor.getRed() / 255F, bottomGreen = rightColor.getGreen() / 255F, bottomBlue = rightColor.getBlue() / 255F, bottomAlpha = rightColor.getAlpha() / 255F;
        glColor4f(topRed, topGreen, topBlue, topAlpha);
        glBegin(GL_TRIANGLES);
        glVertex2f(x, y);
        glVertex2f(x, height);
        glColor4f(bottomRed, bottomGreen, bottomBlue, bottomAlpha);
        glVertex2f(width, height);
        glVertex2f(width, y);
        glVertex2f(width, height);
        glColor4f(topRed, topGreen, topBlue, topAlpha);
        glVertex2f(x, y);
        glEnd();
    }
}

package appu26j.aguiu;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.*;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import appu26j.aguiu.gui.GuiScreen;
import appu26j.aguiu.gui.font.FontRenderer;

public class AgWindow
{
    private static float cursorX, cursorY;
    private static int width, height;
    private static GuiScreen screen;
    private static boolean resized;
    private static long id;
    
    static
    {
        GLFWErrorCallback.createPrint(System.err).set();
        
        if (!glfwInit())
        {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
    }
    
    public static void create(String title, int width, int height, boolean resizable)
    {
        AgWindow.width = width;
        AgWindow.height = height;
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, value(resizable));
        id = glfwCreateWindow(width, height, title, 0, 0);
        
        if (id == 0)
        {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        
        glfwMakeContextCurrent(id);
        glfwSwapInterval(1); // VSync
        setCallbacks();
        setupOpenGL();
        show();
    }
    
    private static void setCallbacks()
    {
        glfwSetWindowSizeCallback(id, new GLFWWindowSizeCallback()
        {
            public void invoke(long id, int width, int height)
            {
                AgWindow.width = width;
                AgWindow.height = height;
                resized = true;
            }
        });
        
        glfwSetMouseButtonCallback(id, new GLFWMouseButtonCallback()
        {
            public void invoke(long id, int button, int action, int mods)
            {
                if (action == 1)
                {
                    if (screen != null)
                    {
                        screen.mouseClicked(button, cursorX, cursorY);
                    }
                }
                
                else
                {
                    if (screen != null)
                    {
                        screen.mouseReleased(button, cursorX, cursorY);
                    }
                }
            }
        });
        
        glfwSetKeyCallback(id, new GLFWKeyCallback()
        {
            public void invoke(long id, int key, int scanCode, int action, int mods)
            {
                if (action == 1)
                {
                    if (screen != null)
                    {
                        screen.keyPressed(key);
                    }
                }
                
                else
                {
                    if (screen != null)
                    {
                        screen.keyReleased(key);
                    }
                }
            }
        });
        
        glfwSetScrollCallback(id, new GLFWScrollCallback()
        {
            @Override
            public void invoke(long id, double xOffset, double yOffset)
            {
                if (screen != null)
                {
                    screen.mouseScrolled(xOffset, yOffset);
                }
            }
        });
    }
    
    public static void beginLoop()
    {
        if (resized)
        {
            resize();
            displayScreen(screen);
            resized = false;
        }
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            DoubleBuffer cursorX = stack.mallocDouble(1), cursorY = stack.mallocDouble(1);
            glfwGetCursorPos(id, cursorX, cursorY);
            AgWindow.cursorX = (float) cursorX.get();
            AgWindow.cursorY = (float) cursorY.get();
        }
    }
    
    public static void setIcon(String iconLocation)
    {
        ByteBuffer icon = FontRenderer.streamByteBuffer(FontRenderer.get(iconLocation));
        
        try (GLFWImage.Buffer icons = GLFWImage.malloc(2); MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width = stack.mallocInt(1), height = stack.mallocInt(1), component = stack.mallocInt(1);
            ByteBuffer pixelsOne = STBImage.stbi_load_from_memory(icon, width, height, component, 4),
            pixelsTwo = STBImage.stbi_load_from_memory(icon, width, height, component, 4);
            icons.position(0).width(width.get(0)).height(height.get(0)).pixels(pixelsOne)
            .position(1).width(width.get(0)).height(height.get(0)).pixels(pixelsTwo).position(0);
            glfwSetWindowIcon(id, icons);
            STBImage.stbi_image_free(pixelsOne);
            STBImage.stbi_image_free(pixelsTwo);
        }
    }
    
    public static void draw()
    {
        if (screen != null)
        {
            screen.drawScreen(cursorX, cursorY);
        }
    }
    
    public static void endLoop()
    {
        glfwSwapBuffers(id);
        glfwPollEvents();
    }
    
    public static boolean isOpen()
    {
        return !glfwWindowShouldClose(id);
    }
    
    public static void hide()
    {
        glfwHideWindow(id);
    }
    
    public static void show()
    {
        glfwShowWindow(id);
    }
    
    public static void changeTitle(String title)
    {
        glfwSetWindowTitle(id, title);
    }
    
    public static void displayScreen(GuiScreen screen)
    {
        if (AgWindow.screen != null)
        {
            AgWindow.screen.onClose();
        }
        
        if (screen != null)
        {
            screen.initGui(width, height);
        }
        
        AgWindow.screen = screen;
    }
    
    public static void setBackgroundColor(int red, int green, int blue, int alpha)
    {
        glClearColor(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }
    
    public static int getWidth()
    {
        return width;
    }
    
    public static int getHeight()
    {
        return height;
    }
    
    private static void setupOpenGL()
    {
        GL.createCapabilities();
        resize();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    
    private static void resize()
    {
        glLoadIdentity();
        glViewport(0, 0, width, height);
        glOrtho(0, width, height, 0, 1, 0);
    }
    
    private static int value(boolean condition)
    {
        return condition ? 1 : 0;
    }
}

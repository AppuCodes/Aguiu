package appu26j.aguiu.gui;

public abstract class GuiScreen extends Gui
{
    protected float width, height;
    
    public void initGui(float width, float height)
    {
        this.width = width;
        this.height = height;
    }
    
    /**
     * ICIB: Is Cursor Inside Box
     */
    protected static boolean ICIB(float cursorX, float cursorY, float x, float y, float width, float height)
    {
        return cursorX < width && cursorX > x && cursorY < height && cursorY > y;
    }
    
    public abstract void drawScreen(float cursorX, float cursorY);
    public void mouseClicked(int button, float cursorX, float cursorY) {};
    public void mouseReleased(int button, float cursorX, float cursorY) {};
    public void keyPressed(int key) {};
    public void onClose() {}
}

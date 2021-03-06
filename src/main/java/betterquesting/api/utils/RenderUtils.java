package betterquesting.api.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.core.BetterQuesting;

@SideOnly(Side.CLIENT)
public class RenderUtils
{
	public static final String REGEX_NUMBER = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text)
	{
		RenderItemStack(mc, stack, x, y, text, Color.WHITE);
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, int color)
	{
		float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
		RenderItemStack(mc, stack, x, y, text, new Color(r, g, b));
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, Color color)
	{
		if(stack == null || stack.getItem() == null)
		{
			return;
		}
		
		ItemStack rStack = stack;
		
		if(stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
		{
			NonNullList<ItemStack> tmp = NonNullList.<ItemStack>create();
			
			stack.getItem().getSubItems(CreativeTabs.SEARCH, tmp);
			
			if(tmp.size() > 0)
			{
				rStack = tmp.get((int)((Minecraft.getSystemTime()/1000)%tmp.size()));
			}
		}
		
		GlStateManager.pushMatrix();
		RenderItem itemRender = mc.getRenderItem();
	    float preZ = itemRender.zLevel;
        
		try
		{
		    GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F);
			RenderHelper.enableGUIStandardItemLighting();
		    GlStateManager.enableRescaleNormal();
			
		    GlStateManager.translate(0.0F, 0.0F, 32.0F);
		    itemRender.zLevel = 200.0F;
		    FontRenderer font = null;
		    if (rStack != null) font = rStack.getItem().getFontRenderer(rStack);
		    if (font == null) font = mc.fontRenderer;
		    itemRender.renderItemAndEffectIntoGUI(rStack, x, y);
		    itemRender.renderItemOverlayIntoGUI(font, rStack, x, y, text);
		    
		    RenderHelper.disableStandardItemLighting();
		} catch(Exception e)
		{
		}
		
	    itemRender.zLevel = preZ; // Just in case
		
        GlStateManager.popMatrix();
	}

    public static void RenderEntity(int posX, int posY, int scale, float rotation, float pitch, Entity entity)
    {
    	try
    	{
	        GlStateManager.enableColorMaterial();
	        GlStateManager.pushMatrix();
	        GlStateManager.enableDepth();
	        GlStateManager.translate((float)posX, (float)posY, 100.0F);
	        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
	        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
	        GlStateManager.rotate(pitch, 1F, 0F, 0F);
	        GlStateManager.rotate(rotation, 0F, 1F, 0F);
	        float f3 = entity.rotationYaw;
	        float f4 = entity.rotationPitch;
	        RenderHelper.enableStandardItemLighting();
	        GlStateManager.translate(0D, entity.getYOffset(), 0D);
	        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
	        rendermanager.setPlayerViewY(180.0F);
	        rendermanager.doRenderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
	        entity.rotationYaw = f3;
	        entity.rotationPitch = f4;
	        GlStateManager.popMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GlStateManager.disableRescaleNormal();
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GlStateManager.disableTexture2D();
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	} catch(Exception e)
    	{
    		// Hides rendering errors with entities which are common for invalid/technical entities
    	}
    }
	
	public static void DrawLine(int x1, int y1, int x2, int y2, float width, int color)
	{
		float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
		GlStateManager.pushMatrix();
		
		GlStateManager.disableTexture2D();
		GlStateManager.color(r, g, b, 1F);
		GL11.glLineWidth(width);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glEnd();
		
		GlStateManager.enableTexture2D();
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		GlStateManager.popMatrix();
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow)
	{
		drawSplitString(renderer, string, x, y, width, color, shadow, 0, renderer.listFormattedStringToWidth(string, width).size() - 1);
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end)
	{
		if(renderer == null || string == null || string.length() <= 0 || start > end)
		{
			return;
		}
		
		string = string.replaceAll("\r", ""); //Line endings from localizations break things so we remove them
		
		List<String> list = renderer.listFormattedStringToWidth(string, width);
		
		for(int i = start; i <= end; i++)
		{
			if(i < 0 || i >= list.size())
			{
				continue;
			}
			
			renderer.drawString(list.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);
		}
	}
	
	/**
	 * Performs a OpenGL scissor based on Minecraft's resolution instead of display resolution
	 */
	@Deprecated
	public static void guiScissor(Minecraft mc, int x, int y, int w, int h)
	{
		ScaledResolution r = new ScaledResolution(mc);
		int f = r.getScaleFactor();
		
		GL11.glScissor(x * f, (r.getScaledHeight() - y - h)*f, w * f, h * f);
	}
	
	private static Stack<IGuiRect> scissorStack = new Stack<IGuiRect>();
	
	/**
	 * Performs a OpenGL scissor based on Minecraft's resolution instead of display resolution and adds it to the stack of ongoing scissors.
	 * Not using this method will result in incorrect scissoring of parent/child GUIs
	 */
	public static void startScissor(Minecraft mc, GuiRectangle rect)
	{
		if(scissorStack.size() >= 100)
		{
			BetterQuesting.logger.log(Level.ERROR, "More than 100 recursive scissor calls have been made!");
			return;
		}
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		ScaledResolution r = new ScaledResolution(mc);
		int f = r.getScaleFactor();
		
		GuiRectangle sRect = rect;
		
		if(!scissorStack.empty())
		{
			IGuiRect parentRect = scissorStack.peek();
			int x = Math.max(parentRect.getX(), rect.getX());
			int y = Math.max(parentRect.getY(), rect.getY());
			int w = Math.min(parentRect.getX() + parentRect.getWidth(), rect.getX() + rect.getWidth());
			int h = Math.min(parentRect.getY() + parentRect.getHeight(), rect.getY() + rect.getHeight());
			w = Math.max(0, w - x); // Clamp to 0 to prevent OpenGL errors
			h = Math.max(0, h - y); // Clamp to 0 to prevent OpenGL errors
			sRect = new GuiRectangle(x, y, w, h, 0);
		}
		
		GL11.glScissor(sRect.getX() * f, (r.getScaledHeight() - sRect.getY() - sRect.getHeight())*f, sRect.getWidth() * f, sRect.getHeight() * f);
		scissorStack.add(sRect);
	}
	
	/**
	 * Pops the last scissor off the stack and returns to the last parent scissor or disables it if there are none
	 */
	public static void endScissor(Minecraft mc)
	{
		scissorStack.pop();
		
		if(scissorStack.empty())
		{
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		} else
		{
			ScaledResolution r = new ScaledResolution(mc);
			int f = r.getScaleFactor();
			
			IGuiRect rect = scissorStack.peek();
			GL11.glScissor(rect.getX() * f, (r.getScaledHeight() - rect.getY() - rect.getHeight())*f, rect.getWidth() * f, rect.getHeight() * f);
		}
	}
	
	/**
	 * Similar to normally splitting a string with the fontRenderer however this variant preserves
	 * the original characters (including new line) and does not not attempt to preserver the format
	 * between lines.
	 */
	public static List<String> splitStringWithoutFormat(String str, int wrapWidth, FontRenderer font)
	{
		List<String> list = new ArrayList<String>();
		
		String lastFormat = ""; // Formatting like bold can affect the wrapping width
		
		String[] nlSplit = str.split("\n");
		
		for(int i = 0; i < nlSplit.length; i++)
		{
			String s = nlSplit[i] + (i + 1 < nlSplit.length? "\n" : ""); // Preserve new line characters for indexing accuracy
			
			while(font.getStringWidth(s) >= wrapWidth)
			{
				lastFormat = FontRenderer.getFormatFromString(lastFormat + s);
				int n = sizeStringToWidth(lastFormat + s, wrapWidth, font);
				n -= lastFormat.length();
				n = Math.max(1, n);
				String subTxt = s.substring(0, n);
				list.add(subTxt);
				s = s.replaceFirst(Pattern.quote(subTxt), "");
			}
			
			list.add(s);
		}
        
        return list;
	}
	
    private static int sizeStringToWidth(String str, int wrapWidth, FontRenderer font)
    {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k)
        {
            char c0 = str.charAt(k);

            switch (c0)
            {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += font.getCharWidth(c0);

                    if (flag)
                    {
                        ++j;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1)
                    {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 108 && c1 != 76)
                        {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (c0 == 10)
            {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth)
            {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }
    
    private static boolean isFormatColor(char colorChar)
    {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }
    
	public static float lerpFloat(float f1, float f2, float blend)
	{
		return (f2 * blend) + (f1 * (1F - blend));
	}
	
	public static int lerpRGB(int c1, int c2, float blend)
	{
		float a1 = c1 >> 24 & 255;
		float r1 = c1 >> 16 & 255;
		float g1 = c1 >> 8 & 255;
		float b1 = c1 & 255;
		
		float a2 = c2 >> 24 & 255;
		float r2 = c2 >> 16 & 255;
		float g2 = c2 >> 8 & 255;
		float b2 = c2 & 255;
		
		int a3 = (int)lerpFloat(a1, a2, blend);
		int r3 = (int)lerpFloat(r1, r2, blend);
		int g3 = (int)lerpFloat(g1, g2, blend);
		int b3 = (int)lerpFloat(b1, b2, blend);
		
		return (a3 << 24) + (r3 << 16) + (g3 << 8) + b3;
	}
}

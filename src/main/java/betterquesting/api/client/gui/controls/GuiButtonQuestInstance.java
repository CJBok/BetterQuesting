package betterquesting.api.client.gui.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestInstance extends GuiButtonThemed
{
	private IQuest quest;
	private List<GuiButtonQuestInstance> parents = new ArrayList<GuiButtonQuestInstance>();
	
	public GuiButtonQuestInstance(int id, int x, int y, int w, int h, IQuest quest)
	{
		super(id, x, y, w, h, "", false);
		this.quest = quest;
	}
	
	public void addParent(GuiButtonQuestInstance btn)
	{
		parents.add(btn);
	}
	
	public List<GuiButtonQuestInstance> getParents()
	{
		return parents;
	}
	
	public IQuest getQuest()
	{
		return quest;
	}

    /**
     * Draws this button to the screen.
     */
	@Override
    public void drawButton(Minecraft mc, int mx, int my, float partialTick)
    {
		UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
		
		if(QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.HARDCORE))
		{
			this.enabled = this.visible = true;
		} else if(mc.player == null)
		{
			this.enabled = false;
			this.visible = true;
		} else
		{
			this.visible = isQuestShown(playerID);
			this.enabled = this.visible && quest.isUnlocked(playerID);
		}
		
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(currentTheme().getGuiTexture());
            GlStateManager.color(1F, 1F, 1F, 1F);
            this.hovered = this.mousePressed(mc, mx, my);
        	
        	for(GuiButtonQuestInstance p : parents)
        	{
        		if(!p.visible)
        		{
        			continue;
        		}
        		
        		float lsx = p.x + p.width/2F;
        		float lsy = p.y + p.height/2F;
        		float lsw = p.width/2F;
        		float lsh = p.height/2F;
        		float lex = x + width/2F;
        		float ley = y + height/2F;
        		float lew = width/2F;
        		float leh = height/2F;
        		
        		double la = Math.atan2(ley - lsy, lex - lsx);
        		double dx = Math.cos(la) * 16;
        		double dy = Math.sin(la) * 16;
        		
        		lsx += MathHelper.clamp((float)dx, -lsw, lsw);
        		lsy += MathHelper.clamp((float)dy, -lsh, lsh);
        		
        		la = Math.atan2(lsy - ley, lsx - lex);
        		dx = Math.cos(la) * 16;
        		dy = Math.sin(la) * 16;
        		lex += MathHelper.clamp((float)dx, -lew, lew);
        		ley += MathHelper.clamp((float)dy, -leh, leh);        		
        		currentTheme().getRenderer().drawLine(quest, playerID, lsx, lsy, lex, ley, mx, my, 1F);
        	}
    		
    		currentTheme().getRenderer().drawIcon(quest, playerID, x, y, width, height, mx, my, 1F);
        	
        	this.mouseDragged(mc, mx, my);
        }
    }
	
	public boolean isQuestShown(UUID uuid)
	{
		if(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player) || quest.getProperties().getProperty(NativeProps.VISIBILITY) == EnumQuestVisibility.ALWAYS)
		{
			return true;
		} else if(quest.getProperties().getProperty(NativeProps.VISIBILITY) == EnumQuestVisibility.HIDDEN)
		{
			return false;
		} else if(quest.getProperties().getProperty(NativeProps.VISIBILITY) == EnumQuestVisibility.UNLOCKED)
		{
			return quest.isUnlocked(uuid) || quest.isComplete(uuid);
		} else if(quest.getProperties().getProperty(NativeProps.VISIBILITY) == EnumQuestVisibility.NORMAL)
		{
			if(!quest.isComplete(uuid))
			{
				for(GuiButtonQuestInstance p : parents)
				{
					if(!p.quest.isUnlocked(uuid))
					{
						return false; // We require something locked
					}
				}
			}
			
			return true;
		} else if(quest.getProperties().getProperty(NativeProps.VISIBILITY) == EnumQuestVisibility.COMPLETED)
		{
			return quest.isComplete(uuid);
		} else if(quest.getProperties().getProperty(NativeProps.VISIBILITY) == EnumQuestVisibility.CHAIN)
		{
			for(GuiButtonQuestInstance q : parents)
			{
				if(q.isQuestShown(uuid))
				{
					return true;
				}
			}
			
			return parents.size() <= 0;
		}
		
		return true;
	}
}

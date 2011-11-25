package net.minecraft.src.CraftGuide.ui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.ItemStack;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.CraftGuide.Recipe;
import net.minecraft.src.CraftGuide.RecipeCache;
import net.minecraft.src.CraftGuide.API.ICraftGuideRecipe;
import net.minecraft.src.CraftGuide.ui.Rendering.CraftingDisplayRect;
import net.minecraft.src.CraftGuide.ui.Rendering.IRenderable;
import net.minecraft.src.CraftGuide.ui.Rendering.ToolTip;

public class CraftingDisplay extends GuiElement
{
	private GuiScrollBar scrollBar;
	private CraftingDisplayRect display;
	private int mouseX, mouseY;
	private ToolTip itemName = new ToolTip("-No Item-");
	private RecipeCache recipeCache;
	
	public CraftingDisplay(int x, int y, int width, int height, GuiScrollBar scrollBar, RecipeCache recipeCache)
	{
		super(x, y, width, height);

		this.recipeCache = recipeCache;
		this.scrollBar = scrollBar;
		
		updateScrollbarSize();
		
		this.display = new CraftingDisplayRect(0, 0, width, height, this);
	}

	@Override
	public void draw()
	{
		render(display);
		super.draw();
		
		drawSelectionName();
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		mouseX = x;
		mouseY = y;
		super.mouseMoved(x, y);
	}

	@Override
	public void mousePressed(int x, int y)
	{
		ItemStack stack = itemStackUnderMouse();
		
		if(stack != null)
		{
			setFilter(stack);
		}
		
		super.mousePressed(x, y);
	}
	
	public void setFilter(ItemStack filter)
	{
		recipeCache.filter(filter);
		updateScrollbarSize();
	}

	private void updateScrollbarSize()
	{
		float max = (recipeCache.getRecipes().size() - 5) / 2;
		if(max < 0)
		{
			max = 0;
		}
		
		scrollBar.setScale(0, max);
	}

	private Recipe recipeAt(int x, int y)
	{
		if(x <= 82 && x > 78)
		{
			return null;
		}
		
		int row = (int)(scrollBar.getValue() + y / (float)58);
		int index = row * 2 + (x > 82? 1 : 0);
		
		if(index < recipeCache.getRecipes().size() && index >= 0)
		{
			return (Recipe)recipeCache.getRecipes().get(index);
		}
		
		return null;
	}

	public void renderRecipes(GuiRenderer renderer, int xOffset, int yOffset)
	{
		List<ICraftGuideRecipe> recipes = recipeCache.getRecipes();
		
		int first = ((int)scrollBar.getValue()) * 2;
		int last = first + 8;
		yOffset += 58 - (int)((scrollBar.getValue() % 1) * 58);
		
		if(last > recipes.size())
		{
			last = recipes.size();
		}
		
		for(int i = first; i < last; i++)
		{
			Recipe recipe = (Recipe)recipes.get(i);
			int x = xOffset + ((i & 1) == 0? 0 : 83);
			int y = yOffset + (((i - first - 2) >> 1) * 58);
			boolean selected = false;
			
			if(isMouseOver(mouseX, mouseY))
			{
				ICraftGuideRecipe selectedRecipe = recipeAt(mouseX - this.x, mouseY - this.y);
				if(recipe == selectedRecipe)
				{
					selected = true;
				}
			}
			
			recipe.draw(renderer, x, y, selected);
		}
		
		DrawSelectionBox();
	}
	
	private void DrawSelectionBox()
	{
		if(isMouseOver(mouseX, mouseY))
		{
			Recipe recipe = recipeAt(mouseX - x, mouseY - y);

			if(recipe != null)
			{
				int x = (mouseX - this.x > 82? mouseX - this.x - 83 : mouseX - this.x);
				int y = (mouseY - this.y  + (int)((scrollBar.getValue() % 1) * 58)) % 58;
				
				if(recipe.getItemUnderMouse(x, y) != null)
				{
					IRenderable selection = recipe.getSelectionBox(x, y);
					
					if (selection != null)
					{
						render(selection, (mouseX - this.x) - x, (mouseY - this.y) - y);
					}
				}
			}
		}
	}
	
	private void drawSelectionName()
	{
		ItemStack stack = itemStackUnderMouse();
		
		if(stack != null)
		{
			String name = StringTranslate.getInstance().translateNamedKey(stack.getItemName());
			itemName.setText(name);
			render(itemName);
		}
	}
	
	private ItemStack itemStackUnderMouse()
	{
		if(isMouseOver(mouseX, mouseY))
		{
			Recipe recipe = recipeAt(mouseX - x, mouseY - y);
			
			if(recipe != null)
			{
				int x = (mouseX - this.x > 82? mouseX - this.x - 82 : mouseX - this.x);
				int y = (mouseY - this.y  + (int)((scrollBar.getValue() % 1) * 58)) % 58;
				
				return recipe.getItemUnderMouse(x, y);
			}
		}
		
		return null;
	}
}

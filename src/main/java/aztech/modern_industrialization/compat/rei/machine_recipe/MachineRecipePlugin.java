package aztech.modern_industrialization.compat.rei.machine_recipe;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.MIMachines;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineScreen;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static me.shedaniel.rei.api.BuiltinPlugin.SMELTING;

public class MachineRecipePlugin implements REIPluginV0 {
    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("machine_recipe");
    }

    @Override
    public void registerPluginCategories(RecipeHelper recipeHelper) {
        for(Map.Entry<MachineRecipeType, MIMachines.RecipeInfo> entry : MIMachines.RECIPE_TYPES.entrySet()) {
            List<MachineFactory> factories = entry.getValue().factories;
            recipeHelper.registerCategory(new MachineRecipeCategory(entry.getKey(), factories.get(factories.size()-1), EntryStack.create(factories.get(0).item)));
        }
    }

    @Override
    public void registerRecipeDisplays(RecipeHelper recipeHelper) {
        for(MachineRecipeType type : MIMachines.RECIPE_TYPES.keySet()) {
            recipeHelper.registerRecipes(type.getId(),
                    (Predicate<Recipe>) recipe ->
                            recipe instanceof MachineRecipe && ((MachineRecipe) recipe).getType() == type,
                    recipe -> new MachineRecipeDisplay(type, (MachineRecipe) recipe));
        }
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        for(Map.Entry<MachineRecipeType, MIMachines.RecipeInfo> entry : MIMachines.RECIPE_TYPES.entrySet()) {
            recipeHelper.registerWorkingStations(entry.getKey().getId(), entry.getValue().factories.stream().map(f -> EntryStack.create(f.item)).toArray(EntryStack[]::new));
            recipeHelper.registerScreenClickArea(new ArrowOverlayRectangle(entry.getValue().factories.get(entry.getValue().factories.size()-1)), MachineScreen.class, entry.getKey().getId());
        }

        for(MachineFactory factory : MIMachines.WORKSTATIONS_FURNACES) {
            recipeHelper.registerWorkingStations(SMELTING, EntryStack.create(factory.item));
        }

        recipeHelper.registerAutoCraftingHandler(new OutputLockTransferHandler());
    }

    // WARNING: Evil hack!
    private static class ArrowOverlayRectangle extends Rectangle {
        private final MachineRecipeType recipeType;
        private ArrowOverlayRectangle(MachineFactory factory) {
            super(factory.getProgressBarDrawX(), factory.getProgressBarDrawY(), factory.getProgressBarSizeX(), factory.getProgressBarSizeY());
            this.recipeType = factory.recipeType;
        }

        @Override
        public boolean contains(int x, int y) {
            return contains((double) x, y);
        }

        @Override
        public boolean contains(double x, double y) {
            Screen sc = MinecraftClient.getInstance().currentScreen;
            return sc instanceof MachineScreen && ((MachineScreen) sc).getScreenHandler().getMachineFactory().recipeType == this.recipeType && super.contains(x, y);
        }
    }
}

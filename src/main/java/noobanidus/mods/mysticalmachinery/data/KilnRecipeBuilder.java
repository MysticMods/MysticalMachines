package noobanidus.mods.mysticalmachinery.data;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger.Instance;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import noobanidus.mods.mysticalmachinery.init.ModRecipes;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class KilnRecipeBuilder {
  private final Item result;
  private final Ingredient ingredient;
  private final float experience;
  private final int cookingTime;
  private final Builder advancementBuilder = Builder.builder();
  private String group;

  private KilnRecipeBuilder(IItemProvider result, Ingredient ingredient, float xp, int cookTime) {
    this.result = result.asItem();
    this.ingredient = ingredient;
    this.experience = xp;
    this.cookingTime = cookTime;
  }

  public static KilnRecipeBuilder kilnRecipe(Ingredient input, IItemProvider result, float xp, int cookTime) {
    return new KilnRecipeBuilder(result, input, xp, cookTime);
  }

  public KilnRecipeBuilder addCriterion(String name, ICriterionInstance instance) {
    this.advancementBuilder.withCriterion(name, instance);
    return this;
  }

  public void build(Consumer<IFinishedRecipe> consumer) {
    this.build(consumer, ForgeRegistries.ITEMS.getKey(this.result));
  }

  public void build(Consumer<IFinishedRecipe> consumer, String name) {
    ResourceLocation resultName = ForgeRegistries.ITEMS.getKey(this.result);
    ResourceLocation recipeName = new ResourceLocation(name);
    if (recipeName.equals(resultName)) {
      throw new IllegalStateException("Recipe " + recipeName + " should remove its 'save' argument");
    } else {
      this.build(consumer, recipeName);
    }
  }

  public void build(Consumer<IFinishedRecipe> consumer, ResourceLocation resource) {
    this.validate(resource);
    this.advancementBuilder.withParentId(new ResourceLocation("recipes/root")).withCriterion("has_the_recipe", new Instance(resource)).withRewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(resource)).withRequirementsStrategy(IRequirementsStrategy.OR);
    consumer.accept(new KilnRecipeBuilder.Result(resource, this.group == null ? "" : this.group, this.ingredient, this.result, this.experience, this.cookingTime, this.advancementBuilder, new ResourceLocation(resource.getNamespace(), "recipes/" + this.result.getGroup().getPath() + "/" + resource.getPath())));
  }

  private void validate(ResourceLocation resource) {
    if (this.advancementBuilder.getCriteria().isEmpty()) {
      throw new IllegalStateException("No way of obtaining recipe " + resource);
    }
  }

  public static class Result implements IFinishedRecipe {
    private final ResourceLocation id;
    private final String group;
    private final Ingredient ingredient;
    private final Item result;
    private final float experience;
    private final int cookingTime;
    private final Builder advancementBuilder;
    private final ResourceLocation advancementId;

    public Result(ResourceLocation resource, String group, Ingredient ingredient, Item output, float xp, int cookTime, Builder advBuilder, ResourceLocation advResource) {
      this.id = resource;
      this.group = group;
      this.ingredient = ingredient;
      this.result = output;
      this.experience = xp;
      this.cookingTime = cookTime;
      this.advancementBuilder = advBuilder;
      this.advancementId = advResource;
    }

    @Override
    public void serialize(JsonObject json) {
      if (!this.group.isEmpty()) {
        json.addProperty("group", this.group);
      }

      json.add("ingredient", this.ingredient.serialize());
      json.addProperty("result", ForgeRegistries.ITEMS.getKey(this.result).toString());
      json.addProperty("experience", this.experience);
      json.addProperty("cookingtime", this.cookingTime);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
      return ModRecipes.KILN_SERIALIZER.get();
    }

    @Override
    public ResourceLocation getID() {
      return this.id;
    }

    @Override
    @Nullable
    public JsonObject getAdvancementJson() {
      return this.advancementBuilder.serialize();
    }

    @Override
    @Nullable
    public ResourceLocation getAdvancementID() {
      return this.advancementId;
    }
  }
}

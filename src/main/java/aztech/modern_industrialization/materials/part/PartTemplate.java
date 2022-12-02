/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.util.TagHelper;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.world.item.Item;

public class PartTemplate implements PartKeyProvider, MaterialItemPartProvider {

    private final PartEnglishNameFormatter englishNameFormatter;
    private final PartKey partKey;
    private final Register register;
    private final TextureGenParams textureGenParams;
    private final PartItemPathFormatter itemPathFormatter;

    public PartTemplate(PartEnglishNameFormatter englishNameFormatter, String key) {
        this(englishNameFormatter, key, (partContext, part, itemPath, itemId, itemTag, englishName) -> {
            var item = createSimpleItem(englishName, itemPath, partContext, part);
            setupTag(part, itemTag, item);
        }, new TextureGenParams.SimpleRecoloredItem());
    }

    public PartTemplate(PartEnglishNameFormatter englishNameFormatter, PartKey key) {
        this(englishNameFormatter, key, (partContext, part, itemPath, itemId, itemTag, englishName) -> {
            var item = createSimpleItem(englishName, itemPath, partContext, part);
            setupTag(part, itemTag, item);
        }, new TextureGenParams.SimpleRecoloredItem(), new PartItemPathFormatter.Default());
    }

    public PartTemplate(String englishName, String key) {
        this(new PartEnglishNameFormatter.Default(englishName), key);
    }

    public PartTemplate(String englishName, PartKey key) {
        this(new PartEnglishNameFormatter.Default(englishName), key);
    }

    private PartTemplate(PartEnglishNameFormatter englishNameFormatter, String key, Register register, TextureGenParams textureGenParams) {
        this(englishNameFormatter, new PartKey(key), register, textureGenParams, new PartItemPathFormatter.Default());
    }

    private PartTemplate(PartEnglishNameFormatter englishNameFormatter, PartKey key, Register register, TextureGenParams textureGenParams,
            PartItemPathFormatter itemPathFormatter) {
        this.partKey = key;
        this.englishNameFormatter = englishNameFormatter;
        this.register = register;
        this.textureGenParams = textureGenParams;
        this.itemPathFormatter = itemPathFormatter;
    }

    public static Item createSimpleItem(String englishName, String itemPath, MaterialBuilder.PartContext partContext, PartKey part) {
        return MIItem.item(englishName, itemPath, SortOrder.MATERIALS.and(partContext.getMaterialName()).and(part)).asItem();
    }

    private static void setupTag(PartKey part, String itemTag, Item item) {
        // item tag
        // items whose path are overridden (such as fire clay ingot -> brick) are not
        // added to the tags
        for (PartKey partTagged : MIParts.TAGGED_PARTS) {
            if (partTagged.equals(part)) {
                TagsToGenerate.generateTag(itemTag.replaceFirst("#", ""), item);
            }
        }
    }

    public PartTemplate asBlock(SortOrder sortOrder, TextureGenParams textureGenParams, float hardness, float resistance, int miningLevel) {
        Register blockRegister = (partContext, part, itemPath, itemId, itemTag, itemEnglishName) -> {

            var blockDefinition = MIBlock.block(
                    itemEnglishName, itemPath,
                    MIBlock.BlockDefinitionParams.of()
                            .clearTags()
                            .addMoreTags(TagHelper.getMiningLevelTag(miningLevel))
                            .sortOrder(sortOrder.and(partContext.getMaterialName()))
                            .destroyTime(hardness)
                            .explosionResistance(resistance));

            setupTag(part, itemTag, blockDefinition.asItem());

        };
        return new PartTemplate(englishNameFormatter, partKey, blockRegister, textureGenParams, itemPathFormatter);
    }

    public PartTemplate asColumnBlock(SortOrder sortOrder) {
        Register columnBlockRegister = (partContext, part, itemPath, itemId, itemTag, itemEnglishName) -> {

            var blockDefinition = MIBlock.block(
                    itemEnglishName,
                    itemPath,
                    MIBlock.BlockDefinitionParams.of()
                            .clearTags()
                            .addMoreTags(TagHelper.getMiningLevelTag(1))
                            .sortOrder(sortOrder.and(partContext.getMaterialName()))
                            .withModel(TexturedModel.COLUMN)
                            .destroyTime(5.0f)
                            .explosionResistance(6.0f)

            );

            setupTag(part, itemTag, blockDefinition.asItem());

        };
        return new PartTemplate(englishNameFormatter, partKey, columnBlockRegister, new TextureGenParams.ColumnBlock(), itemPathFormatter);
    }

    public PartTemplate withoutTextureRegister() {
        return this.withTexture(new TextureGenParams.NoTexture());
    }

    public PartTemplate asBlock(SortOrder sortOrder, TextureGenParams textureGenParams) {
        return asBlock(sortOrder, textureGenParams, 5, 6, 1);
    }

    public PartTemplate withRegister(Register register) {
        return new PartTemplate(englishNameFormatter, partKey, register, textureGenParams, itemPathFormatter);
    }

    public PartTemplate withTexture(TextureGenParams textureGenParams) {
        return new PartTemplate(englishNameFormatter, partKey, register, textureGenParams, itemPathFormatter);
    }

    public PartTemplate withOverlay(PartKey normal, String overlay) {
        return withTexture(new TextureGenParams.SimpleRecoloredItem(normal, overlay));
    }

    public PartTemplate withOverlay(PartKeyProvider normal, String overlay) {
        return withOverlay(normal.key(), overlay);
    }

    public PartTemplate withCustomPath(String itemPath, String itemTag) {
        return new PartTemplate(englishNameFormatter, partKey, register, textureGenParams, new PartItemPathFormatter.Overridden(itemPath, itemTag));
    }

    public PartTemplate withCustomPath(String itemPath) {
        return withCustomPath(itemPath, itemPath);
    }

    public TextureGenParams getTextureGenParams() {
        return textureGenParams;
    }

    public UnregisteredMaterialItemPart create(String material, String materialEnglishName) {

        String itemPath = this.itemPathFormatter.getPartItemPath(material, partKey);
        String itemId = this.itemPathFormatter.getPartItemId(material, partKey);
        String itemTag = this.itemPathFormatter.getPartItemTag(material, partKey);
        String itemEnglishName = englishNameFormatter.format(materialEnglishName);

        return build(itemPath, itemId, itemTag, itemEnglishName, partKey, this.register, this.textureGenParams);

    }

    private static UnregisteredMaterialItemPart build(String itemPath, String itemId, String itemTag, String itemEnglishName,
            PartKey part,
            Register register, TextureGenParams textureGenParams) {
        return new UnregisteredMaterialItemPart() {
            @Override
            public PartKey key() {
                return part;
            }

            @Override
            public String getTaggedItemId() {
                return itemTag;
            }

            @Override
            public String getItemId() {
                return itemId;
            }

            @Override
            public void register(MaterialBuilder.PartContext context) {
                register.register(context, part, itemPath, itemId, itemTag, itemEnglishName);
            }

            @Override
            public TextureGenParams getTextureGenParams() {
                return textureGenParams;
            }

            @Override
            public boolean isRegularPart() {
                return true;
            }
        };
    }

    @Override
    public PartKey key() {
        return partKey;
    }

    @FunctionalInterface
    public interface Register {

        void register(MaterialBuilder.PartContext partContext, PartKey part, String itemPath, String itemId, String itemTag, String itemEnglishName);

    }
}

package kurvcygnus.crispsweetberry.common.features.kiln.client.ui;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import kurvcygnus.crispsweetberry.common.features.kiln.data.KilnContainerData;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import kurvcygnus.crispsweetberry.utils.ui.constants.UIConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.kiln.data.KilnContainerData.*;
import static kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils.throwIf;
import static kurvcygnus.crispsweetberry.utils.ui.constants.UIConstants.NO_OFFSET;

/**
 * This the actual user interface for kiln.
 *
 * @author Kurv Cygnus
 * @see KilnMenu Menu(The gate between client and server)
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity BlockEntity
 * @see kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock Block
 * @since 1.0 Release
 */
public final class KilnScreen extends AbstractContainerScreen<KilnMenu>
{
    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 166;
    
    private static final int CAMPFIRE_X_POS = 47;
    private static final int CAMPFIRE_Y_POS = 62;
    private static final int CAMPFIRE_WIDTH = 17;
    private static final int CAMPFIRE_HEIGHT = 16;
    
    private static final int ARROW_X_POS = 79;
    private static final int ARROW_Y_POS = 34;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    
    private static final int TIP_X_POS = 190;
    private static final int TIP_Y_POS = 98;
    private static final int TIP_WIDTH = 8;
    private static final int TIP_HEIGHT = 8;
    
    //! About namespaces: please use "static" to decrease performance penalty.
    private static final ResourceLocation BACKGROUND_TEXTURE = CrispDefUtils.getModNamespacedLocation("textures/gui/kiln/background.png");
    
    private static final ResourceLocation LIT_CAMPFIRE_TEXTURE = CrispDefUtils.getModNamespacedLocation("kiln/lit_campfire");
    private static final ResourceLocation PROGRESS_ARROW_TEXTURE = CrispDefUtils.getModNamespacedLocation("kiln/progress_arrow");
    private static final ResourceLocation BALANCE_DECREASE_ARROW_TEXTURE = CrispDefUtils.getModNamespacedLocation("kiln/balance_decrease_arrow");
    private static final ResourceLocation BALANCE_INCREASE_ARROW_TEXTURE = CrispDefUtils.getModNamespacedLocation("kiln/balance_increase_arrow");
    private static final ResourceLocation TIP_ARROW_TEXTURE = CrispDefUtils.getModNamespacedLocation("kiln/tip_arrow");
    
    private final KilnInfoWidget widget = new KilnInfoWidget(TIP_X_POS, TIP_Y_POS, TIP_WIDTH, TIP_HEIGHT);
    
    public KilnScreen(@NotNull KilnMenu menu, @NotNull Inventory playerInventory, @NotNull Component title) { super(menu, playerInventory, title); }
    
    @Override
    public void init()
    {
        //! The value assignment of image's width and height should be ahead of `super.init()`.
        //! You can see the reason in the source code of that method.
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT;
        
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        
        this.addRenderableWidget(widget);
    }
    
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        final double progress = KilnContainerData.toStandardProgress(menu.data.get(VISUAL_PROGRESS_INDEX));
        final VisualTrend trend = VisualTrend.toEnum(menu.data.get(PROGRESS_TREND_INDEX));
        final boolean isIgnited = KilnContainerData.toStandardIgnitionState(menu.data.get(IGNITION_STATE_INDEX));
        
        if(isIgnited)
            guiGraphics.blitSprite(LIT_CAMPFIRE_TEXTURE, this.leftPos + CAMPFIRE_X_POS, this.topPos + CAMPFIRE_Y_POS, CAMPFIRE_WIDTH, CAMPFIRE_HEIGHT);
        
        //*                  ↓ We should always tip players about recipes that are banned. 
        if(progress > 0D || trend == VisualTrend.TIP)//! Strictly, the layered arrow texture doesn't belong to background stuff.
        {
            final ResourceLocation layeredArrowTexture;
            Tooltip widgetTipText = null;
            
            switch(trend)
            {
                case BALANCE, BURST ->
                {
                    layeredArrowTexture = trend == VisualTrend.BALANCE ? BALANCE_DECREASE_ARROW_TEXTURE : BALANCE_INCREASE_ARROW_TEXTURE;
                    widgetTipText = KilnInfoWidget.COOLDOWN_TIP;
                }
                case TIP ->
                {
                    layeredArrowTexture = TIP_ARROW_TEXTURE;
                    widgetTipText = KilnInfoWidget.BLAST_TIP;
                }
                default -> layeredArrowTexture = PROGRESS_ARROW_TEXTURE;
            }
            
            if(widgetTipText == null)
                widgetTipText = KilnInfoWidget.EMPTY_TIP;
            
            if(this.widget.getTooltip() != widgetTipText)
                this.widget.setTooltip(widgetTipText);
            
            final int clippedWidth = (int) (ARROW_WIDTH * progress);
            
            blitArrow(guiGraphics, layeredArrowTexture, clippedWidth);
        }
        else if(this.widget.getTooltip() != KilnInfoWidget.EMPTY_TIP)
            this.widget.setTooltip(KilnInfoWidget.EMPTY_TIP);
        
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY)
        { guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, NO_OFFSET, NO_OFFSET, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT); }
    
    /**
     * This wrapper method is used to reduce the complexity of
     * <u>{@link GuiGraphics#blitSprite(ResourceLocation, int, int, int, int, int, int, int, int) blitSprite()}</u>.<br><br>
     * <h2><u>{@link GuiGraphics#blitSprite(ResourceLocation, int, int, int, int, int, int, int, int) blitSprite()}</u> Parameter explanation:</h2><ul><h2>
     * <li>{@code uPosition} The horizontal start position of texture clipping.</li>
     * <li>{@code vPosition} The vertical start position of texture clipping.</li>
     * <li>{@code uWidth} The length of horizontal texture clipping.</li>
     * <li>{@code uHeight} The length of vertical texture clipping.</li>
     * </ul></h2><br>
     * <i>If you can't understand these things with comments, it would be better to learn basic computer graphic knowledge first,
     * that's more important than this if you want to do visual-related stuff.</i><br>
     * Just go <a href="https://en.wikipedia.org/wiki/2D_computer_graphics">here</a> if you really can't understand.
     */
    private void blitArrow(@NotNull GuiGraphics gui, @NotNull ResourceLocation sprite, int progressWidth)
    {
        gui.blitSprite(
            sprite,
            ARROW_WIDTH, ARROW_HEIGHT,
            NO_OFFSET, NO_OFFSET,
            this.leftPos + ARROW_X_POS, this.topPos + ARROW_Y_POS,
            progressWidth, ARROW_HEIGHT
        );
    }
    
    /**
     * The widget to inform players the limitation of kiln.
     *
     * @author Kurv Cygnus
     * @since 1.0 Release
     */
    private static final class KilnInfoWidget extends AbstractWidget
    {
        static final Tooltip COOLDOWN_TIP = Tooltip.create(Component.translatable("crispsweetberry.ui.widget.kiln_info_cooldown"));
        static final Tooltip BLAST_TIP = Tooltip.create(Component.translatable("crispsweetberry.ui.widget.kiln_tip_blast"));
        static final Tooltip EMPTY_TIP = Tooltip.create(Component.empty());
        
        private static final int HIGHLIGHT_COLOR = 0x80FFFFFF;
        
        public KilnInfoWidget(int x, int y, int width, int height)
        {
            super(x, y, width, height, Component.literal("?"));
            throwIf(x < 0, () -> new IllegalArgumentException("Param \"x\" should be a positive integer!"));
            throwIf(y < 0, () -> new IllegalArgumentException("Param \"y\" should be a positive integer!"));
            throwIf(width < 0, () -> new IllegalArgumentException("Param \"width\" should be a positive integer!"));
            throwIf(height < 0, () -> new IllegalArgumentException("Param \"height\" should be a positive integer!"));
            this.setTooltip(EMPTY_TIP);
        }
        
        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
        {
            if(this.getTooltip() == EMPTY_TIP)//! No necessary, then no render.
                return;
            
            final int centerX = this.getX() + this.width / 2;
            final int centerY = this.getY() + (this.height - 8) / 2;
            
            guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                this.getMessage(),
                centerX,
                centerY,
                UIConstants.GRAY_COLOR
            );
            
            if(this.isHovered)
            {
                guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    this.getMessage(),
                    centerX,
                    centerY,
                    UIConstants.GOLD_COLOR
                );
                
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, HIGHLIGHT_COLOR);
            }
        }
        
        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) { this.defaultButtonNarrationText(narrationElementOutput); }
    }
}

package kurvcygnus.crispsweetberry.common.config.gui;

import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * This makes the config editable and visually seeable in game.<br>
 * WIP.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see CrispConfig Config
 */
public final class CrispConfigScreen extends Screen
{
    private final @NotNull Screen lastScreen;
    
    public CrispConfigScreen(@NotNull Screen lastScreen)
    {
        super(Component.translatable("crispsweetberry.config.title"));
        this.lastScreen = lastScreen;
    }
    
    @Override
    protected void init()
    {
        final int buttonWidth = 200;
        final int x = this.width / 2 - buttonWidth / 2;
        final int y = 40;
        
        this.addRenderableWidget(CycleButton.onOffBuilder(CrispConfig.KILN_BE_DEBUG.get())
            .create(x, y, buttonWidth, 20, Component.translatable("crispsweetberry.config.debug.kiln_be"),
                (button, value) -> CrispConfig.KILN_BE_DEBUG.set(value)
            )
        );
        
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) ->
            {
                CrispConfig.SPEC.save();
                CrispFunctionalUtils.doIfNonNull(this.minecraft, mc -> mc.setScreen(lastScreen));
            }
        ).bounds(x, this.height - 28, buttonWidth, 20).build());
    }
    
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public void onClose()
    {
        CrispConfig.SPEC.save();
        CrispFunctionalUtils.doIfNonNull(this.minecraft, mc -> mc.setScreen(lastScreen));
    }
}

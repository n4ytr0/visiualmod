package pl.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pl.visuals.VisualsConfig.Visual;

public class VisualsScreen extends Screen {

    public VisualsScreen() {
        super(Text.literal("Visuals"));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 115;
        int y = this.height / 2 - 90;

        for (Visual v : Visual.values()) {
            ButtonWidget toggle = ButtonWidget.builder(toggleText(v), b -> {
                v.enabled = !v.enabled;
                b.setMessage(toggleText(v));
            }).dimensions(left, y, 160, 20).build();
            this.addDrawableChild(toggle);

            if (v.rainbow) {
                ButtonWidget rb = ButtonWidget.builder(Text.literal("Tęcza"), b -> {})
                        .dimensions(left + 165, y, 65, 20).build();
                rb.active = false;
                this.addDrawableChild(rb);
            } else {
                ButtonWidget color = ButtonWidget.builder(colorText(v), b -> {
                    v.nextColor();
                    b.setMessage(colorText(v));
                }).dimensions(left + 165, y, 65, 20).build();
                this.addDrawableChild(color);
            }
            y += 24;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Zamknij"), b -> this.close())
                .dimensions(this.width / 2 - 50, y + 8, 100, 20).build());
    }

    private static Text toggleText(Visual v) {
        Text state = v.enabled
                ? Text.literal("WŁ").formatted(Formatting.GREEN)
                : Text.literal("WYŁ").formatted(Formatting.RED);
        return Text.literal(v.label + ": ").append(state);
    }

    private static Text colorText(Visual v) {
        int rgb = v.color() & 0xFFFFFF;
        return Text.literal(VisualsConfig.COLOR_NAMES[v.colorIndex]).styled(s -> s.withColor(rgb));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Visuals - patrz celownikiem na przeciwnika"),
                this.width / 2, this.height / 2 - 112, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (VisualsMod.getOpenKey().matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        VisualsConfig.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

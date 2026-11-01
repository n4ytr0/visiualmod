package pl.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

public class VisualsMod implements ClientModInitializer {
    /** Przeciwnik, na którego aktualnie patrzy celownik (albo null). */
    public static Entity target;

    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        VisualsConfig.load();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.visualsmod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new VisualsScreen());
                }
            }
        });

        // Szukanie celu co klatkę
        WorldRenderEvents.START.register(ctx -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            float td = mc.getRenderTickCounter().getTickProgress(false);
            target = TargetFinder.find(mc, td);
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(VisualsRenderer::render);
    }

    public static KeyBinding getOpenKey() {
        return openKey;
    }
}

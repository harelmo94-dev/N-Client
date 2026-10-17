package dev.krypton.gui;

import com.google.gson.JsonObject;
import dev.krypton.KryptonClient;
import dev.krypton.module.Category;
import dev.krypton.module.Module;
import dev.krypton.module.modules.ClickGuiModule;
import dev.krypton.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static ClickGuiScreen instance;

    public static boolean isCreated() { return instance != null; }

    public static ClickGuiScreen get() {
        if (instance == null) instance = new ClickGuiScreen();
        return instance;
    }

    public static void open() {
        ClickGuiScreen s = get();
        s.reset();
        MinecraftClient.getInstance().setScreen(s);
    }

    private final List<CategoryWindow> windows = new ArrayList<>();
    private final Animation fade = new Animation(0, 16);
    private boolean closing;
    private String search = "";
    private long lastNs = System.nanoTime();

    private ClickGuiScreen() {
        super(Text.literal("Krypton ClickGUI"));
        for (Category c : Category.values()) {
            CategoryWindow w = new CategoryWindow(c);
            w.applyLayout(KryptonClient.config().gui());
            windows.add(w);
        }
    }

    private void reset() {
        fade.snap(0);
        fade.setTarget(1);
        closing = false;
        search = "";
        lastNs = System.nanoTime();
    }

    public void writeLayout(JsonObject root) { for (CategoryWindow w : windows) w.writeLayout(root); }

    @Override
    protected void init() {
        double x = 10, y = 32;
        for (CategoryWindow w : windows) {
            if (w.isPlaced()) continue;
            if (x + CategoryWindow.W > width - 10) { x = 10; y += 150; }
            w.setPosition(x, y);
            x += CategoryWindow.W + 8;
        }
    }

    private void startClose() { closing = true; fade.setTarget(0); }

    @Override
    public void tick() {
        if (closing && fade.get() < 0.02 && client != null) client.setScreen(null);
    }

    @Override
    public void removed() { KryptonClient.config().save(); }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        double dt = Math.min(0.1, (now - lastNs) / 1_000_000_000.0);
        lastNs = now;
        fade.update(dt);
        float a = (float) Easing.easeOutCubic(fade.get());

        ClickGuiModule cfg = KryptonClient.modules().get(ClickGuiModule.class);
        ctx.fill(0, 0, width, height, Colors.argb(0x000000, cfg.dim.get().floatValue() * a));

        for (CategoryWindow w : windows) w.render(ctx, mx, my, a, search, dt);
        drawSearch(ctx, a);

        if (cfg.tooltips.get() && !closing) {
            Module hovered = null;
            for (int i = windows.size() - 1; i >= 0; i--) {
                CategoryWindow w = windows.get(i);
                if (w.containsPoint(mx, my)) { hovered = w.hoveredModule(mx, my); break; }
            }
            if (hovered != null) {
                String d = hovered.getDescription();
                int tw = Render2D.width(d) + 10;
                int tx = Math.min(mx + 10, width - tw - 4), ty = my + 12;
                Render2D.rect(ctx, tx, ty, tw, 13, 0x0B0B12, a * 0.95f);
                Render2D.rect(ctx, tx, ty, 1.5, 13, Colors.accent(0.3), a);
                Render2D.text(ctx, d, tx + 5, ty + 3, 0xE6E6F0, a);
            }
        }
    }

    private void drawSearch(DrawContext ctx, float a) {
        int w = 190, h = 18, x = (width - w) / 2, y = 8;
        Render2D.rect(ctx, x, y, w, h, 0x0E0E15, a * 0.95f);
        Render2D.gradientBar(ctx, x, y + h - 1.5, w, 1.5, a);
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (search.isEmpty()) Render2D.text(ctx, "Search modules...", x + 6, y + 5, 0x6A6A7C, a);
        else Render2D.text(ctx, search + (blink ? "_" : ""), x + 6, y + 5, 0xFFFFFF, a);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            CategoryWindow w = windows.get(i);
            if (w.mouseClicked(mx, my, button)) {
                windows.remove(i);
                windows.add(w); // bring to front
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        for (CategoryWindow w : windows) w.mouseDragged(mx);
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        for (CategoryWindow w : windows) w.mouseReleased();
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CategoryWindow w : windows) if (w.handleKey(keyCode)) return true;

        int guiKey = KryptonClient.modules().get(ClickGuiModule.class).getKey();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (guiKey != Module.NO_KEY && keyCode == guiKey)) {
            startClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr != 127 && search.length() < 24) {
            search += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}

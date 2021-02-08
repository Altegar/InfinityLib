package com.infinityraider.infinitylib.modules.dynamiccamera;

import com.google.common.collect.ImmutableList;
import com.infinityraider.infinitylib.InfinityLib;
import com.infinityraider.infinitylib.modules.Module;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Module allowing dynamic control of Minecraft's camera.
 */
public class ModuleDynamicCamera extends Module {
    private static final ModuleDynamicCamera INSTANCE = new ModuleDynamicCamera();

    public static ModuleDynamicCamera getInstance() {
        return INSTANCE;
    }

    private ModuleDynamicCamera() {}

    @OnlyIn(Dist.CLIENT)
    public List<Object> getClientEventHandlers() {
        return ImmutableList.of(this);
    }

    public void startObserving(IDynamicCameraController controller) {
        InfinityLib.instance.proxy().startControllingCamera(controller);
    }

    public void stopObserving() {
        InfinityLib.instance.proxy().stopControllingCamera();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.START) {
            return;
        }
        InfinityLib.instance.proxy().tickCamera();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onWorldUnloaded(WorldEvent.Unload event) {
        InfinityLib.instance.proxy().resetCamera();
    }

    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderHand(RenderHandEvent event) {
        if(InfinityLib.instance.proxy().isCameraActive()) {
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(InfinityLib.instance.proxy().isCameraActive()) {
            if(event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
                this.stopObserving();
            }
        }
    }
}

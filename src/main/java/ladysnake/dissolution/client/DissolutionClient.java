package ladysnake.dissolution.client;

import ladysnake.dissolution.api.event.client.HudEvent;
import ladysnake.dissolution.client.gui.hud.PossessionHud;
import ladysnake.dissolution.client.network.ClientMessageHandling;
import ladysnake.dissolution.common.entity.PossessableEntityImpl;
import ladysnake.reflectivefabric.misc.CalledThroughReflection;
import ladysnake.satin.client.event.RenderEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.client.render.EntityRendererRegistry;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

@CalledThroughReflection
public class DissolutionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        RenderEvent.SHADER_EFFECT.register(ShaderHandler.INSTANCE);
        HudEvent.RENDER_HOTBAR.register(PossessionHud.INSTANCE::onRenderHotbar);
        EntityRendererRegistry.INSTANCE.register(PossessableEntityImpl.class, (r, it) -> new BipedEntityRenderer<>(r, new PlayerEntityModel<>(0f, false), .5f));
    }
}

package com.buuz135.simpleclaims.gui.subscreens;

import com.buuz135.simpleclaims.gui.ChunkInfoGui;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class GuiWithParent<T extends GuiWithParent.GuiWithParentData> extends InteractiveCustomUIPage<T> {

    private final InteractiveCustomUIPage<?> parent;

    public GuiWithParent(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime, @NonNullDecl BuilderCodec<T> eventDataCodec, InteractiveCustomUIPage<?> parent) {
        super(playerRef, lifetime, eventDataCodec);
        this.parent = parent;
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of(GuiWithParentData.BACK_INTERACTION, "BackInteraction"));
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl T data) {
        super.handleDataEvent(ref, store, data);
        if (data.backInteraction != null) {
            var player = store.getComponent(ref, Player.getComponentType());
            assert player != null;
            player.getPageManager().openCustomPage(ref, store, parent);
            return;
        }
    }

    public static class GuiWithParentData {
        public static String BACK_INTERACTION = "BackInteraction";

        public String backInteraction;
    }

}

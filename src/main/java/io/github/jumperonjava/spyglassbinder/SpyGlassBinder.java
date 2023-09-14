package io.github.jumperonjava.spyglassbinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpyGlassBinder implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("spyglassbinder");
	public static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {
		var spykb = new KeyBinding("sgbind.bind",-1,"Spyglass Bind");
		var ref = new Object() {
			Boolean pressedPrevTick = false;
		};
		KeyBindingHelper.registerKeyBinding(spykb);
		ClientTickEvents.END_CLIENT_TICK.register(c-> {
			if (!ref.pressedPrevTick && spykb.isPressed()) {
				onKeyDown();
			}
			if (ref.pressedPrevTick && !spykb.isPressed()) {
				onKeyUp();
			}
			ref.pressedPrevTick = spykb.isPressed();

			client.options.useKey.setPressed(spyglassActive);
		});
	}

	public int spyglassSlot=-1;
	public int onPressSlot=-1;
	public boolean spyglassActive = false;

	public void onKeyDown(){
		spyglassSlot = findSpyglass();
		if(spyglassSlot < 9)
			spyglassSlot+=36;
 		onPressSlot= finalSelectedSlot();
		if(spyglassSlot == -1) {
			client.inGameHud.getChatHud().addMessage(Text.translatable("sgbind.nospyglass"));
			return;
		}
		spyglassActive = true;
		if(onPressSlot!=spyglassSlot){
			swap(onPressSlot,spyglassSlot,client);
		}
	}

	public void onKeyUp(){
		spyglassActive = false;
		if(spyglassSlot == -1)
			return;
		if(onPressSlot!=spyglassSlot) {
			swap(onPressSlot,spyglassSlot,client);
		}
	}

	private int findSpyglass(){
		var inv = client.player.getInventory();
		for(int i=0;i<36;i++){
			if(inv.main.get(i).getItem() == Items.SPYGLASS){
				return i;
			}
		}
		return -1;
	}

	private int finalSelectedSlot(){
		var inv = client.player.getInventory();
		return inv.selectedSlot + 36;
	}

	private static void swap(int slot, int slot2, MinecraftClient client) {
		client.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, client.player);
		client.interactionManager.clickSlot(0, slot2, 0, SlotActionType.PICKUP, client.player);
		client.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, client.player);
	}
}
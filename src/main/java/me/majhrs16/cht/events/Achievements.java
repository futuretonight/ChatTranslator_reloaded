package me.majhrs16.cht.events;

import me.majhrs16.cht.translator.ChatTranslatorAPI;
import me.majhrs16.cht.events.custom.Message;
import me.majhrs16.cht.ChatTranslator;
import me.majhrs16.cht.util.util;

import java.lang.reflect.InvocationTargetException;

import me.majhrs16.lib.minecraft.BukkitUtils;

import org.bukkit.plugin.RegisteredListener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class Achievements implements Listener {
	private HandlerList handlerList;
	private final RegisteredListener registeredListener;

	private final ChatTranslator plugin  = ChatTranslator.getInstance();
	private final ChatTranslatorAPI API  = ChatTranslatorAPI.getInstance();

	public Achievements() {
		registeredListener = new RegisteredListener(
				new Listener() {},
				(listener, event) -> onPlayerAchievementAwarded(event),
				EventPriority.MONITOR,
				plugin,
				false
		);

		try {
			Class<?> achievementAwardedEventClass = Class.forName("org.bukkit.event.player.PlayerAchievementAwardedEvent");

			if (achievementAwardedEventClass.isAnnotationPresent(Deprecated.class))
				return;

			handlerList = (HandlerList) achievementAwardedEventClass.getMethod("getHandlerList").invoke(null);
			handlerList.register(registeredListener);

		} catch (IllegalAccessException
				 | NoSuchMethodException
				 | ClassNotFoundException
				 | InvocationTargetException e) {

			plugin.logger.error("Error while registering achievement listener: %s", e);
		}
	}

	private void onPlayerAchievementAwarded(Event event) {
		if (plugin.isDisabled())
			return;

		Player player = ((PlayerEvent) event).getPlayer();

		String name;
		try {
			Enum<?> achievement = (Enum<?>) event.getClass().getMethod("getAchievement").invoke(event);
			if (achievement == null) return;

			name = (String) achievement.getClass().getMethod("getName").invoke(achievement);

		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			plugin.logger.error("Failed to get achievement name from %s: %s", event.getEventName(), e.getMessage());
			handlerList.unregister(registeredListener);
			return;
		}

		Message model = util.createChat(
				player,
				new String[] { toTitleCase(name.split("_")) },
				util.convertStringToLang("en"),
				API.getLang(player),
				"advancement");

		API.broadcast(model, BukkitUtils.getOnlinePlayers(), API::broadcast);
	}

	public static String toTitleCase(String[] words) {
		StringBuilder titleCase = new StringBuilder();
		for (String word : words) {
			if (!word.isEmpty()) {
				titleCase.append(Character.toUpperCase(word.charAt(0)))
					.append(word.toLowerCase().substring(1))
					.append(" ");
			}
		}

		return titleCase.toString().trim();
	}
}
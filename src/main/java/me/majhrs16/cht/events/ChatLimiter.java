package me.majhrs16.cht.events;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

import me.majhrs16.cht.util.cache.SpamTracker;
import me.majhrs16.cht.events.custom.Message;
import me.majhrs16.cht.ChatTranslator;

public class ChatLimiter implements Listener {
	private BukkitTask task;
	private int MAX_TICKS;
	private int MAX_MESSAGES;
	private long ACTUAL_TICKS;

	private static final ChatTranslator plugin = ChatTranslator.getInstance();
	private static final Map<CommandSender, SpamTracker<Message>> spam = new ConcurrentHashMap<>();

	private void processor() {
		if (plugin.isDisabled()) return;

		FileConfiguration config = plugin.config.get();

		for (Map.Entry<CommandSender, SpamTracker<Message>> entry : new ArrayList<>(spam.entrySet())) {
			CommandSender sender         = entry.getKey();
			SpamTracker<Message> tracker = entry.getValue();

			for (Message event : tracker.getChat(event -> event != null && !event._isProcessed())) {
				if (sender instanceof Player && !event.isCancelled()) {
					tracker.setCount(tracker.getCount() + 1);

					plugin.logger.debug("Player: %s, count: %s", event.getSender().getName(), tracker.getCount());

					if (tracker.getCount() >= MAX_MESSAGES) {
						Bukkit.getScheduler().runTaskLater(
							plugin,
							() -> ((Player) sender).kickPlayer(ChatColor.RED + "SPAM!"),
							1L
						);

						spam.remove(sender);
						break;
					}
				}

				event._setProcessed(true);
				Bukkit.getPluginManager().callEvent(event);
			}
		}

		ACTUAL_TICKS += 1L;
		if (ACTUAL_TICKS > MAX_TICKS) {
			ACTUAL_TICKS = 0L;

			MAX_TICKS    = config.getInt("spam.max-ticks");
			MAX_MESSAGES = config.getInt("spam.max-messages");

			AtomicInteger total_spam_trackers    = new AtomicInteger();
			AtomicInteger total_messages_deleted = new AtomicInteger();

			spam.values().stream().parallel().forEach(tracker -> {
				tracker.setCount(0);
				total_spam_trackers.incrementAndGet();
				tracker.getChat().stream().parallel()
					.filter(Objects::nonNull)
					.filter(obj -> !obj._isProcessed())
					.forEach(obj -> {
						total_messages_deleted.incrementAndGet();
						tracker.getChat().remove(obj);
					});
			});

			plugin.logger.debug("Processed %s messages on %s SpamTrackers.", total_messages_deleted, total_spam_trackers);
		}

		clearOfflinePlayers();
	}

	private void clearOfflinePlayers() {
		spam.keySet().stream().parallel()
			.filter(sender -> sender instanceof Player && !((Player) sender).isOnline())
			.forEach(spam::remove);
	}

	public void start() {
		task = Bukkit.getScheduler().runTaskTimer(
			plugin,
			this::processor,
			0L, 1L
		);
	}

	public void stop() {
		if (task != null) task.cancel();
	}

	public static void add(Message from) {
		spam.computeIfAbsent(
			from.getSender() == null ? Bukkit.getConsoleSender() : from.getSender(), // ARREGLAR ESTO!!
			k -> new SpamTracker<>()
		).getChat().add(from);
	}

	public static void clear() {
		spam.clear();
	}

	public static Message get(UUID uuid) {
		Optional<Message> optionalMessage = spam.values().stream().parallel()
			.flatMap(tracker -> tracker.getChat().stream())
			.filter(Objects::nonNull)
			.filter(from -> from.getUUID().equals(uuid))
			.findFirst();

		if (optionalMessage.isPresent()) {
			Message from = optionalMessage.get();
			plugin.logger.debug("Found! %s, senderName: %s", from.getUUID(), from.getSenderName());
			return from;

		} else {
			plugin.logger.debug("Not found! %s", uuid);
			return null;
		}
	}
}
package me.majhrs16.cht.commands.cht;

import me.majhrs16.cht.exceptions.StorageRegisterFailedException;
import me.majhrs16.cht.util.RunnableWithTriException;
import me.majhrs16.cht.translator.ChatTranslatorAPI;
import me.majhrs16.cht.util.updater.CommandsUpdater;
import me.majhrs16.cht.util.updater.FormatsUpdater;
import me.majhrs16.cht.util.updater.ConfigUpdater;
import me.majhrs16.cht.util.cache.internal.Texts;
import me.majhrs16.cht.util.cache.Permissions;
import me.majhrs16.cht.events.custom.Message;
import me.majhrs16.cht.ChatTranslator;

import me.majhrs16.lib.minecraft.commands.CommandExecutor;
import me.majhrs16.lib.exceptions.ParseYamlException;
import me.majhrs16.lib.storages.YAML;

import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class Reloader implements CommandExecutor {
	private final ChatTranslator plugin = ChatTranslator.getInstance();
	private final ChatTranslatorAPI API = ChatTranslatorAPI.getInstance();

	public boolean apply(CommandSender sender, String path, String[] args) {
		if (plugin.isDisabled())
			return false;

		Message from = new Message()
			.setSender(sender)
			.setLangTarget(API.getLang(sender));

		if (!Permissions.ChatTranslator.ADMIN.IF(sender)) {
			API.sendMessage(from.format("commands.cht.errors.noPermission"));
			return true; // Para evitar mostrar el unknown command.
		}

		from.format("commands.cht.reloader");
		API.sendMessage(from);

		plugin.setDisabled(true);

		switch (args.length == 0 ? "all" : args[0].toLowerCase()) {
			case "all":
				reloadAll(from);
				break;

			case "formats":
				reloadFormats(from);
				break;

			case "config":
				reloadConfig(from);
				break;

			case "commands":
				reloadCommands(from);
				break;

			case "signs":
				reloadSigns(from);
				break;

			case "storage":
				reloadStorage(from);
				break;

			default:
				API.sendMessage(from.format("commands.cht.errors.unknown"));
				break;
		}

		plugin.setDisabled(false);

		return true;
	}

	public void reloadAll(Message from) {
		try {
			reloadFormats(from);
			reloadConfig(from);
			reloadCommands(from);
			reloadSigns(from);
			reloadStorage(from);

		} catch (Exception e) {
			plugin.logger.error(e.toString());
			if (Permissions.ChatTranslator.ADMIN.IF(from.getSender()))
				API.sendMessage(from.format("commands.cht.reloader.error.fatal"));
		}
	}

	private void reload(Message from, String text, RunnableWithTriException<SQLException, ParseYamlException, StorageRegisterFailedException> action) {
		try {
			action.run();

			from.format("commands.cht.reloader.done", s -> s
				.replace("%file%", text)
			);

		} catch (SQLException | ParseYamlException | StorageRegisterFailedException e) {
			from.format("commands.cht.reloader.error.file", s -> s
				.replace("%file%", text)
				.replace("%reason%", e.toString())
			);
		}

		API.sendMessage(from);
	}

	public void reloadFormats(Message from) {
		reload(from, "&bformats.yml", () -> {
			YAML yaml = plugin.formats;
			boolean rescue = yaml.isReadonly();

			if (rescue) {
				String folder  = plugin.getDataFolder().getPath();
				yaml = new YAML(folder, "formats.yml");
			}

			yaml.reload();
			plugin.formats = yaml;

			if (rescue) new FormatsUpdater();

			Texts.reload();
		});
	}

	public void reloadConfig(Message from) {
		reload(from, "&bconfig.yml", () -> {
			YAML yaml = plugin.config;
			boolean rescue = yaml.isReadonly();

			if (rescue) {
				String folder  = plugin.getDataFolder().getPath();
				yaml = new YAML(folder, "config.yml");
			}

			yaml.reload();
			plugin.config = yaml;

			if (rescue) new ConfigUpdater();

			plugin.unregisterDiscordBot();
			plugin.registerDiscordBot();
		});
	}

	public void reloadCommands(Message from) {
		reload(from, "&bcommands.yml", () -> {
			YAML yaml = plugin.commands;
			boolean rescue = yaml.isReadonly();

			if (rescue) {
				String folder  = plugin.getDataFolder().getPath();
				yaml = new YAML(folder, "commands.yml");
			}

			yaml.reload();
			plugin.commands = yaml;

			if (rescue) new CommandsUpdater();
		});
	}

	public void reloadSigns(Message from) {
		reload(from, "&bsigns.yml", () -> {
			YAML yaml = plugin.signs;
			boolean rescue = yaml.isReadonly();

			if (rescue) {
				String folder  = plugin.getDataFolder().getPath();
				yaml = new YAML(folder, "signs.yml");
			}

			yaml.reload();
			plugin.signs = yaml;
		});
	}

	public void reloadStorage(Message from) {
		String text;

		switch (plugin.storage.getType()) {
			case "yaml":
				text ="&b" + plugin.config.get().getString("storage.database") + ".yml";
				break;

			case "sqlite":
				text = "&bSQLite";
				break;

			case "mysql":
				text = "&bMySQL";
				break;

			default:
//				En el dado caso que se haya establecido un almacenamiento desconocido y haya pasado el arranque O_o.
				text = "&9???";
				break;
		}

		reload(from, text,  plugin.storage::reload);
	}
}
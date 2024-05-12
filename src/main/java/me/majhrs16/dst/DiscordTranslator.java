package me.majhrs16.dst;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;

import me.majhrs16.dst.events.ReloadInternetLoss;
import me.majhrs16.dst.events.TerminalLogger;
import me.majhrs16.dst.events.DiscordSync;
import me.majhrs16.dst.events.Chat;

import me.majhrs16.cht.util.cache.Config;
import me.majhrs16.cht.ChatTranslator;

public class DiscordTranslator {
	private static JDA jda;

	private final Chat chat = new Chat();
	private final DiscordSync discordSync = new DiscordSync();
	private final TerminalLogger terminalLogger = new TerminalLogger();
	private final ReloadInternetLoss reloadInternetLoss = new ReloadInternetLoss();
	private final me.majhrs16.dst.events.Commands commands = new me.majhrs16.dst.events.Commands();

	public final static String version  = "${dst_version}";

	public JDA connect(String bot_token) throws InvalidTokenException, InterruptedException {
		if (bot_token == null
				|| bot_token.isEmpty()
				|| bot_token.equalsIgnoreCase("<Your Bot Token>"))
			throw new InvalidTokenException();

		JDABuilder builder = JDABuilder.createDefault(bot_token);
/*
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		builder.setChunkingFilter(ChunkingFilter.ALL);
		builder.enableCache(CacheFlag.ROLE_TAGS);
		builder.disableCache(
			CacheFlag.MEMBER_OVERRIDES,
			CacheFlag.CLIENT_STATUS,
			CacheFlag.ONLINE_STATUS,
			CacheFlag.VOICE_STATE,
			CacheFlag.FORUM_TAGS,
			CacheFlag.ACTIVITY,
			CacheFlag.STICKER,
			CacheFlag.EMOJI
		);
*/
		builder.enableIntents(
			GatewayIntent.MESSAGE_CONTENT,
			GatewayIntent.GUILD_PRESENCES,
			GatewayIntent.GUILD_MEMBERS
		);

		return jda = builder.build();
	}

	public void registerEvents() {
		if (jda == null) return;

		jda.addEventListener(commands);
		jda.addEventListener(chat);
		reloadInternetLoss.start();
		terminalLogger.start();

		discordSync.start();
	}

	public void unregisterEvents() {
		if (jda == null) return;

		jda.removeEventListener(commands);
		jda.removeEventListener(chat);
		reloadInternetLoss.stop();
		terminalLogger.stop();
		discordSync.stop();
	}

	public void registerCommands() {
		if (jda == null) return;

		for (Guild guild : jda.getGuilds())
			guild.updateCommands().addCommands(Commands.message("Translate")).queue();
	}

	public void unregisterCommands() {
	}

	public void disconnect() throws InterruptedException {
		if (jda == null) return;

		jda.shutdown();
	}
	
	public static boolean isDisabled() {
		return ChatTranslator.getInstance().isDisabled()
			|| jda == null
			|| !Config.TranslateOthers.DISCORD.IF();
	}

	public static JDA getJDA() {
		return jda;
	}
}

package me.majhrs16.dst.events;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Deprecated
public class JDAListener extends ListenerAdapter {
/*
	private final ChatTranslatorAPI API = ChatTranslatorAPI.getInstance();

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		if (event.getName().equals("Traducir")) {
			event.deferReply(true).queue();

			Message message = event.getTarget();
			Member member = event.getMember();

			me.majhrs16.cht.events.custom.Message DC = new me.majhrs16.cht.events.custom.Message();
				DC.setForceColor(false);

			if (member != null) {
				UUID authorUuid = AccountManager.getMinecraft(message.getAuthor().getId());
				UUID memberUuid = AccountManager.getMinecraft(member.getId());

				if (memberUuid != null) {
					String from_lang = authorUuid == null ? "auto" : API.getLang(AccountManager.getOfflinePlayer(authorUuid));
					String to_lang = API.getLang(AccountManager.getOfflinePlayer(memberUuid));

					if (Objects.equals(from_lang, to_lang))
						from_lang = "auto";

					DC.getMessages().setTexts(message.getContentDisplay());
					DC.setLangSource(from_lang);
					DC.setLangTarget(to_lang);

					event.getHook().sendMessage(String.join("\n", API.formatMessage(DC).getMessages().getFormats())).queue();
					return;
				}
			}

			DC.getMessages().setTexts(
				"Debe vincular su cuenta de Discord con su Minecraft.",
				"    Por favor, use el comando `/cht link`"
			);
			event.getHook().sendMessage(String.join("\n", API.formatMessage(DC).getMessages().getFormats())).queue();
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		String ID = event.getComponentId();

		if (ID.startsWith("translate")) {
			event.deferReply(true).queue();

			String to_lang;
			UUID userUuid = AccountManager.getMinecraft(event.getUser().getId());

			if (userUuid == null)
				to_lang = plugin.storage.getDefaultLang();

			else
				to_lang = API.getLang(Bukkit.getOfflinePlayer(userUuid));

			String from_lang = ID.split("-")[1];

			me.majhrs16.cht.events.custom.Message DC = util.getDataConfigDefault();
				DC.setMessages(event.getMessage().getContentDisplay());
				DC.setLangSource(from_lang);
				DC.setLangTarget(to_lang);
				DC.setColor(false);

			event.getHook().sendMessage(API.formatMessage(DC).getMessages()).queue();
		}
	}
	*/
}
package mames1.net.mamesosu;

import mames1.net.mamesosu.Utils.Modal;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class CommandHandler extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (!e.getName().equals("copy")) {
            return;
        }

        if (e.getUser().isBot()) {
            return;
        }

        if (!e.getChannelType().isGuild()) {
            return;
        }

        if(!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            e.reply("このコマンドを実行する権限がありません！\n" +
                    "管理者である必要があります。").setEphemeral(true).queue();
            return;
        }

        TextInput fromID = Modal.createTextInput("fromID", "コピー元のチャンネルID", "コピー元のチャンネルIDを入力してください", true, TextInputStyle.SHORT);
         TextInput webhook = Modal.createTextInput("webhook", "Webhook URL", "コピー先のWebhook", true, TextInputStyle.SHORT);
        TextInput category = Modal.createTextInput("category", "カテゴリ", "ch/forum", true, TextInputStyle.SHORT);
        TextInput threadID = Modal.createTextInput("threadID", "スレッドID", "Forumにコピーしたい場合だけ！", false, TextInputStyle.SHORT);

        e.replyModal(net.dv8tion.jda.api.interactions.modals.Modal.create(
                "copy_modal", "チャンネルをフォーラム/チャンネルにコピーする"
        ).addActionRows(
                ActionRow.of(fromID),
                ActionRow.of(webhook),
                ActionRow.of(category),
                ActionRow.of(threadID)
                ).build()
        ).queue();
    }
}

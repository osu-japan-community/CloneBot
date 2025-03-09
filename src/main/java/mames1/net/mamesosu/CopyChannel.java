package mames1.net.mamesosu;

import mames1.net.mamesosu.Utils.Webhook;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CopyChannel extends ListenerAdapter {

    RestAction<?> latestUpdate = null;

    @Override
    public void onModalInteraction(ModalInteractionEvent e) {
        if(e.getModalId().equals("copy_modal")) {


            long fromID;

            // チャンネルIDを取得 (検証込み)
            try {
                fromID = Long.parseLong(e.getValue("fromID").getAsString());
            } catch (Exception ex) {
                e.reply("チャンネルIDは数値である必要があります!\n" +
                        "表示されない場合は開発者モードをオンにしてください!").setEphemeral(true).queue();
                return;
            }

            MessageChannel fromChannel = e.getJDA().getTextChannelById(fromID);

            if(fromChannel == null) {
                e.reply("コピー元のチャンネルが見つかりませんでした!\n" +
                        "正しくコピーできていますか？").setEphemeral(true).queue();
                return;
            }

            if(!e.getValue("category").getAsString().equals("forum") && !e.getValue("category").getAsString().equals("ch")) {
                e.reply("コピー先のカテゴリは<ch/forum>のいずれかである必要があります!\n" +
                        "チャンネルにコピーしたい場合はch、フォーラムにコピーしたい場合はforumと入力してください！").setEphemeral(true).queue();
                return;
            }

            if(e.getValue("category").getAsString().equals("forum")) {
                if(e.getValue("threadID").getAsString().isEmpty()) {
                    e.reply("フォーラムにコピーする場合はスレッドIDを入力する必要があります!\n" +
                            "(コピー先のフォーラムを右クリックするとIDを取得できます)").setEphemeral(true).queue();
                    return;
                }
            }

            e.getChannel().sendMessage("コピーを開始しています。\n" +
                    "進捗はこのチャンネルに表示されます。しばらくお待ちください。").queue();

            e.reply("コピーを開始しています。\n" +
                    "進捗はこのチャンネルに表示されます。しばらくお待ちください。").setEphemeral(true).queue();

            Message progressMessage;

            try {
                progressMessage = e.getChannel().sendMessage(getProgressInfo(0)).complete();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            int MAX_DESIRED = 10000;
            List<Message> messages = new ArrayList<>();
            fromChannel.getIterableHistory()
                    .forEachAsync(message -> {
                        messages.add(message);
                        handleProgressUpdate(progressMessage, messages.size());
                        return messages.size() < MAX_DESIRED;
                    }).thenAccept(_ignored -> {
                        Collections.reverse(messages);

                        for (Message message : messages) {
                            try {
                                if (e.getValue("category").getAsString().equals("forum")) {
                                    Webhook.sendAndCopyWebhookMessage(e.getValue("webhook").getAsString(), e.getValue("threadID").getAsString(), message);
                                } else {
                                    Webhook.sendAndCopyWebhookMessage(e.getValue("webhook").getAsString(), null, message);
                                }
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }

                        e.getChannel().sendMessage("コピーが完了しました！\n" +
                                "メッセージを確認してください。").queue();
                    }).exceptionally(error -> {
                        error.printStackTrace();
                        e.getChannel().sendMessage("処理は次のエラーによって停止されました。 " + error.getMessage()).queue();
                        return null;
                    }).whenComplete((_ignored, _ignored2) -> {
                        latestUpdate = null;
                        progressMessage.delete().queue();
                    });

        }
    }

    private void handleProgressUpdate(Message progressMessage, int totalMessages) {
        RestAction<?> action = progressMessage.editMessage(getProgressInfo(totalMessages));
        latestUpdate = action;

        action.setCheck(() -> action == latestUpdate);
        action.submit().whenComplete((_ignored, _ignored2) -> {
            if (latestUpdate == action) {
                latestUpdate = null;
            }
        });
    }

    private String getProgressInfo(int totalMessages) {
        return "現在メッセージを取得しています。\n取得した合計のメッセージ数: " + totalMessages;
    }
}

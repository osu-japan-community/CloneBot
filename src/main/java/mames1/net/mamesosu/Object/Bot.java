package mames1.net.mamesosu.Object;

import io.github.cdimascio.dotenv.Dotenv;
import mames1.net.mamesosu.CommandHandler;
import mames1.net.mamesosu.CopyChannel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {

    String token;
    JDA jda;

    public Bot() {
        Dotenv env = Dotenv.configure().load();
        token = env.get("BOT_TOKEN");
    }

    public void loadJDA() {
        jda = JDABuilder.createDefault(this.token)
                .setRawEventsEnabled(true)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_PRESENCES
                ).enableCache(
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.ACTIVITY,
                        CacheFlag.ROLE_TAGS,
                        CacheFlag.EMOJI
                )
                .disableCache(
                        CacheFlag.STICKER,
                        CacheFlag.SCHEDULED_EVENTS
                ).setActivity(
                        Activity.playing("Loading...")
                ).addEventListeners(
                        new CommandHandler(),
                        new CopyChannel()
                )
                .build();

        jda.updateCommands().queue();

        jda.upsertCommand("copy", "チャンネル全体をコピーします").queue();

        System.out.println("Botの初期化が完了しました。/copyでコピーを開始できます。");
    }
}

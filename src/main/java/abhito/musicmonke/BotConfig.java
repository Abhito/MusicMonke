package abhito.musicmonke;

import abhito.musicmonke.listeners.MusicListener;
import abhito.musicmonke.listeners.PingListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

/**
 * Starts up the bot and contains the config for it.
 *
 * @author Abhinav Singhal
 * @version 1.3.1
 */
@Configuration
public class BotConfig {

    @Autowired
    private PingListener pingListener;

    @Autowired
    private MusicListener musicListener;

    /**
     * Starts the jda bot
     *
     * @return The jda bot
     * @throws LoginException If the bot fails to log in
     */
    @Bean
    @ConfigurationProperties(value = "discord-api")
    public JDA Discordjda() throws LoginException {
        String token = System.getenv("TOKEN");
        JDA jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        jda.addEventListener(pingListener);
        jda.addEventListener(musicListener);
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("play", "Play from current queue")
                        .addOptions(new OptionData(STRING, "search", "The track you want to play"))
                        .setGuildOnly(true),
                Commands.slash("skip", "Skip current song").setGuildOnly(true),
                Commands.slash("skip-all", "Skip all songs in queue").setGuildOnly(true),
                Commands.slash("stop", "Pause current track").setGuildOnly(true),
                Commands.slash("shuffle", "Shuffle all songs in the queue").setGuildOnly(true)
        );
        commands.queue();
        return jda;
    }

}

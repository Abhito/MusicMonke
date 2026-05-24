package abhito.musicmonke;

import abhito.musicmonke.listeners.MusicListener;
import abhito.musicmonke.listeners.PingListener;
import moe.kyokobot.libdave.NativeDaveFactory;
import moe.kyokobot.libdave.jda.LDJDADaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.audio.dave.DaveSessionFactory;
import net.dv8tion.jda.api.interactions.InteractionContextType;
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
        DaveSessionFactory daveSessionFactory = new LDJDADaveSessionFactory(new NativeDaveFactory());
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setAudioModuleConfig(new AudioModuleConfig()
                        .withDaveSessionFactory(daveSessionFactory))
                .build();
        jda.addEventListener(pingListener);
        jda.addEventListener(musicListener);
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("play", "Play from current queue")
                        .addOptions(new OptionData(STRING, "search", "The track you want to play"))
                        .setContexts(InteractionContextType.GUILD),
                Commands.slash("skip", "Skip current song").setContexts(InteractionContextType.GUILD),
                Commands.slash("skip-all", "Skip all songs in queue").setContexts(InteractionContextType.GUILD),
                Commands.slash("stop", "Pause current track").setContexts(InteractionContextType.GUILD),
                Commands.slash("shuffle", "Shuffle all songs in the queue").setContexts(InteractionContextType.GUILD)
        );
        commands.queue();
        return jda;
    }

}

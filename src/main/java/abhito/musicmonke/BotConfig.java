package abhito.musicmonke;

import abhito.musicmonke.listeners.MusicListener;
import abhito.musicmonke.listeners.PingListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

/**
 * Starts up the bot and contains the config for it.
 * @author Abhinav Singhal
 * @version 1.1.1
 */
@Configuration
public class BotConfig {

    @Autowired
    private PingListener pingListener;

    @Autowired
    private MusicListener musicListener;

    /**
     * Starts the jda bot
     * @return The jda bot
     * @throws LoginException If the bot fails to login
     */
    @Bean
    @ConfigurationProperties(value = "discord-api")
    public JDA Discordjda() throws LoginException {
        String token = System.getenv("TOKEN");
        JDA jda = JDABuilder.createDefault(token).build();
        jda.addEventListener(pingListener);
        jda.addEventListener(musicListener);
        return jda;
    }

}

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

@Configuration
public class BotConfig {

    @Autowired
    private PingListener pingListener;

    @Autowired
    private MusicListener musicListener;

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

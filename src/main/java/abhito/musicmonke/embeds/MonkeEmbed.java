package abhito.musicmonke.embeds;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

/**
 * Custom embed object for bot
 * @author Abhinav Singhal
 * @version 1.0
 */
public class MonkeEmbed {

    public EmbedBuilder eb;

    /**
     * Default settings for embed
     */
    public MonkeEmbed(){
        eb = new EmbedBuilder();
        eb.setColor(Color.yellow);
    }

    /**
     * Create a thumbnail from youtube url
     * @param url The youtube url
     */
    public void createThumbnail(String url){
        eb.setThumbnail(url);
    }
}

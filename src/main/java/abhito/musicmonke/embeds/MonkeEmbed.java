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
        String[] uri = url.split("v=", 2);
        String[] second = uri[1].split("&", 2);
        String start = "https://img.youtube.com/vi/";
        start = start + second[0] + "/0.jpg";
        eb.setThumbnail(start);
    }
}

package abhito.musicmonke.embeds;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class MonkeEmbed {

    public EmbedBuilder eb;

    public MonkeEmbed(){
        eb = new EmbedBuilder();
        eb.setColor(Color.yellow);
    }

    public void createThumbnail(String url){
        String[] uri = url.split("v=", 2);
        String[] second = uri[1].split("&", 2);
        String start = "https://img.youtube.com/vi/";
        start = start + second[0] + "/0.jpg";
        eb.setThumbnail(start);
    }
}

package abhito.musicmonke.listeners.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 * This class represents one Server Music Manager
 * @author Abhinav Singhal
 * @version 1.0
 */
public class GuildMusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;

    /**
     * Creates a player and track scheduler
     * @param manager Used to create the player.
     */
    public GuildMusicManager(AudioPlayerManager manager){
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }
}

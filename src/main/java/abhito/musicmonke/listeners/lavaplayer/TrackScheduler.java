package abhito.musicmonke.listeners.lavaplayer;

import abhito.musicmonke.embeds.MonkeEmbed;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final Queue<AudioTrack> queueList;
    private final Queue<MonkeEmbed> monkeList;
    private TextChannel channel;

    /**
     * Creates a scheduler
     * @param player The audio player the scheduler uses
     */
    public TrackScheduler(AudioPlayer player){
        this.player = player;
        this.queueList = new LinkedList<>();
        this.monkeList = new LinkedList<>();
        channel = null;
    }

    /**
     * Add track to queue or player right away if queue is empty
     * @param track The track to add or play
     */
    public void queue(AudioTrack track, MonkeEmbed embed){
        if(!player.startTrack(track,true)) {
            queueList.offer(track);
            monkeList.offer(embed);
        }
        else {
            embed.eb.setAuthor("Now Playing");
            channel.sendMessageEmbeds(embed.eb.build()).queue();
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack(boolean skip){
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        MonkeEmbed embed = monkeList.poll();
        if(embed != null && !skip) {
            embed.eb.setAuthor("Now Playing");
            channel.sendMessageEmbeds(embed.eb.build()).queue();
        }
        player.startTrack(queueList.poll(), false);
    }

    /**
     * Skip every track in queue
     */
    public void skipAllTracks(){
        if(queueList.isEmpty()){
            nextTrack(true);
        }
        else {
            queueList.poll();
            monkeList.poll();
            skipAllTracks();
        }
    }

    /**
     * What to do when a track ends
     * @param player The current player
     * @param track The track that is ending
     * @param endReason The reason the track ended.
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason){
        if(endReason.mayStartNext){
            nextTrack(false);
        }
    }

    /**
     * Change the channel to send messages to
     * @param channel The new channel
     */
    public void updateChannel(TextChannel channel){
        this.channel = channel;
    }
}

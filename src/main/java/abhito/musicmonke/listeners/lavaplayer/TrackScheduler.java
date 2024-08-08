package abhito.musicmonke.listeners.lavaplayer;

import abhito.musicmonke.embeds.MonkeEmbed;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;


import java.util.*;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final List<TrackEmbedPair> trackEmbedPairs;
    private TextChannel channel;

    /**
     * Contains both the audio track and embed object
     */
    private static class TrackEmbedPair {
        final AudioTrack track;
        final MonkeEmbed embed;

        TrackEmbedPair(AudioTrack track, MonkeEmbed embed) {
            this.track = track;
            this.embed = embed;
        }
    }

    /**
     * Creates a scheduler
     * @param player The audio player the scheduler uses
     */
    public TrackScheduler(AudioPlayer player){
        this.player = player;
        this.trackEmbedPairs =new ArrayList<>();
        channel = null;
    }

    /**
     * Add track to queue or player right away if queue is empty
     * @param track The track to add or play
     */
    public void queue(AudioTrack track, MonkeEmbed embed){
        if(!player.startTrack(track,true)) {
            trackEmbedPairs.add(new TrackEmbedPair(track, embed));
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

        if (!trackEmbedPairs.isEmpty()) {
            TrackEmbedPair pair = trackEmbedPairs.remove(0);
            if (pair.embed != null && !skip) {
                pair.embed.eb.setAuthor("Now Playing");
                channel.sendMessageEmbeds(pair.embed.eb.build()).queue();
            }
            player.startTrack(pair.track, false);
        } else {
            player.startTrack(null, false);
        }
    }

    /**
     * Skip every track in queue
     */
    public void skipAllTracks(){
        if (!trackEmbedPairs.isEmpty()) {
            trackEmbedPairs.clear();
        }
        nextTrack(true);
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

    /**
     * Shuffles all tracks in the queue
     */
    public void shuffleTracks(){
        Collections.shuffle(trackEmbedPairs);
    }

    /**
     * @return track list
     */
    public List<TrackEmbedPair> getTracks() {
        return trackEmbedPairs;
    }
}

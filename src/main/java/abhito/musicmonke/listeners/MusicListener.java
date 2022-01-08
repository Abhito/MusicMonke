package abhito.musicmonke.listeners;


import abhito.musicmonke.listeners.lavaplayer.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MusicListener extends ListenerAdapter {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    /**
     * Sets up global variables
     */
    public MusicListener() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        if("!play".equals(command[0]) && command.length == 2){
            loadAndPlay(event, command[1]);
        }
        else if("!play".equals(command[0]) && command.length == 1){
            startPlayer(event.getTextChannel());
        }
        else if("!skip".equals(command[0]) && command.length == 1){
            skipTrack(event.getTextChannel());
        }
        else if("!skip".equals(command[0]) && "all".equals(command[1])){
            skipAllTrack(event.getTextChannel());
        }
        else if("!stop".equals(command[0])){
            stopTrack(event.getTextChannel());
        }

    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event){
        leaveChannel(event.getGuild(), event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event){
        leaveChannel(event.getGuild(), event.getChannelLeft());
    }

    /**
     * Tells the bot to leave voice channel when no one is there
     * @param guild The server where the bot is
     * @param channelLeft The voice channel to check
     */
    private void leaveChannel(Guild guild, AudioChannel channelLeft) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        AudioManager audioManager = channelLeft.getGuild().getAudioManager();
        if(audioManager.isConnected() && channelLeft.getMembers().size() == 1){
            musicManager.scheduler.skipAllTracks();
            audioManager.closeAudioConnection();
        }
    }

    public void loadAndPlay(final MessageReceivedEvent event, final String url){

        TextChannel channel = event.getTextChannel();
        //create musicManager for server or recall it
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                channel.sendMessage("Adding a track " + audioTrack.getInfo().title).queue();

                play(event, musicManager, audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack firstTrack = audioPlaylist.getSelectedTrack();

                if(firstTrack == null){
                    firstTrack = audioPlaylist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist "
                + audioPlaylist.getName() + ")").queue();

                for(AudioTrack track: audioPlaylist.getTracks()) {
                    play(event, musicManager, track);
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + url).queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("Could not play: " + e.getMessage()).queue();
            }
        });
    }

    /**
     * Creates music managers for each server the bot is used in
     * @param guild Which server to create an music manager for
     * @return A guild music Manager for the server
     */
    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild){
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        //if music manager for the server does not exist then create one
        if(musicManager == null){
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    /**
     * Plays a track
     * @param event Where to play the track
     * @param musicManager Which server to play on
     * @param track The Track to play
     */
    private void play(MessageReceivedEvent event, GuildMusicManager musicManager, AudioTrack track){
        AudioChannel channel = event.getMember().getVoiceState().getChannel();
        if(channel == null){
            event.getTextChannel().sendMessage("Your not in a Voice Channel ~nyan.").queue();
            return;
        }
        connectToVoiceChannel(channel, event.getTextChannel().getGuild().getAudioManager());

        musicManager.scheduler.queue(track);
    }

    /**
     * Skip one track
     * @param channel Channel to send a message to
     */
    private void skipTrack(TextChannel channel){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track ~nyan.").queue();
    }

    /**
     * Skip all tracks
     * @param channel Channel to send a message to
     */
    private void skipAllTrack(TextChannel channel){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.skipAllTracks();

        channel.sendMessage("Skipped all tracks ~nyan.").queue();

    }

    private void stopTrack(TextChannel channel){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.setPaused(true);

        channel.sendMessage("Paused the track ~nyan").queue();
    }

    private void startPlayer(TextChannel channel){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        if(musicManager.player.isPaused()) {
            musicManager.player.setPaused(false);
            channel.sendMessage("UnPausing Player ~nyan.").queue();
        }
        else{
            channel.sendMessage("No track mentioned next to command ~nyan.").queue();
        }

    }

    /**
     * Connect to voice channel
     * @param channel The channel to connect to
     * @param audioManager The server to play on
     */
    private static void connectToVoiceChannel(AudioChannel channel, AudioManager audioManager){
        if(!audioManager.isConnected()) {
            audioManager.openAudioConnection(channel);
        }
    }
}
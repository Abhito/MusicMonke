package abhito.musicmonke.listeners;


import abhito.musicmonke.embeds.MonkeEmbed;
import abhito.musicmonke.listeners.lavaplayer.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.*;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.interactions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MusicListener handles all discord operations related to music
 * @author Abhinav Singhal
 * @version 1.0
 */
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
        playerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
    }

    /**
     * Calls methods when commands related to music are used
     * @param event The Message event
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().isBot() || event.getMember() == null) return;
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        if ("!play".equals(command[0]) && command.length == 2) {
            if(isConnected(event.getMember(), event.getTextChannel()))
                loadAndPlay(event.getMember(), event.getAuthor(), event.getTextChannel(),command[1]);
        } else if ("!play".equals(command[0]) && command.length == 1) {
            if(isConnected(event.getMember(), event.getTextChannel()))
                startPlayer(event.getTextChannel(), null);
        } else if ("!skip".equals(command[0]) && command.length == 1) {
            if(isConnected(event.getMember(), event.getTextChannel()))
                skipTrack(event.getTextChannel(), null);
        } else if ("!skip".equals(command[0]) && "all".equals(command[1])) {
            if(isConnected(event.getMember(), event.getTextChannel()))
                skipAllTrack(event.getTextChannel(), null);
        } else if ("!stop".equals(command[0])) {
            if(isConnected(event.getMember(), event.getTextChannel()))
                stopTrack(event.getTextChannel(), null);
        }

    }

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        // Only accept commands from guilds
        if (event.getGuild() == null || event.getMember() == null)
            return;
        event.deferReply(false).queue();
        InteractionHook hook = event.getHook();
        switch (event.getName()){
            case "play":
                if(event.getOption("search") == null){
                    if(isConnected(event.getMember(), event.getTextChannel())) {
                        startPlayer(event.getTextChannel(), hook);
                    }
                }
                else{
                    if(isConnected(event.getMember(), event.getTextChannel())){
                        hook.setEphemeral(true);
                        hook.sendMessage("Adding your track").queue();
                        loadAndPlay(event.getMember(),
                                event.getUser(), event.getTextChannel() ,event.getOption("search").getAsString());
                    }
                }
                break;
            case "skip":
                if(isConnected(event.getMember(), event.getTextChannel())){
                    skipTrack(event.getTextChannel(), hook);
                }
                break;
            case "skip-all":
                if(isConnected(event.getMember(), event.getTextChannel())){
                    skipAllTrack(event.getTextChannel(), hook);
                }
                break;
            case "stop":
                if(isConnected(event.getMember(), event.getTextChannel())){
                    stopTrack(event.getTextChannel(), hook);
                }
                break;
            default:
                hook.setEphemeral(true);
                if(isConnected(event.getMember(), event.getTextChannel()))
                    hook.sendMessage("Something went wrong ~nyan").queue();
                else hook.sendMessage("Your not in a voice channel").queue();
        }
    }

    public void slashPlayHandler(SlashCommandInteractionEvent event){

    }

    /**
     * Tell the bot to leave when someone leaves
     * @param event When someone leaves
     */
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event){
        leaveChannel(event.getGuild(), event.getChannelLeft());
    }

    /**
     * Tell the bot to leave when someone moves to a different channel
     * @param event When someone moves
     */
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event){
        leaveChannel(event.getGuild(), event.getChannelLeft());
    }

    /**
     * Tells the bot to leave voice channel
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

    /**
     * Takes the provided url and trys to play it
     * @param member The person who is connected to voice
     * @param user The person who sent the message in text channel
     * @param channel The text channel used
     * @param url The song to load
     */
    public void loadAndPlay(final Member member, final User user, final TextChannel channel , final String url){
        //create musicManager for server or recall it
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.updateChannel(channel);

        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                MonkeEmbed embeder = EmbedMaker(audioTrack, user);
                channel.sendMessageEmbeds(embeder.eb.build()).queue();

                play(member, channel, musicManager, audioTrack, embeder);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack firstTrack = audioPlaylist.getSelectedTrack();

                if(firstTrack == null){
                    firstTrack = audioPlaylist.getTracks().get(0);
                }

                if(!url.startsWith("ytsearch: ")) {
                    channel.sendMessage("Adding Playlist " + audioPlaylist.getName() +
                            " (first track of playlist is " + firstTrack.getInfo().title + ")").queue();

                    for (AudioTrack track : audioPlaylist.getTracks()) {
                        play(member, channel, musicManager, track, EmbedMaker(track, user));
                    }
                }
                else{
                    trackLoaded(firstTrack);
                }
            }

            @Override
            public void noMatches() {
                if(!url.startsWith("ytsearch: ")) {
                    String search = "ytsearch: " + url;
                    loadAndPlay(member, user, channel, search);
                }
                else {
                    channel.sendMessage("Nothing found on Youtube ~nyan").queue();
                }
            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("Could not play: " + e.getMessage()).queue();
            }
        });
    }

    /**
     * EmbedMaker makes embed messages
     * @param track The audio track being played
     * @param author The person who played the track
     * @return The embedded message
     */
    private MonkeEmbed EmbedMaker(AudioTrack track, User author) {
        MonkeEmbed embeder = new MonkeEmbed();
        embeder.eb.setAuthor("Added Track ");
        embeder.eb.setTitle(track.getInfo().title, track.getInfo().uri);
        embeder.createThumbnail(track.getInfo().uri);
        embeder.eb.setFooter(author.getAsTag(), author.getAvatarUrl());
        return embeder;
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
     * @param member Connect to the members voice channel
     * @param textChannel Which text channel to send message
     * @param musicManager Which server to play on
     * @param track The Track to play
     */
    private void play(Member member, TextChannel textChannel, GuildMusicManager musicManager, AudioTrack track, MonkeEmbed embed){
        AudioChannel channel = member.getVoiceState().getChannel();

        connectToVoiceChannel(channel, textChannel.getGuild().getAudioManager());

        musicManager.scheduler.queue(track, embed);
    }

    /**
     * Skip one track
     * @param channel Channel to send a message to
     */
    private void skipTrack(TextChannel channel, InteractionHook hook){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.updateChannel(channel);
        if(hook == null) {
            channel.sendMessage("Skipped to next track ~nyan.").queue();
        } else hook.sendMessage("Skipped to next track ~nyan.").queue();
        musicManager.scheduler.nextTrack(false);
    }

    /**
     * Skip all tracks
     * @param channel Channel to send a message to
     */
    private void skipAllTrack(TextChannel channel, InteractionHook hook){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.updateChannel(channel);
        musicManager.scheduler.skipAllTracks();

        if(hook == null) {
            channel.sendMessage("Skipped all tracks ~nyan.").queue();
        } else hook.sendMessage("Skipped all tracks ~nyan.").queue();

    }

    /**
     * Pause playing track
     * @param channel Channel to send message to
     */
    private void stopTrack(TextChannel channel, InteractionHook hook){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.updateChannel(channel);
        musicManager.player.setPaused(true);

        if(hook == null) {
            channel.sendMessage("Paused the track ~nyan").queue();
        } else hook.sendMessage("Paused the track ~nyan").queue();
    }

    /**
     * Unpause the player
     * @param channel Channel to send message to
     */
    private void startPlayer(TextChannel channel, InteractionHook hook){
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.updateChannel(channel);

        if (musicManager.player.isPaused()) {
            musicManager.player.setPaused(false);
            if(hook == null) {
                channel.sendMessage("Resuming Player ~nyan.").queue();
            } else hook.sendMessage("Resuming Player ~nyan.").queue();
        }
        else {
            if(hook == null) {
                channel.sendMessage("No track able to be resumed right now. ~nyan.").queue();
            } else hook.sendMessage("No track able to be resumed right now. ~nyan.").queue();
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

    /**
     * Checks whether user is in a voice channel or if bot is already connected
     * @param member The member that triggers the musicListener
     * @param textChannel The channel that triggers the musicListener
     * @return Whether the bot is already connected
     */
    private boolean isConnected(Member member, TextChannel textChannel) {
        AudioChannel channel = member.getVoiceState().getChannel();
        AudioManager audioManager = textChannel.getGuild().getAudioManager();
        AudioChannel channelConnected = audioManager.getConnectedChannel();
        if(channel == null || (audioManager.isConnected() && channelConnected != channel)){
            textChannel.sendMessage("Your not in a Voice Channel ~nyan.").queue();
            return false;
        }
        else return true;
    }
}
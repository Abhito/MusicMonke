package abhito.musicmonke.listeners;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * Handles random commands that don't belong in other listeners
 * @author Abhinav Singhal
 * @version 1.0
 */
@Component
public class PingListener extends ListenerAdapter {

    /**
     * Performs actions based on commands
     * @param event The message event
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().isBot()) return; //if bot end event

        String content = event.getMessage().getContentRaw();

        if(content.equals("!ping")){
            MessageChannel channel = event.getChannel();
            long time = System.currentTimeMillis();
            channel.sendMessage("Pong!") /* => RestAction<Message> */
                    .queue(response /* => Message */-> {
                response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
            });
        }
        else if(content.equals("!creator")){
            MessageChannel channel = event.getChannel();
            channel.sendMessage("The person who made is called Abhito. Who knows where they are now. ~nyan").queue();
        }
    }
}

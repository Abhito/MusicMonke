package abhito.musicmonke.listeners;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class PingListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().isBot()) return; //if bot end event

        String content = event.getMessage().getContentRaw();

        if(content.equals("!ping")){
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue();
        }
    }
}

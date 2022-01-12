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

        else if(content.equals("!invite")){
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Invite me to your with " +
                    "https://discord.com/oauth2/authorize?client_id=924763309507506206&scope=bot&permissions=420977564737"
            ).queue();
        }
        else if(content.equals("!creator")){
            MessageChannel channel = event.getChannel();
            channel.sendMessage("The person who made is called Abhito. Who knows where they are now. ~nyan").queue();
        }
    }
}

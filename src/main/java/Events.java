import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import net.kyori.adventure.text.Component;

public class Events implements Listener {

	private static final Component motd = Text.plain("the leather club");

	@EventHandler
	private void serverPing(final ServerListPingEvent e) {
		try {
			final var i = Plugin.instance.getServer().loadServerIcon(Random.image(64, 64));
			e.setServerIcon(i);
		} catch (final Exception i) {
			i.printStackTrace();
		}
		e.motd(Events.motd);
	}
}

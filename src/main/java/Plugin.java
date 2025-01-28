import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {
	public static JavaPlugin instance;

	public Plugin() {
		super();
	}

	@Override
	public void onLoad() {
		super.onLoad();
		Plugin.instance = this;
	}

	@Override
	public void onEnable() {
		super.onEnable();

		new Event();
		new Command();
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}
}

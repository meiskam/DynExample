/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.shininet.bukkit.dynexample;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

/**
 * @author meiskam
 */

public class DynamicCommand {

	private static CommandMap commandMap = null;
	private static Constructor<PluginCommand> constructor = null;
	public static final boolean enabled;

	private DynamicCommand() {}
	
	static {
		boolean cont = true;
		try {
			Field field = SimplePluginManager.class.getDeclaredField("commandMap");
			field.setAccessible(true);
			commandMap = (CommandMap)(field.get(Bukkit.getServer().getPluginManager()));
		} catch(Exception e) {
			e.printStackTrace();
			cont = false;
		}
		if (cont) {
			try {
				constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
				constructor.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				cont = false;
			}
		}
		enabled = cont;
	}

	public static PluginCommand getCommand(Plugin plugin, String name) {
		return getCommand(plugin, name, null);
	}
	
	public static PluginCommand getCommand(Plugin plugin, String name, List<String> aliases) {
		return getCommand(plugin, name, aliases, false);
	}
	
	public static PluginCommand getCommand(Plugin plugin, String name, List<String> aliases, boolean steal) {
		if (enabled) {
			Command command = null;
			if (steal) {
				command = commandMap.getCommand(name);
				if ((command != null) && (command instanceof PluginCommand)) {
					((PluginCommand)command).setExecutor(plugin);
					return (PluginCommand)command;
				}
			}
			try {
				command = constructor.newInstance(name, plugin);
				if (aliases != null) {
					command.setAliases(aliases);
				}
				commandMap.register("_", command);
				return (PluginCommand)command;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static boolean unregister(Command command) {
		if (enabled) {
			return command.unregister(commandMap);
		} else {
			return false;
		}
	}
}

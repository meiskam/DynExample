/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.shininet.bukkit.dynexample;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

/**
 * @author meiskam
 */

@SuppressWarnings("unchecked")
public class DynamicCommand {

	private static SimpleCommandMap commandMap;
	private static Map<String, Command> knownCommands;
	private static Set<String> aliases;
	private static Constructor<PluginCommand> constructor;
	public static final boolean enabled;

	private DynamicCommand() {}
	
	static {
		boolean cont = true;
		try {
			Field fieldCommandMap = SimplePluginManager.class.getDeclaredField("commandMap");
			fieldCommandMap.setAccessible(true);
			commandMap = (SimpleCommandMap)(fieldCommandMap.get(Bukkit.getServer().getPluginManager()));
			
			Field fieldKnownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
			fieldKnownCommands.setAccessible(true);
			knownCommands = (Map<String, Command>) fieldKnownCommands.get(commandMap);
			
			Field fieldAliases = SimpleCommandMap.class.getDeclaredField("aliases");
			fieldAliases.setAccessible(true);
			aliases = (Set<String>) fieldAliases.get(commandMap);
			
			constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constructor.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
			cont = false;
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
				commandMap.register("", command);
				return (PluginCommand)command;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Set<String> unregister(Command command) {
		if (enabled) {
			Set<String> output = new HashSet<String>();
			
			for (Iterator<String> it = knownCommands.keySet().iterator(); it.hasNext(); ) {
				String key = it.next();
				Command value = knownCommands.get(key);
				if (value.equals(command)) {
					it.remove();
					aliases.remove(key);
					output.add(key);
				}
			}
			
			if ((command.unregister(commandMap)) && (output.size() > 0)) {
				return output;
			}
		}
		return null;
	}
}

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.shininet.bukkit.dynexample;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author meiskam
 */

public class DynExample extends JavaPlugin {
	
	private HashMap<String, PluginCommand> map = new HashMap<String, PluginCommand>();

	@Override
	public void onEnable(){
		register(DynamicCommand.getCommand(this, "register"));
		register(DynamicCommand.getCommand(this, "unregister"));
		
		PluginCommand foobarCommand = DynamicCommand.getCommand(this, "foobar", Arrays.asList("beefcake", "deadbabe"));
		register(foobarCommand);
		foobarCommand.setExecutor(new DynExampleCommandExecutor());
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("register")) {
			String name;
			if (args.length == 1) {
				name = register(DynamicCommand.getCommand(this, args[0]));
			} else if (args.length > 1) {
				name = register(DynamicCommand.getCommand(this, args[0], Arrays.asList(args)));
			} else {
				sender.sendMessage("Syntax: /register <commandName> [alias ...]");
				return true;
			}
			if (name != null) {
				sender.sendMessage("Successfully registered as: "+name);
				return true;
			} else {
				sender.sendMessage("Could not registered: "+args[0]);
				return true;
			}			
		} else if (cmd.getName().equalsIgnoreCase("unregister")) {
			if (args.length == 1) {
				Set<String> unreg = unregister(args[0]);
				if (unreg != null) {
					sender.sendMessage("Successfully unregistered: "+implode(unreg, ", "));
					return true;
				} else {
					sender.sendMessage("Could not unregister: "+args[0]);
					return true;
				}
			} else {
				sender.sendMessage("Syntax: /unregister <commandName>");
				return true;
			}
		} else {
			sender.sendMessage("[DynExample] You used command: "+cmd.getLabel()+" ("+commandLabel+")"+((args.length>0)?": "+implode(args, " "):""));
			return true;
		}
	}
	
	private String register(PluginCommand command) {
		if (command != null) {
			map.put(command.getLabel(), command);
			return command.getLabel();
		}
		return null;
	}
	
	private Set<String> unregister(String name) {
		String nameLC = name.toLowerCase();
		Set<String> output = null;
		if (map.containsKey(nameLC)) {
			PluginCommand command = map.get(nameLC);
			output = DynamicCommand.unregister(command);
			if (output != null) {
				map.remove(nameLC);
				return output;
			}
		}
		return null;
	}
	
	public static String implode(String[] input, String glue) {
		int i = 0;
		StringBuilder output = new StringBuilder();
		for (String key : input) {
			if (i++ != 0) output.append(glue);
			output.append(key);
		}
		return output.toString();
	}
	
	public static String implode(Iterable<String> input, String glue) {
		int i = 0;
		StringBuilder output = new StringBuilder();
		for (String key : input) {
			if (i++ != 0) output.append(glue);
			output.append(key);
		}
		return output.toString();
	}
}

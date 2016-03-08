package com.kookykraftmc.kookybungee.command.commands;

import com.google.common.collect.ImmutableList;
import com.kookykraftmc.kookybungee.KookyBungee;
import com.kookykraftmc.kookybungee.command.CommandException;
import com.kookykraftmc.kookybungee.command.ICommand;
import com.kookykraftmc.kookybungee.command.SimpleCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class HelpCommand extends SimpleCommand {

    public HelpCommand() {
        super("help", null, "/help", "?", "h", "helpme", "helpop");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        ImmutableList.Builder<BaseComponent> componentBuilder = new ImmutableList.Builder<>();
        TextComponent helptitle = new TextComponent("Help: ");
        helptitle.setColor(ChatColor.GOLD);
        helptitle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "All the commands you will need!")));
        componentBuilder.add(helptitle);
        for (Command c : KookyBungee.getInstance().getPlugman().getCommands(KookyBungee.getInstance().getPlugin())) {
            if (c instanceof ICommand) {
                ICommand iCommand = (ICommand) c;
                if (iCommand.getIPermission() == null || sender.hasPermission(iCommand.getIPermission())) {
                    TextComponent component = new TextComponent("\n - " + iCommand.getUsage());
                    component.setColor(ChatColor.GOLD);
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, iCommand.getUsage()));
                    String command = iCommand.getName();
                    for (String alias : iCommand.getAliases()) {
                        command += ", " + alias;
                    }
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + command)));
                    componentBuilder.add(component);
                }
            }
        }
        return componentBuilder.build().toArray(new BaseComponent[0]);
    }

}

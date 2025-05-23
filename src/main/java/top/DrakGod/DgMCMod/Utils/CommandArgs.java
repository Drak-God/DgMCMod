package top.DrakGod.DgMCMod.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandArgs {
    public CommandSender Sender;
    public Command Command;
    public String Label;
    public String[] Args;

    public CommandArgs(CommandSender Sender, Command Command, String Label, String[] Args) {
        this.Sender = Sender;
        this.Command = Command;
        this.Label = Label; 
        this.Args = Args;
    }
}
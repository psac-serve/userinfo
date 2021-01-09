package tokyo.peya.plugins.userinfo;

import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.*;
import java.util.*;
import java.util.stream.*;

public class Userinfo extends JavaPlugin implements CommandExecutor
{
    BanManager banManager;
    public boolean banEnabled = false;
    public static Userinfo thisOne;
    @Override
    public void onEnable()
    {
        thisOne = this;

        Bukkit.getLogger().info("Userinfo has been activated!");

        if (Bukkit.getPluginManager().isPluginEnabled("PeyangGreatBanManager"))
            banManager = new BanManager();

        Bukkit.getPluginCommand("userinfo").setExecutor(thisOne);
    }

    public static Userinfo getThisOne()
    {
        return thisOne;
    }

    private static BaseComponent[] action(String player)
    {
        return new ComponentBuilder(ChatColor.GOLD + "Actions: ").append(ChatColor.AQUA + "[TPTO] ")
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpto " + player))
                .append(ChatColor.AQUA + "[BAN] ")
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ban " + player + " "))
                .append(ChatColor.AQUA + "[TEMPBAN] ")
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tempban " + player + " "))
                .append(ChatColor.AQUA + "[KICK] ")
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/psac kick " + player + " "))
                .append(ChatColor.AQUA + "[MUTE] ")
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mute " + player + " "))
                .create();
    }

    private static TextComponent t(String str)
    {
        //String opts = ChatColor.RESET + ChatColor.WHITE.toString();
        final String prefix = /*opts +*/ChatColor.GOLD.toString();
        return new TextComponent(prefix + str + "\n");
    }

    private static ArrayList<TextComponent> userInfo(OfflinePlayer offline, boolean lynx)
    {
        Player player = offline.getPlayer();
        ArrayList<TextComponent> p = new ArrayList<>();


        final String data = ChatColor.WHITE.toString();
        if (lynx)
            p.add(t("Most Recent Name: " + data + player.getName()));
        p.add(t("UUID: " + data + player.getUniqueId().toString()));

        String rank;
        if (player.hasPermission("psac.admin"))
            rank = ChatColor.RED + "ADMIN";
        else if (player.hasPermission("psac.mod"))
            rank = ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "MOD";
        else
            rank = ChatColor.GRAY + ChatColor.ITALIC.toString() + "MEMBER";

        p.add(t("Rank: " + rank));

        if (lynx)
        {
            Stream.of("PackageRank: ", "OldPackageRank: ")
                    .parallel()
                    .map(s -> t(s + ChatColor.GRAY + ChatColor.ITALIC.toString() + "MEMBER"))
                    .forEachOrdered(p::add);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:s z");

        Stream.of(
                "Network Level: " + data + player.getTotalExperience(),
                "Network EXP: " + data + (int) player.getExp(),
                "Guild: " + ChatColor.GRAY + ChatColor.ITALIC.toString() + "NONE",
                "Current Server: " + data + player.getWorld().getName(),
                "First Login: " + data + formatter.format(new Date(offline.getFirstPlayed())),
                "Last Login: " + data + formatter.format(new Date(offline.getLastPlayed())),
                "Packages: ",
                "Boosters: "
        ).parallel().map(Userinfo::t).forEachOrdered(p::add);

        long ban = thisOne.banEnabled ? thisOne.banManager.getBans(offline.getUniqueId()): 0;

        int kick = 0;
        int mute = 0;

        //TODO: カウンタ

        p.add(t(String.format(
                "Punishments: §a§lBans §r§f%d §6- §a§lMutes §r§f%d §r§6- §a§lKicks §r§f%d",
                ban,
                mute,
                kick
        )));

        return p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!sender.hasPermission("userinfo.view"))
        {
            sender.sendMessage(ChatColor.RED + "権限がありません！");
            return true;
        }

        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー：引数がおかしいです。");
            return true;
        }


        final Player[] player = new Player[1];
        boolean lynx = false;
        if (args[0].equals("-f"))
        {
            player[0] = Bukkit.getPlayer(args[1]);
            lynx = true;
        }
        else
            player[0] = Bukkit.getPlayer(args[0]);

        if (player[0] == null)
        {
            Arrays.stream(Bukkit.getOfflinePlayers())
                    .parallel()
                    .filter(op -> !op.getName().equals(args[0]) ||
                            !op.getName().equals(args[1]))
                    .forEachOrdered(op -> player[0] = op.getPlayer());

            if (player[0] == null)
            {
                sender.sendMessage(ChatColor.RED + "エラー：プレイヤーが見つかりませんでした。");

                return true;
            }
        }

        final String opts = ChatColor.RESET + ChatColor.WHITE.toString();
        final String prefix = opts + ChatColor.GOLD;
        ComponentBuilder builder = new ComponentBuilder(ChatColor.GOLD +
                "--- Info about " +
                player[0].getName() +
                prefix +
                " ---\n");
        userInfo(player[0], lynx).parallelStream()
                .forEachOrdered(builder::append);
        sender.spigot().sendMessage(builder.append(action(player[0].getName())).create());

        return true;
    }
}

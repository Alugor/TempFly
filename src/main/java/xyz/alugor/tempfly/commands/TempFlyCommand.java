package xyz.alugor.tempfly.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.alugor.tempfly.TempFlyPlugin;
import xyz.alugor.tempfly.database.entity.TempFly;
import xyz.alugor.tempfly.database.service.TempFlyService;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempFlyCommand implements CommandExecutor {
    public TempFlyCommand(TempFlyPlugin plugin) {
        Objects.requireNonNull(plugin.getCommand("tempfly")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0 && sender instanceof Player player) {
            UUID uuid = player.getUniqueId();

            handleFlight(sender, uuid, player);
        } else if (args.length == 1) {
            if (!sender.hasPermission("tempfly.infinite")) {
                sender.sendMessage("Dazu hast du keine Berechtigung.");
                return false;
            }

            String name = args[0];
            Player player = Bukkit.getPlayer(name);

            if (player == null) {
                sender.sendMessage("Der Spieler ist nicht online!");
                return false;
            }

            UUID uuid = player.getUniqueId();
            handleFlight(sender, uuid, player);
        } else if (args.length == 4) {
            if (!sender.hasPermission("tempfly.infinite")) {
                sender.sendMessage("Dazu hast du keine Berechtigung.");
                return false;
            }

            String sub = args[0];

            String name = args[1];
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            if (!player.hasPlayedBefore()) {
                sender.sendMessage("Der Spieler war noch nie hier.");
            }
            UUID uuid = player.getUniqueId();

            TimeUnit timeUnit = convertToTimeUnit(args[3]);
            if (timeUnit == null) {
                sender.sendMessage("Du hast eine ungültige Zeitform angegeben.");
                return false;
            }

            try {
                long time = convertTime(Math.abs(Long.parseLong(args[2])), timeUnit);
                TempFlyService service = TempFlyPlugin.getService();

                switch (sub.toLowerCase()) {
                    case "set":
                        service.createIfNotExists(uuid);
                        service.getTempFlyByUUID(uuid).ifPresent(tempFly -> {
                            tempFly.setDuration(time);
                            service.saveTempFly(tempFly);
                            sender.sendMessage(String.format("Die Fly-Time Dauer wurde für %s verändert.", player.getName()));
                        });
                        break;
                    case "add":
                        service.createIfNotExists(uuid);
                        service.getTempFlyByUUID(uuid).ifPresent(tempFly -> {
                            tempFly.setDuration(tempFly.getDuration() + time);
                            service.saveTempFly(tempFly);
                            sender.sendMessage(String.format("Die Fly-Time Dauer wurde für %s verändert.", player.getName()));
                        });
                        break;
                    case "remove":
                        service.createIfNotExists(uuid);
                        service.getTempFlyByUUID(uuid).ifPresent(tempFly -> {
                            tempFly.setDuration(Math.abs(tempFly.getDuration() - time));
                            service.saveTempFly(tempFly);
                            sender.sendMessage(String.format("Die Fly-Time Dauer wurde für %s verändert.", player.getName()));
                        });
                        break;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Du hast eine ungültige Zahl angegeben.");
            }
        }
        return true;
    }

    private long convertTime(long time, TimeUnit unit) {
        return unit.toSeconds(Math.abs(time));
    }

    private TimeUnit convertToTimeUnit(String timeUnit) {
        try {
            return TimeUnit.valueOf(timeUnit.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void handleFlight(CommandSender sender, UUID uuid, Player player) {
        TempFlyService service = TempFlyPlugin.getService();

        service.createIfNotExists(uuid);
        service.getTempFlyByUUID(uuid).ifPresentOrElse(tempFly -> {
            if (tempFly.getStatus()) {
                disableFlight(player, tempFly);
                service.saveTempFly(tempFly);

                if (!sender.getName().equals(player.getName())) {
                    sender.sendMessage(String.format("Fly-Time für %s wurde deaktiviert", player.getName()));
                }
            } else {
                if (tempFly.getDuration() > 0) {
                    enableFlight(player, tempFly);
                    service.saveTempFly(tempFly);

                    if (!sender.getName().equals(player.getName())) {
                        sender.sendMessage(String.format("Fly-Time für %s wurde aktiviert", player.getName()));
                    }
                } else {
                    if (sender.getName().equals(player.getName())) {
                        sender.sendMessage("Du hast nicht genügend Fly-Time.");
                    } else {
                        sender.sendMessage("Der Spieler hat nicht genügend Fly-Time.");
                    }
                }
            }
        }, () -> sender.sendMessage("Es konnte kein Eintrag in der Datenbank gefunden werden."));
    }

    private void enableFlight(Player player, TempFly tempFly) {
        player.sendMessage("Dein Flugmodus wurde aktiviert.");
        player.setAllowFlight(true);
        player.setFlying(true);
        tempFly.setStatus(true);
    }

    private void disableFlight(Player player, TempFly tempFly) {
        player.sendMessage("Dein Flugmodus wurde deaktiviert.");
        player.setAllowFlight(false);
        player.setFlying(false);
        tempFly.setStatus(false);
    }
}
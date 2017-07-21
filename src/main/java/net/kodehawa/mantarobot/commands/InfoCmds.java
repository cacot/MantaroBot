package net.kodehawa.mantarobot.commands;

import com.google.common.eventbus.Subscribe;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantarobot.db.entities.GuildData;
import net.kodehawa.mantarobot.MantaroBot;
import net.kodehawa.mantarobot.MantaroInfo;
import net.kodehawa.mantarobot.commands.currency.TextChannelGround;
import net.kodehawa.mantarobot.commands.info.CommandStatsManager;
import net.kodehawa.mantarobot.commands.info.GuildStatsManager;
import net.kodehawa.mantarobot.core.CommandProcessorAndRegistry;
import net.kodehawa.mantarobot.core.listeners.command.CommandListener;
import net.kodehawa.mantarobot.data.MantaroData;
import net.kodehawa.mantarobot.db.entities.helpers.ExtraGuildData;
import net.kodehawa.mantarobot.modules.CommandRegistry;
import net.kodehawa.mantarobot.modules.Module;
import net.kodehawa.mantarobot.modules.PostLoadEvent;
import net.kodehawa.mantarobot.modules.commands.CommandPermission;
import net.kodehawa.mantarobot.modules.commands.SimpleCommand;
import net.kodehawa.mantarobot.modules.commands.base.Category;
import net.kodehawa.mantarobot.modules.commands.base.Command;
import net.kodehawa.mantarobot.utils.Utils;
import net.kodehawa.mantarobot.utils.commands.EmoteReference;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static net.kodehawa.mantarobot.commands.info.AsyncInfoMonitor.*;
import static net.kodehawa.mantarobot.commands.info.HelpUtils.forType;
import static net.kodehawa.mantarobot.commands.info.StatsHelper.calculateDouble;
import static net.kodehawa.mantarobot.commands.info.StatsHelper.calculateInt;

@Module
public class InfoCmds {

    @Subscribe
    public static void about(CommandRegistry cr) {
        cr.register("about", new SimpleCommand(Category.INFO) {
            @Override
            protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
                if (!content.isEmpty() && args[0].equals("patreon")) {
                    EmbedBuilder builder = new EmbedBuilder();
                    Guild mantaroGuild = MantaroBot.getInstance().getGuildById("213468583252983809");
                    String donators = mantaroGuild.getMembers().stream().filter(member -> member.getRoles().stream().filter(role ->
                        role.getName().equals("Patron")).collect(Collectors.toList()).size() > 0).map(member ->
                        String.format("%s#%s", member.getUser().getName(), member.getUser().getDiscriminator()))
                        .collect(Collectors.joining(", "));
                    builder.setAuthor("Our Patreon supporters", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                        .setDescription(donators)
                        .setColor(Color.PINK)
                        //<3
                        .addField("Special Mentions",
                                "**MrLar#8117** $100 pledge. <3 + $1025 donation. <3\n" +
                                "**Quartermaster#1262** $40 pledge <3",false)
                        .setFooter("Much thanks for helping make Mantaro better!", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    event.getChannel().sendMessage(builder.build()).queue();
                    return;
                }

                if (!content.isEmpty() && args[0].equals("credits")) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setAuthor("Credits.", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                        .setColor(Color.BLUE)
                        .setDescription("**Main developer**: Kodehawa#3457\n"
                            + "**Developer**: AdrianTodt#0722\n"
                                + "**Developer**: Natan#1289\n"
                                + "**Music**: Steven#6340 (Retired :<)\n"
                                + "**Meme guy**: Adam#9261\n"
                                + "**Documentation:** MrLar#8117 & Yuvira#7832")
                        .addField("Special mentions",
                            "Thanks to bots.discord.pw, Carbonitex and discordbots.org for helping us with increasing the bot's visibility.", false)
                        .setFooter("Much thanks to everyone above for helping make Mantaro better!", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    event.getChannel().sendMessage(builder.build()).queue();
                    return;
                }

                List<Guild> guilds = MantaroBot.getInstance().getGuilds();
                List<TextChannel> textChannels = MantaroBot.getInstance().getTextChannels();
                List<VoiceChannel> voiceChannels = MantaroBot.getInstance().getVoiceChannels();
                long millis = ManagementFactory.getRuntimeMXBean().getUptime();
                long seconds = millis / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                String madeBy = "Bot made by: " + MantaroData.config().get().getOwners().stream()
                    .map(MantaroBot.getInstance()::getUserById)
                    .filter(Objects::nonNull)
                    .map(user -> event.getGuild().isMember(user) ? user.getAsMention() : user.getName() + "#" + user.getDiscriminator())
                    .collect(Collectors.joining(", "));

                if (madeBy.contains("<@")) madeBy += " (say hi to them!)";

                event.getChannel().sendMessage(new EmbedBuilder()
                    .setColor(Color.PINK)
                    .setAuthor("About Mantaro", "http://polr.me/mantaro", "https://puu.sh/suxQf/e7625cd3cd.png")
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setDescription("Hello, I'm **MantaroBot**! I'm here to make your life a little easier. To get started, type `~>help`!\n" +
                        "Some of my features include:\n" +
                        "\u2713 **Moderation made easy** (``Mass kick/ban, prune commands, logs and more!``)\n" +
                        "\u2713 **Funny and useful commands**, see `~>help anime` or `~>help hug` for examples.\n" +
                        "\u2713 **[Extensive support](https://discordapp.com/invite/cMTmuPa)! |" +
                        " [Support Mantaro development!](https://www.patreon.com/mantaro)**\n\n" +
                        EmoteReference.POPPER + madeBy + "\n" + "Check ~>about credits!" + (MantaroData.config().get().isPremiumBot() ? "\nRunning a Patreon Bot instance, thanks you for your support! \u2764" : "")
                    )
                    .addField("MantaroBot Version", MantaroInfo.VERSION, false)
                    .addField("Uptime", String.format(
                        "%d days, %02d hrs, %02d min",
                        days, hours % 24, minutes % 60
                    ), false)
                    .addField("Shards", String.valueOf(MantaroBot.getInstance().getShardedMantaro().getTotalShards()), true)
                    .addField("Threads", String.valueOf(Thread.activeCount()), true)
                    .addField("Servers", String.valueOf(guilds.size()), true)
                    .addField("Users (Online/Unique)", guilds.stream().flatMap
                        (g -> g.getMembers().stream()).filter(u -> !u.getOnlineStatus().equals(OnlineStatus.OFFLINE)).distinct().count() + "/" +
                        guilds.stream().flatMap(guild -> guild.getMembers().stream()).map(user -> user.getUser().getId()).distinct().count(), true)
                    .addField("Text Channels", String.valueOf(textChannels.size()), true)
                    .addField("Voice Channels", String.valueOf(voiceChannels.size()), true)
                    .setFooter(String.format("Invite link: http://polr.me/mantaro (Commands this session: %s | Current shard: %d)", CommandProcessorAndRegistry.getCommandTotal(), MantaroBot.getInstance().getShardForGuild(event.getGuild().getId()).getId() + 1), event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .build()
                ).queue();
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "About Command")
                    .setDescription("**Read info about Mantaro!**")
                    .addField("Information",
                            "`~>about credits` - **Lists everyone who has helped on the bot's development**, " +
                        "`~>about patreon` - **Lists our patreon supporters**", false)
                    .setColor(Color.PINK)
                    .build();
            }
        });
    }

    @Subscribe
    public static void avatar(CommandRegistry cr) {
        cr.register("avatar", new SimpleCommand(Category.INFO) {
            @Override
            protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
                if (!event.getMessage().getMentionedUsers().isEmpty()) {
                    event.getChannel().sendMessage(String.format(EmoteReference.OK + "Avatar for: **%s**\n%s", event.getMessage().getMentionedUsers().get(0).getName(), event.getMessage().getMentionedUsers().get(0).getAvatarUrl())).queue();
                    return;
                }
                event.getChannel().sendMessage(String.format("Avatar for: **%s**\n%s", event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl() + "?size=512")).queue();
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "Avatar")
                    .setDescription("**Get a user's avatar URL**")
                    .addField("Usage",
                        "`~>avatar` - **Get your avatar url**" +
                            "\n `~>avatar <mention>` - **Get a user's avatar url.**", false)
                    .build();
            }
        });
    }

    @Subscribe
    public static void guildinfo(CommandRegistry cr) {
        cr.register("serverinfo", new SimpleCommand(Category.INFO) {
            @Override
            protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
                Guild guild = event.getGuild();
                TextChannel channel = event.getChannel();

                String roles = guild.getRoles().stream()
                    .filter(role -> !guild.getPublicRole().equals(role))
                    .map(Role::getName)
                    .collect(Collectors.joining(", "));

                if (roles.length() > 1024)
                    roles = roles.substring(0, 1024 - 4) + "...";

                channel.sendMessage(new EmbedBuilder()
                    .setAuthor("Server Information", null, guild.getIconUrl())
                    .setColor(guild.getOwner().getColor() == null ? Color.ORANGE : guild.getOwner().getColor())
                    .setDescription("Server information for " + guild.getName())
                    .setThumbnail(guild.getIconUrl())
                    .addField("Users (Online/Unique)", (int) guild.getMembers().stream().filter(u -> !u.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count() + "/" + guild.getMembers().size(), true)
                    .addField("Main Channel", guild.getPublicChannel().getAsMention(), true)
                    .addField("Creation Date", guild.getCreationTime().format(DateTimeFormatter.ISO_DATE_TIME).replaceAll("[^0-9.:-]", " "), true)
                    .addField("Voice/Text Channels", guild.getVoiceChannels().size() + "/" + guild.getTextChannels().size(), true)
                    .addField("Owner", guild.getOwner().getUser().getName() + "#" + guild.getOwner().getUser().getDiscriminator(), true)
                    .addField("Region", guild.getRegion() == null ? "Unknown." : guild.getRegion().getName(), true)
                    .addField("Roles (" + guild.getRoles().size() + ")", roles, false)
                    .setFooter("Server ID: " + String.valueOf(guild.getId()), null)
                    .build()
                ).queue();
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "Server Info Command")
                    .setDescription("**See your server's current stats.**")
                    .setColor(event.getGuild().getOwner().getColor() == null ? Color.ORANGE : event.getGuild().getOwner().getColor())
                    .build();
            }
        });

        cr.registerAlias("serverinfo", "guildinfo");
    }

    @Subscribe
    public static void help(CommandRegistry cr) {
        Random r = new Random();
        List<String> jokes = Collections.unmodifiableList(Arrays.asList(
            "Yo damn I heard you like help, because you just issued the help command to get the help about the help command.",
            "Congratulations, you managed to use the help command.",
            "Helps you to help yourself.",
            "Help Inception.",
            "A help helping helping helping help.",
            "I wonder if this is what you are looking for..."
        ));

        cr.register("help", new SimpleCommand(Category.INFO) {
            @Override
            protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
                if (content.isEmpty()) {
                    String defaultPrefix = MantaroData.config().get().prefix[0], guildPrefix = MantaroData.db().getGuild(event.getGuild()).getData().getGuildCustomPrefix();
                    String prefix = guildPrefix == null ? defaultPrefix : guildPrefix;
                    GuildData dbGuild = MantaroData.db().getGuild(event.getGuild());
                    ExtraGuildData guildData = dbGuild.getData();

                    EmbedBuilder embed = baseEmbed(event, "MantaroBot Help")
                        .setColor(Color.PINK)
                        .setDescription("Command help. For extended usage please use " + String.format("%shelp <command>.", prefix)  +
                                (guildData.getDisabledCommands().isEmpty() ? "" : "\nOnly showing non-disabled commands. Total disabled commands: " + guildData.getDisabledCommands().size()) +
                                 (guildData.getChannelSpecificDisabledCommands().get(event.getChannel().getId()) == null || guildData.getChannelSpecificDisabledCommands().get(event.getChannel().getId()).isEmpty() ?
                                "" : "\nOnly showing non-disabled commands. Total disabled commands: " + guildData.getChannelSpecificDisabledCommands().get(event.getChannel().getId()).size()))
                        .setFooter(String.format("To check command usage, type %shelp <command> // -> Commands: " +
                                CommandListener.PROCESSOR.commands().values().stream().filter(c -> c.category() != null).count()
                            , prefix), null);

                    Arrays.stream(Category.values())
                        .filter(c -> c != Category.CURRENCY || !MantaroData.config().get().isPremiumBot())
                        .filter(c -> c != Category.MODERATION || CommandPermission.ADMIN.test(event.getMember()))
                        .filter(c -> c != Category.OWNER || CommandPermission.OWNER.test(event.getMember()))
                        .forEach(c -> embed.addField(c + " Commands:", forType(event.getChannel(), guildData, c), false));

                    event.getChannel().sendMessage(embed.build()).queue();

                } else {
                    Command command = CommandListener.PROCESSOR.commands().get(content);

                    if (command != null) {
                        final MessageEmbed help = command.help(event);
                        Optional.ofNullable(help).ifPresent((help1) -> event.getChannel().sendMessage(help1).queue());
                        if (help == null)
                            event.getChannel().sendMessage(EmoteReference.ERROR + "There's no extended help set for this command.").queue();
                    } else {
                        event.getChannel().sendMessage(EmoteReference.ERROR + "A command with this name doesn't exist").queue();
                    }
                }
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "Help Command")
                    .setColor(Color.PINK)
                    .setDescription("**" + jokes.get(r.nextInt(jokes.size())) + "**")
                    .addField(
                        "Usage",
                        "`~>help` - **Return information about who issued the command**.\n" +
                        "`~>help <command>` - **Return information about the command specified**.",
                        false
                    ).build();
            }
        });
    }

    @Subscribe
    public static void invite(CommandRegistry cr) {
        cr.register("invite", new SimpleCommand(Category.INFO) {
            @Override
            protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
                event.getChannel().sendMessage(new EmbedBuilder().setAuthor("Mantaro's Invite URL.", null, event.getJDA().getSelfUser().getAvatarUrl())
                    .addField("Invite URL", "http://polr.me/mantaro", false)
                    .addField("Support Server", "https://discordapp.com/invite/cMTmuPa", false)
                    .addField("Patreon URL", "http://patreon.com/mantaro", false)
                    .setDescription("Here are some useful links! " +
                            "**If you have any questions about the bot, feel free to join the support guild and ask**!." +
                        "\nWe provided a patreon link in case you would like to help Mantaro keep running by donating [and getting perks by doing so!]. " +
                        "Thanks you in advance for using the bot! **<3 from the developers**")
                    .setFooter("We hope you have fun with the bot.", event.getJDA().getSelfUser().getAvatarUrl())
                    .build()).queue();
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "Invite command").setDescription("**Gives you a bot OAuth invite link.**").build();
            }
        });
    }

    @Subscribe
    public static void onPostLoad(PostLoadEvent e) {
        start();
    }

    @Subscribe
    public static void stats(CommandRegistry cr) {
        cr.register("stats", new SimpleCommand(Category.INFO) {
            @Override
            protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
                if (content.isEmpty()) {
                    GuildStatsManager.MILESTONE = (((MantaroBot.getInstance().getGuilds().size() + 99) / 100) * 100) + 100;
                    List<Guild> guilds = MantaroBot.getInstance().getGuilds();

                    List<VoiceChannel> voiceChannels = MantaroBot.getInstance().getVoiceChannels();
                    List<VoiceChannel> musicChannels = voiceChannels.parallelStream().filter(vc -> vc.getMembers().contains(vc.getGuild().getSelfMember())).collect(Collectors.toList());

                    IntSummaryStatistics usersPerGuild = calculateInt(guilds, value -> value.getMembers().size());
                    IntSummaryStatistics onlineUsersPerGuild = calculateInt(guilds, value -> (int) value.getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count());
                    DoubleSummaryStatistics onlineUsersPerUserPerGuild = calculateDouble(guilds, value -> (double) value.getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count() / (double) value.getMembers().size() * 100);
                    DoubleSummaryStatistics listeningUsersPerUsersPerGuilds = calculateDouble(musicChannels, value -> (double) value.getMembers().size() / (double) value.getGuild().getMembers().size() * 100);
                    DoubleSummaryStatistics listeningUsersPerOnlineUsersPerGuilds = calculateDouble(musicChannels, value -> (double) value.getMembers().size() / (double) value.getGuild().getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count() * 100);
                    IntSummaryStatistics textChannelsPerGuild = calculateInt(guilds, value -> value.getTextChannels().size());
                    IntSummaryStatistics voiceChannelsPerGuild = calculateInt(guilds, value -> value.getVoiceChannels().size());

                    int musicConnections = (int) voiceChannels.stream().filter(voiceChannel -> voiceChannel.getMembers().contains(
                        voiceChannel.getGuild().getSelfMember())).count();
                    long exclusiveness = MantaroBot.getInstance().getGuilds().stream().filter(g -> g.getMembers().stream().filter(member -> member.getUser().isBot()).count() == 1).count();
                    double musicConnectionsPerServer = (double) musicConnections / (double) guilds.size() * 100;
                    double exclusivenessPercent = (double) exclusiveness / (double) guilds.size() * 100;
                    long bigGuilds = MantaroBot.getInstance().getGuilds().stream().filter(g -> g.getMembers().size() > 500).count();

                    event.getChannel().sendMessage(
                        new EmbedBuilder()
                            .setColor(Color.PINK)
                            .setAuthor("Mantaro Statistics", "https://github.com/Kodehawa/MantaroBot/", event.getJDA().getSelfUser().getAvatarUrl())
                            .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                            .setDescription("Well... I did my math!")
                            .addField("Users per Guild", String.format(Locale.ENGLISH, "Min: %d\nAvg: %.1f\nMax: %d", usersPerGuild.getMin(), usersPerGuild.getAverage(), usersPerGuild.getMax()), true)
                            .addField("Online Users per Server", String.format(Locale.ENGLISH, "Min: %d\nAvg: %.1f\nMax: %d", onlineUsersPerGuild.getMin(), onlineUsersPerGuild.getAverage(), onlineUsersPerGuild.getMax()), true)
                            .addField("Online Users per Users per Server", String.format(Locale.ENGLISH, "Min: %.1f%%\nAvg: %.1f%%\nMax: %.1f%%", onlineUsersPerUserPerGuild.getMin(), onlineUsersPerUserPerGuild.getAverage(), onlineUsersPerUserPerGuild.getMax()), true)
                            .addField("Text Channels per Server", String.format(Locale.ENGLISH, "Min: %d\nAvg: %.1f\nMax: %d", textChannelsPerGuild.getMin(), textChannelsPerGuild.getAverage(), textChannelsPerGuild.getMax()), true)
                            .addField("Voice Channels per Server", String.format(Locale.ENGLISH, "Min: %d\nAvg: %.1f\nMax: %d", voiceChannelsPerGuild.getMin(), voiceChannelsPerGuild.getAverage(), voiceChannelsPerGuild.getMax()), true)
                            .addField("Music Listeners per Users per Server", String.format(Locale.ENGLISH, "Min: %.1f%%\nAvg: %.1f%%\nMax: %.1f%%", listeningUsersPerUsersPerGuilds.getMin(), listeningUsersPerUsersPerGuilds.getAverage(), listeningUsersPerUsersPerGuilds.getMax()), true)
                            .addField("Music Listeners per Online Users per Server", String.format(Locale.ENGLISH, "Min: %.1f%%\nAvg: %.1f%%\nMax: %.1f%%", listeningUsersPerOnlineUsersPerGuilds.getMin(), listeningUsersPerOnlineUsersPerGuilds.getAverage(), listeningUsersPerOnlineUsersPerGuilds.getMax()), true)
                            .addField("Music Connections per Server", String.format(Locale.ENGLISH, "%.1f%% (%d Connections)", musicConnectionsPerServer, musicConnections), true)
                            .addField("Total queue size", Long.toString(MantaroBot.getInstance().getAudioManager().getTotalQueueSize()), true)
                            .addField("Total commands (including custom)", String.valueOf(CommandListener.PROCESSOR.commands().size()), true)
                            .addField("Exclusiveness in Total Servers", Math.round(exclusivenessPercent) + "% (" + exclusiveness + ")", false)
                            .addField("Big Servers", String.valueOf(bigGuilds), true)
                            .setFooter("! Guilds to next milestone (" + GuildStatsManager.MILESTONE + "): " + (GuildStatsManager.MILESTONE - MantaroBot.getInstance().getGuilds().size())
                                , event.getJDA().getSelfUser().getAvatarUrl())
                            .build()
                    ).queue();
                    TextChannelGround.of(event).dropItemWithChance(4, 5);
                    return;
                }

                if (args[0].equals("usage")) {
                    event.getChannel().sendMessage(new EmbedBuilder()
                        .setAuthor("Mantaro's usage information", null, "https://puu.sh/sMsVC/576856f52b.png")
                        .setDescription("Hardware and usage information.")
                        .setThumbnail("https://puu.sh/suxQf/e7625cd3cd.png")
                        .addField("Threads:", getThreadCount() + " Threads", true)
                        .addField("Memory Usage:", getTotalMemory() - getFreeMemory() + "MB/" + getMaxMemory() + "MB", true)
                        .addField("CPU Cores:", getAvailableProcessors() + " Cores", true)
                        .addField("CPU Usage:", getVpsCPUUsage() + "%", true)
                        .addField("Assigned Memory:", getTotalMemory() + "MB", true)
                        .addField("Remaining from assigned:", getFreeMemory() + "MB", true)
                        .build()
                    ).queue();
                    TextChannelGround.of(event).dropItemWithChance(4, 5);
                    return;
                }

                if (args[0].equals("vps")) {
                    TextChannelGround.of(event).dropItemWithChance(4, 5);
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor("Mantaro's VPS information", null, "https://puu.sh/sMsVC/576856f52b.png")
                        .setThumbnail("https://puu.sh/suxQf/e7625cd3cd.png")
                        .addField("CPU Usage", String.format("%.2f", getVpsCPUUsage()) + "%", true)
                        .addField("RAM (TOTAL/FREE/USED)", String.format("%.2f", getVpsMaxMemory()) + "GB/" + String.format("%.2f", getVpsFreeMemory())
                            + "GB/" + String.format("%.2f", getVpsUsedMemory()) + "GB", false);

                    event.getChannel().sendMessage(embedBuilder.build()).queue();
                    return;
                }

                if (args[0].equals("cmds")) {
                    if (args.length > 1) {
                        String what = args[1];
                        if (what.equals("total")) {
                            event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.TOTAL_CMDS, baseEmbed(event, "Command Stats | Total")).build()).queue();
                            return;
                        }

                        if (what.equals("daily")) {
                            event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.DAY_CMDS, baseEmbed(event, "Command Stats | Daily")).build()).queue();
                            return;
                        }

                        if (what.equals("hourly")) {
                            event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.HOUR_CMDS, baseEmbed(event, "Command Stats | Hourly")).build()).queue();
                            return;
                        }

                        if (what.equals("now")) {
                            event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.MINUTE_CMDS, baseEmbed(event, "Command Stats | Now")).build()).queue();
                            return;
                        }
                    }

                    //Default
                    event.getChannel().sendMessage(baseEmbed(event, "Command Stats")
                        .addField("Now", CommandStatsManager.resume(CommandStatsManager.MINUTE_CMDS), false)
                        .addField("Hourly", CommandStatsManager.resume(CommandStatsManager.HOUR_CMDS), false)
                        .addField("Daily", CommandStatsManager.resume(CommandStatsManager.DAY_CMDS), false)
                        .addField("Total", CommandStatsManager.resume(CommandStatsManager.TOTAL_CMDS), false)
                        .build()
                    ).queue();

                    return;
                }

                if (args[0].equals("guilds")) {
                    if (args.length > 1) {
                        String what = args[1];
                        if (what.equals("total")) {
                            event.getChannel().sendMessage(GuildStatsManager.fillEmbed(GuildStatsManager.TOTAL_EVENTS, baseEmbed(event, "Guild Stats | Total")).build()).queue();
                            return;
                        }

                        if (what.equals("daily")) {
                            event.getChannel().sendMessage(GuildStatsManager.fillEmbed(GuildStatsManager.DAY_EVENTS, baseEmbed(event, "Guild Stats | Daily")).build()).queue();
                            return;
                        }

                        if (what.equals("hourly")) {
                            event.getChannel().sendMessage(GuildStatsManager.fillEmbed(GuildStatsManager.HOUR_EVENTS, baseEmbed(event, "Guild Stats | Hourly")).build()).queue();
                            return;
                        }

                        if (what.equals("now")) {
                            event.getChannel().sendMessage(GuildStatsManager.fillEmbed(GuildStatsManager.MINUTE_EVENTS, baseEmbed(event, "Guild Stats | Now")).build()).queue();
                            return;
                        }
                    }

                    //Default
                    event.getChannel().sendMessage(baseEmbed(event, "Guild Stats")
                        .addField("Now", GuildStatsManager.resume(GuildStatsManager.MINUTE_EVENTS), false)
                        .addField("Hourly", GuildStatsManager.resume(GuildStatsManager.HOUR_EVENTS), false)
                        .addField("Daily", GuildStatsManager.resume(GuildStatsManager.DAY_EVENTS), false)
                        .addField("Total", GuildStatsManager.resume(GuildStatsManager.TOTAL_EVENTS), false)
                        .setFooter("Guilds: " + MantaroBot.getInstance().getGuilds().size(), null)
                        .build()
                    ).queue();

                    return;
                }

                onHelp(event);
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "Statistics command")
                    .setDescription("**See the bot, usage or vps statistics**")
                    .addField("Usage", "`~>stats <usage/vps/cmds/guilds>` - **Returns statistical information**", true)
                    .build();
            }
        });
    }

    @Subscribe
    public static void userinfo(CommandRegistry cr) {
        cr.register("userinfo", new SimpleCommand(Category.INFO) {
            @Override
            protected void call(GuildMessageReceivedEvent event, String content, String[] args) {
                User user = event.getMessage().getMentionedUsers().size() > 0 ? event.getMessage().getMentionedUsers().get(0) : event.getAuthor();
                Member member = event.getGuild().getMember(user);
                if (member == null) {
                    String name = user == null ? "Unknown User" : user.getName();
                    event.getChannel().sendMessage(EmoteReference.ERROR + "Sorry but I couldn't get " + name + "'s info. Please make sure you and that person are in the same server!").queue();
                    return;
                }

                String roles = member.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(", "));

                if (roles.length() > MessageEmbed.TEXT_MAX_LENGTH)
                    roles = roles.substring(0, MessageEmbed.TEXT_MAX_LENGTH - 4) + "...";
                event.getChannel().sendMessage(new EmbedBuilder()
                    .setColor(member.getColor())
                    .setAuthor(String.format("User info for %s#%s", user.getName(), user.getDiscriminator()), null, event.getAuthor().getEffectiveAvatarUrl())
                    .setThumbnail(user.getEffectiveAvatarUrl())
                    .addField("Join Date:", member.getJoinDate().format(DateTimeFormatter.ISO_DATE).replace("Z", ""), true)
                    .addField("Account Created:", user.getCreationTime().format(DateTimeFormatter.ISO_DATE).replace("Z", ""), true)
                    .addField("Voice Channel:", member.getVoiceState().getChannel() != null ? member.getVoiceState().getChannel().getName() : "None", false)
                    .addField("Playing:", member.getGame() == null ? "None" : member.getGame().getName(), false)
                    .addField("Color:", member.getColor() == null ? "Default" : "#" + Integer.toHexString(member.getColor().getRGB()).substring(2).toUpperCase(), true)
                    .addField("Status:", Utils.capitalize(member.getOnlineStatus().getKey().toLowerCase()), true)
                    .addField("Roles: [" + String.valueOf(member.getRoles().size()) + "]", roles, true)
                    .setFooter("User ID: " + user.getId(), null)
                    .build()
                ).queue();
            }

            @Override
            public MessageEmbed help(GuildMessageReceivedEvent event) {
                return helpEmbed(event, "User Info Command")
                    .setDescription("**See information about specific users.**")
                    .addField("Usage:",
                            "`~>userinfo @user` - **Get information about the specific user.**" +
                                    "\n`~>userinfo` - **Get information about yourself!**", false)
                    .build();
            }
        });
    }
}
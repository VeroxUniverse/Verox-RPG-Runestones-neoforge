package net.veroxuniverse.verox_rpg_runestones.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.registry.ModBlocks;
import net.veroxuniverse.verox_rpg_runestones.registry.ModItems;

public class ModLanguageProvider extends LanguageProvider {

    private static final String ID = RPGRunestones.MOD_ID;

    public ModLanguageProvider(PackOutput output) {
        super(output, ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add(ModBlocks.RUNESTONE.get(), "Runestone");
        this.add(ModItems.RUNE_DUST.get(), "Rune Dust");
        this.add(ModItems.RUNE_TABLET.get(), "Rune Tablet");
        this.add(ModItems.SOUL_ANCHOR.get(), "Soul Anchor");
        this.add("itemGroup." + ID, "Verox' RPG Runestones");

        this.add("tooltip." + ID + ".rune_tablet", "Travel to a known runestone from anywhere");
        this.add("tooltip." + ID + ".soul_anchor", "Lets your friends travel to you");
        this.add("tooltip." + ID + ".soul_anchor.slot", "Wear it as a charm");

        this.add("message." + ID + ".discovered", "Runestone discovered: %s");
        this.add("message." + ID + ".unknown_target", "This runestone no longer exists.");
        this.add("message." + ID + ".too_far", "You are too far away from the runestone.");
        this.add("message." + ID + ".no_dimension", "Runestones cannot reach into other dimensions.");
        this.add("message." + ID + ".not_enough_dust", "You need %s Rune Dust.");
        this.add("message." + ID + ".not_enough_xp", "You need %s levels.");
        this.add("message." + ID + ".tablet_cooldown", "The tablet is recharging: %s");
        this.add("message." + ID + ".anchor_gone", "Your friend no longer carries a Soul Anchor.");
        this.add("message." + ID + ".protected", "This runestone is protected.");

        this.add("command." + ID + ".list.header", "%s runestones:");
        this.add("command." + ID + ".list.teleport", "[Teleport]");
        this.add("command." + ID + ".removed", "Removed runestone %s.");
        this.add("command." + ID + ".global_on", "%s is now a global runestone.");
        this.add("command." + ID + ".global_off", "%s is now a discovery runestone.");
        this.add("command." + ID + ".renamed", "Renamed %s to %s.");
        this.add("command." + ID + ".invalid_name", "Names must be 1 to %s characters long.");
        this.add("command." + ID + ".not_found", "No runestone with this ID exists.");

        this.add("jade." + ID + ".global", "Global runestone");
        this.add("jade." + ID + ".owner", "Owner: %s");
        this.add("config.jade.plugin_" + ID + ".runestone", "Runestone");
        this.add("message." + ID + ".teleport_charging", "The runestones gather their power...");
        this.add("message." + ID + ".teleport_cancelled", "The teleport was interrupted.");
        this.add("message." + ID + ".teleport_busy", "A teleport is already charging.");
        this.add("message." + ID + ".companions", "%s companions travelled with you.");
        this.add("message." + ID + ".anchor_arrival", "%s travelled to your Soul Anchor.");
        this.add("message." + ID + ".friend_request_received", "%s sent you a friend request. Open a runestone to answer it.");
        this.add("message." + ID + ".friend_requests_pending", "You have %s open friend requests.");

        this.add("gui." + ID + ".menu.title", "Runestones");
        this.add("gui." + ID + ".menu.tablet", "Rune Tablet");
        this.add("gui." + ID + ".menu.choose", "Choose a destination");
        this.add("gui." + ID + ".menu.reorder", "Drag stones to reorder");
        this.add("gui." + ID + ".menu.empty", "No runestones known");
        this.add("gui." + ID + ".menu.page", "Page %s/%s");
        this.add("gui." + ID + ".menu.free", "Free");
        this.add("gui." + ID + ".menu.cost.dust", "%s Rune Dust");
        this.add("gui." + ID + ".menu.cost.xp", "%s Levels");
        this.add("gui." + ID + ".menu.friends", "Friends");
        this.add("gui." + ID + ".menu.friends_tooltip", "Open friends menu");
        this.add("gui." + ID + ".menu.friends_pending", "Open friends menu - %s new requests");

        this.add("gui." + ID + ".friends.title", "Friends");
        this.add("gui." + ID + ".friends.header", "Friends (%s)");
        this.add("gui." + ID + ".friends.requests_header", "Requests");
        this.add("gui." + ID + ".friends.add", "Add Friend");
        this.add("gui." + ID + ".friends.name", "Player name");
        this.add("gui." + ID + ".friends.send", "Send");
        this.add("gui." + ID + ".friends.show_requests", "Requests");
        this.add("gui." + ID + ".friends.show_friends", "Friends");
        this.add("gui." + ID + ".friends.empty", "No friends yet");
        this.add("gui." + ID + ".friends.no_requests", "No requests");
        this.add("gui." + ID + ".friends.online", "Online");
        this.add("gui." + ID + ".friends.offline", "Offline");
        this.add("gui." + ID + ".friends.incoming", "Wants to be friends");
        this.add("gui." + ID + ".friends.outgoing", "Request sent");
        this.add("gui." + ID + ".friends.notice.sent", "Request sent to %s.");
        this.add("gui." + ID + ".friends.notice.received", "%s sent you a friend request.");
        this.add("gui." + ID + ".friends.notice.accepted", "You are now friends with %s.");
        this.add("gui." + ID + ".friends.notice.declined", "Request from %s declined.");
        this.add("gui." + ID + ".friends.notice.cancelled", "Request to %s cancelled.");
        this.add("gui." + ID + ".friends.notice.removed", "%s is no longer your friend.");
        this.add("gui." + ID + ".friends.notice.not_found", "No player named %s found.");
        this.add("gui." + ID + ".friends.notice.self", "You can't add yourself.");
        this.add("gui." + ID + ".friends.notice.already", "%s is already your friend.");
        this.add("gui." + ID + ".friends.notice.pending", "Your request to %s is still pending.");
        this.add("gui." + ID + ".menu.rename", "Click to edit runestone");

        this.add("gui." + ID + ".naming.title", "Name your Runestone");
        this.add("gui." + ID + ".edit.title", "Edit Runestone");
        this.add("gui." + ID + ".edit.mode.discovery", "Mode: Discovery");
        this.add("gui." + ID + ".edit.mode.global", "Mode: Global");
        this.add("gui." + ID + ".edit.mode.discovery.tooltip", "Players have to find this runestone before they can travel here.");
        this.add("gui." + ID + ".edit.mode.global.tooltip", "Every player can travel here right away, without finding it first.");
    }
}

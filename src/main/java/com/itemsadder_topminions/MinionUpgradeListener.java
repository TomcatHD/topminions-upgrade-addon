package com.itemsadder_topminions;

import com.sarry20.topminion.event.minion.MinionUpgradeEvent;
import com.sarry20.topminion.models.minion.MinionObj;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MinionUpgradeListener implements Listener {

    @EventHandler
    public void onMinionUpgrade(MinionUpgradeEvent event) {
        // This might be event.getTarget(), event.getMinion(), etc.
        // Adjust based on available method names in MinionEvent superclass.
        MinionObj minion = event.getMinionObj(); // 🔁 Replace if method is named differently

        if (minion == null) {
            System.out.println("[TopMinions] [DEBUG] No minion found in upgrade event.");
            return;
        }

        UpgradeMenu.openUpgradeGUI(event.getPlayer(), minion);
    }
}

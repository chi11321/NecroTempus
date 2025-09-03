package io.github.cruciblemc.necrotempus.modules.features.playertab.client;

import com.mojang.authlib.GameProfile;
import io.github.cruciblemc.necrotempus.api.playertab.PlayerTab;
import io.github.cruciblemc.necrotempus.api.playertab.TabCell;
import io.github.cruciblemc.necrotempus.modules.features.playertab.client.render.PlayerTabGui;
import io.github.cruciblemc.necrotempus.utils.NetHandlerPlayClientNT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;
import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DefaultPlayerTab extends PlayerTab {

    private static PlayerTab instance;
    private static Long lastCellsUpdate;
    private static List<TabCell> cachedList;

    public static PlayerTab getInstance() {
        return instance != null ? instance : new DefaultPlayerTab();
    }

    private DefaultPlayerTab() {
        super();
        instance = this;
    }

    @Override
    public IChatComponent getHeader() {
        return null;
    }

    @Override
    public IChatComponent getFooter() {
        return null;
    }

    @Override
    public List<TabCell> getCellList() {

        if (lastCellsUpdate != null) {

            long time = System.currentTimeMillis() - lastCellsUpdate;

            if (time <= 250 && cachedList != null)
                return cachedList;
        }

        lastCellsUpdate = System.currentTimeMillis();

        Minecraft minecraft = Minecraft.getMinecraft();

        List<TabCell> tabCells = new ArrayList<>();

        NetHandlerPlayClientNT handlerWrapper = NetHandlerPlayClientNT.of(minecraft.thePlayer.sendQueue);

        for (GuiPlayerInfo guiPlayerInfo : handlerWrapper.getOrderedServerPlayers()) {

            EntityPlayer entityPlayer = minecraft.theWorld.getPlayerEntityByName(guiPlayerInfo.name);

            String name = guiPlayerInfo.name;

            // Yes, sometimes can be blank.
            if (StringUtils.isBlank(name)) {
                continue;
            }

            GameProfile gameProfile = entityPlayer != null ? entityPlayer.getGameProfile() : new GameProfile(null, name);

            tabCells.add(new TabCell(
                    new ChatComponentText(PlayerTabGui.getFormattedPlayerName(guiPlayerInfo.name, minecraft)),
                    guiPlayerInfo.name,
                    gameProfile,
                    guiPlayerInfo.name,
                    gameProfile.getId(),
                    true,
                    guiPlayerInfo.responseTime
            ));
            UUID uuid = gameProfile.getId();
            FMLLog.log(Level.INFO,
                    "[TabCell] 添加玩家: name=%s, formattedName=%s, uuid=%s, ping=%d",
                    new Object[]{
                            guiPlayerInfo.name,
                            PlayerTabGui.getFormattedPlayerName(guiPlayerInfo.name, minecraft),
                            uuid != null ? uuid.toString() : "null",
                            guiPlayerInfo.responseTime
                    });
        }

        cachedList = tabCells;
        return tabCells;
    }

    @Override
    public boolean isDrawPlayerHeads() {
        return true;
    }
}

package com.an0n;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.PreMapLoad;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

@Slf4j
@PluginDescriptor(
        name = "Balloon Hider"
)
public class BalloonHiderPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private BalloonHiderConfig config;

    //1 = Basket
    //2 = Balloon
    private final static Map<Integer, Integer> ballonType  = new HashMap<>() {{
                put(19133,1); put(19134,2); //Entrana
                put(19135,1); put(19136,2); //Taverley
                put(19137,1); put(19138,2); //Castle Wars
                put(19139,1); put(19140,2); //Gnome Stronghold
                put(19141,1); put(19142,2); //Crafting Guild
                put(19143,1); put(19144,2); //Varrock
    }};

    public void refreshObjects(boolean doInvokeLater) {
        // Setting the game state to loading forces a full map reload, which will reload all the objects
        if (client.getGameState() == GameState.LOGGED_IN) {
            if (doInvokeLater) {
                clientThread.invokeLater(() -> {
                    client.setGameState(GameState.LOADING);
                });
            } else {
                client.setGameState(GameState.LOADING);
            }
        }
    }

    //Needed to re-apply deletion when plugin is restarted
    @Override
    protected void startUp() throws Exception
    {
        refreshObjects(true);
    }

    @Override
    protected void shutDown() throws Exception {
        refreshObjects(true); // Restore all hidden objects when the plugin is turned off
    }

    private void checkTileForBalloon(Tile currTile, Scene scene) {
        if (currTile != null) {
            GameObject[] objects = currTile.getGameObjects();
            if (objects != null) {
                for (GameObject gameObject : objects) {
                    if (gameObject != null) {
                        int type = getBalloonType(gameObject.getId());
                        if (type == 2) {
                            scene.removeGameObject(gameObject);
                        } else if (type == 1 && config.hideBasket()) {
                            scene.removeGameObject(gameObject);
                        }
                    }
                }
            }
        }
    }

    private void removeBalloon(Scene scene) {
        Tile[][][] tiles = scene.getExtendedTiles();
        //Only check 104+ since we handle close object removal via onGameObjectSpawned
        for (int x = 104; x < Constants.EXTENDED_SCENE_SIZE; ++x) {
            for (int y = 104; y < Constants.EXTENDED_SCENE_SIZE; ++y) {
                checkTileForBalloon(tiles[1][x][y],scene);
                if(config.hideBasket()){
                    checkTileForBalloon(tiles[0][x][y],scene);
                }
            }
        }
    }

    @Subscribe
    public void onPreMapLoad(PreMapLoad preMapLoad) {
        removeBalloon(preMapLoad.getScene());
    }

    private int getBalloonType(int id){
        var type = ballonType.get(id);
        return type == null ? 0 : type;
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject obj = event.getGameObject();
        int type = getBalloonType(obj.getId());
        if(config.hideBasket() && type == 1){
            client.getScene().removeGameObject(obj);
        } else if(type==2){
            client.getScene().removeGameObject(obj);
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if(event.getGroup().equals(BalloonHiderConfig.GROUP)) {
            refreshObjects(true);
        }
    }

    @Provides
    BalloonHiderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BalloonHiderConfig.class);
    }
}

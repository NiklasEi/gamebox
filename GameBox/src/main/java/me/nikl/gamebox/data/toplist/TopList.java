package me.nikl.gamebox.data.toplist;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.toplist.PlayerScore;
import me.nikl.gamebox.inventory.gui.game.TopListPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by nikl on 22.01.18.
 */
public class TopList {
    public static final int TOP_LIST_LENGTH = 25;
    private String identifier;
    private List<PlayerScore> playerScores = new ArrayList<>();
    private Set<TopListPage> topListPages;

    public TopList(String identifier, List<PlayerScore> playerScores){
        this.identifier = identifier;
        this.playerScores = playerScores;
        topListPages = new HashSet<>();
    }

    public void update(PlayerScore playerScore){
        GameBox.debug("score: " + playerScore.getValue());
        if(!isInList(playerScore.getUuid())){
            GameBox.debug("new player score");
            handleNewPlayerScore(playerScore);
        } else {
            GameBox.debug("updating existing score");
            handleUpdatePlayerScore(playerScore);
        }
        for (TopListPage topListPage : topListPages){
            topListPage.update();
        }
    }

    private void handleUpdatePlayerScore(PlayerScore playerScore) {
        removePlayerScore(playerScore.getUuid());
        addNewScoreEntry(playerScore);
    }

    private void removePlayerScore(UUID uuid) {
        Iterator<PlayerScore> playerScoreIterator = playerScores.iterator();
        while (playerScoreIterator.hasNext()){
            if(playerScoreIterator.next().getUuid().equals(uuid)) {
                playerScoreIterator.remove();
                return;
            }
        }
    }

    private void handleNewPlayerScore(PlayerScore playerScore) {
        if(playerScores.size() < TOP_LIST_LENGTH){
            addNewScoreEntry(playerScore);
        } else {
            playerScores.add(getNewScorePosition(playerScore), playerScore);
            playerScores.remove(TOP_LIST_LENGTH);
        }
    }

    private void addNewScoreEntry(PlayerScore playerScore) {
        int position = getNewScorePosition(playerScore);
        GameBox.debug("Rank: " + (position + 1));
        playerScores.add(position, playerScore);
    }

    private int getNewScorePosition(PlayerScore playerScore) {
        for(int position = 0; position < playerScores.size(); position++){
            if(playerScore.isBetterThen(playerScores.get(position))) {
                return position;
            }
        }
        return playerScores.size();
    }

    public List<PlayerScore> getPlayerScores(){
        return Collections.unmodifiableList(this.playerScores);
    }

    private boolean isInList(UUID uuid){
        for (PlayerScore score : playerScores){
            if(score.getUuid().equals(uuid)) return true;
        }
        return false;
    }

    public void registerTopListPage(TopListPage topListPage) {
        topListPages.add(topListPage);
    }
}

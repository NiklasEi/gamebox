package me.nikl.gamebox.data.toplist;

import me.nikl.gamebox.GameBox;
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
    private Set<TopListUser> topListUsers;

    public TopList(String identifier, List<PlayerScore> playerScores){
        this.identifier = identifier;
        this.playerScores = playerScores;
        topListUsers = new HashSet<>();
    }

    public void update(PlayerScore playerScore){
        GameBox.debug("score: " + playerScore.getValue());
        if(updateSingleScore(playerScore)) updateUsers();
    }

    public void updatePlayerScores(List<PlayerScore> playerScores){
        boolean changed = false;
        for(PlayerScore playerScore : playerScores){
            if(updateSingleScore(playerScore)) changed = true;
        }
        if(changed) updateUsers();
    }

    private boolean updateSingleScore(PlayerScore playerScore) {
        if(updateIfInList(playerScore)) return false;
        if(playerScores.size() >= TOP_LIST_LENGTH && !playerScore.isBetterThen(playerScores.get(TOP_LIST_LENGTH - 1))) return false;
        addNewScoreEntry(playerScore);
        if(playerScores.size() >= TOP_LIST_LENGTH){
            playerScores = playerScores.subList(0, TOP_LIST_LENGTH);
        }
        return true;
    }

    private boolean updateIfInList(PlayerScore playerScore) {
        PlayerScore oldScore = getPlayerScoreFromTopList(playerScore.getUuid());
        if(oldScore == null) return false;
        if(playerScore.isBetterThen(oldScore)) {
            removePlayerScore(playerScore.getUuid());
            addNewScoreEntry(playerScore);
            updateUsers();
        }
        return true;
    }

    private void updateUsers(){
        for (TopListUser topListUser : topListUsers){
            topListUser.update();
        }
    }

    private PlayerScore getPlayerScoreFromTopList(UUID uuid){
        Iterator<PlayerScore> playerScoreIterator = playerScores.iterator();
        PlayerScore current;
        while (playerScoreIterator.hasNext()){
            current = playerScoreIterator.next();
            if(!current.getUuid().equals(uuid)) continue;
            return current;
        }
        return null;
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

    public void registerTopListUser(TopListUser topListUser) {
        topListUsers.add(topListUser);
    }

    public String getIdentifier() {
        return identifier;
    }
}

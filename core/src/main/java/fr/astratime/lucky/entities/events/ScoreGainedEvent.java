package fr.astratime.lucky.entities.events;

public class ScoreGainedEvent extends Event {
    public final int score;

    public ScoreGainedEvent(int score) { this.score = score; }

    @Override
    public String describe() { return "+" + score + " points"; }
}

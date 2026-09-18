package fr.astratime.lucky.entities.events;

/** Événement émis quand les trois symboles tirés sont identiques (jackpot). */
public class JackpotEvent extends Event {
    @Override
    public String describe() { return "JACKPOT !"; }
}

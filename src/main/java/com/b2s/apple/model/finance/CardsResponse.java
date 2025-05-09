package com.b2s.apple.model.finance;

import java.util.ArrayList;
import java.util.List;

public class CardsResponse {

    List<Card> cards = new ArrayList<>();

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}

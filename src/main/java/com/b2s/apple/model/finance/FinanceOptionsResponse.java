package com.b2s.apple.model.finance;

import java.util.ArrayList;
import java.util.List;

public class FinanceOptionsResponse {

    List<FinanceOption> financeOptions = new ArrayList<>();

    public List<FinanceOption> getFinanceOptions() {
        return financeOptions;
    }

    public void setFinanceOptions(List<FinanceOption> financeOptions) {
        this.financeOptions = financeOptions;
    }
}

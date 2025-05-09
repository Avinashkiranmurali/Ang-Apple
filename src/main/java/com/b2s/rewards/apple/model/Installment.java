package com.b2s.rewards.apple.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sjayaraman on 12/6/2018.
 */
public class Installment implements Serializable {

    private static final long serialVersionUID = -7929564740389841886L;
    private List<InstallmentOption> installmentOption;
    private InstallmentOption selectedInstallement;

    public List<InstallmentOption> getInstallmentOption() {
        return installmentOption;
    }

    public void setInstallmentOption(final List<InstallmentOption> installmentOption) {
        this.installmentOption = installmentOption;
    }

    public InstallmentOption getSelectedInstallement() {
        return selectedInstallement;
    }

    public void setSelectedInstallement(final InstallmentOption selectedInstallement) {
        this.selectedInstallement = selectedInstallement;
    }
}

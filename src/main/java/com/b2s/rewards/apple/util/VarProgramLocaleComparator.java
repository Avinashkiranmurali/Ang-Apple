package com.b2s.rewards.apple.util;

import com.b2s.rewards.apple.model.VarProgramLocaleIf;

public final class VarProgramLocaleComparator extends VarProgramComparator {

    public static <T extends VarProgramLocaleIf> int compare(T first, T next) {
        if (!first.getProgramId().equalsIgnoreCase(next.getProgramId())) {
            return next.getProgramId().compareTo(first.getProgramId());
        } else {
            if (!first.getVarId().equalsIgnoreCase(next.getVarId())) {
                return next.getVarId().compareTo(first.getVarId());
            } else {
                return next.getLocale().compareTo(first.getLocale());
            }
        }
    }
}
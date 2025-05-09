package com.b2s.rewards.apple.util;

import com.b2s.rewards.apple.model.VarProgramIf;

public class VarProgramComparator {

    public static <T extends VarProgramIf> int compare(T first, T next) {
        if (!first.getProgramId().equalsIgnoreCase(next.getProgramId())) {
            return next.getProgramId().compareTo(first.getProgramId());
        } else {
            return next.getVarId().compareTo(first.getVarId());
        }
    }
}
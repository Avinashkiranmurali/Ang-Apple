package com.b2s.rewards.constants;

/**
 * Created by ppalpandi on 10/27/2017.
 */

public enum SourceCode {

        GRDI("Diners", "GRDI", "5140"),
        GRBQ("Bank of Queensland", "GRBQ", "5150"),
        GRSC("SunCorp", "GRSC", "5160"),
        GRCS("Card Services", "GRCS", "5170"),
        GRCB("Citi Branded", "GRCB", "5180");

        private final String name;
        private final String code;
        private final String id;

        SourceCode(final String name, final String code, final String id) {
            this.name = name;
            this.code = code;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getId() {
            return id;
        }

    }

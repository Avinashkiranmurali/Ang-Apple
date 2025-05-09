package com.b2s.shop.common.constant;

import com.b2s.rewards.common.util.CommonConstants;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class Constant {

    public static String SUCCESS = "success";
    public static String FAILURE = "failure";
    public static String TIMEOUT = "timeout";

    public static String USER_SESSION_OBJECT = "USER";

    private static final Map<String, String> STATES = new TreeMap<String, String>();
    private static final Map<String, String> COUNTRIES2 = new TreeMap<String, String>();
    private static final Map<String, String> CA_STATES = new TreeMap<String, String>();

    private static final Map<String, String> US_PROTECTORATES = new TreeMap<String, String>();
    public static String SUPPLEMENTAL_POINTS_RANGE = "P";
    public static String SUPPLEMENTAL_DOLLAR_RANGE = "D";

    @Deprecated
    public static final Map<String, String> STATES_DESC_KEY = new TreeMap<String, String>();
    @Deprecated
    public static final Map<String, String> CA_STATES_DESC_KEY = new TreeMap<String, String>();
    @Deprecated
    public static final Map<String, String> US_PROTECTORATES_DESC_KEY = new TreeMap<String, String>();

    private static final Map<String, String> MILITARY_CODES = new TreeMap<>();
    public static final List<String> COUNTRIES = new ArrayList<String>();
    private static final Map<String, String> MX_STATES = new TreeMap<>();
    private static final Map<String, String> AU_STATES = new TreeMap<>();
    private static final Map<String, String> TW_COUNTY_LIST_ZHO = new TreeMap<>();
    private static final Map<String, String> TW_COUNTY_LIST = new TreeMap<>();
    public static final Map<String, String> AE_CITIES = new TreeMap<>();
    private static final Map<String, String> RU_STATES = new TreeMap<>();

    public static String RESULT_NAME = "HotelNaviagationInfo";
    public static String CODE_TYPE_LOGIN = "login";
    public static String CODE_TYPE_MAINTENANCE = "maintenance";

    public static final String AMERICAN_SAMOA = "American Samoa";

    public static final String PALAU = "Palau";

    public static final String MARSHALL_ISLANDS = "Marshall Islands";

    public static final String PUERTO_RICO = "Puerto Rico";

    static {
        STATES.put("AL","AL");
        STATES.put("AK","AK");
        STATES.put("AZ","AZ");
        STATES.put("AR","AR");
        STATES.put("CA","CA");
        STATES.put("CO","CO");
        STATES.put("CT","CT");
        STATES.put("DE","DE");
        STATES.put("DC","DC");
        STATES.put("FL","FL");
        STATES.put("GA","GA");
        STATES.put("HI","HI");
        STATES.put("ID","ID");
        STATES.put("IL","IL");
        STATES.put("IN","IN");
        STATES.put("IA","IA");
        STATES.put("KS","KS");
        STATES.put("KY","KY");
        STATES.put("LA","LA");
        STATES.put("ME","ME");
        STATES.put("MD","MD");
        STATES.put("MA","MA");
        STATES.put("MI","MI");
        STATES.put("MN","MN");
        STATES.put("MS","MS");
        STATES.put("MO","MO");
        STATES.put("MT","MT");
        STATES.put("NE","NE");
        STATES.put("NV","NV");
        STATES.put("NH","NH");
        STATES.put("NJ","NJ");
        STATES.put("NM","NM");
        STATES.put("NY","NY");
        STATES.put("NC","NC");
        STATES.put("ND","ND");
        STATES.put("OH","OH");
        STATES.put("OK","OK");
        STATES.put("OR","OR");
        STATES.put("PA","PA");
        STATES.put("RI","RI");
        STATES.put("SC","SC");
        STATES.put("SD","SD");
        STATES.put("TN","TN");
        STATES.put("TX","TX");
        STATES.put("UT","UT");
        STATES.put("VT","VT");
        STATES.put("VA","VA");
        STATES.put("WA","WA");
        STATES.put("WV","WV");
        STATES.put("WI","WI");
        STATES.put("WY","WY");

        CA_STATES.put("AB","AB");
        CA_STATES.put("BC","BC");
        CA_STATES.put("MB","MB");
        CA_STATES.put("NB","NB");
        CA_STATES.put("NL","NL");
        CA_STATES.put("NT","NT");
        CA_STATES.put("NS","NS");
        CA_STATES.put("NU","NU");
        CA_STATES.put("ON","ON");
        CA_STATES.put("PE","PE");
        CA_STATES.put("QC","QC");
        CA_STATES.put("SK","SK");
        CA_STATES.put("YT","YT");

        US_PROTECTORATES.put("AS", AMERICAN_SAMOA);
        US_PROTECTORATES.put("GU", "Guam");
        US_PROTECTORATES.put("MP", "Mariana Islands");
        US_PROTECTORATES.put("MH", MARSHALL_ISLANDS);
        US_PROTECTORATES.put("FM", "Micronesia");
        US_PROTECTORATES.put("PW", PALAU);
        US_PROTECTORATES.put("PR", PUERTO_RICO);
        US_PROTECTORATES.put("VI", "U.S. Virgin Islands");

        STATES_DESC_KEY.put("Alabama", "AL");
        STATES_DESC_KEY.put("Alaska", "AK");
        STATES_DESC_KEY.put("Arizona", "AZ");
        STATES_DESC_KEY.put("Arkansas", "AR");
        STATES_DESC_KEY.put("California", "CA");
        STATES_DESC_KEY.put("Colorado", "CO");
        STATES_DESC_KEY.put("Connecticut", "CT");
        STATES_DESC_KEY.put("Delaware", "DE");
        STATES_DESC_KEY.put("District of Columbia", "DC");
        STATES_DESC_KEY.put("Florida", "FL");
        STATES_DESC_KEY.put("Georgia", "GA");
        STATES_DESC_KEY.put("Hawaii", "HI");
        STATES_DESC_KEY.put("Idaho", "ID");
        STATES_DESC_KEY.put("Illinois", "IL");
        STATES_DESC_KEY.put("Indiana", "IN");
        STATES_DESC_KEY.put("Iowa", "IA");
        STATES_DESC_KEY.put("Kansas", "KS");
        STATES_DESC_KEY.put("Kentucky", "KY");
        STATES_DESC_KEY.put("Louisiana", "LA");
        STATES_DESC_KEY.put("Maine", "ME");
        STATES_DESC_KEY.put("Maryland", "MD");
        STATES_DESC_KEY.put("Massachusetts", "MA");
        STATES_DESC_KEY.put("Michigan", "MI");
        STATES_DESC_KEY.put("Minnesota", "MN");
        STATES_DESC_KEY.put("Mississippi", "MS");
        STATES_DESC_KEY.put("Missouri", "MO");
        STATES_DESC_KEY.put("Montana", "MT");
        STATES_DESC_KEY.put("Nebraska", "NE");
        STATES_DESC_KEY.put("Nevada", "NV");
        STATES_DESC_KEY.put("New Hampshire", "NH");
        STATES_DESC_KEY.put("New Jersey", "NJ");
        STATES_DESC_KEY.put("New Mexico", "NM");
        STATES_DESC_KEY.put("New York", "NY");
        STATES_DESC_KEY.put("North Carolina", "NC");
        STATES_DESC_KEY.put("North Dakota", "ND");
        STATES_DESC_KEY.put("Ohio", "OH");
        STATES_DESC_KEY.put("Oklahoma", "OK");
        STATES_DESC_KEY.put("Oregon", "OR");
        STATES_DESC_KEY.put("Pennsylvania", "PA");
        STATES_DESC_KEY.put("Rhode Island", "RI");
        STATES_DESC_KEY.put("South Carolina", "SC");
        STATES_DESC_KEY.put("South Dakota", "SD");
        STATES_DESC_KEY.put("Tennessee", "TN");
        STATES_DESC_KEY.put("Texas", "TX");
        STATES_DESC_KEY.put("Utah", "UT");
        STATES_DESC_KEY.put("Vermont", "VT");
        STATES_DESC_KEY.put("Virginia", "VA");
        STATES_DESC_KEY.put("Washington", "WA");
        STATES_DESC_KEY.put("West Virginia", "WV");
        STATES_DESC_KEY.put("Wisconsin", "WI");
        STATES_DESC_KEY.put("Wyoming", "WY");

        CA_STATES_DESC_KEY.put("Alberta", "AB");
        CA_STATES_DESC_KEY.put("British Columbia", "BC");
        CA_STATES_DESC_KEY.put("Manitoba", "MB");
        CA_STATES_DESC_KEY.put("New Brunswick", "NB");
        CA_STATES_DESC_KEY.put("Newfoundland and Labrador", "NL");
        CA_STATES_DESC_KEY.put("Northwest Territories", "NT");
        CA_STATES_DESC_KEY.put("Nova Scotia", "NS");
        CA_STATES_DESC_KEY.put("Nunavut", "NU");
        CA_STATES_DESC_KEY.put("Ontario", "ON");
        CA_STATES_DESC_KEY.put("Prince Edward Island", "PE");
        CA_STATES_DESC_KEY.put("Quebec", "QC");
        CA_STATES_DESC_KEY.put("Saskatchewan", "SK");
        CA_STATES_DESC_KEY.put("Yukon", "YT");

        US_PROTECTORATES_DESC_KEY.put(AMERICAN_SAMOA, "AS");
        US_PROTECTORATES_DESC_KEY.put("Guam", "GU");
        US_PROTECTORATES_DESC_KEY.put("Mariana Islands", "MP");
        US_PROTECTORATES_DESC_KEY.put(MARSHALL_ISLANDS, "MH");
        US_PROTECTORATES_DESC_KEY.put("Micronesia", "FM");
        US_PROTECTORATES_DESC_KEY.put(PALAU, "PW");
        US_PROTECTORATES_DESC_KEY.put(PUERTO_RICO, "PR");
        US_PROTECTORATES_DESC_KEY.put("U.S. Virgin Islands", "VI");

        MILITARY_CODES.put("AA", "Armed Forces Americas");
        MILITARY_CODES.put("AE", "Armed Forces Europe");
        MILITARY_CODES.put("AP", "Armed Forces Pacific");

        COUNTRIES.add("United States");
        COUNTRIES.add("United Kingdom");
        COUNTRIES.add("Afghanistan");
        COUNTRIES.add("Albania");
        COUNTRIES.add("Algeria");
        COUNTRIES.add(AMERICAN_SAMOA);
        COUNTRIES.add("Andorra");
        COUNTRIES.add("Angola");
        COUNTRIES.add("Anguilla");
        COUNTRIES.add("Antarctica");
        COUNTRIES.add("Antigua and Barbuda");
        COUNTRIES.add("Argentina");
        COUNTRIES.add("Armenia");
        COUNTRIES.add("Aruba");
        COUNTRIES.add("Australia");
        COUNTRIES.add("Austria");
        COUNTRIES.add("Azerbaijan");
        COUNTRIES.add("Bahamas");
        COUNTRIES.add("Bahrain");
        COUNTRIES.add("Bangladesh");
        COUNTRIES.add("Barbados");
        COUNTRIES.add("Belarus");
        COUNTRIES.add("Belgium");
        COUNTRIES.add("Belize");
        COUNTRIES.add("Benin");
        COUNTRIES.add("Bermuda");
        COUNTRIES.add("Bhutan");
        COUNTRIES.add("Bolivia");
        COUNTRIES.add("Bosnia and Herzegovina");
        COUNTRIES.add("Botswana");
        COUNTRIES.add("Bouvet Island");
        COUNTRIES.add("Brazil");
        COUNTRIES.add("British Indian Ocean Territory");
        COUNTRIES.add("Brunei Darussalam");
        COUNTRIES.add("Bulgaria");
        COUNTRIES.add("Burkina Faso");
        COUNTRIES.add("Burundi");
        COUNTRIES.add("Cambodia");
        COUNTRIES.add("Cameroon");
        COUNTRIES.add("Canada");
        COUNTRIES.add("Cape Verde");
        COUNTRIES.add("Cayman Islands");
        COUNTRIES.add("Central African Republic");
        COUNTRIES.add("Chad");
        COUNTRIES.add("Chile");
        COUNTRIES.add("China");
        COUNTRIES.add("Christmas Island");
        COUNTRIES.add("Cocos (Keeling) Islands");
        COUNTRIES.add("Colombia");
        COUNTRIES.add("Comoros");
        COUNTRIES.add("Congo");
        COUNTRIES.add("Congo, The Democratic Republic of The");
        COUNTRIES.add("Cook Islands");
        COUNTRIES.add("Costa Rica");
        COUNTRIES.add("Cote D'ivoire");
        COUNTRIES.add("Croatia");
        COUNTRIES.add("Cuba");
        COUNTRIES.add("Cyprus");
        COUNTRIES.add("Czech Republic");
        COUNTRIES.add("Denmark");
        COUNTRIES.add("Djibouti");
        COUNTRIES.add("Dominica");
        COUNTRIES.add("Dominican Republic");
        COUNTRIES.add("Ecuador");
        COUNTRIES.add("Egypt");
        COUNTRIES.add("El Salvador");
        COUNTRIES.add("Equatorial Guinea");
        COUNTRIES.add("Eritrea");
        COUNTRIES.add("Estonia");
        COUNTRIES.add("Ethiopia");
        COUNTRIES.add("Falkland Islands (Malvinas)");
        COUNTRIES.add("Faroe Islands");
        COUNTRIES.add("Fiji");
        COUNTRIES.add("Finland");
        COUNTRIES.add("France");
        COUNTRIES.add("French Guiana");
        COUNTRIES.add("French Polynesia");
        COUNTRIES.add("French Southern Territories");
        COUNTRIES.add("Gabon");
        COUNTRIES.add("Gambia");
        COUNTRIES.add("Georgia");
        COUNTRIES.add("Germany");
        COUNTRIES.add("Ghana");
        COUNTRIES.add("Gibraltar");
        COUNTRIES.add("Greece");
        COUNTRIES.add("Greenland");
        COUNTRIES.add("Grenada");
        COUNTRIES.add("Guadeloupe");
        COUNTRIES.add("Guam");
        COUNTRIES.add("Guatemala");
        COUNTRIES.add("Guinea");
        COUNTRIES.add("Guinea-bissau");
        COUNTRIES.add("Guyana");
        COUNTRIES.add("Haiti");
        COUNTRIES.add("Heard Island and Mcdonald Islands");
        COUNTRIES.add("Holy See (Vatican City State)");
        COUNTRIES.add("Honduras");
        COUNTRIES.add("Hong Kong");
        COUNTRIES.add("Hungary");
        COUNTRIES.add("Iceland");
        COUNTRIES.add("India");
        COUNTRIES.add("Indonesia");
        COUNTRIES.add("Iran, Islamic Republic of");
        COUNTRIES.add("Iraq");
        COUNTRIES.add("Ireland");
        COUNTRIES.add("Israel");
        COUNTRIES.add("Italy");
        COUNTRIES.add("Jamaica");
        COUNTRIES.add("Japan");
        COUNTRIES.add("Jordan");
        COUNTRIES.add("Kazakhstan");
        COUNTRIES.add("Kenya");
        COUNTRIES.add("Kiribati");
        COUNTRIES.add("Korea, Democratic People's Republic of");
        COUNTRIES.add("Korea, Republic of");
        COUNTRIES.add("Kuwait");
        COUNTRIES.add("Kyrgyzstan");
        COUNTRIES.add("Lao People's Democratic Republic");
        COUNTRIES.add("Latvia");
        COUNTRIES.add("Lebanon");
        COUNTRIES.add("Lesotho");
        COUNTRIES.add("Liberia");
        COUNTRIES.add("Libyan Arab Jamahiriya");
        COUNTRIES.add("Liechtenstein");
        COUNTRIES.add("Lithuania");
        COUNTRIES.add("Luxembourg");
        COUNTRIES.add("Macao");
        COUNTRIES.add("Macedonia, The Former Yugoslav Republic of");
        COUNTRIES.add("Madagascar");
        COUNTRIES.add("Malawi");
        COUNTRIES.add("Malaysia");
        COUNTRIES.add("Maldives");
        COUNTRIES.add("Mali");
        COUNTRIES.add("Malta");
        COUNTRIES.add(MARSHALL_ISLANDS);
        COUNTRIES.add("Martinique");
        COUNTRIES.add("Mauritania");
        COUNTRIES.add("Mauritius");
        COUNTRIES.add("Mayotte");
        COUNTRIES.add("Mexico");
        COUNTRIES.add("Micronesia, Federated States of");
        COUNTRIES.add("Moldova, Republic of");
        COUNTRIES.add("Monaco");
        COUNTRIES.add("Mongolia");
        COUNTRIES.add("Montserrat");
        COUNTRIES.add("Morocco");
        COUNTRIES.add("Mozambique");
        COUNTRIES.add("Myanmar");
        COUNTRIES.add("Namibia");
        COUNTRIES.add("Nauru");
        COUNTRIES.add("Nepal");
        COUNTRIES.add("Netherlands");
        COUNTRIES.add("Netherlands Antilles");
        COUNTRIES.add("New Caledonia");
        COUNTRIES.add("New Zealand");
        COUNTRIES.add("Nicaragua");
        COUNTRIES.add("Niger");
        COUNTRIES.add("Nigeria");
        COUNTRIES.add("Niue");
        COUNTRIES.add("Norfolk Island");
        COUNTRIES.add("Northern Mariana Islands");
        COUNTRIES.add("Norway");
        COUNTRIES.add("Oman");
        COUNTRIES.add("Pakistan");
        COUNTRIES.add(PALAU);
        COUNTRIES.add("Palestinian Territory, Occupied");
        COUNTRIES.add("Panama");
        COUNTRIES.add("Papua New Guinea");
        COUNTRIES.add("Paraguay");
        COUNTRIES.add("Peru");
        COUNTRIES.add("Philippines");
        COUNTRIES.add("Pitcairn");
        COUNTRIES.add("Poland");
        COUNTRIES.add("Portugal");
        COUNTRIES.add(PUERTO_RICO);
        COUNTRIES.add("Qatar");
        COUNTRIES.add("Reunion");
        COUNTRIES.add("Romania");
        COUNTRIES.add("Russian Federation");
        COUNTRIES.add("Rwanda");
        COUNTRIES.add("Saint Helena");
        COUNTRIES.add("Saint Kitts and Nevis");
        COUNTRIES.add("Saint Lucia");
        COUNTRIES.add("Saint Pierre and Miquelon");
        COUNTRIES.add("Saint Vincent and The Grenadines");
        COUNTRIES.add("Samoa");
        COUNTRIES.add("San Marino");
        COUNTRIES.add("Sao Tome and Principe");
        COUNTRIES.add("Saudi Arabia");
        COUNTRIES.add("Senegal");
        COUNTRIES.add("Serbia and Montenegro");
        COUNTRIES.add("Seychelles");
        COUNTRIES.add("Sierra Leone");
        COUNTRIES.add("Singapore");
        COUNTRIES.add("Slovakia");
        COUNTRIES.add("Slovenia");
        COUNTRIES.add("Solomon Islands");
        COUNTRIES.add("Somalia");
        COUNTRIES.add("South Africa");
        COUNTRIES.add("South Georgia and The South Sandwich Islands");
        COUNTRIES.add("Spain");
        COUNTRIES.add("Sri Lanka");
        COUNTRIES.add("Sudan");
        COUNTRIES.add("Suriname");
        COUNTRIES.add("Svalbard and Jan Mayen");
        COUNTRIES.add("Swaziland");
        COUNTRIES.add("Sweden");
        COUNTRIES.add("Switzerland");
        COUNTRIES.add("Syrian Arab Republic");
        COUNTRIES.add("Taiwan, Province of China");
        COUNTRIES.add("Tajikistan");
        COUNTRIES.add("Tanzania, United Republic of");
        COUNTRIES.add("Thailand");
        COUNTRIES.add("Timor-leste");
        COUNTRIES.add("Togo");
        COUNTRIES.add("Tokelau");
        COUNTRIES.add("Tonga");
        COUNTRIES.add("Trinidad and Tobago");
        COUNTRIES.add("Tunisia");
        COUNTRIES.add("Turkey");
        COUNTRIES.add("Turkmenistan");
        COUNTRIES.add("Turks and Caicos Islands");
        COUNTRIES.add("Tuvalu");
        COUNTRIES.add("Uganda");
        COUNTRIES.add("Ukraine");
        COUNTRIES.add("United Arab Emirates");
        COUNTRIES.add("United Kingdom");
        COUNTRIES.add("United States");
        COUNTRIES.add("United States Minor Outlying Islands");
        COUNTRIES.add("Uruguay");
        COUNTRIES.add("Uzbekistan");
        COUNTRIES.add("Vanuatu");
        COUNTRIES.add("Venezuela");
        COUNTRIES.add("Viet Nam");
        COUNTRIES.add("Virgin Islands, British");
        COUNTRIES.add("Virgin Islands, U.S.");
        COUNTRIES.add("Wallis and Futuna");
        COUNTRIES.add("Western Sahara");
        COUNTRIES.add("Yemen");
        COUNTRIES.add("Zambia");
        COUNTRIES.add("Zimbabwe");


        //countries with ISO code
        COUNTRIES2.put("US","UNITED STATES");
        COUNTRIES2.put("GB","UNITED KINGDOM");
        COUNTRIES2.put("AF","AFGHANISTAN");
        COUNTRIES2.put("AL","ALBANIA");
        COUNTRIES2.put("DZ","ALGERIA");
        COUNTRIES2.put("AS","AMERICAN SAMOA");
        COUNTRIES2.put("AD","ANDORRA");
        COUNTRIES2.put("AO","ANGOLA");
        COUNTRIES2.put("AI","ANGUILLA");
        COUNTRIES2.put("AQ","ANTARCTICA");
        COUNTRIES2.put("AG","ANTIGUA AND BARBUDA");
        COUNTRIES2.put("AR","ARGENTINA");
        COUNTRIES2.put("AM","ARMENIA");
        COUNTRIES2.put("AW","ARUBA");
        COUNTRIES2.put("AU","AUSTRALIA");
        COUNTRIES2.put("AT","AUSTRIA");
        COUNTRIES2.put("AZ","AZERBAIJAN");
        COUNTRIES2.put("BS","BAHAMAS");
        COUNTRIES2.put("BH","BAHRAIN");
        COUNTRIES2.put("BD","BANGLADESH");
        COUNTRIES2.put("BB","BARBADOS");
        COUNTRIES2.put("BY","BELARUS");
        COUNTRIES2.put("BE","BELGIUM");
        COUNTRIES2.put("BZ","BELIZE");
        COUNTRIES2.put("BJ","BENIN");
        COUNTRIES2.put("BM","BERMUDA");
        COUNTRIES2.put("BT","BHUTAN");
        COUNTRIES2.put("BO","BOLIVIA");
        COUNTRIES2.put("BA","BOSNIA AND HERZEGOVINA");
        COUNTRIES2.put("BW","BOTSWANA");
        COUNTRIES2.put("BV","BOUVET ISLAND");
        COUNTRIES2.put("BR","BRAZIL");
        COUNTRIES2.put("IO","BRITISH INDIAN OCEAN TERRITORY");
        COUNTRIES2.put("BN","BRUNEI DARUSSALAM");
        COUNTRIES2.put("BG","BULGARIA");
        COUNTRIES2.put("BF","BURKINA FASO");
        COUNTRIES2.put("BI","BURUNDI");
        COUNTRIES2.put("KH","CAMBODIA");
        COUNTRIES2.put("CM","CAMEROON");
        COUNTRIES2.put("CA","CANADA");
        COUNTRIES2.put("CV","CAPE VERDE");
        COUNTRIES2.put("KY","CAYMAN ISLANDS");
        COUNTRIES2.put("CF","CENTRAL AFRICAN REPUBLIC");
        COUNTRIES2.put("TD","CHAD");
        COUNTRIES2.put("CL","CHILE");
        COUNTRIES2.put("CN","CHINA");
        COUNTRIES2.put("CX","CHRISTMAS ISLAND");
        COUNTRIES2.put("CC","COCOS (KEELING) ISLANDS");
        COUNTRIES2.put("CO","COLOMBIA");
        COUNTRIES2.put("KM","COMOROS");
        COUNTRIES2.put("CG","CONGO");
        COUNTRIES2.put("CD","CONGO, THE DEMOCRATIC REPUBLIC OF THE");
        COUNTRIES2.put("CK","COOK ISLANDS");
        COUNTRIES2.put("CR","COSTA RICA");
        COUNTRIES2.put("CX","COTE D'IVOIRE");
        COUNTRIES2.put("HR","CROATIA");
        COUNTRIES2.put("CU","CUBA");
        COUNTRIES2.put("CY","CYPRUS");
        COUNTRIES2.put("CZ","CZECH REPUBLIC");
        COUNTRIES2.put("DK","DENMARK");
        COUNTRIES2.put("DJ","DJIBOUTI");
        COUNTRIES2.put("DM","DOMINICA");
        COUNTRIES2.put("DO","DOMINICAN REPUBLIC");
        COUNTRIES2.put("EC","ECUADOR");
        COUNTRIES2.put("EG","EGYPT");
        COUNTRIES2.put("SV","EL SALVADOR");
        COUNTRIES2.put("GQ","EQUATORIAL GUINEA");
        COUNTRIES2.put("ER","ERITREA");
        COUNTRIES2.put("EE","ESTONIA");
        COUNTRIES2.put("ET","ETHIOPIA");
        COUNTRIES2.put("FK","FALKLAND ISLANDS (MALVINAS)");
        COUNTRIES2.put("FO","FAROE ISLANDS");
        COUNTRIES2.put("FJ","FIJI");
        COUNTRIES2.put("FI","FINLAND");
        COUNTRIES2.put("FR","FRANCE");
        COUNTRIES2.put("GF","FRENCH GUIANA");
        COUNTRIES2.put("PF","FRENCH POLYNESIA");
        COUNTRIES2.put("TF","FRENCH SOUTHERN TERRITORIES");
        COUNTRIES2.put("GA","GABON");
        COUNTRIES2.put("GM","GAMBIA");
        COUNTRIES2.put("GE","GEORGIA");
        COUNTRIES2.put("DE","GERMANY");
        COUNTRIES2.put("GH","GHANA");
        COUNTRIES2.put("GI","GIBRALTAR");
        COUNTRIES2.put("GR","GREECE");
        COUNTRIES2.put("GL","GREENLAND");
        COUNTRIES2.put("GD","GRENADA");
        COUNTRIES2.put("GP","GUADELOUPE");
        COUNTRIES2.put("GU","GUAM");
        COUNTRIES2.put("GT","GUATEMALA");
        COUNTRIES2.put("GN","GUINEA");
        COUNTRIES2.put("GW","GUINEA-BISSAU");
        COUNTRIES2.put("GY","GUYANA");
        COUNTRIES2.put("HT","HAITI");
        COUNTRIES2.put("HM","HEARD ISLAND AND MCDONALD ISLANDS");
        COUNTRIES2.put("VA","HOLY SEE (VATICAN CITY STATE)");
        COUNTRIES2.put("HN","HONDURAS");
        COUNTRIES2.put("HK","HONG KONG");
        COUNTRIES2.put("HU","HUNGARY");
        COUNTRIES2.put("IS","ICELAND");
        COUNTRIES2.put("IN","INDIA");
        COUNTRIES2.put("ID","INDONESIA");
        COUNTRIES2.put("IR","IRAN, ISLAMIC REPUBLIC OF");
        COUNTRIES2.put("IQ","IRAQ");
        COUNTRIES2.put("IE","IRELAND");
        COUNTRIES2.put("IL","ISRAEL");
        COUNTRIES2.put("IT","ITALY");
        COUNTRIES2.put("JM","JAMAICA");
        COUNTRIES2.put("JP","JAPAN");
        COUNTRIES2.put("JO","JORDAN");
        COUNTRIES2.put("KZ","KAZAKHSTAN");
        COUNTRIES2.put("KE","KENYA");
        COUNTRIES2.put("KI","KIRIBATI");
        COUNTRIES2.put("KP","KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF");
        COUNTRIES2.put("KR","KOREA, REPUBLIC OF");
        COUNTRIES2.put("KW","KUWAIT");
        COUNTRIES2.put("KG","KYRGYZSTAN");
        COUNTRIES2.put("LA","LAO PEOPLE'S DEMOCRATIC REPUBLIC");
        COUNTRIES2.put("LV","LATVIA");
        COUNTRIES2.put("LB","LEBANON");
        COUNTRIES2.put("LS","LESOTHO");
        COUNTRIES2.put("LR","LIBERIA");
        COUNTRIES2.put("LY","LIBYAN ARAB JAMAHIRIYA");
        COUNTRIES2.put("LI","LIECHTENSTEIN");
        COUNTRIES2.put("LT","LITHUANIA");
        COUNTRIES2.put("LU","LUXEMBOURG");
        COUNTRIES2.put("MO","MACAO");
        COUNTRIES2.put("MK","MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF");
        COUNTRIES2.put("MG","MADAGASCAR");
        COUNTRIES2.put("MW","MALAWI");
        COUNTRIES2.put("MY","MALAYSIA");
        COUNTRIES2.put("MV","MALDIVES");
        COUNTRIES2.put("ML","MALI");
        COUNTRIES2.put("MT","MALTA");
        COUNTRIES2.put("MH","MARSHALL ISLANDS");
        COUNTRIES2.put("MQ","MARTINIQUE");
        COUNTRIES2.put("MR","MAURITANIA");
        COUNTRIES2.put("MU","MAURITIUS");
        COUNTRIES2.put("YT","MAYOTTE");
        COUNTRIES2.put("MX","MEXICO");
        COUNTRIES2.put("FM","MICRONESIA, FEDERATED STATES OF");
        COUNTRIES2.put("MD","MOLDOVA, REPUBLIC OF");
        COUNTRIES2.put("MC","MONACO");
        COUNTRIES2.put("MN","MONGOLIA");
        COUNTRIES2.put("MS","MONTSERRAT");
        COUNTRIES2.put("MA","MOROCCO");
        COUNTRIES2.put("MZ","MOZAMBIQUE");
        COUNTRIES2.put("MM","MYANMAR");
        COUNTRIES2.put("NA","NAMIBIA");
        COUNTRIES2.put("NR","NAURU");
        COUNTRIES2.put("NP","NEPAL");
        COUNTRIES2.put("NL","NETHERLANDS");
        COUNTRIES2.put("AN","NETHERLANDS ANTILLES");
        COUNTRIES2.put("NC","NEW CALEDONIA");
        COUNTRIES2.put("NZ","NEW ZEALAND");
        COUNTRIES2.put("NI","NICARAGUA");
        COUNTRIES2.put("NE","NIGER");
        COUNTRIES2.put("NG","NIGERIA");
        COUNTRIES2.put("NU","NIUE");
        COUNTRIES2.put("NF","NORFOLK ISLAND");
        COUNTRIES2.put("MP","NORTHERN MARIANA ISLANDS");
        COUNTRIES2.put("NO","NORWAY");
        COUNTRIES2.put("OM","OMAN");
        COUNTRIES2.put("PK","PAKISTAN");
        COUNTRIES2.put("PW","PALAU");
        COUNTRIES2.put("PS","PALESTINIAN TERRITORY, OCCUPIED");
        COUNTRIES2.put("PA","PANAMA");
        COUNTRIES2.put("PG","PAPUA NEW GUINEA");
        COUNTRIES2.put("PY","PARAGUAY");
        COUNTRIES2.put("PE","PERU");
        COUNTRIES2.put("PH","PHILIPPINES");
        COUNTRIES2.put("PN","PITCAIRN");
        COUNTRIES2.put("PL","POLAND");
        COUNTRIES2.put("PT","PORTUGAL");
        COUNTRIES2.put("PR","PUERTO RICO");
        COUNTRIES2.put("QA","QATAR");
        COUNTRIES2.put("RE","REUNION");
        COUNTRIES2.put("RO","ROMANIA");
        COUNTRIES2.put("RU","RUSSIAN FEDERATION");
        COUNTRIES2.put("RW","RWANDA");
        COUNTRIES2.put("SH","SAINT HELENA");
        COUNTRIES2.put("KN","SAINT KITTS AND NEVIS");
        COUNTRIES2.put("LC","SAINT LUCIA");
        COUNTRIES2.put("PM","SAINT PIERRE AND MIQUELON");
        COUNTRIES2.put("VC","SAINT VINCENT AND THE GRENADINES");
        COUNTRIES2.put("WS","SAMOA");
        COUNTRIES2.put("SM","SAN MARINO");
        COUNTRIES2.put("ST","SAO TOME AND PRINCIPE");
        COUNTRIES2.put("SA","SAUDI ARABIA");
        COUNTRIES2.put("SN","SENEGAL");
        COUNTRIES2.put("RS","SERBIA AND MONTENEGRO");
        COUNTRIES2.put("SC","SEYCHELLES");
        COUNTRIES2.put("SL","SIERRA LEONE");
        COUNTRIES2.put("SG","SINGAPORE");
        COUNTRIES2.put("SK","SLOVAKIA");
        COUNTRIES2.put("SI","SLOVENIA");
        COUNTRIES2.put("SB","SOLOMON ISLANDS");
        COUNTRIES2.put("SO","SOMALIA");
        COUNTRIES2.put("ZA","SOUTH AFRICA");
        COUNTRIES2.put("GS","SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS");
        COUNTRIES2.put("ES","SPAIN");
        COUNTRIES2.put("LK","SRI LANKA");
        COUNTRIES2.put("SD","SUDAN");
        COUNTRIES2.put("SR","SURINAME");
        COUNTRIES2.put("SJ","SVALBARD AND JAN MAYEN");
        COUNTRIES2.put("SZ","SWAZILAND");
        COUNTRIES2.put("SE","SWEDEN");
        COUNTRIES2.put("CH","SWITZERLAND");
        COUNTRIES2.put("SY","SYRIAN ARAB REPUBLIC");
        COUNTRIES2.put("TW","TAIWAN, PROVINCE OF CHINA");
        COUNTRIES2.put("TJ","TAJIKISTAN");
        COUNTRIES2.put("TZ","TANZANIA, UNITED REPUBLIC OF");
        COUNTRIES2.put("TH","THAILAND");
        COUNTRIES2.put("TL","TIMOR-LESTE");
        COUNTRIES2.put("TG","TOGO");
        COUNTRIES2.put("TK","TOKELAU");
        COUNTRIES2.put("TO","TONGA");
        COUNTRIES2.put("TT","TRINIDAD AND TOBAGO");
        COUNTRIES2.put("TN","TUNISIA");
        COUNTRIES2.put("TR","TURKEY");
        COUNTRIES2.put("TM","TURKMENISTAN");
        COUNTRIES2.put("TC","TURKS AND CAICOS ISLANDS");
        COUNTRIES2.put("TV","TUVALU");
        COUNTRIES2.put("UG","UGANDA");
        COUNTRIES2.put("UA","UKRAINE");
        COUNTRIES2.put("AE","UNITED ARAB EMIRATES");
        COUNTRIES2.put("GB","UNITED KINGDOM");
        COUNTRIES2.put("US","UNITED STATES");
        COUNTRIES2.put("UM","UNITED STATES MINOR OUTLYING ISLANDS");
        COUNTRIES2.put("UY","URUGUAY");
        COUNTRIES2.put("UZ","UZBEKISTAN");
        COUNTRIES2.put("VU","VANUATU");
        COUNTRIES2.put("VE","VENEZUELA");
        COUNTRIES2.put("VN","VIET NAM");
        COUNTRIES2.put("VG","VIRGIN ISLANDS, BRITISH");
        COUNTRIES2.put("VI","VIRGIN ISLANDS, U.S.");
        COUNTRIES2.put("WF","WALLIS AND FUTUNA");
        COUNTRIES2.put("EH","WESTERN SAHARA");
        COUNTRIES2.put("YE","YEMEN");
        COUNTRIES2.put("ZM","ZAMBIA");
        COUNTRIES2.put("ZW","ZIMBABWE");

        MX_STATES.put("Aguascalientes", "Aguascalientes");
        MX_STATES.put("Baja California N", "Baja California N");
        MX_STATES.put("Baja California S", "Baja California S");
        MX_STATES.put("Campeche", "Campeche");
        MX_STATES.put("Chiapas", "Chiapas");
        MX_STATES.put("Chihuahua", "Chihuahua");
        MX_STATES.put("Coahuila", "Coahuila");
        MX_STATES.put("Colima", "Colima");
        MX_STATES.put("Distrito Federal", "Distrito Federal");
        MX_STATES.put("Durango", "Durango");
        MX_STATES.put("Estado de México", "Estado de México");
        MX_STATES.put("Guanajuato", "Guanajuato");
        MX_STATES.put("Guerrero", "Guerrero");
        MX_STATES.put("Hidalgo", "Hidalgo");
        MX_STATES.put("Jalisco", "Jalisco");
        MX_STATES.put("México Region", "México Region");
        MX_STATES.put("Michoacán", "Michoacán");
        MX_STATES.put("Morelos", "Morelos");
        MX_STATES.put("Nayarit", "Nayarit");
        MX_STATES.put("Nuevo León", "Nuevo León");
        MX_STATES.put("Oaxaca", "Oaxaca");
        MX_STATES.put("Puebla", "Puebla");
        MX_STATES.put("Querétaro", "Querétaro");
        MX_STATES.put("Quintana Roo", "Quintana Roo");
        MX_STATES.put("San Luis Potosí", "San Luis Potosí");
        MX_STATES.put("Sinaloa", "Sinaloa");
        MX_STATES.put("Sonora", "Sonora");
        MX_STATES.put("Tabasco", "Tabasco");
        MX_STATES.put("Tamaulipas", "Tamaulipas");
        MX_STATES.put("Tlaxcala", "Tlaxcala");
        MX_STATES.put("Veracruz", "Veracruz");
        MX_STATES.put("Yucatán", "Yucatán");
        MX_STATES.put("Zacatecas", "Zacatecas");

        TW_COUNTY_LIST.put("Keelung City","Keelung City");
        TW_COUNTY_LIST.put("Taipei City","Taipei City");
        TW_COUNTY_LIST.put("New Taipei City","New Taipei City");
        TW_COUNTY_LIST.put("Taoyuan City","Taoyuan City");
        TW_COUNTY_LIST.put("Hsinchu City","Hsinchu City");
        TW_COUNTY_LIST.put("Hsinchu County","Hsinchu County");
        TW_COUNTY_LIST.put("Miaoli County","Miaoli County");
        TW_COUNTY_LIST.put("Taichung City","Taichung City");
        TW_COUNTY_LIST.put("Changhua County","Changhua County");
        TW_COUNTY_LIST.put("Nantou County","Nantou County");
        TW_COUNTY_LIST.put("Yunlin County","Yunlin County");
        TW_COUNTY_LIST.put("Chiayi City","Chiayi City");
        TW_COUNTY_LIST.put("Chiayi County","Chiayi County");
        TW_COUNTY_LIST.put("Tainan City","Tainan City");
        TW_COUNTY_LIST.put("Kaohsiung City","Kaohsiung City");
        TW_COUNTY_LIST.put("Pingtung County","Pingtung County");
        TW_COUNTY_LIST.put("Taitung County","Taitung County");
        TW_COUNTY_LIST.put("Hualien County","Hualien County");
        TW_COUNTY_LIST.put("Yilan County","Yilan County");
        TW_COUNTY_LIST.put("Wuhu County","Wuhu County");
        TW_COUNTY_LIST.put("Jinmen County","Jinmen County");
        TW_COUNTY_LIST.put("Lianjiang County","Lianjiang County");

        TW_COUNTY_LIST_ZHO.put("基隆市","基隆市");
        TW_COUNTY_LIST_ZHO.put("臺北市","臺北市");
        TW_COUNTY_LIST_ZHO.put("新北市","新北市");
        TW_COUNTY_LIST_ZHO.put("桃園市","桃園市");
        TW_COUNTY_LIST_ZHO.put("新竹市","新竹市");
        TW_COUNTY_LIST_ZHO.put("新竹縣","新竹縣");
        TW_COUNTY_LIST_ZHO.put("苗栗縣","苗栗縣");
        TW_COUNTY_LIST_ZHO.put("台中市","台中市");
        TW_COUNTY_LIST_ZHO.put("彰化縣","彰化縣");
        TW_COUNTY_LIST_ZHO.put("南投縣","南投縣");
        TW_COUNTY_LIST_ZHO.put("雲林縣","雲林縣");
        TW_COUNTY_LIST_ZHO.put("嘉義市","嘉義市");
        TW_COUNTY_LIST_ZHO.put("嘉義縣","嘉義縣");
        TW_COUNTY_LIST_ZHO.put("台南市","台南市");
        TW_COUNTY_LIST_ZHO.put("高雄市","高雄市");
        TW_COUNTY_LIST_ZHO.put("屏東縣","屏東縣");
        TW_COUNTY_LIST_ZHO.put("台東縣","台東縣");
        TW_COUNTY_LIST_ZHO.put("花蓮縣","花蓮縣");
        TW_COUNTY_LIST_ZHO.put("宜蘭縣","宜蘭縣");
        TW_COUNTY_LIST_ZHO.put("澎湖縣","澎湖縣");
        TW_COUNTY_LIST_ZHO.put("金門縣","金門縣");
        TW_COUNTY_LIST_ZHO.put("連江縣","連江縣");


        AU_STATES.put("ACT","ACT");
        AU_STATES.put("NSW","NSW");
        AU_STATES.put("NT","NT");
        AU_STATES.put("QLD","QLD");
        AU_STATES.put("SA","SA");
        AU_STATES.put("TAS","TAS");
        AU_STATES.put("VIC","VIC");
        AU_STATES.put("WA","WA");

        AE_CITIES.put("ABU DHABI","ABU DHABI");
        AE_CITIES.put("AJMAN","AJMAN");
        AE_CITIES.put("AL AQAA","AL AQAA");
        AE_CITIES.put("AL AIN","AL AIN");
        AE_CITIES.put("DUBAI","DUBAI");
        AE_CITIES.put("FUJAIRAH","FUJAIRAH");
        AE_CITIES.put("KHORFAKKAN","KHORFAKKAN");
        AE_CITIES.put("QIDFA","QIDFA");
        AE_CITIES.put("RAS AL KHAIMAH","RAS AL KHAIMAH");
        AE_CITIES.put("RUWAIS","RUWAIS");
        AE_CITIES.put("SHARJAH","SHARJAH");
        AE_CITIES.put("UMM AL QUWAIN","UMM AL QUWAIN");

        RU_STATES.put("Москва","Москва");
        RU_STATES.put("Московская область","Московская область");
        RU_STATES.put("Санкт-Петербург","Санкт-Петербург");
        RU_STATES.put("Екатеринбург","Екатеринбург");
        RU_STATES.put("Ростов-на-Дону","Ростов-на-Дону");
        RU_STATES.put("Самара","Самара");
        RU_STATES.put("Челябинск","Челябинск");
        RU_STATES.put("Казань","Казань");
        RU_STATES.put("Краснодар","Краснодар");
        RU_STATES.put("Нижний Новгород","Нижний Новгород");
        RU_STATES.put("Пермь","Пермь");
        RU_STATES.put("Уфа","Уфа");
        RU_STATES.put("Волгоград","Волгоград");
        RU_STATES.put("Краснодарский край","Краснодарский край");
        RU_STATES.put("Свердловская область","Свердловская область");
        RU_STATES.put("Ростовская область","Ростовская область");


    }

    public static final Map<String, Map<String, String>> STATE_BY_COUNTRY= new HashMap<>();
    public static final Map<String, Map<String, String>> CITIES_BY_COUNTRY= new HashMap<>();


    static {
        STATE_BY_COUNTRY.put("US", STATES);
        STATE_BY_COUNTRY.put("CA", CA_STATES);
        STATE_BY_COUNTRY.put("MX", MX_STATES);
        STATE_BY_COUNTRY.put("AU", AU_STATES);

        CITIES_BY_COUNTRY.put("AE", AE_CITIES);
    }

    /**
     * Gets states by country and if country is null then it assumes it is US
     *
     * @param country the country
     * @return the states by country
     */
    public static Map<String, String> getStatesByCountry(String country) {
        String countryLookingFor = country;
        //US is treated as default.
        if (null == countryLookingFor) {
            countryLookingFor = "US";
        }
        return STATE_BY_COUNTRY.get(countryLookingFor);
    }

    public static Map<String, String> getCitiesByCountryLocale(final String country, final Locale locale) {
        if (StringUtils.isEmpty(country)) {
            return Collections.EMPTY_MAP;
        }
        else if(country.equalsIgnoreCase(CommonConstants.COUNTRY_CODE_TW)){
            if (locale.getLanguage().equals(Locale.CHINESE.getLanguage())){
                return ImmutableMap.copyOf(TW_COUNTY_LIST_ZHO);
            }
            else{
                return ImmutableMap.copyOf(TW_COUNTY_LIST);
            }
        }
        else {
            return CITIES_BY_COUNTRY.get(country);
        }

    }

    public static Map<String, String> getCitiesByCountry(final String country) {
        if (StringUtils.isEmpty(country)) {
            return Collections.EMPTY_MAP;
        }
        return CITIES_BY_COUNTRY.get(country);
    }

    public static Map<String, String> getStatesByCode(String country, String state, Locale locale) {

        if (StringUtils.isNotBlank(country) && country.equalsIgnoreCase(CommonConstants.COUNTRY_CODE_MX)){
            return ImmutableMap.copyOf(MX_STATES);
        }

        if (CommonConstants.COUNTRY_CODE_AU.equalsIgnoreCase(country)){
            return ImmutableMap.copyOf(AU_STATES);
        }

        if(CommonConstants.COUNTRY_CODE_RU.equalsIgnoreCase(country)){
            return ImmutableMap.copyOf(RU_STATES);
        }

        if ((country == null) || (!country.equalsIgnoreCase("CA"))) {
            if ((state == null) || (!US_PROTECTORATES.containsKey(state))) {
                return ImmutableMap.copyOf(STATES);
            } else {
                return ImmutableMap.copyOf(US_PROTECTORATES);
            }
        } else {
            return ImmutableMap.copyOf(CA_STATES);
        }
    }
    public static Map<String, String> getCountriesByCode() {

        return ImmutableMap.copyOf(COUNTRIES2);
    }

    public static Map<String, String> getMilitaryCodes(){
        return ImmutableMap.copyOf(MILITARY_CODES);
    }

}

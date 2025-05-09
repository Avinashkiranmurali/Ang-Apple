package com.b2s.rewards.apple.model

import com.b2s.apple.model.finance.CreditCardDetails
import com.b2s.common.services.exception.ServiceException
import com.b2s.common.services.exception.ServiceExceptionEnums
import com.b2s.rewards.apple.util.AppleUtil
import com.b2s.rewards.common.context.AppContext
import com.b2s.rewards.common.util.CommonConstants
import com.b2s.shop.common.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification

class AppleUtilSpec extends Specification {

    def 'test encodeSpecialChar()'() {
        AppleUtil uu = new AppleUtil();
        given:
        final def String value = passValue

        when:
        final def result = AppleUtil.encodeSpecialChar(value)

        then:
        result.contains('%25') == expected

        where:
        passValue   ||  expected
        ""          ||  false
        "with%"     ||  true
        "out"       ||  false

    }

    def 'test skuSearch()'() {
        when:
        String result = AppleUtil.skuSearch(keyword)

        then:
        expected == result

        where:
        keyword                         || expected
        null                            || null
        'MVQ22ZM/A, HMQJ2ZM/A'          || 'MVQ22ZMA HMQJ2ZMA'
        'MVQ22ZM/A, HMQJ2ZM/A, macbook' || 'MVQ22ZMA HMQJ2ZMA macbook'
        'iphone 7, ipad, macbook'       || 'iphone 7, ipad, macbook'
    }

    def 'test populateBillTo()'() {
        given:
        User user = getUser()
        CreditCardDetails cardDetails= getCardDetails()

        when:
        AppleUtil.populateBillTo(user, cardDetails)

        then:
        user.billTo != null
        user.billTo.city == cardDetails.city
    }

    def 'test buildReturnHostPrefix() with value from property'() {
        given:
        final request = new MockHttpServletRequest()
        final session = new MockHttpSession()
        final context = Mock(ApplicationContext)

        session.setAttribute(CommonConstants.USER_SESSION_OBJECT, getUser())
        request.session = session
        AppContext.applicationContext = context
        Properties props = new Properties()
        props.put("ua.redirect.home.url", "http://localhost")
        AppContext.applicationContext.getBean(CommonConstants.APPLICATION_PROPERTIES, Properties.class) >> props

        when:
        final url = AppleUtil.buildReturnHostPrefix(request)

        then:
        url == "http://localhost"

    }

    def 'test buildReturnHostPrefix() without value from property'() {
        given:
        final request = new MockHttpServletRequest()
        final session = new MockHttpSession()
        final context = Mock(ApplicationContext)
        request.serverPort = 90
        request.scheme = "http"
        request.serverName = "server"
        when:
        final url = AppleUtil.buildReturnHostPrefix(request)

        then:
        url == "http://server:90"

    }

    def 'test validateInit()'() {
        given:
        final request = new MockHttpServletRequest()
        final session = new MockHttpSession()
        CreditCardDetails cardDetails= getCardDetails()

        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, getProgram())
        request.session = session

        when:
        AppleUtil.validateInit(cardDetails, request)

        then:
        notThrown ServiceException

    }

    def 'test validateInit() throws Exception'() {
        given:
        final request = new MockHttpServletRequest()
        final session = new MockHttpSession()
        CreditCardDetails cardDetails= getCardDetails()
        Program program = getProgram()
        program.ccFilters.get(0).filter = "9778457"
        session.setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program)
        request.session = session

        when:
        AppleUtil.validateInit(cardDetails, request)

        then:
        ServiceException ex = thrown()
        ex.message == ServiceExceptionEnums.BIN_FILTER_EXCEPTION.errorMessage

    }

    def 'getValueFromProgramConfig'(){

        given:
        def program=Mock(Program.class)


        when:
        def output=AppleUtil.getValueFromProgramConfig(program,key,defaultValue)

        then:
        program.getConfig()>> Mock(HashMap.class)
        program.getConfig().containsKey(key) >> containesKey
        program.getConfig().get(key) >> mockedVPCData
        output == result

        where:
        key         |   defaultValue   | containesKey   |  mockedVPCData        ||  result
        "aliveUrl"  |   "default"      |      true      |  "https://url"        ||  "https://url"
        "aliveUrl"  |   "default"      |      true      |  ""                   ||  "default"
        "aliveUrl"  |   null           |      true      |  ""                   ||   ""
        "aliveUrl"  |   null           |      false     |  "https://url"        ||   null
        "aliveUrl"  |   "default"      |      false     |  "https://url"        ||  "default"
        "keepAliveJSONP"  |   "false"     |      true     |  true                 ||  "true"
        "keepAliveJSONP"  |   "false"     |      true     |  false                ||  "false"
        "keepAliveJSONP"  |   "false"     |      false     |  ""                ||  "false"
        "keepAliveJSONP"  |   "true"      |      false     |  ""                ||  "true"

    }

    def 'testDeeplinkURL'() {
        when:
        String result = AppleUtil.getDeeplinkForAngular(keyword)

        then:
        expected == result

        where:
        keyword                         || expected
        null                            || null
        'https://webapp-vip.aplqa1.bridge2solutions.net/apple-gr/merchandise/landing.jsp#/store/configure/ipad/ipad-pro/'          || 'https://webapp-vip.aplqa1.bridge2solutions.net/apple-gr/ui/store/configure/ipad/ipad-pro/'
       }



    def getUser() {
        User user = new User()
        user.varId = "UA"
        user.programId = "MP"
        user.locale = Locale.US
        return user
    }

    def getProgram(){
        Program program=new Program()
        List<Program.CCBin> ccBinList = new ArrayList<>()
        Program.CCBin ccBin = new Program.CCBin()
        ccBin.filter = "4929736909778457"
        ccBinList.add(ccBin)
        program.ccFilters = ccBinList
        return program
    }

    def getCardDetails(){
        CreditCardDetails cardDetails=new CreditCardDetails()
        cardDetails.ccNum = "4929736909778457"
        cardDetails.ccUsername = "Saranya Kathirvel"
        cardDetails.addr1 = "4236  Veltri Drive"
        cardDetails.city = "Anchorage"
        cardDetails.state = "AK"
        cardDetails.zip = "23456"
        cardDetails.country = "US"
        cardDetails.phoneNumber = "907-272-3609"
        return cardDetails
    }

    def 'test isStringsEqualIgnoreCase'(){
        when:
        boolean result = AppleUtil.isStringsEqualIgnoreCase(keyword1, keyword2)

        then:
        expected == result

        where:
        keyword1    || keyword2     || expected
        null        || null         || true
        null        || 'test'       || false
        ''          || '   '        || false
        'random'    || 'RanDom'     || true
        'random'    || 'RanDome'    || false
    }

    def 'test getAmountWithoutCurrencyCode'(){
        when:
        String result = AppleUtil.getAmountWithoutCurrencyCode(amount)

        then:
        expected == result

        where:
        amount       || expected
        null         || null
        'test'       || null
        '   '        || null
        'USD 123'    || '123'
        'USD 0123'   || '0123'
        'USD123'     || null
    }

    def 'test isValidCurrencyCode'(){
        when:
        boolean result = AppleUtil.isValidCurrencyCode(amount)

        then:
        expected == result

        where:
        amount       || expected
        null         || false
        'test'       || false
        '   '        || false
        'USD'        || true
        'USD 0123'   || false
        'EUR'        || true
    }

    def 'test isNotificationAmp()' () {
        when:
        boolean result = AppleUtil.isNotificationAmp(notificationName)

        then:
        expected == result

        where:
        notificationName                               || expected
        CommonConstants.NotificationName.AMP_MUSIC     || true
        CommonConstants.NotificationName.AMP_NEWS_PLUS || true
        CommonConstants.NotificationName.AMP_TV_PLUS   || true
        CommonConstants.NotificationName.CONFIRMATION  || false
        CommonConstants.NotificationName.ECERT         || false
    }

    def 'test parseStringToMap()'() {
        when:
        Map<String, String> result = AppleUtil.parseStringToMap("jp_rpc=0420&jp_aoc=00420", CommonConstants.AND,
                CommonConstants.EQUAL)

        then:
        result != null
        result.size() == 2
        result.get("jp_rpc").equalsIgnoreCase("0420")
        result.get("jp_aoc").equalsIgnoreCase("00420")

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AppleUtilSpec.class);

    void main(String[] args) {//Use static for Stand-alone execution
        AppleUtilSpec serviceSpec = new AppleUtilSpec()
        serviceSpec.readSheetAndGenerateDBScript()
    }

    /*enum Constraints{
        PK("Primary key")

        String excelDesc
        //String logStmt
        Constraints(String excelDesc){
            this.excelDesc = excelDesc
            //this.logStmt = logStmt
        }
    }*/

    Action addTitle(StandardizationDB record, List<String> keyToBeConsidered) {
        if (record.getOrder().contains("Drop_Order")) {
            LOGGER.error("DROPPING " + keyToBeConsidered + " constraints starts for " + record.getOldConstraintName() + " environment.")
            return Action.DROP
        }
        if (record.getOrder().contains("Create_Order")) {
            LOGGER.error("CREATING " + keyToBeConsidered + " constraints starts for " + record.getOldConstraintName() + " environment.")
            return Action.CREATE
        }
    }

    void printDropScript(StandardizationDB record) {
        LOGGER.warn("IF EXISTS (SELECT * FROM sys.objects WHERE name ='" + record.getOldConstraintName() + "')")
        LOGGER.warn("BEGIN")
        LOGGER.warn(sp2 + "ALTER TABLE " + record.getTableName() + " DROP CONSTRAINT " + record.getOldConstraintName() + ";")
        LOGGER.warn("END")
        LOGGER.warn("GO")
        LOGGER.warn("")
    }

    void createPK(StandardizationDB record) {
        LOGGER.warn("IF EXISTS (SELECT * FROM sys.tables WHERE object_id = OBJECT_ID(N'[dbo].[" + record.getTableName() + "]'))")
        LOGGER.warn("AND NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS TC JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE CCU")
        LOGGER.warn(sp2 + "ON TC.CONSTRAINT_NAME = CCU.Constraint_name WHERE TC.TABLE_NAME = '" + record.getTableName() + "' AND TC.CONSTRAINT_TYPE = 'PRIMARY KEY')")
        LOGGER.warn("BEGIN")
        LOGGER.warn(sp2 + "ALTER TABLE [dbo].[" + record.getTableName() + "] ADD CONSTRAINT [" + record.getNewConstraintName() + "] " +
                "PRIMARY KEY (" + record.getConstraintKeys() + ");")
        LOGGER.warn("END")
        LOGGER.warn("GO")
        LOGGER.warn("")
    }

    void createFK(StandardizationDB record) {
        LOGGER.warn("IF EXISTS (SELECT * FROM sys.tables WHERE object_id = OBJECT_ID(N'[dbo].[" + record.getTableName() + "]'))")
        LOGGER.warn("AND EXISTS (SELECT * FROM sys.tables WHERE object_id = OBJECT_ID(N'[dbo].[" + record.getDefaultOrParentTable() + "]'))")
        LOGGER.warn("AND NOT EXISTS (SELECT * FROM sys.objects WHERE name ='" + record.getNewConstraintName() + "')")
        LOGGER.warn("BEGIN")
        LOGGER.warn(sp2 + "ALTER TABLE [dbo].[" + record.getTableName() + "] ADD CONSTRAINT [" + record.getNewConstraintName() + "]")
        LOGGER.warn(sp2 + sp2 + "FOREIGN KEY (" + record.getConstraintKeys() + ") REFERENCES " + record.getDefaultOrParentTable() + " (" + record.getParentFKeys() + ");")
        LOGGER.warn("END")
        LOGGER.warn("GO")
        LOGGER.warn("")
    }

    void createDC(StandardizationDB record) {
        LOGGER.warn("IF EXISTS (SELECT * FROM sys.tables WHERE object_id = OBJECT_ID(N'[dbo].[" + record.getTableName() + "]'))")
        LOGGER.warn("AND NOT EXISTS (SELECT o.name FROM sysobjects o")
        LOGGER.warn(sp2 + "INNER JOIN syscolumns c ON o.id = c.cdefault")
        LOGGER.warn(sp2 + "INNER JOIN sysobjects t ON c.id = t.id")
        LOGGER.warn(sp2 + "WHERE o.xtype = 'D'")
        LOGGER.warn(sp2 + "AND c.name IN ('" + record.getConstraintKeys() + "')")
        LOGGER.warn(sp2 + "AND t.name = '" + record.getTableName() + "'")
        LOGGER.warn(sp2 + "AND o.name = '" + record.getNewConstraintName() + "')")
        LOGGER.warn("BEGIN")
        LOGGER.warn(sp2 + "ALTER TABLE [dbo].[" + record.getTableName() + "] ADD CONSTRAINT [" + record.getNewConstraintName() + "] " +
                "DEFAULT " + record.getDefaultOrParentTable() + " FOR [" + record.getConstraintKeys() + "];")
        LOGGER.warn("END")
        LOGGER.warn("GO")
        LOGGER.warn("")
    }

    void createUC(StandardizationDB record) {
        LOGGER.error("DON'T REFER THIS SCRIPT AT THIS POINT OF TIME");
        LOGGER.warn("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name = '" + record.getNewConstraintName() + "' AND type = 'UQ'")
        LOGGER.warn("BEGIN")
        LOGGER.warn(sp2 + "ALTER TABLE [dbo].[" + record.getTableName() + "] ADD CONSTRAINT [" + record.getNewConstraintName() + "] UNIQUE (" + record.getConstraintKeys() + ");")
        LOGGER.warn("END")
        LOGGER.warn("GO")
        LOGGER.warn("")
    }

    static String sp2 = "  "
    enum Action {
        CREATE, DROP
    }

    enum PLATFORM {
        APPLE(17, "Q"),
        CHASE(12, "L"),
        CITI(14, "N")

        PLATFORM(int lastCol, String alphabet){
            this.lastCol = lastCol
            this.alphabet = alphabet
        }

        int lastCol
        String alphabet

        public int getColSize(){
            return lastCol
        }

        public String getAlphabet(){
            return alphabet
        }
    }

    List<StandardizationDB> readFile() {
        String line
        int order = 0
        int tableName = 1
        int constraintType = 3

        //APPLE: Dev-4, AngDev-5, QA1-6, AngQA1-7, QA2-8, UAT-9, BF-10, VIT2-11, PROD-12
        //CHASE: BF-4, UAT-5, QA-6, PROD-7
        //CITI: PROD-4, PAT-5, DEV-6, PUAT-7, QA-8, UAT-9
        PLATFORM platform = PLATFORM.APPLE
        int env = 5
        String filePath = "C:\\sheet\\read.csv";

        int constraintKeys = platform.getColSize() - 4
        int stdConstraintName = platform.getColSize() - 3
        int option = platform.getColSize() - 2 //FK_Parent_Table or Default
        int fkParentKeys = platform.getColSize() - 1

        List<StandardizationDB> tempList = new ArrayList<>()
        FileReader fileReader = null
        BufferedReader br = null
        try {
            fileReader = new FileReader(filePath)
            br = new BufferedReader(fileReader)

            while ((line = br.readLine()) != null) {
                String[] ar = line.split(",")
                String parentFKeys = null
                String defaultOrParentTable = null
                if (ar.length == platform.getColSize()) {
                    defaultOrParentTable = ar[option]
                    parentFKeys = ar[fkParentKeys]
                } else if (ar.length == (platform.getColSize() - 1)) {
                    defaultOrParentTable = ar[option]
                }
                tempList.add(new StandardizationDB(ar[order], ar[tableName], ar[constraintType], ar[env], ar[constraintKeys], ar[stdConstraintName], defaultOrParentTable, parentFKeys))
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while reading Records: " + ex)
        } finally {
            fileReader.close()
            br.close()
        }
        return tempList
    }

    void readSheetAndGenerateDBScript() {
        String PK = "Primary key"
        String FK = "Foreign key"
        String DC = "Default constraint"
        String UC = "Unique constraint"
        try {
            LOGGER.error("CAUTION: Check Environment ")
            List<String> keyToBeConsidered = Arrays.asList(PK, FK, DC, UC)
            Action action = null;
            for (StandardizationDB record : readFile()) {
                if (!"Table Not Present".equalsIgnoreCase(record.getOldConstraintName())) {
                    if (record.getTableName() == "Tables") {
                        action = addTitle(record, keyToBeConsidered)
                    } else {
                        if (keyToBeConsidered.contains(record.getConstraintType())) {
                            if (action == Action.DROP) {
                                printDropScript(record)
                            } else if (action == Action.CREATE) {
                                if (PK == record.getConstraintType()) {
                                    createPK(record)
                                }
                                if (FK == record.getConstraintType()) {
                                    createFK(record)
                                }
                                if (DC == record.getConstraintType()) {
                                    createDC(record)
                                }
                                if (UC == record.getConstraintType()) {
                                    LOGGER.error("Unique Constraint - Still in DEV phase!!!")
                                    createUC(record)
                                }
                            } else {
                                LOGGER.error("Data issue.. Create/Drop constraint? Please check!!!")
                            }
                        } else {
                            LOGGER.error("Data issue.. Invalid Constraint type? " + record.getConstraintType() + " Please check!!!")
                        }
                    }
                } else {
                    LOGGER.error("Not considering row: " + record.getOrder() + ".")
                }
            }
        } catch (IOException e) {
            LOGGER.error("Exception: " + e)
        }
    }
}

class StandardizationDB {
    String order
    String tableName
    String constraintType
    String oldConstraintName
    String constraintKeys
    String newConstraintName
    String defaultOrParentTable
    String parentFKeys

    StandardizationDB(String order, String tableName, String constraintType, String oldConstraintName,
                      String constraintKeys, String newConstraintName, String defaultValue, String parentFKeys) {
        this.order = order
        this.tableName = tableName
        this.constraintType = constraintType
        this.oldConstraintName = oldConstraintName
        this.constraintKeys = constraintKeys
        this.newConstraintName = newConstraintName
        this.defaultOrParentTable = defaultValue
        this.parentFKeys = parentFKeys
    }
}

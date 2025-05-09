package com.b2s.rewards.apple.validator

import com.b2s.common.services.exception.ServiceExceptionEnums
import com.b2s.rewards.apple.model.Address
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.apple.util.CartItemOption
import com.b2s.shop.common.User
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.context.support.StaticMessageSource
import spock.lang.Specification
import spock.lang.Subject

import static com.b2s.rewards.apple.validator.AddressValidatorConstants.*
import static com.b2s.rewards.common.util.CommonConstants.CHAR_SPACE_AND_DASH_ONLY_REG_EX
import static com.b2s.rewards.common.util.CommonConstants.CITY_CA_FRENCH_REG_EX

class AddressValidatorImplSpec extends Specification {

    MessageSource messageSource
    def invalidZip4 = "4 digit Zip Code is invalid"
    def static invalidName = "First and Last name must be alpha characters, each with 1 characters minimum."
    def static invalidAddress1 = "Please enter a valid address"
    def static invalidCity = "Please provide a valid city name."
    def static invalidZip5 = "Please enter a valid 5 digit zip code"
    def static invalidPhoneNumber  = "Enter a valid phone number"
    def static invalidCountry  = "Please enter a valid Country"
    def static invalidState  = "Please enter a valid State"
    def static invalidEmailAddress = "Invalid email address"
    def static poBoxNotAllowed = "Item cannot be shipped to PO Box address"
    def static invalidDistrict = "Please enter a District."

    def setup() {
        messageSource = new StaticMessageSource()
        messageSource.addMessage(INVALID_ZIP_4, Locale.US, invalidZip4)
        messageSource.addMessage(INVALID_ZIP_4, Locale.UK, invalidZip4)
        messageSource.addMessage(INVALID_NAME, Locale.US, invalidName)
        messageSource.addMessage(INVALID_ADDRESS_LINE_1, Locale.US, invalidAddress1)
        messageSource.addMessage(INVALID_CITY, Locale.US, invalidCity)
        messageSource.addMessage(INVALID_CITY, Locale.CANADA, invalidCity)
        messageSource.addMessage(INVALID_CITY, new Locale('en', 'AE'), invalidCity)
        messageSource.addMessage(INVALID_ZIP_5, Locale.ENGLISH, invalidZip5)
        messageSource.addMessage(INVALID_ZIP_5, Locale.US, invalidZip5)
        messageSource.addMessage(INVALID_PHONE_NUMBER, Locale.US, invalidPhoneNumber)
        messageSource.addMessage(INVALID_PHONE_NUMBER, new Locale('ru', 'RU'), invalidPhoneNumber)
        messageSource.addMessage(COUNTRY_MISSING, Locale.US, invalidCountry)
        messageSource.addMessage(STATE_PROVINCE_MISSING, Locale.US, invalidState)
        messageSource.addMessage(STATE_PROVINCE_MISSING, Locale.CANADA, invalidState)
        messageSource.addMessage(INVALID_EMAIL_ADDRESS, Locale.US, invalidEmailAddress)
        messageSource.addMessage(INVALID_EMAIL_ADDRESS, Locale.CANADA, invalidEmailAddress)
        messageSource.addMessage(PO_BOX_NOT_ALLOWED, Locale.US, poBoxNotAllowed)
    }

    @Subject
    private AddressValidatorMock addressValidator = new AddressValidatorMock()

    private AddressValidatorRUImpl addressValidatorRU = new AddressValidatorRUImpl()

    def 'isInvalidAddressUpdate() - businessNameLocked flag'() {

        when:
        def result = addressValidator.isInvalidAddressUpdate(cartAddress, newShippingAddress, program)

        then:
        result == expected

        where:

        expected | program                                                      | cartAddress              | newShippingAddress
        true     | getProgram('MX', 'b2s_qa_only', 'businessNameLocked', true)  | cart('Saranya', 'Bakkt') | cart('Saranya', '')
        false    | getProgram('MX', 'b2s_qa_only', 'businessNameLocked', true)  | cart('Saranya', 'Bakkt') | cart('Saranya', 'Bakkt')
        false    | getProgram('MX', 'b2s_qa_only', 'businessNameLocked', false) | cart('Saranya', 'Bakkt') | cart('Saranya', 'Bakkt')
        true     | getProgram('WF', 'b2s_qa_only', 'ShipToNameLocked', true)    | cart('', 'Bakkt')        | cart('Saranya', 'Bakkt')
        false    | getProgram('WF', 'b2s_qa_only', 'ShipToNameLocked', false)   | cart('Saranya', 'Bakkt') | cart('Saranya', 'Bakkt')
        false    | getProgram('WF', 'b2s_qa_only', 'ShipToNameLocked', true)    | cart('Saranya', 'Bakkt') | cart('Saranya', 'Bakkt')
        false    | getProgram('WF', 'b2s_qa_only', 'mercAddressLocked', false)  | cart('Saranya', 'Bakkt') | cart('Saranya', 'Bakkt')

    }

    def 'mandatoryNullCheck'() {
        given:
        def address = new Address()
        when:
        addressValidator.mandatoryNullCheck(address, fieldValue, serviceErrorMessage, field, defaultMessage)

        then:
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == defaultMessage
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:

        fieldValue| serviceErrorMessage                                                | field                                  | defaultMessage
        null      | ServiceExceptionEnums.CITY_EMPTY_EXCEPTION.getErrorMessage()       | CartItemOption.CITY.getValue()         | invalidCity
        null      | ServiceExceptionEnums.COUNTRY_EMPTY_EXCEPTION.getErrorMessage()    | CartItemOption.COUNTRY.getValue()      | invalidCountry //AU
        null      | ServiceExceptionEnums.ADDRESS_EMPTY_EXCEPTION.getErrorMessage()    | CartItemOption.ADDRESS1.getValue()     | invalidAddress1 // default
        null      | ServiceExceptionEnums.STATE_EMPTY_EXCEPTION.getErrorMessage()      | CartItemOption.STATE.getValue()        | invalidState // MX
        null      | ServiceExceptionEnums.CITY_TEXT_EXCEPTION.getErrorMessage()        | CartItemOption.SUBCITY.getValue()      | invalidDistrict // TH
        "Saranya" | ServiceExceptionEnums.ALPHA_ONLY_CHARS_EXCEPTION.getErrorMessage() | CartItemOption.FIRST_NAME.getValue()   | invalidName

    }

    def 'mandatoryPhoneCheckandSet() - format the phone number'() {
        given:
        def address = addressWithCountry
        address.phoneNumber = phoneNumber

        when:
        addressValidator.mandatoryPhoneCheckandSet(address, messageSource, countryLocale)

        then:
        if(address.errorMessage.size() == 0){
            assert address.phoneNumber == formattedNumber
        }
        else {
            assert address.errorMessage.toString().contains("phoneNumber:invalidPhoneNumber")
        }

        where:

        countryLocale                           |   addressWithCountry          |   phoneNumber         | formattedNumber
        Locale.US                               |   getAddress("US")    |   "8143847122"        | "(814) 384-7122"   //UA
        new Locale("es", "MX") |   getAddress("MX")    |   "315-963-8400"      | "315 963 8400"     //MX
        new Locale("en", "CA") |   getAddress("CA")    |   "7705551234"        | "(770) 555-1234"   //VitalityCA
        new Locale("en", "SG") |   getAddress("SG")    |   "+65 65424555"      | "6542 4555"        //SG
        new Locale("en", "AU") |   getAddress("AU")    |   "+61262399500"      | "(02) 6239 9500"   //AU
    }

    def 'mandatoryCityCheck'() {
        given:
        def address = getAddress("AE")
        address.city = fieldValue
        def field = CartItemOption.CITY.getValue()

        when:
        addressValidator.mandatoryCityCheck(address, messageSource, new Locale('en','AE'))

        then:
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == defaultMessage
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:

        fieldValue  | defaultMessage
        null        | invalidCity //AE
        "Dubai"     | invalidCity //AE - city exists, no error will be thrown
        "SHARJA"    | invalidCity //AE

    }

    def 'mandatoryZipCheck() - zip Length' () {
        given:
        def address = new Address()
        address.zip5 = fieldValue
        def field = CartItemOption.ZIP5.getValue()

        when:
        addressValidator.mandatoryZipCheck(address, zipLength, messageSource, Locale.ENGLISH)

        then:
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == invalidZip5
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:
        fieldValue | zipLength
        "6009"     | 4         //AU
        "15620"    | 5         //MX
        "408600"   | 6         //SG
        "30005"    | 4         //PH
        "1562A"    | 5         //MY

    }

    def 'mandatoryZipCheck() - min, max length' () {
        given:
        def address = new Address()
        address.zip5 = fieldValue
        def field = CartItemOption.ZIP5.getValue()

        when:
        addressValidator.mandatoryZipCheck(address, minLength, maxLength, invalidZip5)

        then:
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == invalidZip5
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:
        fieldValue | minLength| maxLength
        "6009"     | 4        | 4         //CH
        "15620"    | 5        | 5         //FR
        "408600"   | 6        | 6         //RU
        "11060"    | 3        | 5         //TW
        "546080"   | 3        | 5         //TW
        "15620"    | 4        | 4         //ZA

    }

    def 'mandatoryEmailCheck'() {
        given:
        def address = new Address()
        address.email = fieldValue
        def field = CartItemOption.EMAIL.getValue()

        when:
        addressValidator.mandatoryEmailCheck(address, messageSource, Locale.US)

        then:
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == defaultMessage
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:

        fieldValue      | defaultMessage
        "xyz@bakkt.com" | null
        "xyz@bakkt.in"  | null
        "xyz.bakkt.in"  | invalidEmailAddress

    }

    def 'mandatoryStateCheck'() {
        given:
        def address = addressWithCountry
        address.state = fieldValue
        def field = CartItemOption.STATE.getValue()

        when:
        addressValidator.mandatoryStateCheck(address, messageSource, locale)

        then:
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == defaultMessage
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:

        locale          | addressWithCountry       | fieldValue | defaultMessage
        Locale.CANADA   | getAddress("CA") | "AB"       | null //CA - State exists
        Locale.CANADA   | getAddress("CA") | "AC"       | invalidState //CA - // Invalid State
        Locale.US       | getAddress("US") | "AK"       | null //US - State exists
        Locale.US       | getAddress("US") | "BC"       | invalidState //US - // Invalid State

    }

    def 'replaceSpecialCharWithEmpty'() {
        given:
        def address = getAddressWithSpecialChar()

        when:
        addressValidator.replaceSpecialCharWithEmpty(address, Locale.US.toString())

        then:
        address.firstName == "ab c"
        address.lastName == "xyz"
        address.city == "NEW BERLIN"    //'//' is replaced with empty string
        address.address1 == "123"   //'<>' is replaced with empty string
        address.address2 == "#2nd street"   //';' is replaced with empty string
        address.address3 == ""
        address.businessName == "[B2S]"

    }

    def 'zipCode4LengthValidation'() {
        given:
        def address = new Address()
        address.zip4 = zip4
        Locale locale = localeValue
        def field = CartItemOption.ZIP4.getValue()

        when:
        addressValidator.zipCode4LengthValidation(address, messageSource, locale)

        then:
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == invalidZip4
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:
        localeValue |   zip4
        Locale.UK   |   "4711"      //GB
        Locale.UK   |   "DG98HX"    //GB
        Locale.US   |   null        //US

    }

    def 'hasValidationError()'() {
        given:
        final ApplicationContext mockApplicationContext = Mock(ApplicationContext)
        mockApplicationContext.getBean(_ as String) >> messageSource
        def address = addressObject;
        def user = getUser()

        when:
        def result = addressValidator.hasValidationError(address, null, user)

        then:
        result == expected
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == errorMessageString
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:
        expected | addressObject                            | field                                    | errorMessageString
        true     | new Address(firstName: '')               | CartItemOption.FIRST_NAME.getValue()     | invalidName
        true     | new Address(address1 : '')               | CartItemOption.ADDRESS1.getValue()       | invalidAddress1
        true     | new Address(city : '@b')                 | CartItemOption.CITY.getValue()           | invalidCity
        true     | new Address(zip5 : '303032')             | CartItemOption.ZIP5.getValue()           | invalidZip5
        true     | new Address(zip4 : '')                   | CartItemOption.ZIP4.getValue()           | null
        true     | new Address(phoneNumber :'1234567890')   | CartItemOption.PHONE_NUMBER.getValue()   | invalidPhoneNumber
        true     | new Address(country : '')                | CartItemOption.COUNTRY.getValue()        | invalidCountry
        true     | new Address(state : '')                  | CartItemOption.STATE.getValue()          | invalidState
        false    | getUSAddress()                           | null                                     | null       // Without error

    }

    def 'validateAndSetAddress()'() {

        given:
        def address = addressObject

        when:
        addressValidator.validateAndSetAddress(address, messageSource, Locale.US)

        then:
        if(address.errorMessage.size() > 0){
            assert address.errorMessage.get(field) == errorMessageString
        }
        else {
            assert address.errorMessage.get(field) == null
        }

        where:
        expected | addressObject                            | field                                    | errorMessageString
        true     | new Address(firstName: '')               | CartItemOption.FIRST_NAME.getValue()     | invalidName
        true     | new Address(address1 : '')               | CartItemOption.ADDRESS1.getValue()       | invalidAddress1
        true     | new Address(address1 : 'PO Box 1033')    | CartItemOption.ADDRESS1.getValue()       | poBoxNotAllowed
        true     | new Address(email : 'xyz.bakkt.in')      | CartItemOption.EMAIL.getValue()          | invalidEmailAddress
        true     | new Address(country : '')                | CartItemOption.COUNTRY.getValue()        | invalidCountry
        true     | new Address(email : 'xyz@bakkt.com')     | CartItemOption.EMAIL.getValue()          | null   // no error message for email
        false    | getUSAddress()                           | null                                     | null   // success flow
    }

    def 'validateCity()'() {

        given:
        def address = addressObject
        def field = CartItemOption.CITY.getValue()

        when:
        addressValidator.validateCity(address, messageSource, locale, pattern)

        then:
        address.errorMessage.get(field) == errorMessageString

        where:
        expected | addressObject            | locale       | errorMessageString | pattern
        true     | new Address(city : '#&a')| Locale.CANADA| invalidCity        | CITY_CA_FRENCH_REG_EX
        true     | new Address(city : '@b') | Locale.US    | invalidCity        | CHAR_SPACE_AND_DASH_ONLY_REG_EX
        true     | new Address(city : '')   | Locale.US    | invalidCity        | CHAR_SPACE_AND_DASH_ONLY_REG_EX
        false    | getUSAddress()           | Locale.US    | null               | CHAR_SPACE_AND_DASH_ONLY_REG_EX
    }

    def 'hasValidationError() - RU phone format check'() {
        given:
        def address = getAddress("RU")
        def user = new User()
        user.locale = new Locale('ru', 'RU')
        def field = CartItemOption.PHONE_NUMBER.getValue()

        when:
        def result = addressValidatorRU.hasValidationError(address, messageSource, user)

        then:
        if(address.errorMessage.size() == 0){
            assert address.phoneNumber == formattedNumber
        }
        else {
            assert address.errorMessage.get(field) == invalidPhoneNumber
        }

        where:
        phoneNumber         | formattedNumber
        "820225-63-04"      | "+7 820 225-63-04"  //RU - success flow
        "65424555"          | null               //RU incorrect
    }

    def 'test validateState'() {
        given:
        def state = states
        def countryCode = countryCodes

        when:
        def result = addressValidator.validateState(state,countryCode)

        then:
        result == results

        where:

        states     | countryCodes       | results
        "xxx"      | "CA"               | false
        "NSW"      | "CA"               | false
        "NSW"      | "AU"               | true
        "TX"       | ""                 | true
        ""         | "AU"               | false
        "TX"       | "AU"               | false
        "Hidalgo"  | "MX"               | true
        "TX"       | "US"               | true
        "TX"       | "MY"               | true
        "TN"       | "MX"               | false

    }

    def getAddressWithSpecialChar() {
        def address = new Address()
        address.firstName = "ab c"
        address.lastName = "xyz"
        address.city = "\\NEW BERLIN\\"
        address.address1 = "<123>"
        address.address2 = "#2nd street;"
        address.address3 = ""
        address.businessName = "[B2S]"
        return address;
    }

    def getAddress(String country) {
        def address = new Address()
        address.country = country
        return address;
    }

    def getUser() {
        def user = new User()
        user.varId = "UA"
        user.programId = "b2s_qa_only"
        user.locale = new Locale("en", "US")
        return user
    }

    def getUSAddress() {
        Address address = new Address()
        address.setFirstName("Saranya")
        address.setLastName("Kathirvel")
        address.setAddress1("101 A YOUNG INTERNATIONAL BOULEVARD")
        address.setCity("Atlanta")
        address.setState("GA")
        address.setZip5("30303")
        address.setCountry("US")
        address.setPhoneNumber("7705551234")
        address.setEmail("abc@b2s.com")
        return address;
    }

    def getProgram(varId, programId, key, value) {
        Program program = new Program()
        program.varId = varId
        program.programId = programId
        Map<String, Object> configs = new HashMap<>()
        configs.put(key, value)
        program.setConfig(configs)
        return program
    }

    def cart(firstName, businessName) {
        Address address = new Address(firstName: firstName, lastName: 'Kathirvel', businessName: businessName, address1: '104 Gustavo Baz',
        )
        return address
    }

    def 'formatPhoneNumber() - isPhoneNumberValidForTheCountryCode'() {

        when:
        def result = addressValidator.formatPhoneNumber(phoneNumber, countryCode, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)

        then:
        result == expected

        where:

        expected        | phoneNumber   | countryCode
        "8941 2621"     | "6589412621"  | "SG"
        "6339 8616"     | "6563398616"  | "SG"
        "8841 2621"     | "6588412621"  | "SG"
        null            | "1588412621"  | "SG"
        null            | "6533412621"  | "SG"
        "9010 0972"     |   "90100972"  | "SG"
        "9636 8181"     |   "96368181"  | "SG"
        "(672) 555-1234"| "6725551234"  | "CA"

    }

    class AddressValidatorMock extends AddressValidatorImpl {
        protected MessageSource getMessageSource(final String varId) {
            return messageSource
        }
    }


}
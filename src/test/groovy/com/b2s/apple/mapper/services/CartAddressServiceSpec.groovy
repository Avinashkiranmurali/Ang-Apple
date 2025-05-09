package com.b2s.apple.mapper.services

import com.b2r.util.address.melissadata.GlobalAddressValidator
import com.b2s.apple.services.CartAddressService
import com.b2s.common.services.pricing.impl.LocalPricingServiceV2
import com.b2s.rewards.apple.model.Address
import com.b2s.rewards.apple.model.Cart
import com.b2s.rewards.apple.model.Program
import com.b2s.rewards.common.context.AppContext
import com.b2s.shop.common.User
import com.b2s.web.B2RReloadableResourceBundleMessageSource
import com.google.gson.internal.LinkedTreeMap
import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Subject

class CartAddressServiceSpec extends Specification{

    def messageSource = Mock(B2RReloadableResourceBundleMessageSource)
    ApplicationContext context = Mock(ApplicationContext)
    def pricingServiceV2 = Mock(LocalPricingServiceV2);
    def globalAddressValidator = Mock(GlobalAddressValidator);

    @Subject
    def cartAddressService = new CartAddressService(messageSource: messageSource, pricingServiceV2:pricingServiceV2,
            globalAddressValidator:globalAddressValidator)

    def 'test updateShippingInformation'(){
        given:
        AppContext.applicationContext = context
        context.getBean(_) >> messageSource
        //New Address Fields
        Map addressMap = new LinkedTreeMap()
        addressMap.put("address1",new_address1)
        addressMap.put('address2',"Crsent Park")
        addressMap.put('address3',"For MX Var")
        addressMap.put('firstName',fName)
        addressMap.put('lastName',lName)
        addressMap.put('businessName',"")
        addressMap.put('phoneNumber',phoneNumber)
        addressMap.put('email',email)
        addressMap.put('city',city)
        addressMap.put('zip5',zip5)
        addressMap.put('state',state)
        addressMap.put('country',country)

        Locale locale = new Locale(lang, country)
        User user = new User(locale: locale)
        Program program = new Program(config: config)
        //Session Address Fields
        Address shippingAddress =  new Address(address1: session_address1,
                address2: 'Crsent Park',
                address3: 'For MX Var',
                city: city,
                country: country,
                phoneNumber: phoneNumber,
                email: 'abc@bakkt.com',
                firstName: fName,
                lastName: lName,
                middleName: '',
                businessName: '',
                ignoreSuggestedAddress: false,
                cartTotalModified: false,
                state: state,
                zip5: zip5)
        Cart sessionCart =  new Cart(shippingAddress: shippingAddress)

        when:
        def result  = cartAddressService.updateShippingInformation(sessionCart,addressMap,user,program)

        then:
        result.addressError == addressError
        result.newShippingAddress.address1 == final_new_ship_address
        result.shippingAddress.address1 == final_session_ship_address
        result.shippingAddress.email == final_session_email
        result.shippingAddress.phoneNumber == final_session_phone

        where:
        No| config                             | session_address1                  | new_address1                  | state  | country | lang | fName  | lName | city        | zip5     | email           | phoneNumber     || final_new_ship_address|| final_session_ship_address   || final_session_email || final_session_phone || addressError
        1 | getAllFieldsLockConfig()           | '10 admiralty street'             | '10 admiralty street'         | 'SG'   | 'SG'    | 'en' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'abc@bakkt.com' | '9455 6522'     || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '9455 6522'         || false
        2 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'SG'    | 'en' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'abc@bakkt.com' | '9455 6522'     || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '9455 6522'         || false
        3 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'SG'    | 'en' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '+65 9455 6522'     || true
        4 | getContactInfoLockOverridesConfig()| '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'SG'    | 'en' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'xyz@bakkt.com'     || '9455 6522'         || false
        5 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '120 Saint street'            | 'SG'   | 'SG'    | 'en' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'abc@bakkt.com' | '9455 6522'     || '120 Saint street'    || '10 admiralty street'        || 'abc@bakkt.com'     || '9455 6522'         || true
        6 | getMercAddressLockOverridesConfig()| '120 Saint street'                | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'SG'    | 'en' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'abc@bakkt.com' | '9455 6522'     || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '9455 6522'         || false
        7 | getConfig()                        | '120 Saint street'                | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'SG'    | 'en' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'xyz@bakkt.com'     || '9455 6522'         || false

        1 | getAllFieldsLockConfig()           | '10 admiralty street'             | '10 admiralty street'         | 'QLD'  | 'AU'    | 'en' | 'abc'  | 'xyz' | 'ST RUTH'   | '5769'   | 'abc@bakkt.com' | '(07) 4539 6331'|| '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '(07) 4539 6331'    || false
        2 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'QLD'  | 'AU'    | 'en' | 'abc'  | 'xyz' | 'ST RUTH'   | '5769'   | 'abc@bakkt.com' | '(07) 4539 6331'|| '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '(07) 4539 6331'    || false
        3 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'QLD'  | 'AU'    | 'en' | 'abc'  | 'xyz' | 'ST RUTH'   | '5769'   | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '+65 9455 6522'     || true
        4 | getContactInfoLockOverridesConfig()| '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'QLD'  | 'AU'    | 'en' | 'abc'  | 'xyz' | 'ST RUTH'   | '5769'   | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'xyz@bakkt.com'     || '9455 6522'         || false
        5 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '120 Saint street'            | 'QLD'  | 'AU'    | 'en' | 'abc'  | 'xyz' | 'ST RUTH'   | '5769'   | 'abc@bakkt.com' | '9455 6522'     || '120 Saint street'    || '10 ad\\m<>ir\\"al"ty street'|| 'abc@bakkt.com'     || '9455 6522'         || true
        6 | getMercAddressLockOverridesConfig()| '120 Saint street'                | '10 ad\\m<>ir\\"al"ty street' | 'QLD'  | 'AU'    | 'en' | 'abc'  | 'xyz' | 'ST RUTH'   | '5769'   | 'abc@bakkt.com' | '(07) 4539 6331'|| '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '(07) 4539 6331'    || false
        7 | getConfig()                        | '120 Saint street'                | '10 ad\\m<>ir\\"al"ty street' | 'QLD'  | 'AU'    | 'en' | 'abc'  | 'xyz' | 'ST RUTH'   | '5769'   | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'xyz@bakkt.com'     || '9455 6522'         || false
        8 | getAllFieldsLockConfig()           | '10 admiralty street'             | '10 admiralty street'         | 'XXX'  | 'AU'    | 'en' | 'abc'  | 'xyz' | 'ST RUTH'   | '5769'   | 'abc@bakkt.com' | '(07) 4539 6331'|| '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '(07) 4539 6331'    || true

        1 | getAllFieldsLockConfig()           | '10 admiralty street'             | '10 admiralty street'         | 'SG'   | 'HK'    | 'zh' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'abc@bakkt.com' | '9455 6522'     || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '9455 6522'         || false
        2 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'HK'    | 'zh' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'abc@bakkt.com' | '9455 6522'     || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '9455 6522'         || false
        3 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'HK'    | 'zh' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '+65 9455 6522'     || true
        4 | getContactInfoLockOverridesConfig()| '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'HK'    | 'zh' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'xyz@bakkt.com'     || '9455 6522'         || false
        5 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '120 Saint street'            | 'SG'   | 'HK'    | 'zh' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'abc@bakkt.com' | '9455 6522'     || '120 Saint street'    || '10 admiralty street'        || 'abc@bakkt.com'     || '9455 6522'         || true
        6 | getMercAddressLockOverridesConfig()| '120 Saint street'                | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'HK'    | 'zh' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'abc@bakkt.com' | '9455 6522'     || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '9455 6522'         || false
        7 | getConfig()                        | '120 Saint street'                | '10 ad\\m<>ir\\"al"ty street' | 'SG'   | 'HK'    | 'zh' | 'abc'  | 'xyz' | 'Singapore' | '757695' | 'xyz@bakkt.com' | '+65 9455 6522' || '10 admiralty street' || '10 admiralty street'        || 'xyz@bakkt.com'     || '9455 6522'         || false

        1 | getAllFieldsLockConfig()           | '10 admiralty street'             | '10 admiralty street'         |'Puebla'| 'MX'    | 'es' | 'abc'  | 'xyz' | 'Puebla'    | '91631'  | 'abc@bakkt.com' | '228 820 0104'  || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '228 820 0104'      || false
        2 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' |'Puebla'| 'MX'    | 'es' | 'abc'  | 'xyz' | 'Puebla'    | '91631'  | 'abc@bakkt.com' | '228 820 0104'  || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '228 820 0104'      || false
        3 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' |'Puebla'| 'MX'    | 'es' | 'abc'  | 'xyz' | 'Puebla'    | '91631'  | 'xyz@bakkt.com' | '228.820-0104'  || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '228.820-0104'      || true
        4 | getContactInfoLockOverridesConfig()| '10 ad\\m<>ir\\"al"ty street'     | '10 ad\\m<>ir\\"al"ty street' |'Puebla'| 'MX'    | 'es' | 'abc'  | 'xyz' | 'Puebla'    | '91631'  | 'xyz@bakkt.com' | '228.820-0104'  || '10 admiralty street' || '10 admiralty street'        || 'xyz@bakkt.com'     || '228 820 0104'      || false
        5 | getAllFieldsLockConfig()           | '10 ad\\m<>ir\\"al"ty street'     | '120 Saint street'            |'Puebla'| 'MX'    | 'es' | 'abc'  | 'xyz' | 'Puebla'    | '91631'  | 'abc@bakkt.com' | '228 820 0104'  || '120 Saint street'    || '10 admiralty street'        || 'abc@bakkt.com'     || '228 820 0104'      || true
        6 | getMercAddressLockOverridesConfig()| '120 Saint street'                | '10 ad\\m<>ir\\"al"ty street' |'Puebla'| 'MX'    | 'es' | 'abc'  | 'xyz' | 'Puebla'    | '91631'  | 'abc@bakkt.com' | '228 820 0104'  || '10 admiralty street' || '10 admiralty street'        || 'abc@bakkt.com'     || '228 820 0104'      || false
        7 | getConfig()                        | '120 Saint street'                | '10 ad\\m<>ir\\"al"ty street' |'Puebla'| 'MX'    | 'es' | 'abc'  | 'xyz' | 'Puebla'    | '91631'  | 'xyz@bakkt.com' | '228.820-0104'  || '10 admiralty street' || '10 admiralty street'        || 'xyz@bakkt.com'     || '228 820 0104'      || false

        1 | getAllFieldsLockConfig()           | '10 a\'dmiralty street'           | '10 a\'dmiralty street'       | 'QC'   | 'CA'   | 'fr' | 'abc'| 'xyz'|'Ville de Québec'| 'A1C 5M2'| 'abc@bakkt.com' | '(770) 410-6035'||'10 a\'dmiralty street'  ||'10 a%27dmiralty street'       || 'abc@bakkt.com'  || '(770) 410-6035'    || false
        2 | getAllFieldsLockConfig()           | '10 a\'d\\\'m<>ir\\"al"ty street' | '10 a\'d\\\'m<>iral"ty street'| 'QC'   | 'CA'   | 'fr' | 'abc'| 'xyz'|'Ville de Québec'| 'A1C 5M2'| 'abc@bakkt.com' | '(770) 410-6035'||'10 a\'d\'miralty street'||'10 a%27d%27miralty street'    || 'abc@bakkt.com'  || '(770) 410-6035'    || false
        3 | getAllFieldsLockConfig()           | '10 a\'d\\\'m<>ir\\"al"ty street' | '10 a\'d\\\'m<>iral"ty street'| 'QC'   | 'CA'   | 'fr' | 'abc'| 'xyz'|'Ville de Québec'| 'A1C 5M2'| 'xyz@bakkt.com' | '(770) 410-6035'||'10 a\'d\'miralty street'||'10 a%27d%27miralty street'    || 'abc@bakkt.com'  || '(770) 410-6035'    || true
        4 | getContactInfoLockOverridesConfig()| '10 a\'d\\\'m<>ir\\"al"ty street' | '10 a\'d\\\'m<>iral"ty street'| 'QC'   | 'CA'   | 'fr' | 'abc'| 'xyz'|'Ville de Québec'| 'A1C 5M2'| 'xyz@bakkt.com' | '(770) 410-6035'||'10 a\'d\'miralty street'||'10 a%27d%27miralty street'    || 'xyz@bakkt.com'  || '(770) 410-6035'    || false
        5 | getAllFieldsLockConfig()           | '10 a\'d\\\'m<>ir\\"al"ty street' | '850 Place d\'Youville'       | 'QC'   | 'CA'   | 'fr' | 'abc'| 'xyz'|'Ville de Québec'| 'A1C 5M2'| 'abc@bakkt.com' | '(770) 410-6035'||'850 Place d\'Youville'  ||'10 a%27d%27miralty street'    || 'abc@bakkt.com'  || '(770) 410-6035'    || true
        6 | getMercAddressLockOverridesConfig()| '850 Place d\'Youville'           | '10 a\'d\\\'m<>iral"ty street'| 'QC'   | 'CA'   | 'fr' | 'abc'| 'xyz'|'Ville de Québec'| 'A1C 5M2'| 'abc@bakkt.com' | '(770) 410-6035'||'10 a\'d\'miralty street'||'10 a%2527d%2527miralty street'|| 'abc@bakkt.com'  || '(770) 410-6035'    || false
        7 | getConfig()                        | '850 Place d\'Youville'           | '10 a\'d\\\'m<>iral"ty street'| 'QC'   | 'CA'   | 'fr' | 'abc'| 'xyz'|'Ville de Québec'| 'A1C 5M2'| 'xyz@bakkt.com' | '(770) 410-6035'||'10 a\'d\'miralty street'||'10 a%27d%27miralty street'    || 'xyz@bakkt.com'  || '(770) 410-6035'    || false

        /*1. getAllFieldsLockConfig --> SUCCESS --> All fields are same, so no issues
        2. getAllFieldsLockConfig --> SUCCESS --> All fields are same(address1 contains special characters, but fixed it), so no issues
        3. getAllFieldsLockConfig --> FAILURE --> As the new email & phone fields are different
        4. getContactInfoLockOverridesConfig --> SUCCESS --> As override is enabled, even though the new email & phone fields are different, they are overridden and gets into success scenario
        5. getAllFieldsLockConfig --> FAILURE --> As the new address field, address1 is different
        6. getMercAddressLockOverridesConfig--> SUCCESS --> As override is enabled, even though the new address field, address1 is different, it is overridden and gets into success scenario
        7. getConfig --> SUCCESS --> As lock is not configured, fields are updated with new address fields*/
    }

    def getMercAddressLockOverridesConfig() {
        Map<String, Object> config = getAllFieldsLockConfig()
        config.put("MercAddressLockOverrides",'address1,city,subCity')
        return config
    }

    def getContactInfoLockOverridesConfig() {
        Map<String, Object> config = getAllFieldsLockConfig()
        config.put("ContactInfoLockOverrides",'email,phoneNumber')
        return config
    }

    def getAllFieldsLockConfig() {
        Map<String, Object> config = getConfig()
        config.put("MercAddressLocked",true)
        config.put("ContactInfoLocked",true)
        config.put("ShipToNameLocked",true)
        config.put("businessNameLocked",true)
        return config
    }

    def getConfig(){
        Map<String, Object> config = new HashMap<>()
        config.put('skipAddressValidation', true)
        return config
    }

    def 'setNewShippingAddressToCart() - businessNameLocked flag'() {
        given:
        def cartAddress = getAddress('Kathirvel','B2S')
        def cart = new Cart(shippingAddress: cartAddress)
        when:
        def result = cartAddressService.setNewShippingAddressToCart(shipAddress, cart, program)

        then:
        result.businessName == expected.businessName

        where:
        program                                                                             |shipAddress                                                  |expected
        getProgram('MX','b2s_qa_only','businessNameLocked',true) |getAddress('Saranya','Bakkt')          |getAddress('Kathirvel','B2S')
        getProgram('MX','b2s_qa_only','businessNameLocked',false)|getAddress('Priya','Bridge2')          |getAddress('Priya','Bridge2')
    }

    def 'setNewShippingAddressToCart() - ShipToNameLocked flag'() {
        given:
        def cartAddress = getAddress('Kathirvel','B2S')
        def cart = new Cart(shippingAddress: cartAddress)

        when:
        def result = cartAddressService.setNewShippingAddressToCart(shipAddress, cart, program)

        then:
         result.firstName == expected.firstName
         result.lastName == expected.lastName

        where:
        program                                                                             |shipAddress                                                  |expected
        getProgram('WF','b2s_qa_only','ShipToNameLocked',true)   |getAddress('Keerthana','Bakkt-Classic')|getAddress('Kathirvel','B2S')
        getProgram('WF','b2s_qa_only','ShipToNameLocked',false)  |getAddress('Suriya','Bridge2-Classic') |getAddress('Suriya','Bridge2-Classic')
    }

    def getProgram(varId, programId, key, value) {
        Program program = new Program()
        program.varId = varId
        program.programId = programId
        Map<String, Object> configs = new HashMap<>()
        configs.put(key, value)
        configs.put('MercAddressLocked', true)
        configs.put('ContactInfoLocked', true)
        program.setConfig(configs)
        return program
    }

    def getAddress(firstName, businessName) {
        Address address = new Address(firstName: firstName, lastName: 'Kathirvel', businessName: businessName, address1: '104 Gustavo Baz')
        return address
    }
}

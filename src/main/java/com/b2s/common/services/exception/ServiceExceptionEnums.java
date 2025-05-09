package com.b2s.common.services.exception;

import java.text.MessageFormat;

/**
 * <p>
 *  Encapsulates all the exception messages that can be thrown while initializing and calling services that interact with external systems
   @author sjonnalagadda
  * Date: 7/11/13
  * Time: 4:13 PM
  *
 */
public enum ServiceExceptionEnums {

    SERVICE_INITIALIZATION_EXCEPTION(100, "Unable to initialize service", ErrorSeverity.HIGH),
    ARGUMENT_CAN_NOT_BE_NULL(11, "Argument should be present", ErrorSeverity.HIGH),
    FACET_VALUE_CAN_NOT_BE_NULL(11, "Facet value should be present", ErrorSeverity.HIGH),
    FACET_COUNT_CAN_NOT_BE_NULL(11, "Facet count should be present", ErrorSeverity.HIGH),
    TRANSFORMER_HELPER_CAN_NOT_BE_NULL(11, "Transformer helper should be present", ErrorSeverity.HIGH),
    TRANSFORMER_HELPER_OBJECT_IS_DIFFERENT(11, "Transformer helper is different ", ErrorSeverity.HIGH),
    PRODUCT_SEARCH_INFO_IS_NULL(11, "Product search info cannot be null", ErrorSeverity.HIGH),
    PRODUCT_SERVICE_URL_VALUE_ABSENT(11, "Product service URL value is absent", ErrorSeverity.HIGH),
    PRODUCT_SERVICE_TIMEOUT_VALUE_ABSENT(11, "Product service  timeout value is absent", ErrorSeverity.HIGH),
    PRODUCT_SERVICE_TIMEOUT_VALUE_IS_NOT_POSITIVE(11, "Product service  timeout value should be greater than zero", ErrorSeverity.HIGH),
    PRODUCT_SERVICE_CONN_POOL_VALUE_ABSENT(11, "Product service  connection pool value is absent", ErrorSeverity.HIGH),
    PRODUCT_SERVICE_CONN_POOL_VALUE_IS_NOT_POSITIVE(11, "Product service  connection pool value should be greater than zero", ErrorSeverity.HIGH),
    CLIENT_REQUEST_IS_ABSENT(11, "Client request is absent", ErrorSeverity.HIGH),
    SERVER_REQUEST_IS_ABSENT(11, "Server request is absent", ErrorSeverity.HIGH),
    SERVER_RESPONSE_IS_ABSENT(11, "Server response is absent", ErrorSeverity.HIGH),
    SUPPLIER_PRODUCT_MAPPING_ABSENT(11, "Supplier product mapper is not present", ErrorSeverity.HIGH),
    SUPPLIER_PRICING_CATEGORY_MAPPING_ABSENT(11, "supplier pricing category is absent", ErrorSeverity.HIGH),
    IMAGE_OBFUSCATORY_ABSENT(11, "Image obfuscatory is absent", ErrorSeverity.HIGH),
    LEGACY_MERCHANT_MAPPER_ABSENT(11, "Legacy merchant mapper is absent", ErrorSeverity.HIGH),
    PRODUCT_DETAIL_REQUEST_ABSENT(11, "Product Detail request is absent", ErrorSeverity.HIGH),
    PRODUCT_DETAIL_RESPONSE_ABSENT(11, "Product Detail response is absent", ErrorSeverity.HIGH),
    SEARCH_TRANSFORMERS_HOLDER_ABSENT(11, "Search transformers holder is absent", ErrorSeverity.HIGH),
    SEARCH_REQUEST_TRANSFORMER_ABSENT(11, "Search request transformer is absent", ErrorSeverity.HIGH),
    SEARCH_RESPONSE_TRANSFORMER_ABSENT(11, "Search response transformer is absent", ErrorSeverity.HIGH),
    DETAIL_TRANSFORMERS_HOLDER_ABSENT(11, "Detail transformers holder is absent", ErrorSeverity.HIGH),
    DETAIL_REQUEST_TRANSFORMER_ABSENT(11, "Detail request transformer is absent", ErrorSeverity.HIGH),
    DETAIL_RESPONSE_TRANSFORMER_ABSENT(11, "Detail response transformer is absent", ErrorSeverity.HIGH),
    PRODUCT_FACTORY_WRAPPER_ABSENT(11, "Product factory wrapper is absent", ErrorSeverity.HIGH),
    CATEGORY_TRANSFORMERS_HOLDER_ABSENT(11, "Category transformers holder is absent", ErrorSeverity.HIGH),
    CATEGORY_REQUEST_TRANSFORMER_ABSENT(11, "Category request transformer is absent", ErrorSeverity.HIGH),
    CATEGORY_RESPONSE_TRANSFORMER_ABSENT(11, "Category response transformer is absent", ErrorSeverity.HIGH),
    STORE_AVAILABILITY_TRANSFORMERS_HOLDER_ABSENT(11, "Store availability transformers holder is absent", ErrorSeverity.HIGH),
    STORE_AVAILABILITY_REQUEST_TRANSFORMER_ABSENT(11, "Store availability request transformer is absent", ErrorSeverity.HIGH),
    STORE_AVAILABILITY_RESPONSE_TRANSFORMER_ABSENT(11, "Store availability response transformer is absent", ErrorSeverity.HIGH),
    STORE_LOOKUP_TRANSFORMERS_HOLDER_ABSENT(11, "Store lookup transformers holder is absent", ErrorSeverity.HIGH),
    STORE_LOOKUP_REQUEST_TRANSFORMER_ABSENT(11, "Store lookup request transformer is absent", ErrorSeverity.HIGH),
    STORE_LOOKUP_RESPONSE_TRANSFORMER_ABSENT(11, "Store lookup response transformer is absent", ErrorSeverity.HIGH),
    LOCALE_INFORMATION_ABSENT(11, "Locale information is absent", ErrorSeverity.HIGH),
    BROWSE_NODE_ID_ABSENT(11, "Browse node id is absent", ErrorSeverity.HIGH),
    CATEGORY_SERVICE_ABSENT(11, "Category service is absent", ErrorSeverity.HIGH),
    SUPPORTED_LOCALES_ABSENT(11, "Supported locales is absent", ErrorSeverity.HIGH),
    SLUG_ABSENT(11, "Slug is absent", ErrorSeverity.HIGH),
    CATEGORY_NAME_ABSENT(11, "Category name is absent", ErrorSeverity.HIGH),
    HIERARCHY_FROM_ROOT_NODE_ABSENT(11, "Hierarchy from root node is absent", ErrorSeverity.HIGH),
    ROOT_CATEGORY_NAME_IS_ABSENT(11, "Root category node is absent", ErrorSeverity.HIGH),
    CATEGORY_NODE_GRAPH_ABSENT(11, "Category node graph is absent", ErrorSeverity.HIGH),
    CATEGORY_INFO_ABSENT(11, "Category info is absent", ErrorSeverity.HIGH),
    CATEGORIES_ABSENT(11, "Category for a given locale is absent", ErrorSeverity.HIGH),
    CATEGORIES_STUB_INDICATOR_ABSENT(11, "Categories stub indicator is absent", ErrorSeverity.HIGH),
    CATEGORIES_STUB_FILE_NAME_ABSENT(11, "Categories stub file name is absent", ErrorSeverity.HIGH),
    CATEGORIES_REPO_SIZE_NOT_MATCHING(11, "Categories repo size not matching by category", ErrorSeverity.HIGH),
    CATEGORIES_NOT_LOADED_FROM_STUB(11, "Unable to load category information from stub", ErrorSeverity.HIGH),
    USER_LANGUAGE_IS_ABSENT(11, "User Language is absent", ErrorSeverity.HIGH),
    SERVICE_EXECUTION_EXCEPTION(444, "Service execution exception", ErrorSeverity.HIGH),
    CATEGORIES_NOT_FOUND_EXCEPTION(11, "There are no categories available", ErrorSeverity.HIGH),
    QUANTITY_RESTRICTION_EXCEEDED_EXCEPTION(11, "Quantity exceeded the maximum limitation", ErrorSeverity.HIGH),
    QUANTITY_INVALID_EXCEPTION(101, "Invalid Quantity", ErrorSeverity.HIGH),
    ITEM_NO_LONGER_AVAILABLE_EXCEPTION(11, "Sorry, Product {0} is no longer available", ErrorSeverity.HIGH),
    ENGRAVED_ITEMS_QUANTITY_RESTRICTION_EXCEPTION(11, "Engraved items can not be bulk ordered", ErrorSeverity.HIGH),
    GIFT_MESSAGE_LENGTH_EXCEEDED_EXCEPTION(11, "Gift Message Line can not have more than 30 characters", ErrorSeverity.HIGH),
    ADDRESS_EMPTY_EXCEPTION(11, "Address can not be empty", ErrorSeverity.HIGH),
    INVALID_EMAIL_ADDRESS(11, "Invalid email address.", ErrorSeverity.HIGH),
    INVALID_PHONE_NUMBER(11, "Phone Number {} is incorrect. Please enter a valid phone number", ErrorSeverity.HIGH),
    STATE_EMPTY_EXCEPTION(11, "Please enter a valid State", ErrorSeverity.HIGH),
    COUNTRY_EMPTY_EXCEPTION(11, "Please enter a valid Country", ErrorSeverity.HIGH),
    INVALID_ZIP_CODE(11, "Please enter a valid zip code", ErrorSeverity.HIGH),
    INVALID_POSTAL_CODE(11, "Please enter a valid postal code", ErrorSeverity.HIGH),
    INVALID_ZIP5_CODE(11, "5 digit Zip Code is invalid", ErrorSeverity.HIGH),
    INVALID_ZIP4_CODE(11, "4 digit Zip Code is invalid", ErrorSeverity.HIGH),
    PO_BOX_NOT_ALLOWED(11, "Item can not be shipped to PO Box address..", ErrorSeverity.HIGH),
    IGNORE_SUGGESTED_ADDRESS(11, "Invalid Ignore Suggested Address flag ", ErrorSeverity.HIGH),
    UNKNOWN_HOST_EXCEPTION(11, "Host Unavailable / Unknown Host Exception", ErrorSeverity.HIGH),
    CART_TOTAL_MODIFIED_EXCEPTION(11, "Cart content has changed since it was loaded, possibly by another session", ErrorSeverity.HIGH),
    NAUGHTY_WORD_FOUND_EXCEPTION(11, "Engrave text contains naughty/bad word(s) or the following special characters: ", ErrorSeverity.HIGH),
    INVALID_CHARACTERS_FOUND_EXCEPTION(11, "These characters cannot be engraved: ", ErrorSeverity.HIGH),
    INACTIVE_SUPPLEMENTAL_CONFIGURATION_FOR_CC_SLIDER(11, "Supplemental Configuration is NOT active for CC_SLIDER payment option set for this program: ", ErrorSeverity.HIGH),
    ALPHA_ONLY_CHARS_EXCEPTION(11, "Numbers, space and/or special symbols are not allowed", ErrorSeverity.HIGH),
    CITY_TEXT_EXCEPTION(11, "Numbers and/or special symbols are not allowed", ErrorSeverity.HIGH),
    CITY_EMPTY_EXCEPTION(11, "Please enter a valid City", ErrorSeverity.HIGH),
    FINANCE_OPTIONS_SERVICE_NOT_FOUND_EXCEPTION(11, "No Finance Options Service implementation found for {0}", ErrorSeverity.HIGH),
    BIN_FILTER_EXCEPTION(11, "Invalid BIN", ErrorSeverity.HIGH),
    MULTIPLE_PROMOTIONS_FOUND(11, "Multiple promotions found for the selected qualifying product", ErrorSeverity.HIGH),
    EMAIL_FAILURE(11, "Failed to send email", ErrorSeverity.HIGH);

    private final int errorCode;
    private final String errorMessage;
    private final ErrorSeverity errorSeverity;

    public final static String PRICING_SERVICE_ADDRESS_ERROR_STR ="Address was not found";

    /**
     * Constructs exception from <code>ServiceExceptionEnums</code> enum constants
     *
     * @param errorCode error code related to exception.
     * @param errorMessage error message related to exception.
     * @param errorSeverity Severity of exception.
     */

    private ServiceExceptionEnums(final int errorCode, final String errorMessage,final ErrorSeverity errorSeverity){
        this.errorCode = errorCode;
        this.errorMessage =  errorMessage;
        this.errorSeverity =  errorSeverity;
    }

    /**
     * @return int error code related to exception.
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * @return String error message related to exception.
     */

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getErrorMessage(String args[]) {
        MessageFormat mf = new MessageFormat(this.errorMessage);
        return mf.format(args);
    }
    /**
     * @return String error severity related to exception.
     */
    public ErrorSeverity getErrorSeverity() {
        return this.errorSeverity;
    }
}

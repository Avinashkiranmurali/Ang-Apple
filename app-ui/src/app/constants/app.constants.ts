export class AppConstants {
  public static ACCESSORIES = 'accessories';
  public static CURATED = 'curated';
  public static WATCH = 'watch';
  public static ALL_ACCESSORIES = 'all-accessories';
  public static NONE = 'none';
  public static baseUrl = '/apple-gr/service';
  public static BROWSE_BASE_URL = '/store/browse/';
  public static CURATED_BASE_URL = '/store/curated/';
  public static CONFIGURE_BASE_URL = '/store/configure/';
  public static MULTI_PRODUCT_BANNER_TYPE = 'multiproduct';
  public static  paymentOption = {
    installment: 'INSTALLMENT',
    cash: 'CASH',
    points: 'POINTS'
  };
  public static paymentType = {
    cc_variable: 'cc_variable',
    cc_fixed: 'cc_fixed',
    cash_only: 'cash_only',
    points_only: 'points_only',
    points_fixed: 'points_fixed',
    points_variable: 'points_variable',
    cash_variable: 'cash_variable',
    cash_fixed: 'cash_fixed',
    cash_subsidy: 'cash_subsidy',
    payroll_default: 'payroll_default',
    pd_variable: 'pd_variable',
    pd_fixed: 'pd_fixed',
    pd_only: 'pd_only',
    no_pay: 'no_pay'
  };
  public static paymentTemplate = {
    cash_subsidy: 'cash_subsidy',
    points_fixed: 'points_fixed',
    points_variable: 'points_variable',
    points_rounded: 'points_rounded',
    pd_fixed: 'pd_fixed',
    cc_fixed: 'cc_fixed',
    cc_default: 'cc_default',
    points_default: 'points_default',
    cash_default: 'cash_default',
    installment_monthly: 'installment_monthly'
  };
  public static redemptions = {
    pointsonly: 'pointsonly',
    pointsfixed: 'pointsfixed',
    splitpay: 'splitpay',
    cashonly: 'cashonly',
    finance: 'finance',
    splitpay_finance: 'splitpay_finance',
    cashonly_finance: 'cashonly_finance',
    payroll_deduction: 'payroll_deduction',
    nopay: 'nopay'
  };
  public static analyticServices = {
    MATOMO: 'matomo',
    ENSIGHTEN: 'ensighten',
    TEALIUM: 'tealium',
    WEBTRENDS: 'webtrends',
    GOOGLE: 'google',
    HEAP: 'heap',
    HEAP_EVENTS: {
      ITEM_VIEWED: 'Item Viewed',
      ITEM_ADDED_TO_CART: 'Item Added to Cart',
      ITEM_REMOVED_FROM_CART : 'Item Removed from Cart',
      ORDER_SUCCESS: 'Order Placed',
      ORDER_LINE_PLACED: 'Order Line Placed',
      SESSION_STARTED: 'Session Started'
    },
    HEAP_CONSTANTS: {
      APPLE: 'Apple',
      MERCHANDISE: 'Merchandise',
      STOREFRONT: 'storefront',
      VAR_ID: 'VAR ID',
      VAR_NAME: 'VAR Name',
      PROGRAM_ID: 'Program ID',
      LOYALTY_PARTNER_USER_ID: 'Loyalty Partner User ID',
      PROGRAM_NAME: 'Program Name',
      PLATFORM: 'Platform',
      STOREFRONT_TYPE: 'Storefront Type',
      STOREFRONT_NAME: 'Storefront Name',
      ON_BEHALF_OF: 'On Behalf Of',
      EVENT_SOURCE: 'Event Source',
      ITEM_SKU: 'Item SKU',
      ITEM_NAME: 'Item Name',
      ITEM_CATEGORY: 'Item Category',
      ITEM_SUBCATEGORY: 'Item Subcategory',
      ITEM_BRAND: 'Item Brand',
      ITEM_SUPPLIER: 'Item Supplier',
      ITEM_PRICE: 'Item Price',
      ITEM_POINTS: 'Item Points',
      ORDER_ID: 'Order ID',
      ORDER_ITEM_TOTAL: 'Order Items Total',
      ORDER_ITEM_TOTAL_POINTS: 'Order Items Total Points',
      ORDER_TOTAL: 'Order Total',
      ORDER_TOTAL_POINTS: 'Order Total Points',
      BALANCE: 'Balance',
      BALANCE_OLD: 'Balance Old',
      BALANCE_NEW: 'Balance New',
      SPLIT_TENDER: 'Split Tender',
      ORDER_LINES: 'Order Lines',
      ORDER_QUANTITY: 'Order Quantity',
      ORDER_LINE_QUANTITY: 'Order Line Quantity',
      ORDER_LINE_ITEM_PRICE: 'Order Line Item Price',
      ORDER_LINE_TOTAL: 'Order Line Total',
      ORDER_LINE_ITEM_POINTS: 'Order Line Item Points',
      ORDER_LINE_TOTAL_POINTS: 'Order Line Total Points',
      POINTS_NAME: 'Points Name',
      ITEM_QUANTITY: 'Item Quantity'
    },
    EVENTS: {
      ORDER_SUCCESS: 'OrderSuccess',
      ROUTE: 'Route',
      PRODUCT_VIEW: 'ProductView',
      REMOVE_FROM_CART: 'RemoveFromCart',
      UPDATE_CART: 'UpdateCart',
      CATEGORY_SEARCH: 'CategorySearch',
      CANONICAL_PAGE: 'CanonicalPage',
      ENGRAVING: 'Engraving',
      ERROR: 'AppleError'
    },
    CANONICAL_CONSTANTS: {
      TERMS_AND_CONDITIONS: 'Apple Terms and Conditions',
      FAQS: 'Apple FAQs',
      ORDER_HISTORY: 'Apple Order History',
      ORDER_HISTORY_DETAILS: 'Apple Order Details',
      SESSION_TIMEOUT: 'Apple Session Timeout',
      ERROR: 'Apple Error',
      MERCHANDISE_LANDING: 'Apple Landing',
      CURATED_PRODUCTS: 'Apple Curated Products',
      MERCHANDISE_SEARCH_RESULTS: 'Apple Search Results',
      MERCHANDISE_BROWSE_RESULTS: 'Apple Browse Results',
      MERCHANDISE_PRODUCT_DETAILS: 'Apple Product Details',
      PRODUCTS_CART: 'Apple Cart',
      PRODUCTS_CHECKOUT: 'Apple Checkout',
      PRODUCTS_ORDER_CONFIRMATION: 'Apple Order Confirmation',
      CATALOG_LANDING: 'Apple Catalog Landing',
      ENGRAVE_PRODUCT: 'Apple Engrave',
      ADDRESS: 'Apple Edit Address',
      ADD_ENGRAVING: 'Add Engraving',
      SKIP_ENGRAVING: 'Skip Engraving',
      POSTBACK: 'Apple Postback'
    },
    OPTIONS: {
      DENOMINATION: 'denomination'
    },
    STORE_NAME: {
      MERCHANDISE: 'Merchandise'
    },
    OTHER: {
      ANONYMOUS_USER: 'Anonymous'
    }
  };
  public static SEPARATOR = {
    DOUBLE_PIPE: ' || ',
    HYPHEN: ' - '
  };
  public static slugName = {
    accessories: 'accessories',
    all_accessories: 'all-accessories',
    non_configurable: ['mac', 'tv', 'watch', 'music'],
    apple_tv: 'tv',
    ipad: 'ipad',
    iphone: 'iphone',
    ipod: 'ipod'
  };
  public static pageName = {
    shop: 'SHOP'
  };
  public static localeMapper = {
    en_us: {language: 'en', country: 'US', shortLocale: ''},
    en_ca: {language: 'en', country: 'CA', shortLocale: 'ca'},
    fr_ca: {language: 'fr', country: 'CA', shortLocale: 'xf'},
    es_mx: {language: 'es', country: 'MX', shortLocale: 'mx'},
    en_gb: {language: 'en', country: 'GB', shortLocale: 'uk'},
    zh_hk: {language: 'zh', country: 'HK', shortLocale: 'hk-zh'},
    en_hk: {language: 'en', country: 'HK', shortLocale: 'hk'},
    zh_tw: {language: 'zh', country: 'TW', shortLocale: 'tw'},
    th_th: {language: 'th', country: 'TH', shortLocale: 'th'},
    en_au: {language: 'en', country: 'AU', shortLocale: 'au'},
    en_sg: {language: 'en', country: 'SG', shortLocale: 'sg'},
    en_my: {language: 'en', country: 'MY', shortLocale: 'my'},
    en_ph: {language: 'en', country: 'PH', shortLocale: 'ph'}
  };
  public static templateType = {
    LANDING: 'LANDING',
    CATEGORYLIST: 'CATEGORYLIST',
    LISTORGRID: 'LIST/GRID',
    CONFIGURABLE: 'CONFIGURABLE',
    CATEGORY: 'CATEGORY'
  };
  public static USE_MAX_POINTS = 'useMaxPoints';
  public static USE_MIN_POINTS = 'useMinPoints';
  public static USE_CUSTOM_POINTS = 'useCustomPoints';
  public static NEXT_STEP_PAYMENT = 'nextStepPayment';
  public static NEXT_STEP_REVIEW_YOUR_ORDER = 'nextStepReviewYourOrder';
  public static btnLink = 'modal'
}

export const KEYPHRASE = '8tvoeOvUdhLORg6owQvEnUYT2KHSfB4c';

// select payment constants to test, will be removed.
export const STATE =
  {
    payments: {
      pointsonly: {
        name: 'pointsonly',
        value: 'Points Only',
        isDisabled: false,
        optionHeading: 'payment-pointsonly',
        optionDescription: 'payment-pointsonly-desc', // add a low balance case
        paySummarySubtitle: 'pointsonly-paymentSummarySubtitle',
        paySummaryTemplate: 'formula-view',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          nextStep: ['/store', 'checkout'],
          skipLocationChange: false
        }
      },
      pointsfixed: {
        name: 'pointsfixed',
        value: 'Points Fixed',
        isDisabled: false,
        optionHeading: 'payment-pointsonly',
        optionDescription: 'payment-pointsonly-desc', // add a low balance case
        paySummarySubtitle: 'pointsfixed-paymentSummarySubtitle',
        paySummaryTemplate: 'pointsfixed',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          nextStep: ['/store', 'payment', 'card'],
          skipLocationChange: true
        },
        splitPayOptions: {
          useMaxPoints: {
            name: 'useMaxPoints',
            optionHeading: 'payment-useMaxPoints',
            optionDescription: 'payment-useMaxPoints-desc',
            optionDescriptionPayment: 'payment-useMaxPoints-payment-desc',
            paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
            paySummaryTemplate: 'additional-amount',
            checked: false,
            nextStepBtnLabel: 'nextStepReviewYourOrder',
            isPaymentRequired: true,
            isValid: true
          }
        }
      },
      splitpay: {
        name: 'splitpay',
        value: 'Split Pay',
        isDisabled: false,
        optionHeading: 'payment-splitpay',
        optionDescription: 'payment-splitpay-desc',
        paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
        paySummaryTemplate: '',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepHowManyPoints',
          nextStep: ['/store', 'payment', 'split'],
          skipLocationChange: true
        },
        splitPayOptions: {
          useMaxPoints: {
            name: 'useMaxPoints',
            optionHeading: 'payment-useMaxPoints',
            optionDescription: 'payment-useMaxPoints-desc',
            optionDescriptionPayment: 'payment-useMaxPoints-payment-desc',
            paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
            paySummaryTemplate: 'additional-amount',
            checked: false,
            nextStepBtnLabel: 'nextStepReviewYourOrder',
            isPaymentRequired: false,
            isValid: true
          },
          useMinPoints: {
            name: 'useMinPoints',
            optionHeading: 'payment-useMinPoints',
            optionDescription: 'payment-useMinPoints-desc',
            paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
            paySummaryTemplate: 'additional-amount',
            checked: false,
            nextStepBtnLabel: 'nextStepPayment',
            isPaymentRequired: true,
            isValid: true
          },
          useCustomPoints: {
            name: 'useCustomPoints',
            optionHeading: 'payment-useCustomPoints',
            optionDescription: 'payment-useCustomPoints-desc',
            paySummarySubtitle: 'splitpay-paymentSummarySubtitle',
            paySummaryTemplate: 'additional-amount',
            checked: false,
            nextStepBtnLabel: 'nextStepReviewYourOrder',
            isPaymentRequired: true,
            isValid: true,
            subView: {
              name: 'useCustomPoints'
            }
          }
        }
      },
      cashonly: {
        name: 'cashonly',
        value: 'Pay by Card',
        isDisabled: false,
        optionHeading: 'payment-cashonly',
        optionDescription: 'payment-cashonly-desc',
        paySummarySubtitle: 'cashonly-paymentSummarySubtitle',
        paySummaryTemplate: 'amount-due',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepCardPayment',
          nextStep: ['/store', 'payment', 'card'],
          skipLocationChange: true
        }
      },
      finance: {
        name: 'finance',
        value: 'Financing',
        isDisabled: false,
        optionHeading: 'payment-finance',
        optionDescription: 'payment-finance-desc',
        paySummarySubtitle: 'finance-paymentSummarySubtitle',
        paySummaryTemplate: 'finance',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          skipLocationChange: false
        }
      },
      splitpay_finance: {
        name: 'splitpay_finance',
        value: 'splitpay finance',
        isDisabled: false,
        optionHeading: 'payment-splitpay_finance',
        optionDescription: 'payment-splitpay_finance-desc',
        paySummarySubtitle: 'splitpay_finance-paymentSummarySubtitle',
        paySummaryTemplate: 'splitpay_finance',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          skipLocationChange: false
        }
      },
      cashonly_finance : {
        name: 'cashonly_finance',
        value: 'cashonly finance',
        isDisabled: false,
        optionHeading: 'payment-cashonly_finance',
        optionDescription: 'payment-cashonly_finance-desc',
        paySummarySubtitle: 'cashonly_finance-paymentSummarySubtitle',
        paySummaryTemplate: 'cashonly_finance',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          skipLocationChange: false
        }
      },
      payroll_deduction: {
        name: 'payroll_deduction',
        value: 'payroll_deduction',
        isDisabled: false,
        optionHeading: 'payment-payroll_deduction',
        optionDescription: 'payment-payroll_deduction-desc',
        paySummarySubtitle: 'payroll_deduction-paymentSummarySubtitle',
        paySummaryTemplate: 'payroll_deduction',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          skipLocationChange: false
        }
      },
      nopay: {
        name: 'nopay',
        value: 'No Pay',
        isDisabled: false,
        optionHeading: 'payment-nopay',
        optionDescription: 'payment-nopay-desc',
        paySummarySubtitle: 'nopay-paymentSummarySubtitle',
        paySummaryTemplate: 'nopay',
        checked: false,
        actionPanel: {
          nextStepBtnLabel: 'nextStepReviewYourOrder',
          skipLocationChange: false
        }
      }
    },
    selections: {}
  };

package com.b2s.rewards.apple.model;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.persistence.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by rperumal on 9/9/2015.
 */

@Entity
@Table(name = "shopping_cart_items",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "supplier_id", "options_xml", "shopping_cart_id"})}
)
public class ShoppingCartItem {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartItem.class);

    private Long id;
    private ShoppingCart shoppingCart;

    private Date addedDate;
    private Integer supplierId;
    private String productId;
    private String productName;
    private String imageURL;
    private String parentProductId;
    private String merchantId;
    private Integer quantity;
    private String optionsXml;
    private Long storeId;
    private Long productGroupId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "NUMERIC")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne()
    @JoinColumn(name = "shopping_cart_id", nullable = false, columnDefinition = "NUMERIC")
    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    @Column(nullable = false, name = "added_date")
    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    @Column(nullable = false, name="supplier_id")
    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    @Column(nullable = false, name = "product_id")
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Column(nullable = true, name = "product_name")
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Column(nullable = true, name = "image_url")
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Column(name = "merchant_id")
    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Column(nullable = false, name = "quantity")
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Column(nullable = true, name = "options_xml")
    public String getOptionsXml() {
        return optionsXml;
    }

    public void setOptionsXml(String optionsXml) {
        this.optionsXml = optionsXml;
    }
    @Column(nullable = true, name = "parent_product_id")
    
    public String getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(String parentProductId) {
        this.parentProductId = parentProductId;
    }

    @Column(nullable = true, name = "store_id")
    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    @Column(nullable = true, name = "product_group_id")
    public Long getProductGroupId() {
        return productGroupId;
    }

    public void setProductGroupId(Long productGroupId) {
        this.productGroupId = productGroupId;
    }

    /**
     * Convert Map to XML
     * @param optionsMap
     * @return
     */
    //TODO: Remove this once we get rid off OptionXML column
    public String convertToOptionsXml(Map<String, String> optionsMap) {
        String optionsXml = "";
        XMLStreamWriter xsw = null;

        try {
            StringWriter e = new StringWriter();
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            xsw = xof.createXMLStreamWriter(e);
            xsw.writeStartElement("xml");
            xsw.writeStartElement("options");
            Iterator i$ = optionsMap.entrySet().iterator();

            while(i$.hasNext()) {
                Map.Entry e1 = (Map.Entry)i$.next();
                xsw.writeStartElement("name");
                xsw.writeCharacters(((String)e1.getKey()).toString());
                xsw.writeEndElement();
                xsw.writeStartElement("value");
                xsw.writeCharacters(((String)e1.getValue()).toString());
                xsw.writeEndElement();
            }

            xsw.writeEndElement();
            xsw.writeEndElement();
            xsw.close();
            optionsXml = e.toString();
        } catch (Exception var7) {
            ;
        }

        return optionsXml;
    }
    //TODO: Remove this once we get rid off OptionXML column
    public Map<String,String> convertToMap(String optionsXml){
        Map optionsMap = new HashMap();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // disable external entities
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        } catch (ParserConfigurationException e) {
            logger.error("Error while parsing OptionXML to map structure..");
        }
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder;
        if (optionsXml==null || optionsXml.trim().equals(""))
            return optionsMap;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(optionsXml)));
            NodeList list = document.getElementsByTagName("options");
            list = list.item(0).getChildNodes();
            String myKey="";
            String myValue="";
            for (int i=0;i<list.getLength()/2;i++){
                Node node = list.item(i*2);
                myKey = node.getChildNodes().item(0).getNodeValue();
                node = list.item(i*2+1);
                myValue = node.getChildNodes().item(0).getNodeValue();
                optionsMap.put(myKey, myValue);
            }
        } catch (Exception e) {
            logger.error("Error converting OptionXML to map structure..");
        }

        return optionsMap;
    }


}
package com.b2s.apple.mapper.services

import com.b2s.apple.services.AppleRestService
import com.b2s.apple.services.LAUSDIntegrationService
import com.b2s.rewards.apple.integration.model.lausd.LAUSDParentResponse
import com.b2s.rewards.apple.integration.model.lausd.ParentInfo
import com.b2s.rewards.apple.util.HttpClientUtil
import com.b2s.security.oauth.OAuthCredentials
import com.b2s.security.oauth.Token
import com.b2s.shop.common.User
import com.b2s.shop.common.order.var.UserLAUSD
import com.google.gson.Gson
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject

class LAUSDIntegrationServiceSepc extends Specification{


    def appleRestService = Mock(AppleRestService)
    def restServiceTimeout=1

    @Subject
    LAUSDIntegrationService service=new LAUSDIntegrationService(appleRestService:appleRestService,restServiceTimeout:restServiceTimeout)

    def "test getParentInformation"(){

        given:
        def result
        User user=new UserLAUSD()
        user.varId="LAUSD"

        OAuthCredentials credentials=new OAuthCredentials()
        credentials.clientId="dummy"
        credentials.serviceAccountId="dummy"
        credentials.serviceAccountPassword="dummy"

        Token token=new Token()
        token.accessToken="dummy"

        def restTemplate =Mock(RestTemplate)

        appleRestService.getRestTemplate(*_)>>restTemplate
        restTemplate.exchange(*_) >> response



        when:
        result=service.getParentInformation(user,credentials,token)

        then:
        result.size()== expectedSize;
        if(expectedSize!=0){
            result.get(0).parentEmail.equals(expectedEmail)
        }


        where:
        response                 || expectedSize || expectedEmail
        getResponse(2 )   ||  2           || "user1@yahoo.com"
        getResponse(1 )   ||  1           || "sample-email@gmail.com"
        getResponse(0 )   ||  0           || ""


    }

    def getResponse(def count){
        Gson gson=new Gson();
        def body=null

        if (count==2){
            body= gson.fromJson("{\n" +
                    "    \"parentEmailList\": [\n" +
                    "        {\n" +
                    "            \"parentEmail\": \"user1@yahoo.com\",\n" +
                    "            \"parentFirstName\": \"FIRSTNAME1\",\n" +
                    "            \"parentLastName\": \"LASTNAME1\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"parentEmail\": \"sample-email@gmail.com\",\n" +
                    "            \"parentFirstName\": \"FIRSTNAME\",\n" +
                    "            \"parentLastName\": \"LASTNAME\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"studentEmail\": \"jdoe0001@mymail.lausd.net\",\n" +
                    "    \"studentFirstName\": \"JOHN\",\n" +
                    "    \"studentLastName\": \"DOE\"\n" +
                    "}", LAUSDParentResponse)
        }else if(count ==1){
            body= gson.fromJson("{\n" +
                    "    \"parentEmailList\": [\n" +
                    "        {\n" +
                    "            \"parentEmail\": \"sample-email@gmail.com\",\n" +
                    "            \"parentFirstName\": \"FIRSTNAME\",\n" +
                    "            \"parentLastName\": \"LASTNAME\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"studentEmail\": \"jdoe0001@mymail.lausd.net\",\n" +
                    "    \"studentFirstName\": \"JOHN\",\n" +
                    "    \"studentLastName\": \"DOE\"\n" +
                    "}",LAUSDParentResponse)
        } else {
            body= gson.fromJson("{\n" +
                    "    \"parentEmailList\": [  ],\n" +
                    "    \"studentEmail\": \"jdoe0001@mymail.lausd.net\",\n" +
                    "    \"studentFirstName\": \"JOHN\",\n" +
                    "    \"studentLastName\": \"DOE\"\n" +
                    "}",LAUSDParentResponse)
        }

        return new ResponseEntity<LAUSDParentResponse>(body,HttpStatus.OK)

    }

}

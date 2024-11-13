package com.example.personalmassenger

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {
    private val firebaseMessagingScope="https://www.googleapis.com/auth/firebase.messaging"
    fun getAccessToken():String?{
        try{
            val jsonString:String = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"personal-messenger-8c6b0\",\n" +
                    "  \"private_key_id\": \"fb69adbc00ec4ed36fc68158c7ba9ba05daabbfa\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDIEk5JJ+SFR1zy\\ncPHLktDJjtZpSkS9He1TCxitQyT9iHUiPhKGrSZzosFtvO9WPKuRIF+BO3NSwXbb\\naY270CQxaVmhyJeIqxgQKK170P93hXIm+//CqR8PLf6xD101wqa3G9VPXfMm2ezb\\naLE3ukgcHKre2EaL09+HZPxiiVbP7dDUkNF7X6E6Ms7IDRRjj7w0AErd+1KnSZHm\\njRqDMCGt2CCC9uepHnjGchD5LPFIVoG0Bjewkp8GfvpehF/Xgi0b/ke0bSzDU38M\\n/gz9P2uty0SVWB0AHm8R0fiioL8tggXN5k4vgw63F/EyioDe5iTV68Y8XLL2niRM\\nwCt9n4WxAgMBAAECggEATb7nUwBAihYYJVIEc+IiZiCuaw3kNkU54upN9Y5BApCT\\nS/IpCtKgPZFCbx1bN/ytguC8nCB5CPCK+mpDHHeeiODyZq7nS8GIwUa9W370twJR\\nQJruYTuNCT4LWcypoOl6FW+obYYJpdBLfVtqekhWGy58q8oJZ+62TwvME5Jg/L0x\\n79Wi3Q8VMEBn9EgplM3g1PlmP+63SEJF2UDE/30H3ItixwbiUX9+t0nic23xScK6\\nHeRgaxOnltsd0xUfLdqvIr7Dxle2OCAl9j8N9I6wooUydmhrGrX31VTPy+FbOYob\\nw5KbXXi8IFHCPGaPJ90vlG6n/yIIBw4dxlZz4fvRvQKBgQDSmBY3phWwAe7qkOwY\\nGG/PfRLoa6tqvc5eLJfBs5Jzabpt6o00krcd1Qh7qf1Em6QpDFUqFOMgWaY/pe3B\\nsBcezVcwSBVXMSTMRRAfzMKaAkKnGqn6fZFyQmXkJWu+hT2GFBcty3eHwe9LLCL5\\n1cPd9++TI0360Br1EB08SLHXuwKBgQDzNWqe8+sjIsHsl4xx9xLM1JQ6qA/M+31A\\nasVTLd2E1cfCCAvWpgBrFsz44i0LLbTuU5HT7IZYKPROS7aXXnSbCLhqkvqdRSiY\\nhUXfFHnATDXI9YUEHFlalZ2LtilSIbHxyqcjr+h9Of9g6hRMraKWyUFN6MbA/EgW\\n1eaDZv3TgwKBgDwndTEwH8HGy5f1DXtUf5uk/mGlX7F9xhTgZ/jMcmjFKpt7BBqg\\nTe4Xi/TG+bE/RAT1oN4EWVxEP1iu7NsGkZQa02zXKkFdqEUw9QogUvXq718MHsGO\\nXJ1mmfQxPDWPmaNS2cwa8mm028V9NgBnLyuYRragvwWFrC2UDMXLfgFBAoGANzk8\\n0YTvMlY1FGTA9iu08B+g3MTGebPtF9Bmp+F1ODFWn182WPHujGJr/mNU+QHS4H7H\\nUQiW5LO/7XtRpYNEFOPhBAoFr+pBMoioeePrVVe4qr3dCzboryHj2RJuxYBzKETs\\nuLS7pJujujzMiTCwB28BThB5+N4P/GdrZRDgt8MCgYEAuWt21FjfQOqrUpMQphZW\\nhj3YPI/zTNe8p0R8PVTOjjdpOMQv6ckLjwiFmWAZHnHJRDXWGUsA8/rxbsBs91Nu\\nwcx+LO6G4YwZ/9l7gOM/Mgi1eaTfLt/K3zS4ERETUks6q6H6YZ/oT6ny6Eqz9mag\\n95mZitAUzwDQ5sysYFvdvT4=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-wpj1q@personal-messenger-8c6b0.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"115865609717470071514\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-wpj1q%40personal-messenger-8c6b0.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n"
            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))

            val googleCredentials = GoogleCredentials.fromStream(stream).createScoped(arrayListOf(
                firebaseMessagingScope))
            googleCredentials.refresh()
            return googleCredentials.accessToken.tokenValue
        }
        catch (e:IOException){
            return null
        }
    }
}
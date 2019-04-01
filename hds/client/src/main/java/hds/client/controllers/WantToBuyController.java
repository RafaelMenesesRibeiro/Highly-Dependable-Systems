package hds.client.controllers;

import hds.client.helpers.ClientProperties;
import hds.security.SecurityManager;
import hds.security.domain.SignedTransactionData;
import hds.security.domain.TransactionData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class WantToBuyController {
    private static final String OPERATION = "wantToBuy";

    @PostMapping(value = "/wantToBuy") //TODO: IT'S NOT VOID!!!!!!!!!!!
    public void wantToBuy(@RequestBody SignedTransactionData signedTransactionData) {
        byte[] buyerSignature = signedTransactionData.getBuyerSignature();
        TransactionData transactionData = signedTransactionData.getPayload();
        String sellerID = transactionData.getSellerID();
        String buyerID = transactionData.getBuyerID();
        String goodID = transactionData.getGoodID();

        try {
            SecurityManager.verifySignature(SecurityManager.getPublicKeyFromResource(ClientProperties.getPort()), buyerSignature, SecurityManager.getByteArray(transactionData));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

}

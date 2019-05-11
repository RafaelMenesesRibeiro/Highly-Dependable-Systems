package hds.client.helpers;

import hds.security.DateUtils;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.GoodStateResponse;
import hds.security.msgtypes.SaleCertificateResponse;
import hds.security.msgtypes.WriteResponse;

import java.util.List;

import static hds.client.helpers.ClientProperties.print;
import static hds.client.helpers.ClientProperties.printError;

public class ONRRMajorityVoting {

    public static boolean assertOperationSuccess(int ackCount, String operation) {
        if (ackCount > ClientProperties.getMajorityThreshold()) {
            print(operation + " operation finished with majority quorum!");
            return true;
        } else {
            print(operation + " operation failed... Not enough votes.");
            return false;
        }
    }

    public static BasicMessage selectMostRecentGoodState(List<GoodStateResponse> readList) {
        GoodStateResponse highest = null;
        for (GoodStateResponse message : readList) {
            if (highest == null) {
                highest = message;
            } else if (DateUtils.isOneTimestampAfterAnother(message.getWts(), highest.getWts())) {
                highest = message;
            }
        }
        return highest;
    }

    public static int isWriteResponseAcknowledge(long wts, BasicMessage message) {
        if (message == null) {
            return 0;
        } else if (message instanceof WriteResponse) {
            if (((WriteResponse) message).getWts() == wts) {
                return 1;
            } else {
                printError("Response contained wts different than the one that was sent on request");
                return 0;
            }
        } else if (message instanceof SaleCertificateResponse) {
            if (((SaleCertificateResponse) message).getWts() == wts) {
                return 1;
            }
            return 0;
        }
        printError(message.toString());
        return 0;
    }
}

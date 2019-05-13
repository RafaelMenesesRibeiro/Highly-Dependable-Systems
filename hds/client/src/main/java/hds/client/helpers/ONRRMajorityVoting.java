package hds.client.helpers;

import hds.security.DateUtils;
import hds.security.msgtypes.*;

import java.util.List;

import static hds.client.helpers.ClientProperties.print;
import static hds.client.helpers.ClientProperties.printError;
import static hds.security.SecurityManager.*;

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

    public static int iwWriteAcknowledge(long wts, BasicMessage message) {
        if (message == null) {
            printError("A replica timed out. No information regarding the replicaId...");
            return 0;
        } else if (message instanceof WriteResponse) {
            if (((WriteResponse) message).getWts() == wts) {
                return 1;
            }
            printError("Response contained wts different than the one that was sent on request");
            return 0;
        } else if (message instanceof SaleCertificateResponse) {
            if (((SaleCertificateResponse) message).getWts() == wts) {
                return 1;
            }
            printError("Response contained wts different than the one that was sent on request");
            return 0;
        } else {
            printError(message.toString());
            return 0;
        }
    }

    public static int isGoodStateReadAcknowledge(int rid, BasicMessage message, List<GoodStateResponse> readList) {
        if (message == null) {
            return 0;
        } else if (message instanceof GoodStateResponse) {
            GoodStateResponse goodStateResponse = (GoodStateResponse) message;

            if (rid != goodStateResponse.getRid()) {
                return 0;
            }

            // TODO UPDATE GET FIELDS WITH NEW DATA TYPE DONE BY RAFAEL
            if (!verifyWriteOnGoodsDataResponseSignature(
                    goodStateResponse.getGoodID(),
                    goodStateResponse.isOnSale(),
                    goodStateResponse.getWriterID(),
                    goodStateResponse.getWts(),
                    goodStateResponse.getWriteOperationSignature()
            )) {
                return 0;
            }

            if (!verifyWriteOnOwnershipSignature(
                    goodStateResponse.getGoodID(),
                    goodStateResponse.getWriterID(),
                    goodStateResponse.getWts(),
                    goodStateResponse.getWriteOperationSignature()
            ))

            readList.add(goodStateResponse);
            return 1;
        }
        printError(message.toString());
        return 0;
    }

    public static int isSimpleAcknowledge(BasicMessage message) {
        if (message instanceof ChallengeRequestResponse) {
            return 1;
        } else {
            printError(message.toString());
            return 0;
        }
    }
}

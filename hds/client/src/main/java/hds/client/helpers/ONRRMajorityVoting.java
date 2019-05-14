package hds.client.helpers;

import hds.security.DateUtils;
import hds.security.msgtypes.*;
import org.javatuples.Pair;
import org.javatuples.Quartet;

import java.util.List;

import static hds.client.helpers.ClientProperties.print;
import static hds.client.helpers.ClientProperties.printError;
import static hds.security.SecurityManager.verifyWriteOnGoodsDataResponseSignature;
import static hds.security.SecurityManager.verifyWriteOnOwnershipSignature;

@SuppressWarnings("Duplicates")
public class ONRRMajorityVoting {

    public static boolean assertOperationSuccess(int ackCount, String operation) {
        if (ackCount > ClientProperties.getMajorityThreshold()) {
            print(operation + " operation finished with majority quorum!");
            return true;
        } else {
            print(operation + " operation failed... insufficient quorum!.");
            return false;
        }
    }

    public static Pair<ReadWtsResponse, Integer> selectMostRecentWts(List<ReadWtsResponse> readList) {
        ReadWtsResponse highest = null;

        for (ReadWtsResponse message : readList) {
            System.out.println("----- READING WTS -----");
            System.out.println(message.toString());
            System.out.println("  ----- END READ -----  ");

            if (highest == null) {
                highest = message;
            } else if (DateUtils.isOneTimestampAfterAnother(message.getWts(), highest.getWts())) {
                highest = message;
            }
        }

        if (highest == null) {
            return null;
        }

        return new Pair<>(highest, highest.getWts());
    }

    public static Quartet<GoodStateResponse, Boolean, GoodStateResponse, String> selectMostRecentGoodState(List<GoodStateResponse> readList) {
        GoodStateResponse highestOnSale = null;
        GoodStateResponse highestOwner = null;

        for (GoodStateResponse message : readList) {
            System.out.println("----- READING GOOD STATE -----");
            System.out.println(message.toString());
            System.out.println("    ----- END READ -----    ");

            if (highestOnSale == null) {
                highestOnSale = message;
            } else if (DateUtils.isOneTimestampAfterAnother(message.getOnGoodsWriteTimestamp(), highestOnSale.getOnGoodsWriteTimestamp())) {
                highestOnSale = message;
            }

            if (highestOwner == null) {
                highestOwner = message;
            } else if (DateUtils.isOneTimestampAfterAnother(message.getOnOwnershipWriteTimestamp(), highestOwner.getOnOwnershipWriteTimestamp())) {
                highestOwner = message;
            }
        }

        if (highestOnSale == null || highestOwner == null) {
            return null;
        }

        return new Quartet<>(highestOnSale, highestOnSale.isOnSale(), highestOwner, highestOwner.getOwnerID());
    }

    public static int isReadWtsAcknowledge(int rid, BasicMessage message, List<ReadWtsResponse> readList) {
        if (message == null) {
            return 0;
        } else if (message instanceof ReadWtsResponse) {
            ReadWtsResponse readWtsResponse = (ReadWtsResponse) message;

            if (rid != readWtsResponse.getRid()) {
                return 0;
            }

            readList.add(readWtsResponse);
            return 1;
        }
        printError(message.toString());
        return 0;
    }

    public static int isReadWtsWriteBackAcknowledge(int rid, BasicMessage message) {
        if (message == null) {
            printError("A replica timed out. No information regarding the replicaId...");
            return 0;
        } else if (message instanceof WriteBackResponse) {
            if (((WriteBackResponse) message).getRid() == rid) {
                return 1;
            }
        }
        printError("Response contained rid different than the one that was sent on write back message...");
        return 0;
    }

    public static int isGetGoodStateAcknowledge(int rid, BasicMessage message, List<GoodStateResponse> readList) {
        if (message == null) {
            return 0;
        } else if (message instanceof GoodStateResponse) {
            GoodStateResponse goodStateResponse = (GoodStateResponse) message;

            if (rid != goodStateResponse.getRid()) {
                return 0;
            }

            if (!verifyWriteOnGoodsDataResponseSignature(
                    goodStateResponse.getGoodID(),
                    goodStateResponse.isOnSale(),
                    goodStateResponse.getOnGoodsWriterID(),
                    goodStateResponse.getOnGoodsWriteTimestamp(),
                    goodStateResponse.getWriteOnGoodsSignature()
            )) {
                return 0;
            }

            if (!verifyWriteOnOwnershipSignature(
                    goodStateResponse.getGoodID(),
                    goodStateResponse.getOnOwnershipWriterID(),
                    goodStateResponse.getOnOwnershipWriteTimestamp(),
                    goodStateResponse.getWriteOnOwnershipSignature()
            )) {
                return 0;
            }

            readList.add(goodStateResponse);
            return 1;
        }
        printError(message.toString());
        return 0;
    }

    public static int isGetGoodStateWriteBackAcknowledge(int rid, BasicMessage message) {
        if (message == null) {
            printError("A replica timed out. No information regarding the replicaId...");
            return 0;
        } else if (message instanceof WriteBackResponse) {
            if (((WriteBackResponse) message).getRid() == rid) {
                return 1;
            }
            printError("Response contained rid different than the one that was sent on write back message...");
            return 0;
        } else {
            printError("isGetGoodStateWriteBackAcknowledge: \n" + message.toString());
            return 0;
        }
    }

    public static int isIntentionToSellAcknowledge(long wts, BasicMessage message) {
        return iwWriteAcknowledge(wts, message);
    }

    public static int isBuyGoodAcknowledge(long wts, BasicMessage message) {
        return iwWriteAcknowledge(wts, message);
    }

    public static int isTransferGoodAcknowledge(long wts, BasicMessage message) {
        return iwWriteAcknowledge(wts, message);
    }

    private static int iwWriteAcknowledge(long wts, BasicMessage message) {
        if (message == null) {
            printError("A replica timed out. No information regarding the replicaId...");
            return 0;
        } else if (message instanceof WriteResponse) {
            if (((WriteResponse) message).getWts() == wts) {
                return 1;
            }
            printError("Response contained wts different than the one that was sent on the write request");
            return 0;
        } else if (message instanceof SaleCertificateResponse) {
            if (((SaleCertificateResponse) message).getWts() == wts) {
                return 1;
            }
            printError("Response contained wts different than the one that was sent on the write request");
            return 0;
        } else {
            printError("iwWriteAcknowledge: \n" + message.toString());
            return 0;
        }
    }

}

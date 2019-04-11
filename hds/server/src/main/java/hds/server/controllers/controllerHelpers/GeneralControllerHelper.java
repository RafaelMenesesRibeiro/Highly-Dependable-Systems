package hds.server.controllers.controllerHelpers;

import hds.security.CryptoUtils;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.server.domain.MetaResponse;
import hds.server.exception.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("all")
public class GeneralControllerHelper {
	private static final String FROM_SERVER = "server";

	public static ResponseEntity<BasicMessage> getResponseEntity(MetaResponse metaResponse, String operation, boolean isSuccess) {
		BasicMessage payload = metaResponse.getPayload();
		try {
			payload.setSignature(CryptoUtils.signData(payload));
			if (isSuccess) {
				return new ResponseEntity<>(payload, HttpStatus.OK);
			}
			return new ResponseEntity<>(payload, HttpStatus.valueOf(metaResponse.getStatusCode()));
		}
		catch (SignatureException ex) {
			ErrorResponse unsignedPayload = new ErrorResponse("0", operation, FROM_SERVER, "unkwown", "", ControllerErrorConsts.CANCER, ex.getMessage());
			return new ResponseEntity<>(unsignedPayload, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

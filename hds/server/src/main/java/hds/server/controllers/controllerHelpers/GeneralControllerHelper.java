package hds.server.controllers.controllerHelpers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

@SuppressWarnings("all")
public class GeneralControllerHelper {
	private static final String FROM_SERVER = "server";

	public static ResponseEntity<BasicMessage> getResponseEntity(MetaResponse metaResponse, String requestID, String to, String operation) {
		BasicMessage payload = metaResponse.getPayload();
		try {
			// TODO //
			// payload.setSignature(CryptoUtils.signData(payload));
			return new ResponseEntity<>(payload, HttpStatus.valueOf(metaResponse.getStatusCode()));
		}
		catch (SignatureException ex) {
			ErrorResponse unsignedPayload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CANCER, ex.getMessage());
			return new ResponseEntity<>(unsignedPayload, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public static MetaResponse handleException(Exception ex, String requestID, String to, String operation) {
		if (ex instanceof IllegalArgumentException) {
			ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_PARAMS, ex.getMessage());
			return new MetaResponse(400, payload);
		}
		else if (ex instanceof DBConnectionRefusedException) {
			ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CONN_REF, ex.getMessage());
			return new MetaResponse(401, payload);
		}
		else if (ex instanceof DBClosedConnectionException) {
			ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CONN_CLOSED, ex.getMessage());
			return new MetaResponse(503, payload);
		}
		else if (ex instanceof  DBNoResultsException) {
			ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.NO_RESP, ex.getMessage());
			return new MetaResponse(500, payload);
		}
		else if (ex instanceof DBSQLException || ex instanceof SQLException) {
			ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_SQL, ex.getMessage());
			return new MetaResponse(500, payload);
		}
		ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CANCER, ex.getMessage());
		return new MetaResponse(500, payload);
	}
}

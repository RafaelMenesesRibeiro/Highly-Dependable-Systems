package hds.server.controllers.controllerHelpers;

import hds.security.exceptions.SignatureException;
import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

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
		if (ex instanceof DBConnectionRefusedException) {
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
		else if (ex instanceof SignatureException || ex instanceof IncorrectSignatureException) {
			ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_TRANSACTION, ex.getMessage());
			return new MetaResponse(403, payload);
		}
		ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CANCER, ex.getMessage());
		return new MetaResponse(500, payload);
	}

	public static MetaResponse handleInputValidationResults(BindingResult result, String requestID, String to, String operation) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("\tRequestBody not valid:");
		String reason = "Cannot detect reason";
		List<ObjectError> errors = result.getAllErrors();
		for (ObjectError error : errors) {
			if (error instanceof FieldError) {
				FieldError ferror = (FieldError) error;
				reason = "Parameter " + ferror.getField() + " with value " + ferror.getRejectedValue() +
						" is not acceptable: " + ferror.getDefaultMessage();
				logger.info("\t\t" + reason);
			}
		}
		ErrorResponse payload = new ErrorResponse(requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_PARAMS, reason);
		return new MetaResponse(400, payload);
	}


}

package hds.server.controllers.controllerHelpers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import hds.server.helpers.ServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.io.IOException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import static hds.security.DateUtils.generateTimestamp;
import static hds.security.SecurityManager.setMessageSignature;

public class GeneralControllerHelper {
	private static final LinkedHashMap<UserRequestIDKey, ResponseEntity<BasicMessage>> recentMessages = new CacheMap<>();
	private static final String FROM_SERVER = "server";
	private static final int MAX_CACHED_ENTRIES = 128;

	public static void cacheRecentRequest(UserRequestIDKey key, ResponseEntity<BasicMessage> value) {
		recentMessages.put(key, value);	 // TODO should be persistable
	}

	public static ResponseEntity<BasicMessage> tryGetRecentRequest(UserRequestIDKey key) {
		return recentMessages.get(key);
	}

	public static ResponseEntity<BasicMessage> getResponseEntity(MetaResponse metaResponse, String requestID, String to, String operation) {
		BasicMessage payload = metaResponse.getPayload();
		try {
			setMessageSignature(ServerProperties.getPKCS11(), ServerProperties.getCCSessionID(), ServerProperties.getCCSignatureKey(), payload);
			return new ResponseEntity<>(payload, HttpStatus.valueOf(metaResponse.getStatusCode()));
		}
		catch (SignatureException | IOException ex) {
			ErrorResponse unsignedPayload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CRASH, ex.getMessage());
			return new ResponseEntity<>(unsignedPayload, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public static MetaResponse handleException(Exception ex, String requestID, String to, String operation) {
		Logger logger = Logger.getAnonymousLogger();
		logger.warning("\tException caught - " + ex.getClass().getName());
		logger.warning("\tMessage: " + ex.getMessage());
		if (ex instanceof DBConnectionRefusedException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CONN_REF, ex.getMessage());
			return new MetaResponse(401, payload);
		}
		else if (ex instanceof DBClosedConnectionException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CONN_CLOSED, ex.getMessage());
			return new MetaResponse(503, payload);
		}
		else if (ex instanceof  DBNoResultsException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.NO_RESP, ex.getMessage());
			return new MetaResponse(500, payload);
		}
		else if (ex instanceof DBSQLException || ex instanceof SQLException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_SQL, ex.getMessage());
			return new MetaResponse(500, payload);
		}
		else if (ex instanceof SignatureException || ex instanceof IncorrectSignatureException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_TRANSACTION, ex.getMessage());
			return new MetaResponse(403, payload);
		}
		ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CRASH, ex.getMessage());
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
		ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_PARAMS, reason);
		return new MetaResponse(400, payload);
	}

}

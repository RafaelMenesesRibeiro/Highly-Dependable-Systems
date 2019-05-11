package hds.server.controllers.controllerHelpers;

import hds.security.helpers.ControllerErrorConsts;
import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.ErrorResponse;
import hds.server.ServerApplication;
import hds.server.controllers.BaseController;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.acl.NotOwnerException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import static hds.security.DateUtils.generateTimestamp;
import static hds.security.ResourceManager.getPrivateKeyFromResource;
import static hds.security.SecurityManager.setMessageSignature;

/**
 * Contains methods used by all Server's Controllers.
 *
 * @author 		Rafael Ribeiro
 * @author 		Francisco Barros
 */
public class GeneralControllerHelper {
	private static final LinkedHashMap<UserRequestIDKey, ResponseEntity<BasicMessage>> recentMessages = new CacheMap<>();
	private static final String FROM_SERVER = ServerApplication.getPort();

	/**
	 * Handles similar IntentionToSell and TransferGood Controllers code.
	 * Deals with Input validation.
	 * Deals with Exception handling.
	 *
	 * @param 	requestData		BasicMessage
	 * @param 	result    		result of validators for inputs of transactionData
	 * @param 	controller		BaseController instance, specific controller for which to call execute()
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 * @see        ApproveSaleRequestMessage
	 * @see 	BindingResult
	 */
	public static ResponseEntity<BasicMessage> generalControllerSetup(BasicMessage requestData, BindingResult result, BaseController controller) {
		UserRequestIDKey key = new UserRequestIDKey(requestData.getFrom(), requestData.getRequestID());
		ResponseEntity<BasicMessage> cachedResponse = GeneralControllerHelper.tryGetRecentRequest(key);
		if (cachedResponse != null) {
			return cachedResponse;
		}

		String operation = controller.OPERATION;
		MetaResponse metaResponse;

		if(result.hasErrors()) {
			metaResponse = GeneralControllerHelper.handleInputValidationResults(result, requestData.getRequestID(), requestData.getFrom(), operation);
			ResponseEntity<BasicMessage> response = GeneralControllerHelper.getResponseEntity(metaResponse, requestData.getRequestID(), requestData.getFrom(), operation);
			GeneralControllerHelper.cacheRecentRequest(key, response);
			return response;
		}

		try {
			metaResponse = controller.execute(requestData);
		}
		catch (Exception ex) {
			metaResponse = GeneralControllerHelper.handleException(ex, requestData.getRequestID(), requestData.getFrom(), operation);
		}
		ResponseEntity<BasicMessage> response = GeneralControllerHelper.getResponseEntity(metaResponse, requestData.getRequestID(), requestData.getFrom(), operation);
		GeneralControllerHelper.cacheRecentRequest(key, response);
		return response;
	}

	/**
	 * Adds a newly responded message to the recent messages cached. Used in case of
	 * client's duplicate requests.
	 *
	 * @param   key				Key for the cached map
	 * @param 	value			ResponseEntity sent to client
	 * @see     UserRequestIDKey
	 * @see     BasicMessage
	 * @see     ResponseEntity
	 */
	public static void cacheRecentRequest(UserRequestIDKey key, ResponseEntity<BasicMessage> value) {
		recentMessages.put(key, value);
	}

	/**
	 * Gets the response sent associated with the @param key.
	 *
	 * @param   key				Key for the cached map
	 * @return  ResponseEntity 	Response sent associated with the key, or null, if the
	 * 							the request associated with the key was never responded
	 * @see     UserRequestIDKey
	 * @see     BasicMessage
	 * @see     ResponseEntity
	 */
	public static ResponseEntity<BasicMessage> tryGetRecentRequest(UserRequestIDKey key) {
		return recentMessages.get(key);
	}

	/**
	 * Returns the ResponseEntity with the given HttpStatus code and BasicMessage as payload.
	 * If the BasicMessage cannot be signed, the signature field is an empty String.
	 *
	 * @param   metaResponse	Contains int to represent HttpStatus code and BasicMessage
	 * @param 	requestID		RequestID of the request. Same requestID for the response
	 * @param	to				ID of the response receiver
	 * @param 	operation		Represents the operation regarding the request/response
	 * @return  ResponseEntity	The wrapper to be sent as response
	 * @see     MetaResponse
	 * @see     ResponseEntity
	 */
	public static ResponseEntity<BasicMessage> getResponseEntity(MetaResponse metaResponse, String requestID, String to, String operation) {
		BasicMessage payload = metaResponse.getPayload();
		try {
			PrivateKey pKey = getPrivateKeyFromResource(ServerApplication.getPort());
			setMessageSignature(pKey, payload);
			return new ResponseEntity<>(payload, HttpStatus.valueOf(metaResponse.getStatusCode()));
		}
		catch (SignatureException | NoSuchAlgorithmException | IOException | InvalidKeySpecException ex) {
			ErrorResponse unsignedPayload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CRASH, ex.getMessage());
			return new ResponseEntity<>(unsignedPayload, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Returns a MetaResponse encapsulating an HttpStatus code to identify the exception and an ErrorResponse
	 * describing the exception.
	 *
	 * @param   ex				Thrown exception
	 * @param 	requestID		RequestID of the request. Same requestID for the response
	 * @param	to				ID of the response receiver
	 * @param 	operation		Represents the operation regarding the request/response
	 * @return  MetaResponse	Contains HttpStatus code and ErrorResponse
	 * @see     MetaResponse
	 * @see 	ErrorResponse
	 */
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
		else if (ex instanceof JSONException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_JSON, ex.getMessage());
			return new MetaResponse(500, payload);
		}
		else if (ex instanceof SQLException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_SQL, ex.getMessage());
			return new MetaResponse(500, payload);
		}
		else if (ex instanceof SignatureException || ex instanceof IncorrectSignatureException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.BAD_TRANSACTION, ex.getMessage());
			return new MetaResponse(401, payload);
		}
		else if (ex instanceof OldMessageException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.OLD_MESSAGE, ex.getMessage());
			return new MetaResponse(408, payload);
		}
		else if (ex instanceof NoPermissionException) {
			ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.NO_PERMISSION, ex.getMessage());
			return new MetaResponse(403, payload);
		}
		ErrorResponse payload = new ErrorResponse(generateTimestamp(), requestID, operation, FROM_SERVER, to, "", ControllerErrorConsts.CRASH, ex.getMessage());
		return new MetaResponse(500, payload);
	}

	/**
	 * Returns a MetaResponse encapsulating an HttpStatus code 400 and an ErrorResponse with the reason the input
	 * was invalid.
	 *
	 * @param   result			BindingResult from input validators
	 * @param 	requestID		RequestID of the request. Same requestID for the response
	 * @param	to				ID of the response receiver
	 * @param 	operation		Represents the operation regarding the request/response
	 * @return  MetaResponse	Contains HttpStatus code 400 and ErrorResponse
	 * @see     MetaResponse
	 * @see     ErrorResponse
	 */
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

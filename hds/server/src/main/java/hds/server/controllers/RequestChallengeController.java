package hds.server.controllers;

import hds.security.msgtypes.BasicMessage;
import hds.server.domain.ChallengeData;
import hds.security.msgtypes.ChallengeRequestResponse;
import hds.server.ServerApplication;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.controllers.controllerHelpers.UserRequestIDKey;
import hds.server.domain.MetaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.logging.Logger;

import static hds.security.DateUtils.generateTimestamp;
import static hds.server.controllers.controllerHelpers.GeneralControllerHelper.cacheUnansweredChallenge;

/**
 * Responsible for handling POST requests for the endpoint /requestChallenge.
 * With a requestID, returns a computationally intensive challenge needed to interact with TransferGoodController
 * so clients are discouraged of flooding the servers with fake / malicious requests.
 *
 * @author 		Rafael Ribeiro
 */
@RestController
public class RequestChallengeController extends BaseController {
		private static final String FROM_SERVER = ServerApplication.getPort();
		private static final String OPERATION = "requestChallenge";

	/**
	 * REST Controller responsible for returning a computationally intensive challenge.
	 *
	 * @param	challengeRequestData	ChallengeRequestResponse
	 * @param 	result    		result of validators for inputs of ownerData
	 * @return 	ResponseEntity 	Responds to the received request wrapping a BasicMessage
	 * @see        ChallengeRequestResponse
	 * @see 	BindingResult
	 */
	@PostMapping(value = "/requestChallenge",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> requestChallenge(@RequestBody @Valid BasicMessage challengeRequestData, BindingResult result) {
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Received Request Challenge request.");
		logger.info("\tRequest: " + challengeRequestData.toString());

		return GeneralControllerHelper.generalControllerSetup(challengeRequestData, result, this);
	}


	/**
	 * Creates a challenge for the client, and stores it until it responds with a solution encapsulated
	 * in TransferGoodController's received data.
	 *
	 * @param 	requestData			BasicMessage
	 * @return 	MetaResponse 		Contains an HttpStatus code and a BasicMessage
	 * @see    	BasicMessage
	 * @see 	MetaResponse
	 */
	@Override
	public MetaResponse execute(BasicMessage requestData) {
		ChallengeData challengeData = createChallenge(requestData.getRequestID());
		UserRequestIDKey key = new UserRequestIDKey(requestData.getFrom(), requestData.getRequestID());
		cacheUnansweredChallenge(key, challengeData);
		BasicMessage payload = new ChallengeRequestResponse(generateTimestamp(), requestData.getRequestID(), OPERATION, FROM_SERVER, requestData.getFrom(), "", challengeData.getChangeWhenDecided());
		return new MetaResponse(payload);
	}

	/**
	 * Creates a challenge for the client.
	 *
	 * @param 	requestID			RequestID to be used when interacting with TransferGoodController
	 * @return 	ChallengeData 		Challenge to be sent
	 * @see    	ChallengeData
	 */
	private ChallengeData createChallenge(String requestID) {
		return new ChallengeData(requestID, -3);
	}
}

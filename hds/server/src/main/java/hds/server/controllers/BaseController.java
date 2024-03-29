package hds.server.controllers;

import hds.security.msgtypes.ApproveSaleRequestMessage;
import hds.security.msgtypes.BasicMessage;
import hds.security.msgtypes.SaleCertificateResponse;
import hds.server.ServerApplication;
import hds.server.controllers.controllerHelpers.UserRequestIDKey;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

public abstract class BaseController {
	protected static final String FROM_SERVER = ServerApplication.getPort();
	public String OPERATION = "BASE_CONTROLLER";

	/**
	 * Responsible for the core of the controller. Overridden in every child class.
	 *
	 * @param 	requestData			ApproveSaleRequestMessage
	 * @return 	MetaResponse 		Contains an HttpStatus code and a BasicMessage
	 * @throws 	JSONException					Can't create / parse JSONObject
	 * @throws 	SQLException					The DB threw an SQLException
	 * @throws 	DBClosedConnectionException		Can't access the DB
	 * @throws 	DBConnectionRefusedException	Can't access the DB
	 * @throws 	DBNoResultsException			The DB did not return any results
	 */
	public abstract MetaResponse execute(BasicMessage requestData)
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException,
			DBNoResultsException, OldMessageException, NoPermissionException;

	/**
	 * Checks if the relevant 'To' (either the inner or the wrapping) is the same as the replica's port.
	 *
	 * @param   requestData		BasicMessage received
	 * @see     BasicMessage
	 */
	public boolean checkIfMessageForThisReplica(BasicMessage requestData) {
		return requestData.getTo().equals(ServerApplication.getPort());
	}
}

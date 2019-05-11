package hds.server.controllers;

import hds.security.msgtypes.BasicMessage;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import org.json.JSONException;

import java.sql.SQLException;

public class BaseController {
	public String OPERATION = "BASE_CONTROLLER";

	public MetaResponse execute(BasicMessage requestData)
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException,
			DBNoResultsException, OldMessageException, NoPermissionException {
		return null;
	}
}

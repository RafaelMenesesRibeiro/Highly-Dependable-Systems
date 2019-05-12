package hds.server.controllers;

import hds.security.msgtypes.BasicMessage;
import hds.server.ServerApplication;
import hds.server.domain.MetaResponse;
import hds.server.exception.*;
import org.json.JSONException;

import java.sql.SQLException;

public abstract class BaseController {
	protected static final String FROM_SERVER = ServerApplication.getPort();
	public String OPERATION = "BASE_CONTROLLER";

	public abstract MetaResponse execute(BasicMessage requestData)
			throws JSONException, SQLException, DBClosedConnectionException, DBConnectionRefusedException,
			DBNoResultsException, OldMessageException, NoPermissionException;
}

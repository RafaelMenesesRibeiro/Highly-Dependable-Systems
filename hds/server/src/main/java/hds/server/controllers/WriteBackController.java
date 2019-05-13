package hds.server.controllers;

import hds.security.msgtypes.BasicMessage;
import hds.server.controllers.controllerHelpers.GeneralControllerHelper;
import hds.server.domain.MetaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Responsible for handling POST requests for write back operation needed in (1, N) Byzantine Atomic Registers.
 *
 * @author 		Rafael Ribeiro
 */
@RestController
public class WriteBackController extends BaseController {
	private WriteBackController() {
		OPERATION = "writeBack";
	}

	/**
	 * REST Controller responsible for the write back operation of (1, N) Byzantine Atomic Registers.
	 *
	 * @param
	 * @param
	 * @return
	 */
	// TODO - Javadoc. //
	@PostMapping(value = "/writeBack",
			headers = {"Accept=application/json", "Content-type=application/json;charset=UTF-8"})
	public ResponseEntity<BasicMessage> writeBack(@RequestBody @Valid BasicMessage writeBackData, BindingResult result) {
		return GeneralControllerHelper.generalControllerSetup(writeBackData, result, this);
	}


	/**
	 * Creates a challenge for the client, and stores it until it responds with a solution encapsulated
	 * in TransferGoodController's received data.
	 *
	 * @param
	 * @return
	 */
	// TODO - Javadoc. //
	@Override
	public MetaResponse execute(BasicMessage requestData) {
		return null;
	}
}

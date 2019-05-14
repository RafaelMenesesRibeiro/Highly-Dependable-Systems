package hds.server.exception;

/**
 * Exception to represent the challenge failed. This challenge was given to the client when it was requested,
 * and is used for discourage clients to flood the server with TransferGoodController calls; because this challenge
 * is computationally intensive.
 *
 * @author 		Rafael Ribeiro
 */
public class ChallengeFailedException extends RuntimeException {
	public ChallengeFailedException(String msg) {
		super(msg);
	}
}

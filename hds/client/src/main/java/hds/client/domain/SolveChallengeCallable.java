package hds.client.domain;

import hds.security.ChallengeSolver;
import hds.security.msgtypes.ChallengeRequestResponse;
import org.javatuples.Pair;

import java.util.concurrent.Callable;

public class SolveChallengeCallable implements Callable<Pair<String, String>> {

    private final String replicaId;
    private final ChallengeRequestResponse challenge;

    public SolveChallengeCallable(String replicaId, ChallengeRequestResponse challenge) {
        this.replicaId = replicaId;
        this.challenge = challenge;
    }

    @Override
    public Pair<String, String> call() {
        System.out.println("Solving challenge for replica: " + replicaId);
        String solution = ChallengeSolver.solveChallenge(
                challenge.getHashedOriginalString(),
                challenge.getOriginalStringSize(),
                challenge.getAlphabet()
        );
        System.out.println("Found possible solution: " + solution + ", for challenge of replica: " + replicaId);
        return new Pair<>(replicaId, solution);
    }
}

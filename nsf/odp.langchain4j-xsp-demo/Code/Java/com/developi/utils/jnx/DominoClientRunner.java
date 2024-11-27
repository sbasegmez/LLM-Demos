package com.developi.utils.jnx;

import java.util.function.Function;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;

public class DominoClientRunner {

    public static <T> T runOnDominoClient(Function<DominoClient, T> function) {

        try (DominoProcess.DominoThreadContext ctx = DominoProcess.get()
                                                                  .initializeThread();
             DominoClient dc = DominoClientBuilder.newDominoClient()
                                                  .asIDUser()
                                                  .build()) {
            return function.apply(dc);
        }


    }

}

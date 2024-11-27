package com.developi.jnx.utils;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractStandaloneJnxApp {

    private static final Logger logger = Logger.getLogger(AbstractStandaloneJnxApp.class.getName());
    private String[] args;

    public void run(String[] args) {
        this.args = args;

        init();

        // Set the jnx.skipNotesThread property to true to avoid creating a NotesThread.
        // Otherwise, we are going to spend precious time to find a non-error exception!
        System.setProperty("jnx.skipNotesThread", "true");

        // Although the documentation suggests a single string argument, we use an array.
        // The second parameter would be the notes.ini file path, but we don't need it, I guess.
        String[] initArgs = new String[]{
                System.getenv("Notes_ExecDirectory")
        };

        try {
            DominoProcess.get()
                         .initializeProcess(initArgs);

            try (DominoProcess.DominoThreadContext ignored = DominoProcess.get()
                                                                          .initializeThread();

                 // At this point, it's best to keep the Notes client open. Otherwise, it will ask for a password.
                 DominoClient dc = DominoClientBuilder.newDominoClient()
                                                      .asIDUser()
                                                      .build()) {

                _run(dc);
            }

        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error in running Domino subroutine!", t);
        } finally {
            DominoProcess.get()
                         .terminateProcess();
        }
    }

    private void init() {
        // Initialise dotenv
        Utils.initDotenv();

        try {
            // Initialize the app
            _init();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error in initialising Domino subroutine!", t);
        }
    }


    public String[] getArgs() {
        return args == null ? new String[0] : args;
    }

    protected abstract void _init();
    protected abstract void _run(DominoClient dominoClient);

}

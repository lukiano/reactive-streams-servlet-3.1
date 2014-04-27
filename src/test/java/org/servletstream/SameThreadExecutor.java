package org.servletstream;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

public class SameThreadExecutor implements Executor {

    @Override
    public void execute(final @Nonnull Runnable command) {
        command.run();
    }
}

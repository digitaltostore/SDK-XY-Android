package com.dataxy;

import android.text.TextUtils;

import com.mappy.utils.LogLine;
import com.mappy.utils.Logger;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Copy of <code>AndroidJUnit4</code> that catches thrown <code>Throwable</code> from tests
 * <p>
 * {@inheritDoc}
 */
public class TestRunner extends BlockJUnit4ClassRunner {
    /**
     * Constructs a new instance of the default runner
     */
    public TestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }


    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        //override to override errors with logs
        Description description = describeChild(method);
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            run(methodBlock(method), description, notifier);
        }
    }

    protected void run(Statement statement, Description description, RunNotifier notifier) {
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
        try {
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            String throwableMessage = e.getMessage();
            String message = "\nLast logs :\n" + logsToString() + "\n\nStackTrace :\n" + e.getClass() + (TextUtils.isEmpty(throwableMessage) ? "" : ":" + throwableMessage);

            try {
                Field detailMessage = Throwable.class.getDeclaredField("detailMessage");
                detailMessage.setAccessible(true);
                detailMessage.set(e, message);
            } catch (Exception e1) {
            }
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }


    private static String logsToString() {
        List<LogLine> logs = Logger.getLogs();
        StringBuilder logBuilder = new StringBuilder();
        String sep = "";
        for (LogLine logLine : logs) {
            logBuilder.append(sep).append(logLine.toString());
            sep = "\n";
        }
        return logBuilder.toString();
    }
}

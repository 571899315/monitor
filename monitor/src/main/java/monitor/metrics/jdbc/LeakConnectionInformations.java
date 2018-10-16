/*
 * Copyright 2008-2016 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monitor.metrics.jdbc;

import java.io.Serializable;
import java.sql.Connection;

public class LeakConnectionInformations implements Serializable {
    private static final long serialVersionUID = -6063966419161604125L;
    private static final String OWN_PACKAGE = LeakConnectionInformations.class.getName().substring(0,
            LeakConnectionInformations.class.getName().lastIndexOf('.'));

    private final long openingTime;
    private final StackTraceElement[] openingStackTrace;
    private final long threadId;

    LeakConnectionInformations() {
        this.openingTime = System.currentTimeMillis();
        final Thread currentThread = Thread.currentThread();
        this.openingStackTrace = currentThread.getStackTrace();
        this.threadId = currentThread.getId();
    }

    static int getUniqueIdOfConnection(Connection connection) {
        return System.identityHashCode(connection);
    }

    long getOpeningTime() {
        return openingTime;
    }

    StackTraceElement[] getOpeningStackTrace() {
        return openingStackTrace;
    }

    long getThreadId() {
        return threadId;
    }

    @Override
    public String toString() {
//TODO		return getClass().getSimpleName() + "[openingDate=" + MetricsManager.
// toLocalDate(getOpeningTime()+ ", threadId="
//				+ getThreadId() + ", stackTrace="+getOpeningStackTrace()+']';
        return getClass().getSimpleName() + "[openingDate=" + getOpeningTime() + ", threadId="
                + getThreadId() + ", stackTrace=" + getOpeningStackTrace() + ']';
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.kafka.producer.shell;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.kafka.producer.datasync.AlarmDataStore;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;

@Command(scope = "kafka-producer", name = "list-alarms", description = "Enumerates the alarms that are currently in the Kafka data store.")
@Service
public class ListAlarms implements Action {

    @Reference
    private AlarmDataStore alarmDataStore;

    @Option(name = "-key", aliases = "--reduction-key", description = "Lookup the alarm at a specific reduction key.")
    private String reductionKey;

    @Override
    public Object execute() {
        if (alarmDataStore.isEnabled()) {
            System.out.println("The alarm data store is currently disabled and must be enabled for this shell command to function.");
        }

        // Wait for the alarm data store to be ready
        if (!isAlarmDataStoreReady()) {
            final long startTime = System.currentTimeMillis();
            System.out.println("Waiting for alarm data store to be ready..");
            while (true) {
                try {
                    System.out.print(".");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    if (isAlarmDataStoreReady()) {
                        System.out.printf("\nReady in %d ms.\n\n", System.currentTimeMillis() - startTime);
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("\nInterrupted.");
                    return null;
                }
            }
        }

        // Get
        final Map<String, OpennmsModelProtos.Alarm> alarmsByReductionKey = new LinkedHashMap<>();
        if (reductionKey != null) {
            alarmsByReductionKey.put(reductionKey, alarmDataStore.getAlarm(reductionKey));
        } else {
            alarmsByReductionKey.putAll(alarmDataStore.getAlarms());
        }

        // Dump
        alarmsByReductionKey.forEach(this::printAlarm);

        return null;
    }

    private boolean isAlarmDataStoreReady() {
        try {
            return alarmDataStore.isReady();
        } catch (Exception e) {
            return false;
        }
    }

    private void printAlarm(String reductionKey, OpennmsModelProtos.Alarm alarm) {
        System.out.printf("%s\n\t%s\n", reductionKey, alarm != null ? alarm.getLastEvent().getLabel() : "(No alarm)");
    }

}

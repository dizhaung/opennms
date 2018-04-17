/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.internal.classifier.Classifier;
import org.opennms.netmgt.flows.classification.internal.classifier.CombinedClassifier;
import org.opennms.netmgt.flows.classification.internal.value.PortValue;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.RuleComparator;

public class DefaultClassificationEngine implements ClassificationEngine {

    private final List<List<Classifier>> classifierPortList = new ArrayList<>(Rule.MAX_PORT_VALUE);
    private final Comparator<Rule> ruleComparator = new RuleComparator();
    private final ClassificationRuleProvider ruleProvider;

    public DefaultClassificationEngine(ClassificationRuleProvider ruleProvider) {
        this.ruleProvider = Objects.requireNonNull(ruleProvider);
        this.reload();
    }

    @Override
    public void reload() {
        // Reset existing data
        classifierPortList.clear();

        // Load rules
        final List<Rule> rules = ruleProvider.getRules();

        // (port) -> rule mapping
        final List<List<Rule>> rulePortList = new ArrayList<>(Rule.MAX_PORT_VALUE);

        // Rules which are not bound to a src OR dst port are stored here temporarily
        final List<Rule> anyPortRules = new ArrayList<>();

        // Initialize each element
        for (int i=Rule.MIN_PORT_VALUE; i<Rule.MAX_PORT_VALUE; i++) {
            rulePortList.add(new ArrayList<>());
            classifierPortList.add(new ArrayList<>());
        }

        // Bind each rule to a port
        for (Rule eachRule : rules) {
            // src AND dst port are defined, only map rule to dst port
            if (eachRule.hasSrcPortDefinition() && eachRule.hasDstPortDefinition()) {
                for (Integer eachPort : new PortValue(eachRule.getDstPort()).getPorts()) {
                    final List<Rule> portRules = rulePortList.get(eachPort);
                    if (!portRules.contains(eachRule)) {
                        portRules.add(eachRule);
                    }
                }
            } else if (eachRule.hasSrcPortDefinition() || eachRule.hasDstPortDefinition()) {
                // either src or dst is defined
                final PortValue portValue = new PortValue(eachRule.hasDstPortDefinition() ? eachRule.getDstPort() : eachRule.getSrcPort());
                for (Integer eachPort : portValue.getPorts()) {
                    rulePortList.get(eachPort).add(eachRule);
                }
            } else if (!eachRule.hasDstPortDefinition() && !eachRule.hasSrcPortDefinition()) {
                // Special treatment for rules which don't define a src or dst port, which are added ONCE
                // to all ports
                anyPortRules.add(eachRule);
            }
        }

        // Add rules with a missing port mapping (Src or dst) to ALL ports, if not already added
        for (final List<Rule> theRules : rulePortList) {
            theRules.addAll(anyPortRules);
        }

        // Sort rules by priority
        for (int i=0; i<rulePortList.size(); i++) {
            final List<Rule> portRules = rulePortList.get(i);
            Collections.sort(portRules, ruleComparator);
        }

        // Finally create classifiers
        for (int i = 0; i < rulePortList.size(); i++) {
            final int port = i;
            final List<Rule> portRules = rulePortList.get(port);

            // Convert rule to classifier
            final List<Classifier> classifiers = portRules.stream().map(rule -> {
                final Rule portRule = new RuleBuilder()
                        .withName(rule.getName())
                        .withProtocol(rule.getProtocol())
                        .withSrcAddress(rule.getSrcAddress())
                        .withDstAddress(rule.getDstAddress())
                        .build();
                // Check weather to apply rule for src or dst port (both may be very unlikely, but possible)
                if (rule.hasDstPortDefinition() && rule.hasSrcPortDefinition()) {
                    portRule.setSrcPort(rule.getSrcPort()); // keep src port as is, to apply filter
                } else {
                    // Only src or dst ports are defined (or none)
                    // if none, the value of either src or dst port may be empty, as the filtering already occurred
                    // through the index of the rule in the classifierPortList.
                    if (rule.hasDstPortDefinition()) {
                        portRule.setDstPort(Integer.toString(port));
                    }
                    if (rule.hasSrcPortDefinition()) {
                        portRule.setSrcPort(Integer.toString(port));
                    }
                }
                return new CombinedClassifier(portRule);
            })
            .collect(Collectors.toList());
            classifierPortList.set(port, classifiers);
        }
    }

    @Override
    public String classify(ClassificationRequest classificationRequest) {
        final Collection<Classifier> filteredClassifiers = getClassifiers(classificationRequest);
        final Optional<String> first = filteredClassifiers.stream()
                .map(classifier -> classifier.classify(classificationRequest))
                .filter(classifier -> classifier != null)
                .findFirst();

        // We return null instead of 'Undefined', to let the caller (e.g. rest service, or ui) decide
        // what an unmapped definition should be named.
        // This prevents a collision with an existing rule, which may map to 'Undefined'
        return first.orElse(null);
    }

    private Collection<Classifier> getClassifiers(ClassificationRequest request) {
        final List<Classifier> srcPortClassifiers = classifierPortList.get(request.getSrcPort());
        final List<Classifier> dstPortClassifiers = classifierPortList.get(request.getDstPort());

        // If rules for either src or dst ports are empty, use the opposite
        if (srcPortClassifiers.isEmpty()) {
            return dstPortClassifiers;
        }
        if (dstPortClassifiers.isEmpty()) {
            return srcPortClassifiers;
        }

        // If both are equal, nothing to do, just use either srcPortClassifiers or dstPortClassifiers
        if (Objects.equals(srcPortClassifiers, dstPortClassifiers)) {
            return dstPortClassifiers;
        }

        // If rules for src and dst port exist and they are not identical,
        // they must be deduped (merging and sorting the classifiers)
        // 1. Merge
        final Set<Classifier> classifiers = new HashSet<>();
        classifiers.addAll(srcPortClassifiers);
        classifiers.addAll(dstPortClassifiers);

        // 2. Sort
        final List<Classifier> sortedClassifiers = new ArrayList<>(classifiers);
        Collections.sort(sortedClassifiers);
        return sortedClassifiers;
    }
}

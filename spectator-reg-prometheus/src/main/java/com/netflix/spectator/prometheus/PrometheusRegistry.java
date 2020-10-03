/*
 * Copyright 2014-2019 Netflix, Inc.
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
package com.netflix.spectator.prometheus;

import com.netflix.spectator.api.*;
import com.netflix.spectator.api.patterns.PolledMeter;
import com.netflix.spectator.impl.StepDouble;
import io.prometheus.client.SimpleCollector;

/**
 * Registry implementation that maps spectator types to the prometheus simple client library.
 */
public class PrometheusRegistry extends AbstractRegistry {

    private final io.prometheus.client.CollectorRegistry impl;

    /**
     * Create a new instance.
     */
    public PrometheusRegistry() {
        this(Clock.SYSTEM, io.prometheus.client.CollectorRegistry.defaultRegistry);
    }

    /**
     * Create a new instance.
     */
    public PrometheusRegistry(Clock clock, io.prometheus.client.CollectorRegistry impl) {
        super(clock);
        this.impl = impl;
    }

    @Override
    protected Counter newCounter(Id id) {
        return new PrometheusCounter(clock(), id, build(io.prometheus.client.Counter.build(), id));
    }

    @Override
    protected DistributionSummary newDistributionSummary(Id id) {
        return new PrometheusDistributionSummary(clock(), id, build(io.prometheus.client.Summary.build(), id));
    }

    @Override
    protected Timer newTimer(Id id) {
        return new PrometheusTimer(clock(), id, build(io.prometheus.client.Summary.build(), id));
    }

    @Override
    protected Gauge newGauge(Id id) {
        return new PrometheusGauge(clock(), id, build(io.prometheus.client.Gauge.build(), id));
    }

    @Override
    protected Gauge newMaxGauge(Id id) {
        // Note: prometheus doesn't support this type directly so it uses an arbitrary
        // window of 1m
        StepDouble value = new StepDouble(Double.NaN, clock(), 60000L);
        PolledMeter.using(this).withId(id).monitorValue(value.getCurrent());
        return new PrometheusMaxGauge(clock(), id, value);
    }

    private <B extends SimpleCollector.Builder<B, C>, C extends SimpleCollector<D>, D> D build(B builder, Id id) {
        return builder
                .name(id.name())
                .help(id.name())
                .labelNames(labelNames(id))
                .register(impl)
                .labels(labelValues(id));
    }

    private String[] labelNames(Id id) {
        String[] labelNames = new String[id.size() - 1];
        int i = 0;
        for (Tag tag : id.tags()) {
            labelNames[i] = tag.key();
            i++;
        }
        return labelNames;
    }

    private String[] labelValues(Id id) {
        String[] labelValues = new String[id.size() - 1];
        int i = 0;
        for (Tag tag : id.tags()) {
            labelValues[i] = tag.value();
            i++;
        }
        return labelValues;
    }
}
